package jcog.sort;

import jcog.Util;
import jcog.decide.Roulette;
import jcog.math.FloatSupplier;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.IntFunction;

import static java.lang.Float.NEGATIVE_INFINITY;

/**
 * warning: this keeps duplicate insertions
 */
public class TopN<X> extends SortedArray<X> implements FloatFunction<X>, TopFilter<X> {

    public FloatRank<X> rank;
    float min = NEGATIVE_INFINITY;

    public TopN(X[] target) {
        this.items = target;
    }


    /**
     * try to use the FloatRank if a scoring function can be interrupted
     */
    public TopN(X[] target, FloatFunction<X> rank) {
        this(target, FloatRank.the(rank));
    }

    public TopN(X[] target, FloatRank<X> rank) {
        this(target);
        rank(rank);
    }

    @Override
    public void clear() {
        super.clear();
        //clearFast();
        min = NEGATIVE_INFINITY;
    }


    public final boolean isSorted() {
        return isSorted(this);
    }


    public TopFilter<X> rank(FloatRank<X> rank) {
        this.rank = rank;
        return this;
    }


    @Override
    protected boolean exhaustiveFind() {
        return false;
    }


    public void clear(int newCapacity, IntFunction<X[]> newArray) {
        min = NEGATIVE_INFINITY;
        if (items == null || items.length != newCapacity) {
            items = newArray.apply(newCapacity);
            size = 0;
        } else {
            super.clear();
        }
    }


    /** TODO accelerate somehow? */
    public final boolean addUnique(/*@NotNull*/ X x) {
        return !contains(x) && add(x);
    }

    public final boolean add(/*@NotNull*/ X x) {

        if (!addRanked(x))
            return false;

        commit();

        return true;
    }

    private boolean addRanked(X x) {
        float negRank = floatValueOf(x);
        if (-negRank > min) {
            int r = addRanked(x, negRank, this);
            return r >= 0;
        }
        return false;
    }


    @Override
    protected boolean grows() {
        return false;
    }


    @Override
    public final void accept(X e) {
        add(e);
    }

    protected final void commit() {
        min = size >= capacity() ? _minValue() : NEGATIVE_INFINITY;
    }

    @Override
    public @Nullable X pop() {
        X x = removeFirst();
        if (x!=null)
            commit();
        return x;
    }

    protected float value(int item) {
        return rank.rank(items[item]);
    }

//    float maxValue() {
//        return size > 0 ? value(0) : Float.NaN;
//    }

    @Override
    public X remove(int index) {
        X x = super.remove(index);
        commit();
        return x;
    }

    /** manually re-calculates minimum value */
    public final float minValue() {
        int s = size;
        if (s == capacity())
            return min; //return the cached value
        return _minValue();
    }

    private float _minValue() {
        int s = size;
        return s <= 0 ? Float.NaN : value(s - 1);
    }

    public final float minValueIfFull() {
        return min;
    }

    /** for use only by sortedarray.  the value is negated for its default descending sort */
    @Deprecated @Override public final float floatValueOf(X x) {
        float r = this.rank.rank(x, min);
        return r != r ? Float.POSITIVE_INFINITY : -r;
    }


    public final @Nullable X getRoulette(Random rng) {
        return switch (this.size) {
            case 0 -> null;
            case 1 -> first();
            default -> getRoulette(rng::nextFloat);
        };
    }

    public final @Nullable X getRoulette(FloatSupplier rng) {
        int n = size;
        switch (n) {
            case 0:
                return null;
            case 1:
                return items[0];
            default:
                float[] w = new float[n];
                float min = Float.POSITIVE_INFINITY, max = NEGATIVE_INFINITY;
                int count = 0;
                for (int i = 0; i < n; i++) {
                    float v = value(i);
                    if (!Float.isFinite(v)) {
                        w[i] = Float.NaN;
                        continue;
                    }
                    w[i] = v;
                    min = Util.min(min, v);
					max = Util.max(max, v);
                    count++;
                }
                if (count == 0) return null;

                float range = max-min;
                if (count == 1 || range < Float.MIN_NORMAL) {
                    //flat except where non-finite
                    for (int i = 0; i < n; i++) {
                        float v = w[i];
                        if (v != v) v = 0;
                        else v = 1;
                        w[i] = v;
                    }
                } else {
                    float margin = 1f/count; //softmax like probablity of selecting lowest item
                    for (int i = 0; i < n; i++) {
                        float v = w[i];
                        if (v != v) v = 0; //store as zero; to be ignored by roulette select
                        else v = (v - min)/range + margin;
                        w[i] = v;
                    }
                }

                return items[ Roulette.selectRoulette(w, rng) ];
        }
    }

//    public void compact(float thresh) {
//        int s = size;
//        if (s == 0) {
//            items = null;
//        } else {
//            if (!isFull(thresh)) {
//                items = Arrays.copyOf(items, s);
//            }
//        }
//    }


//    /**
//     * creates a copy of the array, trimmed
//     */
//    public X[] toArray() {
//        return Arrays.copyOf(items, size);
//    }


//    @Nullable
//    public X[] toArrayIfSameSizeOrRecycleIfAtCapacity(@Nullable X[] x) {
//        int s = size();
//        if (s == 0)
//            return null;
//
//        int xl = x != null ? x.length : 0;
//        if (xl == s) {
//            System.arraycopy(items, 0, x, 0, s);
//            return x;
//        } else {
//            return items.length == s ? items : Arrays.copyOf(items, s);
//        }
//    }


//    public List<X> drain(int count) {
//        count = Util.min(count, size);
//        List<X> x = new Lst<>(count);
//        for (int i = 0; i < count; i++)
//            x.add(removeFirst());
//        commit();
//        return x;
//    }

//    public X[] drain(X[] next) {
//
//        X[] current = this.items;
//
//        this.items = next;
//        this.size = 0;
//        commit();
//
//        return current;
//    }

//    /**
//     * what % to remain; ex: rate of 25% removes the lower 75%
//     */
//    public void removePercentage(float below, boolean ofExistingOrCapacity) {
//        assert (below >= 0 && below <= 1.0f);
//        int belowIndex = (int) (ofExistingOrCapacity ? size() : capacity() * below);
//        if (belowIndex < size) {
//            size = belowIndex;
//            Arrays.fill(items, size, items.length - 1, null);
//            commit();
//        }
//    }

//    public Set<X> removePercentageToSet(float below) {
//        assert(below >= 0 && below <= 1.0f);
//        int belowIndex = (int) Math.floor(size() * below);
//        if (belowIndex == size)
//            return Set.of();
//
//        int toRemove = size - belowIndex;
//        Set<X> removed = new HashSet();
//        for (int i = 0; i < toRemove; i++) {
//            removed.addAt(removeLast());
//        }
//        return removed;
//    }


//    /**
//     * 0 < thresh <= 1
//     */
//    private boolean isFull(float thresh) {
//        return (size >= capacity() * thresh);
//    }

//    public boolean removeIf(Predicate<X> test) {
//        int sBefore = size();
//        if (sBefore == 0) return false;
//
//        int sAfter = sBefore;
//        X[] items = this.items;
//        for (int i = 0, itemsLength = Util.min(sBefore , items.length); i < itemsLength; i++) {
//            X x = items[i];
//            if (x==null)
//                sAfter--;
//            else if (test.test(x)) {
//                items[i] = null;
//                sAfter--;
//            }
//        }
//        if (sBefore!=sAfter) {
//            ArrayUtil.sortNullsToEnd(items, 0, sBefore);
//            this.size = sAfter;
//            return true;
//        }
//        return false;
//    }
}