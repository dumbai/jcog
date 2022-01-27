package jcog.pri.bag.impl;

import jcog.Is;
import jcog.TODO;
import jcog.Util;
import jcog.data.bit.AtomicIntBitSet;
import jcog.data.list.Lst;
import jcog.pri.ArrayHistogram;
import jcog.pri.DistributionApproximator;
import jcog.pri.Prioritizable;
import jcog.pri.Prioritized;
import jcog.pri.bag.Bag;
import jcog.pri.bag.Sampler;
import jcog.pri.op.PriMerge;
import jcog.signal.NumberX;
import jcog.sort.IntifySmoothSort2;
import jcog.sort.SortedIntArray;
import jcog.util.ArrayUtil;
import jcog.util.SingletonIterator;
import org.eclipse.collections.api.block.function.primitive.ShortFunction;
import org.eclipse.collections.api.block.function.primitive.ShortToFloatFunction;
import org.eclipse.collections.api.block.function.primitive.ShortToIntFunction;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectShortHashMap;
import org.jetbrains.annotations.Nullable;
import org.roaringbitmap.IntIterator;
import org.roaringbitmap.RoaringBitmap;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.random.RandomGenerator;

import static jcog.Util.clamp;
import static jcog.Util.emptyIterator;


/**
 * A bag implemented as a combination of a
 * Map and a SortedArrayList
 *
 * ArrayBag, does maintain an eventually sorted array
 * (an exhausive sort happens at each .commit but item
 * priority can change in between) and this is directly
 * sampled from according to priority (using a compued
 * priority histogram for fairness when priority is not
 * distributed linearly, which in most cases it wont be).
 *
 * this makes insertions and evictions simple because
 * it's just a matter of comparing against the lowest
 * ranked item's priority.
 *
 * TODO extract a version of this which will work for any Prioritized, not only BLink
 */
public abstract class ArrayBag<X, Y extends Prioritizable> extends Bag<X, Y> {

    public static final float SHARP_DEFAULT =
        2;
        //1;

    private static final int HISTOGRAM_THRESH = 4;

    /** >= 1.  higher values increase histogram sampling precision */
    private static final float HISTOGRAM_DETAIL = 1.5f;

    @Is("Inelastic_collision")
    private static final boolean pressurizeAccept = false;

    @Is("Elastic_collision")
    private static final boolean pressurizeReject = true;

    public final DistributionApproximator hist = new ArrayHistogram();
    private final ArrayBagModel<X, Y> model;
    private final SortedIntArray sort = new SortedIntArray();
    private final StampedLock lock = new StampedLock();
    //2
    //4
    //2
    //3
    ;
    public float sharp = SHARP_DEFAULT;

    private transient volatile float priMin;

    /**
     * capacity is initially zero
     */
    public ArrayBag(PriMerge merge, ArrayBagModel<X, Y> m) {
        this(m);
        merge(merge);
    }

    public ArrayBag(ArrayBagModel<X, Y> m) {
        this.model = m;
    }

    protected ArrayBag(PriMerge merge) {
        this(merge, new ListArrayBagModel());
    }

    /**
     * whether to attempt re-sorting the list after each merge, in-between commits
     */
    protected boolean sortContinuously() {
        //TODO try policy of: randomly in proportion to bag fill %
        return true;
        //return false;
    }

    /**
     * override and return 0 to effectively disable histogram sampling (for efficiency if sampling isnt needed)
     */
    protected int histogramBins(int s) {
        if (s <= 1) return 0;

        //TODO refine
        int thresh = HISTOGRAM_THRESH;
        return s <= thresh ? s : thresh +
            (int)(Math.log(1 + s - thresh) * HISTOGRAM_DETAIL)
            //Util.cbrtInt(s - thresh)
            //Util.sqrtInt(s - thresh)
        ;
    }

    @Override
    public final void clear() {
        pop(Integer.MAX_VALUE, false, null);
    }

    @Deprecated
    private short[] items() {
        return sort.items;
    }

    @Override
    public final int size() {
        return sort.size();
    }

    @Nullable
    @Override
    public final Y get(Object key) {
        return model.get(key);
    }

