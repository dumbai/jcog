package jcog.pri.bag.impl;

import jcog.Is;
import jcog.Research;
import jcog.Util;
import jcog.data.list.FastAtomicIntegerArray;
import jcog.data.list.FastAtomicReferenceArray;
import jcog.decide.Roulette;
import jcog.mutex.SpinMutex;
import jcog.mutex.SpinMutexArray64;
import jcog.pri.Prioritized;
import jcog.pri.bag.Bag;
import jcog.signal.NumberX;
import jcog.util.ArrayUtil;
import org.eclipse.collections.api.block.function.primitive.IntToFloatFunction;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.VarHandle;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Float.NEGATIVE_INFINITY;

/**
 * unsorted priority queue with stochastic replacement policy
 * <p>
 * it uses a AtomicReferenceArray<> to hold the data but Unsafe CAS operations might perform better (i couldnt get them to work like NBHM does).  this is necessary when an index is chosen for replacement that it makes certain it was replacing the element it thought it was (that it hadnt been inter-hijacked by another thread etc).  on an insert i issue a ticket to the thread and store this in a small ConcurrentHashMap<X,Integer>.  this spins in a busy putIfAbsent loop until it can claim the ticket for the object being inserted. this is to prevent the case where two threads try to insert the same object and end-up puttnig two copies in adjacent hash indices.  this should be rare so the putIfAbsent should usually work on the first try.  when it exits the update critical section it removes the key,value ticket freeing it for another thread.  any onAdded and onRemoved subclass event handling happen outside of this critical section, and all cases seem to be covered.
 * <p>
 * <p>
 * although its called Bag (because of its use in other parts of this project) think of it like a sawed-off non-blocking hashmap with prioritized cells that compete with each other on insert. the way it works is like a probing hashtable but there is no guarantee something will be inserted.
 * <p>
 * first a spinlock is acquired to ensure that only one thread is inserting or removing per hash id at any given time.  i use a global atomic long array to combine each HijackBag's instance with the current item's hash to get a ticket that can ensure this condition.
 * <p>
 * an insertion can happen in a given cell range defined by the 'reprobes' parameter.  usually a number like 3 or 4 is ok but it can be arbitrarily long, but generally much less than the total bag capacity.  the hash computes the starting cell and then reprobe numer of cells forward, modulo capacity and these are the potential cells that a get/put/remove works with.   a get and remove only need to find the first cell with equivalent key.  put is the most complicated operation: first it must see if a cell exists and if so then an overridable merge operation is possible.  otherwise, while doing the initial pass, it chooses a victim based on the weakest priority of the cells it encounters, preferring a null cell to all others (NEGATIVE_INFINITY).  but if it chooses a non-null cell as a victim then a replacement test is applied to see if the new insertion can replace the existing. this can be decided by a random number generator in proportion to the relative priorities of the competing cells.
 *
 * HijackBag stores all items in an array and they aren't re-ordered,
 * unless the array is resized.
 *
 * the sampling process slides a window of some size across the
 * indices of the array into a temporary sorting queue (by priority)
 * and then that sorted array is sampled.  the decision whether
 * an item is inserted into full HijackBag is stochastic -
 * it decides by relative random probability if the incoming
 * object is able to 'hijack' the lowest ranked slot from
 * its potential hash probe targets.
 *
 */
@Research
@Is("Concurrent_computing")
public abstract class HijackBag<K, V> extends Bag<K, V> {

    private static final VarHandle SIZE = Util.VAR(HijackBag.class, "size", int.class);
    private volatile int size;

    //final VarHandle TABLE = Util.VAR(HijackBag.class, "table", HijackTable.class)
    private static final AtomicReferenceFieldUpdater<HijackBag, HijackTable> TABLE =
        AtomicReferenceFieldUpdater.newUpdater(HijackBag.class, HijackTable.class, "table");

    private static final SpinMutex mutex = new SpinMutexArray64(
          /* TODO Tune */
    Runtime.getRuntime().availableProcessors()*2, 2
    );

    private static final AtomicInteger serial = new AtomicInteger();

    private static final int PUT_ATTEMPTS = 1;

    //private static final float VICTIM_NOISE = 0.15f;
    /**
     * when size() reaches this proportion of space(), and space() < capacity(), grows
     */
    private static final float loadFactor = 0.75f;
    private static final float growthRate = 1.5f;
    public final int reprobes;
    /**
     * id unique to this bag instance, for use in treadmill
     */
    private final int id;
    private volatile HijackTable table = HijackTable.EMPTY;
    private volatile float min;
    private volatile float max;

