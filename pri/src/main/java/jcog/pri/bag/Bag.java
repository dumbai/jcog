package jcog.pri.bag;

import com.google.common.collect.Streams;
import jcog.Util;
import jcog.data.list.FastAtomicReferenceArray;
import jcog.data.list.Lst;
import jcog.data.list.table.Table;
import jcog.pri.Forgetting;
import jcog.pri.Pressurizable;
import jcog.pri.Prioritizable;
import jcog.pri.Prioritized;
import jcog.pri.op.PriMerge;
import jcog.signal.MutableFloat;
import jcog.signal.MutableInteger;
import jcog.signal.NumberX;
import org.eclipse.collections.api.block.function.primitive.FloatFloatToFloatFunction;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;


/**
 * X=key, Y = item/value of type Item
 * TODO make an abstract class by replacing ArrayBag's superclass inheritance with delegation
 */
public abstract class Bag<X, Y> implements Table<X, Y>, Sampler<Y>, Pressurizable {

//    private static final VarHandle MASS = Util.VAR(Bag.class, "mass", float.class);
//    private static final VarHandle PRESSURE = Util.VAR(Bag.class, "pressure", float.class);
//    private static final VarHandle CAPACITY = Util.VAR(Bag.class, "capacity", float.class);
//    private volatile int mass;
//    private volatile int pressure;
//    private volatile int capacity;

    private final MutableFloat mass = new MutableFloat(), pressure = new MutableFloat();
    protected final MutableInteger capacity = new MutableInteger();

    protected PriMerge merge;

    /**
     * The NumberX of items in the bag
     *
     * @return The NumberX of items
     */
    @Override
    public abstract int size();

    /**
     * commits the next set of changes and updates budgeting
     *
     *
     * @param update*/
    public abstract void commit(@Nullable Consumer<? super Y> update);


    public abstract void forEach(int max, Consumer<? super Y> action);

    /**
     * resolves the key associated with a particular value
     */
    public abstract X key(Y value);


//    protected static <X, Y> void forEachActive(Bag<X, Y> bag, AtomicReferenceArray<Y> map, Consumer<? super Y> e) {
//        forEach(map, bag::active, e);
//    }
    protected static <X, Y> void forEachActive(Bag<X, Y> bag, FastAtomicReferenceArray map, Consumer<? super Y> e) {
        forEach(map, bag::active, e);
    }

    protected static <Y> void forEach(FastAtomicReferenceArray map, Predicate<Y> accept, Consumer<? super Y> e) {
        forEach(map, Integer.MAX_VALUE, accept, e);
    }

    protected static <Y> void forEach(FastAtomicReferenceArray map, int max, Predicate<Y> accept, Consumer<? super Y> e) {
        for (int c = Math.min(max, map.length()), j = 0; j < c; j++) {
            Object _v = map.getOpaque(j);
            if (_v != null) {
                Y v = (Y) _v;
                if (accept.test(v))
                    e.accept(v);
            }
        }
    }

    protected static <Y> void forEach(FastAtomicReferenceArray map, int max, Consumer<? super Y> e) {
        for (int c = Math.min(max, map.length()), j = 0; j < c; j++) {
            Object _v = map.getOpaque(j);
            if (_v != null)
                e.accept((Y) _v);
        }
    }

    protected static <Y> void forEach(AtomicReferenceArray<Y> map, Predicate<Y> accept, Consumer<? super Y> e) {
        for (int c = map.length(), j = 0; j < c; j++) {
            Y v = map.getOpaque(j);
            if (v != null && accept.test(v)) {
                e.accept(v);
            }
        }
    }
    protected static <Y> void forEach(AtomicReferenceArray<Y> map, int max, Consumer<? super Y> e) {
        for (int c = map.length(), j = 0; j < c; j++) {
            Y v = map.getOpaque(j);
            if (v != null) {
                e.accept(v);
                if (--max <= 0)
                    return;
            }
        }
    }


    protected abstract void _setCapacity(int before, int after);

    @Override
    public abstract @Nullable Y remove(X x);

    public abstract Y put(Y b, @Nullable NumberX overflowing);