    public final @Nullable Y get(int index) {
        return model.get(sort.get(index));
    }

//    /** different from sample(..); this will return uniform random results */
//    public final @Nullable Y get(Random rng) {
//        Object[] items = this.items.items;
//        int s = Util.min(items.length, size());
//        return s > 0 ? (Y)items[rng.nextInt(s)] : null;
//    }

    @Override
    protected final void _setCapacity(int before, int after) {
        long l = lock.writeLock(); //TODO read -> write
        try {
            if (after < size())
                _commit(null);
            sort.capacity(after);
            model.capacity(after);
        } finally {
            lock.unlockWrite(l);
        }
    }


//    @Override
//    public Stream<Y> stream() {
//        ////return IntStream.range(0, s).map(i -> sort[i]).mapToObj(i -> model.get((short) i)).filter(Objects::nonNull);
//        return Streams.stream(this);
//    }

    protected float sortedness() {
        return 1f;
    }

    private void sort() {
        int s = size();
        if (s <= 1)
            return;

        float c = sortedness();
        int from, to;

        if (c >= 1f - Float.MIN_NORMAL) {
            //int from /* inclusive */, int to /* inclusive */
            from = 0;
            to = s;
        } else {
            int toSort = (int) Math.ceil(c * s);
            float f = ThreadLocalRandom.current().nextFloat();
            int center = (int) (Util.sqr(f) * (s - toSort) + toSort / 2f); //sqr adds curve to focus on the highest priority subsection
            from = Math.max(center - toSort / 2, 0);
            to = Math.min(center + toSort / 2, s);
        }

        if ((to = clamp(to, from, s)) - (from = Math.max(0, from)) > 1)
            new IntifySmoothSort2(items(), model.priInt).sort(from, to);
    }

    /**
     * update histogram, remove values until under capacity
     */
    private void _commit(@Nullable Consumer<? super Y> update) {
        int n = size();

        boolean sorted = true;

        double m = 0;
        float p0 = Float.POSITIVE_INFINITY;
        short[] xy;
        if (n > 0) {
            RoaringBitmap removals = null;

            xy = sort.array();
            //s = Math.min(xy.length, s);

            for (int i = 0; i < n; i++) {

                short x = xy[i];
                Y y = model.get(x);

                float p = pri(y);

                if (p == p) {

                    if (update != null) {
                        update.accept(y);

                        pri(x, p = pri(y));
                    }

                    m += p;

                    if (sorted) {
                        if (p - p0 >= Prioritized.EPSILON / 2)
                            sorted = false;
                        else
                            p0 = p;
                    }

                } else {
                    (removals == null ? removals = new RoaringBitmap() : removals).add(i);
                }
            }

            if (removals != null)
                n = removeRemovals(n, removals);

        } else
            xy = null;

        if (n > 0) {
            if (n > 1 && !sorted)
                sort();

            int c = capacity();
            if (n > c) {
                _free(n - c);
                n = size();
            }
        }

        DistributionApproximator h = this.hist;
        float pMin;
        if (n > 0) {
            pMin = pri(xy[n - 1]);
            if (n > 1)
                commitHistogram(xy, n, h, pri(xy[0]) - pMin, pMin);

        } else {
            //empty
            m = 0;
            pMin = 0;
        }

        priMin = pMin;
        massSet((float) m);
    }

    private int removeRemovals(int n, RoaringBitmap removals) {
        IntIterator rr = removals.getReverseIntIterator();
        while (rr.hasNext()) {
            _remove(sort.remove(rr.next()));
            n--;
        }
        return n;
    }

    @Deprecated private void commitHistogram(short[] sort, int n, DistributionApproximator h, float pRange, float pMin) {
        //initialize with first value after applying the first update
        int bins = histogramBins(n);
        if (bins == 0)
            h.commitFlat(0, n);
        else {
            h.start(bins);

            model.histogram(sort, n, pMin, pRange, h);

            h.commit(0, n, Math.max(3, bins-1));
        }
    }

    private void _free(int n) {
        if (n > 0) {
            int s = sort.size();
            for (int i = 0; i < n; i++)
                evicted(_remove(sort.get(--s)));
            sort.removeLast(n);
        }
    }

    /**
     * immediate sample
     */
    @Override
    public @Nullable Y sample(@Nullable RandomGenerator rng) {
        float r = rng != null ? randomCurve(rng) : 0;

//        long l = lock.readLock();
//        try {
        return _sample(r);
//        } finally {
//            lock.unlockRead(l);
//        }
    }