    protected HijackBag(int reprobes) {
        this(0, reprobes);
    }

    protected HijackBag(int initialCapacity, int reprobes) {
        this.id = serial.getAndIncrement();

        this.reprobes = reprobes; assert (reprobes >= 2 && reprobes < Byte.MAX_VALUE - 1);

        setCapacity(initialCapacity);
    }

//    public static boolean hijackGreedy(float newPri, float weakestPri) {
//        return weakestPri <= newPri;
//    }
//
//    private static void updateEach(AtomicReferenceArray map, Function update) {
//        for (int c = map.length(), j = 0; j < c; j++) {
//            //map.updateAndGet(j, (vv) -> vv != null ? update.apply(vv) : null);
//            map.accumulateAndGet(j, update, (vv, u) -> vv!=null ? ((Function)u).apply(vv) : null);
//        }
//    }

    /** default impl = x.hashCode() */
    protected static int hash(Object x) {
        int h = x.hashCode();
        if (h == 0) h = 1;
        return h;
    }

    private static int index(int h, int cap) {


        //mix for additional mix
//        h ^= h >>> 20 ^ h >>> 12;
//        h ^= h >>> 7 ^ h >>> 4;

//        h = HashCommon.mix(h);

        //modulo
        h %= cap;
        if (h < 0)
            h += cap;

        return h;
    }

    public static Random random() {
        return ThreadLocalRandom.current();
    }

    /**
     * roulette fair
     */
    private static boolean hijackFair(float newPri, float oldPri) {
        return random().nextFloat() < newPri / (newPri + oldPri);
    }

    /**
     * SUSPECT
     */
    public static <X> Stream<X> stream(AtomicReferenceArray<X> a) {
        return IntStream.range(0, a.length()).mapToObj(a::get);
    }