    /**
     * returns the priority of a value, or NaN if such entry is not present
     */
    public abstract float pri(Y key);


//    /**
//     * iterates all items in (approximately) descending priority
//     * forEach may be used to avoid allocation of iterator instances
//     */
//    @Override
//    public abstract Iterator<Y> iterator();
//
//    /**
//     * returns the bag to an empty state.
//     * should also depressurize to zero
//     */
//    @Override
//    public abstract void clear();

    public final Y put(Y x) {
        return put(x, null);
    }


    public Stream<Y> stream() {
        Stream<Y> s = Streams.stream(this);
        //return s;
        return s.filter(Objects::nonNull); //HACK
    }

//    public final Y[] toArray(@Nullable Y[] _target) {
//        return toArray(_target, y->y);
//    }
//
//    /** subclasses may have more efficient ways of doing this */
//    public <Z> Z[] toArray(@Nullable Z[] _target, Function<Y,Z> apply) {
//        int s = size();
//        if (s == 0) return (Z[]) ArrayUtil.EMPTY_OBJECT_ARRAY;
//
//        Z[] target = _target == null || _target.length < s ? Arrays.copyOf(_target, s) : _target;
//
//        int[] i = {0}; //HACK this is not good. use a AND predicate iteration or just plain iterator?
//
//        forEach(s, (y) -> target[i[0]++] = apply.apply(y));
//
//        //either trim the array. size could have decreased while iterating, or its perfect sized
//        return i[0] < target.length ? Arrays.copyOf(target, i[0]) : target;
//    }
//
//    public @Nullable X reduce(BiFunction<Y,X,X> each, X init) {
//        X x = init;
//        for (Y y : this)
//            x = each.apply(y, x);
//        return x;
//    }


    /**
     * Check if an item is in the bag.  both its key and its value must match the parameter
     *
     * @param it An item
     * @return Whether the Item is in the Bag
     */
    public final boolean contains(X it) {
        return get(it) != null;
    }

    public boolean isEmpty() {
        return size() == 0;
    }


    public void onRemove(Y value) {

    }

//    public void onEvict(Y incoming) {
//        if (incoming instanceof Prioritized)
//            ((ScalarValue)incoming).delete();
//    }

    /**
     * called if an item which was attempted to be inserted was not
     */
    public void onReject(Y value) {

    }

    public void onAdd(Y y) {

    }

    /**
     * true if an item is not deleted
     */
    public final boolean active(Y key) {
        float x = pri(key);
        return (x == x);

    }

    public float priElse(Y key, float valueIfMissing) {
        float p = pri(key);
        return (p == p) ? p : valueIfMissing;
    }


    public void print() {
        print(System.out);
    }

    public void print(PrintStream p) {
        forEach(p::println);
    }


    /**
     * priIfy only non-deleted items
     */
    private float priIfyNonDeleted(float initial, FloatFloatToFloatFunction reduce) {
        float[] x = {initial};
        for (Y y : this) {
            float p = pri(y);
            if (p == p)
                x[0] = reduce.valueOf(x[0], p);
        }
        return x[0];
    }

//    /**
//     * should visit items highest priority first, if possible.
//     * for some bags this may not be possible.
//     */
//    public float priSum() {
//        return priIfyNonDeleted(0, Float::sum);
//    }

    /**
     * public slow implementation.
     * returns a value between 0..1.0. if empty, returns 0
     */
    @Deprecated public float priMin() {
        float m = priIfyNonDeleted(Float.POSITIVE_INFINITY, Math::min);
        return Float.isFinite(m) ? m : 0;
    }

    /**
     * public slow implementation.
     * returns a value between 0..1.0. if empty, returns 0
     */
    @Deprecated public float priMax() {
        float m = priIfyNonDeleted(Float.NEGATIVE_INFINITY, Math::max);
        return Float.isFinite(m) ? m : 0;
    }

    public double priMean() {
        double s = 0;
        int c = 0;
        for (Y x : this) {
            s += priElse(x, 0);
            c++;
        }
        return c > 0 ? s/c : 0;
    }


    public final void setCapacity(int capNext) {
        assert(capNext >= 0);
        int capBefore = capacity.getAndSet(capNext);
        if (capBefore!=capNext)
           _setCapacity(capBefore, capNext);
    }

    public final <B extends Bag<X,Y>> B capacity(int c) {
        setCapacity(c);
        return (B) this;
    }