    @Nullable
    private Y _sample(float rng) {
        short[] ii = this.items();
        int s = Math.min(ii.length, size());
        return s == 0 ? null : model.get(ii[sampleNext(rng, s)]);
    }

    /**
     * chooses a starting index randomly then iterates descending the list
     * of items. if the sampling is not finished it restarts
     * at the top of the list. so for large amounts of samples
     * it will be helpful to call this in batches << the size of the bag.
     */
    @Override
    public void sample(RandomGenerator rng, Function<? super Y, SampleReaction> each) {

        Y y;
        while ((y = sample(rng)) != null) {

            SampleReaction next = each.apply(y);

            if (next.remove) {
                remove(key(y));
            } else {
                if (rng == null)
                    throw new TODO("without a fix, this will continue to spin on 0th item"); //HACK
            }

            if (next.stop)
                return;

        }
    }

    /**
     * warning: not thread-safe
     */
    @Override
    public final Iterator<Y> sampleUnique(RandomGenerator rng) {
        int s = size();
        if (s > 0) {
            short[] items = items();
            if ((s = Math.min(s, items.length)) > 0)
                return s == 1 ?
                        new SingletonIterator(model.get(items[0])) :
                        model.sampleUnique(items, s, sampleNext(randomCurve(rng), s));
        }
        return emptyIterator;
    }

    private float randomCurve(RandomGenerator rng) {
        return (float) Math.pow(rng.nextFloat(), sharp);
    }

    private int sampleNext(float rng, int size) {
        return (rng != rng || size <= 1) ? 0 :
                Math.min(hist.sampleInt(rng), size - 1);
    }

    @Override
    public final @Nullable Y remove(X x) {
        long l = lock.writeLock();
        try {
            short i = model.id(x);
            return i < 0 ? null : _remove(x, i);
        } finally {
            lock.unlockWrite(l);
        }
    }

    private Y _remove(X x, short i) {
        if (sort.remove(i, model)) {
            Y removed = model.remove(x, this); //HACK
            if (removed != null)
                return removed;
        }
        throw new ConcurrentModificationException("inconsistency while attempting removal: " + x + "," + i);
    }

    @Override
    @Nullable
    public Y put(Y x, NumberX overflow) {

        if (this.capacity() == 0)
            return null;

        float xp = x.pri();
        if (xp != xp) return null; //already deleted

        X k = key(x);
        //if (k == null) throw new NullPointerException();

        long l = lock.writeLock();

        short existingID = model.id(k);
        return existingID < 0 ?
                _insert(k, x, xp, l) :
                _merge(existingID, x, xp, l, overflow);
    }

    private Y _merge(short existingID, Y x, float xp, long l, NumberX overflow) {
        Y existing = model.get(existingID);
        if (existing == x) {
            lock.unlock(l); //exact same instance
            return x;
        } else
            return _merge(existing, existingID, x, xp, overflow, l);
    }

    @Nullable
    private Y _insert(X key, Y incoming, float pri, long lock) {
        boolean insert;
        try {
            int size = size(), capacity = capacity();
            if (insert = acceptInsert(pri, size, capacity)) {
                //lock = readToWrite(lock, this.lock);
                /*if (insert = acceptInsert(pri, size, capacity))*/
                { //check again

                    _free(1 + size - capacity);
                    short item = model.put(key, incoming, pri);
                    //assert (existing == null) : "map.put(" + key + "," + incoming + "): item existed in map: " + existing;

                    int index = sort.insert(item, -pri, model);

                    assert (index >= 0 && index < capacity);

                    _priMin();
                }
            }

        } finally {
            this.lock.unlock(lock);
        }


        if (insert) {
            if (pressurizeAccept)
                pressurize(pri);
            massAdd(pri);
            onAdd(incoming);
            return incoming;
        } else {
            if (pressurizeReject)
                pressurize(pri);
            onReject(incoming);
            return null;
        }
    }

    private boolean acceptInsert(float pri, int size, int capacity) {
        return size < capacity || _priMin() <= /* < */ pri;
    }

    /**
     * update priMin
     */
    private float _priMin() {
        return priMin = pri(sort.last());
    }

    private float pri(short i) {
        return model.pri(i);
    }