    public static <X> List<X> list(AtomicReferenceArray<X> a) {
        return IntStream.range(0, a.length()).mapToObj(a::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    protected void _setCapacity(int oldCap, int newCap) {

        int s = Math.max(reprobes, size());

        newCap = Math.min(s, newCap);

        if (((oldCap < newCap && (s >= oldCap   /* should grow */)
            ||
            (newCap < space() /* must shrink */)))) {
            resize(newCap);
        }

    }

    /**
     * minimum allocation
     */
    protected int spaceMin() {
        return reprobes;
    }

    protected void resize(int newSpace) {
        HijackTable[] prev = new HijackTable[1];

        HijackTable next = newSpace != 0 ? new HijackTable(newSpace) : HijackTable.EMPTY;
        if (next == TABLE.updateAndGet(this, (x) -> {
            if (x.length() != newSpace) {
                prev[0] = x;
                return next;
            } else return x;
        })) {


            forEachActive(this, prev[0].values, b -> {
                if (put(b) == null)
                    onRemove(b);
            });

            commit(null);
        }
    }


//    protected static boolean optimisticPut() {
//        return false;
//    }
//    protected static boolean optimisticRemove() {
//        return false;
//    }

    @Override
    public void clear() {

        HijackTable x = _clearedTable();

        int l = x.length();
        FastAtomicReferenceArray m = x.values;
        FastAtomicIntegerArray h = x.hashes;
        for (int i = 0; i < l; i++) {
            Object xi = m.getAndSet(i, null);
            if (xi!=null) {
                h.setOpaque(i, 0);
                onRemove((V)xi);
            }
        }
        _clearMeta();
    }

    private void _clearMeta() {
        pressureZero();
        massSet(0);
        SIZE.set(this, 0);
        min = max = 0;
    }

    @Nullable
    private HijackTable _clearedTable() {
        HijackTable current = table();
        HijackTable x = (current == null || reshrink(space())) ? reset(spaceMin()) : current;
        return x;
    }

    private HijackTable table() {
        return TABLE.get(this);
    }

    protected boolean reshrink(int length) {
        return true;
    }

    private @Nullable HijackTable reset(int space) {

        if ((int)SIZE.getAndSet(this, 0) != 0) {
            HijackTable newTable = new HijackTable(space);

            HijackTable prevTable = TABLE.getAndSet(this, newTable);

            if (prevTable == newTable)
                commit();

            return prevTable;
        } else {
            return HijackTable.EMPTY; //remains empty
        }
    }

    /**
     * the current allocation, which is less than or equal to the value returned by capacity()
     */
    public int space() {
        return table.values.length();
    }

    public float density() {
        FastAtomicReferenceArray m = table.values;
        int mm = m.length();
        int filled = 0;
        for (int i = 0; i < mm; i++) {
            if (m.getOpaque(i) != null)
                filled++;
        }
        return ((float) filled) / mm;
    }

    /** completes an add/remove operation */
    private void commit(V toAdd, V toRemove, @Nullable NumberX overflowing) {
        if (toAdd!=null || toRemove!=null) {
            int dSize = (toAdd != null ? +1 : 0) + (toRemove != null ? -1 : 0);
            //only used if toAdd!=null
            int sizeBefore = (int)SIZE.getAndAdd(this, dSize) /* hack */;

            if (toAdd != null) {
                _onAdded(toAdd);

                if (dSize > 0) {
                    int sizeAfter = sizeBefore + dSize;
                    if (regrowForSize(sizeAfter /* size which has been increased by the insertion */, space())) {

//                    if (toRemove != null) {
//                        _put(key(toRemove), toRemove, overflowing);
//                        toRemove = null;
//                    }
                    }
                }
            }

            if (toRemove != null) {
                onRemove(toRemove);
            }
        }

    }

    /** allow duplicates and other unexpected phenomena resulting from disabling locking write access */
    protected boolean unsafe() {
        return false;
    }

//    protected boolean chooseVictim(float potentialVictimPri, float currentVictimPri) {
//
////        //OPTIONAL: victim noise
////        //      causes insertion to underestimate the priority allowing disproportionate replacement
////        //      in some low probability of cases, allowing more item churn
////        if (VICTIM_NOISE> 0) {
////            float victimNoise = VICTIM_NOISE / (reprobes * reprobes);
////            currentVictimPri *= victimNoise > 0 ? (1 - (random().nextFloat() * victimNoise)) : 1;
////        }
//
//
//        return (potentialVictimPri < currentVictimPri);
//    }


//    /** loss of priority if survives */
//    protected void cut(V p) {
//        p.priSub(CACHE_SURVIVE_COST);
//    }

//    abstract public void priAdd(V entry, float amount);

    private float priElseNegInfinity(V x) {
        float p = pri(x);
        return p == p ? p : NEGATIVE_INFINITY;
    }

    protected final boolean keyEquals(Object k, V p) {
        //return k.equals(key(p));
        return key(p).equals(k);
    }

    /**
     * if no existing value then existing will be null.
     * this should modify the existing value if it exists,
     * or the incoming value if none.
     * <p>
     * if adding content, pressurize() appropriately
     * <p>
     * <p>
     * NOTE:
     * this should usually equal the amount of priority increased by the
     * insertion (considering the scale's influence too) as if there were
     * no existing budget to merge with, even if there is.
     * <p>
     * this supports fairness so that existing items will not have a
     * second-order budgeting advantage of not contributing as much
     * to the pressure as new insertions.
     * <p>
     * if returns null, the merge is considered failed and will try inserting/merging
     * at a different probe location
     */
    protected abstract V merge(V existing, V incoming, NumberX overflowing);

    /**
     * HIJACK test
     * true if the incoming priority is sufficient to replace the existing value
     * can override in subclasses for custom replacement policy.
     * <p>
     * a potential eviction can be intercepted here
     * <p>
     * returns incomingPower, possibly reduced by the fight with the existing.
     * or NaN if the incoming wins
     */
    protected boolean replace(V incoming, float inPri, V existing, float exPri) {
        return hijackFair(inPri, exPri);
    }

    @Override
    public @Nullable V remove(K k) {

        HijackTable table = table();

        FastAtomicReferenceArray map = table.values;
        FastAtomicIntegerArray hashes = table.hashes;

        int c = map.length(); if (c == 0)  return null;

        int kHash = hash(k), start = index(kHash, c);

        boolean locking = !unsafe();
        int mutexTicket = locking ? mutex.start(id, start) : -1;

        V toReturn = null, toRemove = null;
        try {
            for (int i = start, j = reprobes; j > 0; j--) {

                if (kHash == hashes.get(i)) { //fast primitive pre-filter

                    V v = (V) map.getAcquire(i);
                    if (v != null && keyEquals(k, v)) {
                        if (map.weakCompareAndSetRelease(i, v, null)) {
                            //hashes.compareAndSet(i, kHash, 0);
                            hashes.setRelease(i, 0);
                            toReturn = toRemove = v;
                            break;
                        }
                        //else: this actually shouldnt happen if access to the hashes has been correctly restricted by the mutex
                    }
                }

                if (++i == c) i = 0;
            }
        } finally {
            if (locking) mutex.end(mutexTicket);
        }
        commit(null, toRemove, null);
        return (V)toReturn;
    }

    protected boolean hijackSoftmax2(float newPri, float oldPri, Random random) {


        newPri = newPri * newPri * reprobes;
        oldPri = oldPri * oldPri * reprobes;
        if (oldPri > 2 * Float.MIN_NORMAL) {
            float thresh = 1.0f - (1.0f - (oldPri / (newPri + oldPri)));
            return random.nextFloat() > thresh;
        } else {
            return (newPri >= Float.MIN_NORMAL) || random.nextBoolean();

        }
    }

    /**
     *
     */
    @Override
    public final V put(V x,  @Nullable NumberX overflowing) {

        K k = key(x);
        if (k == null)
            return null;

        V toReturn = _put(k, x, overflowing);

        if (toReturn == null)
            onReject(x);

        return toReturn;
    }

    private V _put(K k, V x,  @Nullable NumberX overflowing) {
        HijackTable table = table();
        FastAtomicReferenceArray map = table.values;
        FastAtomicIntegerArray hashes = table.hashes;

        int c = map.length(); if (c == 0)  return null;

        int kHash = hash(k), start = index(kHash, c);

        float incomingPri;
        if ((incomingPri = pri(x)) != incomingPri)
            return null; //deleted before insert

        V toReturn = null, toRemove = null, toAdd = null;

        boolean locking = !unsafe();
        int mutexTicket = locking ? mutex.start(id, start) : -1;
        try {

            //int retriesRemain = PUT_ATTEMPTS;
            //mainLoop: do {
                int victimIndex = -1;
                float victimPri = Float.POSITIVE_INFINITY;
                V victimValue = null;

                for (int i = start, j = reprobes; j > 0; j--) {
                    V y = (V) map.getOpaque(i);
                    if (y == null) {
                        //empty
                        victimIndex = i;
                        victimPri = NEGATIVE_INFINITY;
                        victimValue = null;
                        break;
                    } else if (x == y) {
                        //identity match
                        toReturn = y;
                        incomingPri = 0;
                        break;
                    } else if (kHash == hashes.getOpaque(i) && keyEquals(k, y)) {
                        //non-identity equality match
                        float before = pri(y);
                        toReturn = merge(y, x, overflowing);
                        float after = pri(y);
                        incomingPri -= (after-before); //reduce pressure
                        break;
                    } else {
                        //select weakest victim
                        float yp = priElseNegInfinity(y);
                        if (victimPri > yp) {// || (victimPri==yp && random().nextFloat() < 1f/(j+1))) {
                            victimPri = yp;
                            victimValue = y;
                            victimIndex = i;
                        }
                    }

                    if (++i == c) i = 0;
                }
                if (toReturn==null) {

                    //ATTEMPT HIJACK
                    //if (victimIndex >= 0) {
                    if (victimValue == null || replace(x, incomingPri, victimValue, victimPri)) {
                        //int victimHash = hashes.get(victimIndex);
                        if (map.compareAndSet(victimIndex, victimValue, x)) {
                            hashes.setOpaque(victimIndex, kHash); //hashes.compareAndSet(victimIndex, victimHash, kHash);
                            toRemove = victimValue;
                            toReturn = toAdd = x;
                        }
                    }
                    //}
                }

            //} while (--retriesRemain > 0);
        } finally {
            if (locking) mutex.end(mutexTicket);
        }

        commit(toAdd, toRemove, overflowing);

        pressurize(incomingPri - (toRemove!=null ? pri(toRemove) : 0));

        return toReturn;
    }

    @Override
    public final @Nullable V get(Object k) {

        HijackTable t = table();
        FastAtomicReferenceArray values = t.values;
        int c = values.length();
        if (c != 0) {
            FastAtomicIntegerArray hashes = t.hashes;
            int kHash = hash(k);
            for (int i = index(kHash, c), j = reprobes; j > 0; j--) {
                if (kHash == hashes.getOpaque(i)) { //fast primitive pre-filter
                    Object v = values.getOpaque(i);
                    if (v == null) continue;

                    V V = (V) v;
                    if (keyEquals(k, V))
                        return V;
                }
                if (++i == c) i = 0; //wrap-around
            }
        }

        return null;
    }


    @Override
    public void sample(RandomGenerator random, Function<? super V, SampleReaction> each) {
        int s;

        //System.out.println(); System.out.println();

        restart:
        while ((s = size()) > 0) {
            HijackTable table = this.table;
            FastAtomicReferenceArray map = table.values;
            int c = map.length();
            if (c == 0)
                break;

            int i = random.nextInt(c);

            boolean direction = random.nextBoolean();

            int windowCap = Math.min(s,

                //(1 + reprobes)
                Math.min(s, 2 * reprobes)
            );

            float[] wPri = new float[windowCap];
            Object[] wVal = new Object[windowCap];

            /* emergency null counter, in case map becomes totally null avoids infinite loop*/
            int mapNullSeen = 0;

            IntToFloatFunction weight = (k) -> wPri[k];
            //MutableRoulette roulette = new MutableRoulette(windowCap, weight, random);

            int windowSize = 0;

            while ((mapNullSeen + windowSize) < c /*&& size > 0*/) {
                V v = (V) map.getOpaque(i);

                if (v != null) {
                    float p = priElse(v, 0);
                    if (p != p) {
                        evict(table, i, v, true);
                        mapNullSeen++;
                    } else {
                        wVal[windowSize] = v;
                        wPri[windowSize] = p;
                        if (++windowSize >= windowCap) {
                            break;
                        }
                    }
                } else {
                    mapNullSeen++;
                }

                i = Util.next(i, direction, c);

            }

            if (windowSize == 0)
                return; //emptied

            mapNullSeen = 0;

            while (mapNullSeen < c) {

                //System.out.println(n2(wPri) + "\t" + Arrays.toString(wVal));

                //int which = roulette.reweigh(weight).next();
                int which = Roulette.selectRoulette(windowSize, weight, random);
//                if (which < 0)
//                    throw new WTF(); //HACK temporary

                V v = (V) wVal[which];
                if (v == null)
                    continue; //assert(v!=null);


                SampleReaction next = each.apply(v);

                if (next.stop) {
                    break restart;
                } else if (next.remove) {
                    if (windowSize == windowCap) {
                        wVal[which] = null;
                        wPri[which] = 0;
                    } else {
                        //compact the array by swapping the empty cell with the entry cell's (TODO or any other non-null)
                        ArrayUtil.swap(wVal, windowSize - 1, which);
                        ArrayUtil.swapFloat(wPri, windowSize - 1, which);
                    }
                    remove(key(v));
                    if (windowSize > size()) windowSize--;
                }

                if (map != this.table.values)
                    continue restart;

                //shift window

                mapNullSeen = 0;
                float p = Float.NaN;
                V v0;
                do {
                    v0 = (V) map.getOpaque(i = Util.next(i, direction, c));
                    if (v0 == null) {
                        if (mapNullSeen++ >= c)
                            break restart;
                    } else if ((p = pri(v0)) != p) {
                        evict(table, i, v0, true);
                        if (mapNullSeen++ >= c)
                            break restart;
                    }
                } while (v0 == null);

                if (windowSize >= windowCap) {
                    //TODO if there are any holes in the window maybe fill those rather than sliding
                    System.arraycopy(wVal, 1, wVal, 0, windowSize - 1);
                    System.arraycopy(wPri, 1, wPri, 0, windowSize - 1);
                }
                wVal[windowSize - 1] = v0;
                wPri[windowSize - 1] = Math.max(p, Prioritized.EPSILON);

            }

        }
    }

    private void evict(HijackTable table, int i, V v, boolean updateSize) {
        if (table.evict(i, v)) {
            if (updateSize) {
                int sizeBefore = (int) SIZE.getAndAdd(this, -1);
            }
            onRemove(v);
        }
    }

    @Override
    public final int size() {
        //return Math.max(0, (int)SIZE.getOpaque(this));
        return size;
    }

    @Override
    public void forEach(Consumer<? super V> e) {
        forEachActive(this, table.values, e);
    }

    @Override
    public void forEach(int max, Consumer<? super V> e) {
        Bag.forEach(table.values, max, e);
    }

    /** direct access.  when reaching the end of the buffer, wraps-around to the beginning */
    public void forEachIndex(int start, int n, Consumer<? super V> e) {
        FastAtomicReferenceArray m = table.values;

        int actualCap = table.values.length();
        int cap = Math.min(actualCap, capacity());

        n = Math.min(n, cap); //dont visit any index more than once

        int end = (start + n) % cap;
        for (int i = start; i != end; ) {
            Object x = m.getOpaque(i);
            if (x!=null) e.accept((V)x);
            if (++i == cap) i = 0;
        }
    }

    @Override
    public Spliterator<V> spliterator() {
        return stream().spliterator();
    }

    @Override
    public Iterator<V> iterator() {
        //return stream().iterator();
        return new HijackIterator(table.values);
    }

    @Override
    public Stream<V> stream() {
        FastAtomicReferenceArray map = table.values;
        return IntStream.range(0, map.length())
            .mapToObj(map::getOpaque).filter(Objects::nonNull).map(z -> (V)z);
    }

    /**
     * linear scan through the map with kontinue callback. returns the last value
     * encountered or null if totally empty
     * TODO
     * TODO hash-comparing version
     */
    public V next(int offset, Predicate<V> each) {
        FastAtomicReferenceArray map = table.values;
        int n = map.length();
        V xx = null;
        for (int i = offset; i < n; i++) {
            V x = (V) map.getOpaque(i);
            if (x != null && !each.test(xx = x)) break;
        }
        return xx;
    }

    @Override
    public float priMin() {
        return min;
    }

    @Override
    public float priMax() {
        return max;
    }

    @Override
    public void commit(@Nullable Consumer<? super V> update) {


        float mass = 0;
        float min = Float.POSITIVE_INFINITY;
        float max = NEGATIVE_INFINITY;

        int nextSize = 0;

        HijackTable table = this.table;
        FastAtomicReferenceArray map = table.values;

        int len = map.length();
        for (int i = 0; i < len; i++) {
            V f = (V) map.getOpaque(i);
            if (f == null)
                continue;

            float p = pri(f);

            if (update != null && p == p) {
                update.accept(f);
                p = pri(f);
            }

            if (p == p) {
                mass += p;
                if (p > max) max = p;
                if (p < min) min = p;
                nextSize++;
            } else
                evict(table, i, f, false);
        }

        if (nextSize <= 0) {
            mass = 0;
            min = max = 0;
        }

        SIZE.set(this, nextSize);
        massSet(mass);
        this.min = min;
        this.max = max;


    }

    private void _onAdded(V x) {

        onAdd(x);

    }

    protected boolean regrowForSize(int s, int sp) {


        if (s >= (loadFactor * sp)) {
            int cp = capacity();
            if (sp < cp) {
                int ns =
                    Math.min(cp, Math.round(sp * growthRate));
                if (ns != sp) {
                    resize(ns);
                    return true;
                }
            }
        }


        return false;
    }

    private static final class HijackIterator<V> implements Iterator<V> {

        final int n;
        private final FastAtomicReferenceArray map;
        int i = 0;
        V next;

        HijackIterator(FastAtomicReferenceArray map) {
            this.map = map;
            n = map.length();
        }

        @Override
        public boolean hasNext() {
            Object v = null;
            int i = this.i;
            while (i < n && v==null) {
                v = map.getOpaque(i++);
            }
            this.i = i;
            return (next = (V) v)!=null;
        }

        @Override
        public V next() {
            return next;
        }
    }

    public static class HijackTable {

        static final HijackTable EMPTY = new HijackTable(0);

        /**
         * TODO make non-public
         */
        final FastAtomicReferenceArray values;
        final FastAtomicIntegerArray hashes;
        private final int length;

        HijackTable(int length) {
            this.values = new FastAtomicReferenceArray(length);
            this.hashes = new FastAtomicIntegerArray(length);
            this.length = length;
        }

        public int length() {
            return length;
        }

        boolean evict(int i, Object v) {
            //int victimHash = hashes.get(i);
            if (values.compareAndSet(i, v, null)) {
                //hashes.compareAndSet(i, victimHash, 0);
                hashes.setOpaque(i, 0);
                //if the map is still active

//                if (this.table.values == table.values) {
//                }
                return true;
            }
            return false;
        }

    }


































    /*private static int i(int c, int hash, int r) {
        return (int) ((Integer.toUnsignedLong(hash) + r) % c);
    }*/


}