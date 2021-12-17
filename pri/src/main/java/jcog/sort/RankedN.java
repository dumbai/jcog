package jcog.sort;

import jcog.util.ArrayUtil;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;

/** caches item rank values for fast entry comparison as each entry
 * TODO maybe make autocloseable to enforce .clear (aka .close()) for returning Ranked to pools
 * */
public class RankedN<X> extends TopN<X> {

    /** cached rank/strength/weight/value table; maintained to be synchronized with the items array.
     * stored negated for use by SortedArray
     * */
    private float[] value;


    public RankedN(X[] items) {
        super(items);
        value = new float[capacity()];
        //Arrays.fill(value, Float.NaN); //HACK
    }


    @Override
    public void delete() {
        super.delete();
        rank = null;
        value = null;
    }

    public RankedN(X[] buffer, FloatFunction<X> ranking) {
        this(buffer, FloatRank.the(ranking));
    }

    public RankedN(X[] buffer, FloatRank<X> ranking) {
        this(buffer);
        rank(ranking);
    }

    public RankedN<X> rank(FloatRank<X> rank) {
        if (rank!=this.rank) {
            this.rank = rank;
            rerank();
        }
        return this;
    }

    private void rerank() {
        int s = this.size;
        if (s > 0) {
            boolean nulls = false;
            for (int i = 0; i < s; i++) {
                final float vi = floatValueOf(items[i]);
                if (vi == Float.POSITIVE_INFINITY) {
                    items[i] = null; nulls = true;
                }
                value[i] = vi;
            }

            if (nulls) {
                removeNulls();
                s = size();
            }

            if (s > 1)
                QuickSort.quickSort(0, s, this::valueComparator, this::swap);

            commit();
        }
    }

    @Override
    protected int addEnd(X x, float elementRank) {
        int i = super.addEnd(x, elementRank);
        if (i!=-1)
            insertValue(i, elementRank);
        return i;
    }

    @Override
    protected int addAt(int index, X element, float elementRank, int sizeBefore) {
        int i = super.addAt(index, element, elementRank, sizeBefore);
        if (i!=-1)
            insertValue(i, elementRank);

        return i;
    }

    @Override
    protected final float value(int item) {
        return -value[item];
    }

    /** only for use by sorted Array */
    @Override protected final float valueAt(int item, FloatFunction<X> cmp) {
        return value[item];
    }

    private void insertValue(int i, float elementRank) {
        float[] v = this.value;

        int shift = size-1-i;
        if (shift > 0)
            System.arraycopy(v, i, v, i+1, shift );

        v[i] = elementRank;
    }


    @Override
    public X remove(int index) {
        int totalOffset = this.size - index - 1;
        if (totalOffset < 0)
            return null;

        X[] list = this.items;
        X previous = list[index];
        if (totalOffset > 0) {
            float[] value = this.value;
            System.arraycopy(value, index + 1, value, index, totalOffset);
            System.arraycopy(list, index + 1, list, index, totalOffset);
        }
        --size;
        value[size] = 0;
        list[size] = null;
        commit();
        return previous;
    }


    @Override
    protected final void rejectExisting(X e) {
    }

    @Override
    protected final void rejectOnEntry(X e) {

    }

    private void swap(int a, int b) {
        ArrayUtil.swapObjFloat(items, value, a, b);
    }

    private int valueComparator(int a, int b) {
        if (a == b)
            return 0;
        float[] v = this.value;
        return Float.compare(v[b], v[a]);
    }

//    @Nullable
//    public X getRoulette(FloatSupplier rng, Predicate<X> filter, boolean cached) {
//        int n = size();
//        if (n == 0)
//            return null;
//        if (n == 1)
//            return get(0);
//
//        IntToFloatFunction select = i -> filter.test(get(i)) ? (cached ? rankCached(i) : rank.rank(get(i))) : Float.NaN;
//        return get( //n < 8 ?
//                this instanceof RankedN ?
//                        Roulette.selectRoulette(n, select, rng) : //RankedTopN acts as the cache
//                        Roulette.selectRouletteCached(n, select, rng) //must be cached for consistency
//        );
//
//    }
}