    private void pri(short i, float p) {
        if (p != p) p = -1;
        model.pri(i, p);
    }

    /**
     * will not need to be sorted after calling this; the index is automatically updated
     * <p>
     * handles delta pressurization
     * postcondition: write-lock will be unlocked asap
     */
    private Y _merge(Y existing, short existingID, Y incoming, float incomingPri, @Nullable NumberX overflow, long lock) {


        float delta;

        try {
            float priBefore = pri(existingID);

            delta = merge(existing, incoming, incomingPri);

            if (Math.abs(delta) >= Prioritized.EPSILON) {


                pri(existingID, existing.pri());

                if (sortContinuously()) {

                    //if removed, or significant change occurred
                    float priAfter = priBefore + delta; //HACK

                    sort.update(existingID, priBefore, priAfter, model);

                    priMin = Util.min(priMin, priAfter);
                }
            } else
                delta = 0;

        } finally {
            this.lock.unlock(lock);
        }

        if (delta != 0) {
            if (overflow != null) {
                float over = Util.max(0, incomingPri - delta);
                if (over > 0)
                    overflow.add(over);
            }

            if (pressurizeAccept)
                pressurize(delta);

            massAdd(delta);
        }

        return existing;
    }

    protected float merge(Y existing, Y incoming, float incomingPri) {
        return merge.delta(existing, incomingPri);
    }

    /**
     * remove from list should have occurred before the map removal
     */
    private Y _remove(short i) {
        return model.remove(key(model.get(i)), this);
    }

    @Override
    public void commit(@Nullable Consumer<? super Y> update) {

        long l = lock.writeLock();
        try {
            _commit(update);
        } finally {
            lock.unlockWrite(l);
        }

    }

    /**
     * called when removed non-explicitly
     */
    private void evicted(Y y) {

//        float p = priElse(y, 0);
//        if (p > Float.MIN_NORMAL) massAdd(-p);

        onRemove(y);
    }

    /**
     * warning: doesnt lock
     */
    @Override
    public void forEach(Consumer<? super Y> action) {
        forEach(Integer.MAX_VALUE, action);
    }

    private void popBuffer(int n, float forget, Consumer<? super Y> each) {
        Lst<Y> popped = new Lst<>(n);

        popped.ensureCapacity(n);

        popBatch(n, forget, popped::addFast);

        popped.forEach(each);

        popped.delete();
    }

    /**
     * removes the top n items
     *
     * @param n # to remove, if -1 then all are removed
     */
    public void pop(int n, boolean buffered, Consumer<? super Y> each) {
        float forget = 1;
        if (buffered) {
            popBuffer(n, forget, each);
        } else {
            popBatch(n, forget, each);
        }
    }


    /**
     * TODO option for buffering to a list to escape critical section quicker than calling the popped lambda
     */
    public Sampler<Y> popBatch(int n, float forget, @Nullable Consumer<? super Y> popped) {
        if (n > 0) {
            long l = lock.writeLock();
            try {
//            verify(); //TEMPORARY

                int s = size();
                int toRemove = Math.min(n, s);
                if (toRemove > 0) {
                    if (toRemove < s) {
                        //SOME
                        sort.removeRange(0, toRemove,
                                popped != null ? i ->
                                        popped.accept(_remove(i))
                                        :
                                        this::_remove
                        );
                        _commit(forget(forget));

                    } else {
                        //ALL
                        if (popped != null)
                            sort.forEach(i -> popped.accept(model.get(i)));
                        sort.clear();
                        model.clear();
                        massSet(0);
                        priMin = 0;
                    }//            int sAfter = s - toRemove;
//            assert (model.size() == sAfter && size() == sAfter);
//            if (sAfter <= 0)
//                hist.clear();
//            verify(); //TEMPORARY
                }


            } finally {
                lock.unlockWrite(l);
            }
        }

        return this;
    }

    /**
     * for testing purposes
     */
    private void verify() {
        int ms = model.size();
        int ss = sort.size();
        if (ms != ss)
            throw new NullPointerException("model/sort fault: model=" + ms + ", sort=" + ss);
        sort.forEach(s -> {
            if (model.get(s) == null)
                throw new NullPointerException("missing entry for id=" + s);
        });
    }


    @Override
    public float pri(Y value) {
        return value.pri();
    }