    public float[] histogram(float[] x) {
        int bins = x.length;
        for (Y budget : this) {
            float p = priElse(budget, 0);
            int b = Util.bin(p, bins - 1);
            x[b]++;
        }
        double total = 0;
        for (float e : x) {
            total += e;
        }
        if (total > 0) {
            for (int i = 0; i < bins; i++)
                x[i] /= total;
        }
        return x;
    }


    @Deprecated public final void commit() {
        commit(forget(1));
    }

    /**
     * creates a forget procedure for the current bag's
     * state, which can be applied as a parameter to the commit(Consumer<Y>) method
     * temperature is a value >=0 controlling
     * how fast the bag should allow new items.
     *
     * >1: active forgetting
     * 1: balances pressure with forgetting
     * <1: sustains existing elements
     * 0: no forgetting
     *
     */
    @Deprecated public @Nullable Consumer<Y> forget(float temperature) {
        return Forgetting.forget((Bag) this, temperature);
        //return Forgetting.forgetL1WeightDecay((Bag) this, temperature);
    }


    @Override
    public void forEachKey(Consumer<? super X> each) {
        for (Y b : this)
            each.accept(key(b));
    }

    /** sets the bag's merge strategy */
    public Bag<X,Y> merge(PriMerge merge) {
        this.merge = merge;
        return this;
    }

    /** gets the bag's merge strategy */
    public final PriMerge merge() {
        return merge;
    }
    protected final void massAdd(float m) {
        mass.add(m);
    }
    protected final void massSet(float m) {
        mass.set(m);
    }
    protected final void pressureZero() {
        pressure.set(0);
    }

    @Override
    public int capacity() {
        return capacity.intValue();
    }

    public float mass() {
        return mass.asFloat();
    }
    @Override
    public float pressure() {
        return pressure.asFloat();
    }

    /**
     * WARNING this is a duplicate of code in hijackbag, they ought to share this through a common Pressure class extending AtomicDouble or something
     */
    @Override
    public void pressurize(float f) {
        if (Math.abs(f) > Float.MIN_NORMAL)
            pressure.add(f);
    }

    /**
     * WARNING this is a duplicate of code in hijackbag, they ought to share this through a common Pressure class extending AtomicDouble or something
     */
    @Override
    public void depressurize(float toRemove) {
        if (toRemove > Float.MIN_NORMAL)
            pressure.update((p, r) -> Math.max(0, p - r), toRemove);
    }

    @Override
    public float depressurizePct(float percentToRemove) {
        if (percentToRemove < Prioritized.EPSILON)
            return 0; //remove nothing

        float p;
        if (percentToRemove >= 1) {
            p = pressure.getAndSet(0);
        } else {

            float percentToRemain = 1 - percentToRemove;

            float[] delta = new float[1];
            pressure.update((priBefore, f) -> {
                float priAfter = priBefore * f;
                delta[0] = priBefore - priAfter;
                return priAfter;
            }, percentToRemain);
            p = delta[0];
        }

        return Math.max(0, p);
    }
//
//    /** HACK this impl sux */
//    @Nullable public Y poll(@Nullable Random rng) {
//        final Object[] y = new Object[1];
//        pop(rng,1, ((yi) -> y[0] = yi));
//        return (Y)y[0];
//    }

    /** estimates the available capacity */
    public int available() {
        return Math.max(0, capacity() - size());
    }

    /** a "soft" forget/clear that doesnt result in element removal, but makes them instantly vulnerable for eviction */
    public void clear(float rate) {
        float mult = 1 - rate;
        Consumer<Y> u;
        if (mult <= Float.MIN_NORMAL) {
            u = x -> ((Prioritizable)x).pri(0); //zero
        } else {
            u = x -> ((Prioritizable)x).priMul(mult);
        }
        depressurizePct(1 /*rate*/);
        commit(u);
    }

    /** sets all items to priority zero */
    public void clearSoft() {
        commit(z -> ((Prioritizable)z).pri(0));
        depressurizePct(1);
    }

    public void addAllTo(Collection<Y> l) {
        forEach(l::add);
    }


//    /**
//     * TODO super-bag acting as a router for N sub-bags
//     */
//    abstract class CompoundBag<K, Y> implements Bag<K, Y> {
//        abstract public Bag<K, Y> bag(int selector);
//
//        /**
//         * which bag to associate with a keys etc
//         */
//        abstract protected int insertToWhich(K key);
//    }
}