    @Override
    public float priMax() {
        Y x = model.get(sort.first());
        return x != null ? priElse(x, 0) : 0;
    }

    @Override
    public double priMean() {
        return model.priMean(items(), size());
    }

    @Override
    public float priMin() {
        return priMin;
//        Y x = items.last();
//        return x != null ? priElse(x, 0) : 0;
    }

    public final Iterator<Y> iterator() {
        short[] sort = items();
        int s = Math.min(size(), sort.length);
        return s == 0 ? Collections.emptyIterator() :
                model.iterator(sort, s);
    }

    /**
     * for diagnostic purposes
     */
    final boolean isSorted() {
        return sort.isSorted(model);
    }

    @Override
    public final void forEach(int max, Consumer<? super Y> action) {
//        long l = lock.readLock();
//        try {
        int s = size();
        if (s > 0)
            model.forEach(items(), action, Math.min(s, max));
//        } finally {
//            lock.unlockRead(l);
//        }
    }


//    /**
//     * priority of the middle index item, if exists; else returns average of priMin and priMax
//     */
//    private float priMedian() {
//
//        Object[] ii = table.items.items;
//        int s = Util.min(ii.length, size());
//        if (s > 2)
//            return pri((Y) ii[s / 2]);
//        else if (s > 1)
//            return (priMin() + priMax()) / 2;
//        else
//            return priMin();
//    }

    public abstract static class ArrayBagModel<X, Y> implements ShortToFloatFunction {

        final ShortToIntFunction priInt = this::priInt;


        @Override
        public final float valueOf(short y) {
            return -priElseNeg1(y);
        }

        public abstract Y remove(X x, Bag b);

        @Nullable Y get(Object key) {
            short i = id(key);
            return i >= 0 ? get(i) : null;
        }

        protected abstract Y get(short index);

        protected abstract short id(Object key);


        /**
         * put(..) semantics
         */
        protected abstract short put(X key, Y incoming, float pri);

        protected abstract int size();

        protected abstract void capacity(int after);

        /**
         * priority set
         */
        protected abstract void pri(short id, float p);

        /**
         * priority get
         */
        protected abstract float pri(short id);

        /**
         * priority get, as float->integer bits
         */
        final int priInt(short i) {
            return Float.floatToIntBits(priElseNeg1(i));
        }

        protected abstract void histogram(short[] sort, int n, float pMin, float pRange, DistributionApproximator h);

        final float priElse(short y, float f) {
            float p = pri(y);
            return (p == p) ? p : f;
        }

        final float priElseNeg1(short y) {
            return priElse(y, -1);
        }

        final float priElseZero(short y) {
            return priElse(y, 0);
        }

        void forEach(short[] sort, Consumer<? super Y> action, int max) {
            int n = Math.min(max, sort.length);
            for (int j = 0; j < n; j++) {
                Y v = get(sort[j]);
                if (v != null)
                    action.accept(v);
            }
        }

        final Iterator<Y> iterator(short[] sort, int s) {
            return new ArrayBagIterator(sort, s);
        }

        abstract Iterator<Y> sampleUnique(short[] items, int s, int sampleNext);


        public double priMean(short[] items, int c) {
            c = Math.min(c, items.length);
            if (c == 0) return 0;

            double s = 0;
            for (int i = 0; i < c; i++)
                s += priElseZero(items[i]);
            return s / c;
        }

        abstract public void clear();


        private final class ArrayBagIterator implements Iterator<Y> {
            private final int s;
            private final short[] sort;
            private int i;
            private Y k;

            ArrayBagIterator(short[] sort, int s) {
                this.sort = sort;
                this.s = Math.min(sort.length, s);
            }

            @Override
            public boolean hasNext() {
                if (k != null)
                    return true;

                while (i < s && (k = get(sort[i++])) == null) {
                }
                return k != null;
            }

            public Y next() {
                Y kk = this.k;
                this.k = null;
                return kk;
            }
        }
    }

    /**
     * fast, safe for single-thread use
     */
    public static class PlainListArrayBagModel<X, Y> extends ListArrayBagModel<X, Y> {
        @Override
        public Y get(short index) {
            return (Y) list[index];
        }
    }

    public static class ListArrayBagModel<X, Y> extends ArrayBagModel<X, Y> implements ShortFunction<Y> {
        private static final VarHandle LIST =
                MethodHandles.arrayElementVarHandle(Object[].class)
                        //.withInvokeExactBehavior()
                ;
//        private static final MethodHandle LIST_get =
//                MethodHandles.arrayElementGetter(Object[].class);

        private final ObjectShortHashMap<X> map;
        //private volatile MetalBitSet used = null;
        protected /*volatile*/ Object[] list = ArrayUtil.EMPTY_OBJECT_ARRAY;
        protected /*volatile*/ float[] pri = ArrayUtil.EMPTY_FLOAT_ARRAY;
        private volatile AtomicIntBitSet used = AtomicIntBitSet.EMPTY;
        private /*volatile*/ int cap;

        public ListArrayBagModel() {
            this.map = new ObjectShortHashMap<>();
        }

        private static AtomicIntBitSet used(int cap, int capPrev, Object[] nextList) {
            AtomicIntBitSet nextUsed =
                    new AtomicIntBitSet(cap);
            //MetalBitSet.bits(cap);
            for (int i = 0; i < capPrev; i++) if (nextList[i] != null) nextUsed.set(i);
            return nextUsed;
        }

        @Override
        Iterator<Y> sampleUnique(short[] items, int s, int sampleNext) {
            return new MySampleUniqueIterator<>(s, sampleNext, items, list);
        }

        @Override
        public void clear() {
            int c = cap;
            used.clear();
            Arrays.fill(list, 0, c, null);
            map.clear();
        }

        @Override
        public Y get(short xy) {
            return (Y) LIST.getOpaque(list, (int)xy);
            //return (Y) list[xy];
//            try {
//                return (Y) LIST_get.invokeExact(list, (int)xy);
//            } catch (Throwable e) {
//                //e.printStackTrace();
//                return null;
//            }
        }

        private Y remove(short xy) {
            return (Y) LIST.getAndSet(list, (int)xy, (Object)null);
//            Y[] l = this.list;
//            Y y = l[xy];
//            l[xy] = null;
//            return y;
        }

        @Override
        public void capacity(int cap) {
            //TODO resize, trim the bag if necessary

            int capPrev;
            float[] pri = this.pri;
            if ((capPrev = pri.length) < cap) {
                //grow
                float[] nextPri = Arrays.copyOf(pri, cap);
                Arrays.fill(nextPri, capPrev, cap, Float.NaN);
                Object[] nextList = Arrays.copyOf(list, cap);
                //TODO better atomic. maybe fill the current bitvector while setting
                used = used(cap, capPrev, nextList);
                list = nextList;
                this.pri = nextPri;
            }
            this.cap = cap;
        }

        @Override public void histogram(short[] items, int n, float pMin, float pRange, DistributionApproximator h) {
            if (pRange < Prioritized.EPSILON) {
                h.commitFlat(0, n);
            } else {
                float[] pri = this.pri;
                for (int i = 0; i < n; i++) {
                    float p = pri[items[i]];
                    if (p > 0)
                        h.accept(Util.max(0,
                                //1 - (p - pMin) / pRange //NORMALIZED TO RANGE
                                1 - p                     //UNNORMALIZED
                        ));
                }
            }
        }

        public final Y remove(X x, Bag b) {
            short xy = map.removeKeyIfAbsent(x, (short) -1);
            if (xy < 0)
                throw new NullPointerException();

//            VarHandle.fullFence(); //experimental. intended to enforce changes in non-volatile 'map' before continuing

            Y y = remove(xy);

            float[] pri = this.pri;
            float p = pri[xy];
            pri[xy] = Float.NaN;

            used.clear(xy);

            if (pressurizeAccept)
                b.depressurize(p);

            return y;
        }

        /**
         * acquires a new slot
         */
        @Override
        public short shortValueOf(Y y) {
            int xy = used.setNext(cap);
            if (xy < 0)
                throw new NullPointerException(/*x.toString()*/);

            //this.list[xy] = y;

            //LIST.setVolatile(list, xy, y);
            LIST.setOpaque(list, xy, y);

//            Object prev = LIST.getAndSet(list, xy, y); if (prev!=null)
//                throw new WTF(); //TEMPORARY

            return (short) xy;
        }

        @Override
        public short put(X x, Y y, float p) {
//            if (y == null)
//                throw new NullPointerException(); //HACK TEMPORARY
//            //TEMPORARY
//            if (map.getIfAbsent(x, (short)-1)>=0)
//                throw new WTF();
            short s = map.getIfAbsentPutWith(x, this, y);
//            //TEMPORARY
//            if (map.getIfAbsent(x, (short)-1)<0)
//                throw new WTF();
            pri[s] = p;
            return s;
        }


        @Override
        public short id(Object key) {
            return map.getIfAbsent(key, (short) -1);
        }


        @Override
        public int size() {
            return map.size();
            //assert(m == used.size());
        }

        @Override
        public void pri(short id, float p) {
            pri[id] = p;
        }

        public float pri(short id) {
            return pri[id];
        }


    }

    /**
     * scan radially around point, O(N)
     */
    abstract static class SampleUniqueIterator<Y> implements Iterator<Y> {

        private final int s;
        private int u, d;
        private Y next;
        private boolean dir;

        private SampleUniqueIterator(int size, int center) {
            this.s = size;
            u = center;
            d = center + 1;
        }

        abstract Y get(int i);

        private boolean _hasNext() {
            int n;
            if ((this.dir = (!this.dir))) {
                if ((n = u--) < 0) return false; //up
            } else {
                if ((n = d++) >= s) return false; //down
            }
            return (next = get(n)) != null;
        }

        @Override
        public boolean hasNext() {
            return next != null || _hasNext() || _hasNext();
        }

        @Override
        public Y next() {
            Y n = this.next;
            this.next = null;
            return n;
        }

    }

    private static final class MySampleUniqueIterator<Y> extends SampleUniqueIterator<Y> {
        private final short[] items;
        private final Object[] list;

        MySampleUniqueIterator(int s, int sampleNext, short[] items, Object[] list) {
            super(s, sampleNext);
            this.items = items;
            this.list = list;
        }

        @Override
        Y get(int i) {
            return (Y) list[items[i]];
        }
    }
}


//    public static class ArrayBagPrimitiveModel<X> extends ArrayBagModel<X, PriReference<X>> {
//
//        float[] pri = ArrayUtil.EMPTY_FLOAT_ARRAY;
//
//        volatile int cap = 0;
//
//        final ArrayDeque<PriReference<X>> ids = new ArrayDeque();
//        private final ObjectIntHashMap<X> map = new ObjectIntHashMap<>();
//
//        public void capacity(int cap) {
//            int capBefore = pri.length;
//            if (capBefore < cap) {
//                pri = Arrays.copyOf(pri, cap);
//                this.cap = cap;
//                for (int i = cap-1; i >= capBefore; i--)
//                    create(i);
//            }
//        }
//        private void create(int i) {
//            ids.push(i);
//        }
//        private void offer(int id) {
//            if (cap > id)
//                create(id);
//        }
//        private int take() {
//            int c = cap;
//            if (c == 0) return -1;
//            PLink<X> i;
//            while ((i = ids.pop()) >= cap) {
//
//            }
//            return i;
//        }
//
//
//
//        @Override
//        public PLink<X> remove(X x) {
//            int id = map.removeKeyIfAbsent(x, -1);
//            if (id >= 0) {
//                float p = pri[id];
//                offer(id);
//                return link(x, p);
//            } else
//                return null;
//        }
//
//        private PLink link(X x, float p) {
//            return new PLink(x, p);
//        }
//
//        @Override
//        @Nullable public PLink<X> get(Object x) {
//            int id = id(x);
//            return get((X) x, id);
//        }
//
//        private int id(Object x) {
//            return map.getIfAbsent(x, -1);
//        }
//
//        @Override
//        public void set(X key, float p) {
//            set(id(key), p);
//        }
//
//        private void set(int id, float p) {
//            this.pri[id] = p;
//        }
//
//        @Nullable private PLink<X> get(X x, int id) {
//            return (id >= 0) ? link(x, pri[id]) : null;
//        }
//
//        @Override
//        public PLink<X> put(X key, PriReference<X> r) {
//            int id = map.getIfAbsentPutWith(key, (R)->{
//                int i = take();
//                set(i, R.pri());
//                return i;
//            }, r);
//            return link(id);
//        }
//
//        @Override
//        public int size() {
//            return map.size();
//        }
//    }