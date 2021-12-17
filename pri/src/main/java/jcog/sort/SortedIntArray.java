package jcog.sort;

import jcog.pri.Prioritized;
import jcog.util.ArrayUtil;
import org.eclipse.collections.api.block.function.primitive.ShortToFloatFunction;
import org.eclipse.collections.api.block.function.primitive.ShortToShortFunction;
import org.eclipse.collections.api.block.predicate.primitive.ShortPredicate;
import org.eclipse.collections.api.block.procedure.primitive.ShortProcedure;

import java.util.Arrays;

public class SortedIntArray /*extends IntIterator*/ {

    /** TODO TUNE */
    private static final int
        BINARY_SEARCH_THRESHOLD_INSERT = 4,

        /** find tests primitive short equality and doesnt need to involve priority tests,
         *  so it can be larger */
        BINARY_SEARCH_THRESHOLD_FIND = 8;

    private static final float GROWTH_RATE = 1.5f;

    /** item indices */
    public short[] items = ArrayUtil.EMPTY_SHORT_ARRAY;

    protected int size;

    public static int grow(int oldSize) {
        return 1 + (int) (oldSize * GROWTH_RATE);
    }


//    public final short getSafe(int i) {
//        short[] ii = this.items;
//        int s = Math.min(ii.length, size);
//        return s == 0 || i >= s ? null : ii[i];
//    }


    public final short get(int i) {
        return items[i];//(X) ITEM.getOpaque(items, i);
    }

    /**
     * direct array access; use with caution ;)
     */
    public short[] array() {
        return items;
    }

    public final int size() {
        return size;
    }

    public void removeLast(int n) {
        if (n <= 0) return;

        int index = size - 1;
        int shift = this.size - index - n;
        //if (shift >= 0) {

        short[] list = this.items;
        if (shift > 0)
            System.arraycopy(list, index + n, list, index, shift);

        size-=n;

    }

    public short remove(int index) {

        int shift = this.size - index - 1;
        if (shift >= 0) {
            short[] list = this.items;
            short previous = list[index];
            if (shift > 0)
                System.arraycopy(list, index + 1, list, index, shift);

            --size;
            return previous;
        }
        return Short.MIN_VALUE;
    }


    protected final boolean removeFast(short x, int index) {
        short[] items = this.items;
        if (items[index] == x) {
            int shift = (this.size--) - index - 1;
            if (shift > 0)
                System.arraycopy(items, index + 1, items, index, shift);
            return true;
        }
        return false;
    }

    public boolean remove(short removed, ShortToFloatFunction cmp) {
        int i = indexOf(removed, cmp);
        if (i >= 0) {
            short removed2 = remove(i);
            if (removed2!=removed)
                throw new NullPointerException(SortedIntArray.class.getSimpleName() + " removal fault");
            return true;
        }
        return false;
    }

    public void delete() {
        size = 0;
        items = ArrayUtil.EMPTY_SHORT_ARRAY;
    }

    public void clear() {
        size = 0;
    }

//    public final int add(short element, ShortToFloatFunction cmp) {
//        float elementRank = cmp.valueOf(element);
//        int i = (elementRank == elementRank) ? insert(element, elementRank, cmp) : -1;
//        if (i < 0)
//            rejectOnEntry(element);
//        return i;
//    }

    public final int insert(short item, float negPri, ShortToFloatFunction cmp) {
        int y = insert(item, indexOf(item, negPri, cmp, true));
//        //TEMPORARY
//        if (_count(item) > 1)
//            throw new WTF();
        return y;
    }

    @Deprecated private int _count(short x) {
        int c = 0;
        for (int i = 0; i < size; i++) {
            if (items[i]==x) c++;
        }
        return c;
    }

    private int insert(short item, int index) {
        //        assert (index != -1);
        int s = size;
        return index == s ?
                addEnd(item) :
                addAt(index, item, s);

//        if (!isSorted(cmp)) throw new WTF();
    }

    public final boolean isEmpty() {
        return size == 0;
    }

    protected boolean grows() {
        return true;
    }

    protected int addEnd(short x) {
        int s = this.size;
        if (capacity() == s) {
            if (!grows())
                return -1;

            capacity(grow(s));
        }
        items[size++] = x;
        return s;
    }

    private static final int SHRINK_FACTOR_THRESHOLD = 2;

    public void capacity(int newLen) {
        //assert (newLen >= size);
        short[] ii = items;
        int oldLen = ii.length;
        if (newLen > oldLen || oldLen > newLen * SHRINK_FACTOR_THRESHOLD) {
            this.items = Arrays.copyOf(ii, grow(newLen));
        }
        this.size = Math.min(size, newLen);
    }

    protected int addAt(int index, short element, int sizeBefore) {

        short[] items = this.items;

        boolean adding;

        int c = capacity();
        if (c == sizeBefore) {
            if (adding = grows()) {
                this.items = items = Arrays.copyOf(items, c = grow(sizeBefore));
            } else {
                rejectExisting(items[sizeBefore - 1]); //pop
            }
        } else {
            adding = true;
        }

        if (adding)
            size++;

        int ss = Math.min(sizeBefore - 1, c - 2) + 1;
        if (ss - index >= 0)
            System.arraycopy(items, index, items, index + 1, ss - index);

        items[index] = element;

        return index;

    }

    /**
     * called when the lowest value has been kicked out of the list by a higher ranking insertion
     */
    protected void rejectExisting(short e) {

    }

    public short removeFirst() {
        return /*size == 0 ? Short.MIN_VALUE :*/ remove(0);
    }

    public short removeLast() {
        return remove(size-1);
    }


    public final int capacity() {
        return items.length;
    }

//    /**
//     * tests for descending sort
//     */
//    public boolean isSorted(ShortToFloatFunction f) {
//        short[] ii = this.items;
//        for (int i = 1; i < size; i++) {
//            if (f.floatValueOf(ii[i - 1]) >= f.floatValueOf(ii[i])) //TODO use valueAt(
//                return false;
//        }
//        return true;
//    }

    public int indexOf(short element, ShortToFloatFunction cmp) {
        return size == 0 ?
                -1 :
                indexOf(element, cmp.valueOf(element) /*Float.NaN*/, cmp, false);
    }

    /** p is actually the negative of the rank TODO invert this and all comparisons */
    public final int indexOf(short x /* can be -1 for find */, float p /* HACK */, ShortToFloatFunction f, boolean insertOrFind) {

        int s = size;
        if (s == 0) return insertOrFind ? 0 : -1;

        short[] items = this.items;
        int left = 0, right = s;

        int searchThresh = insertOrFind ? BINARY_SEARCH_THRESHOLD_INSERT : BINARY_SEARCH_THRESHOLD_FIND;

        while (right - left > searchThresh) {

            int mid = (left + right)/2;

            short m = items[mid];
            if (!insertOrFind && m == x)
                return mid;

            float mm = f.valueOf(m);
            if (p == mm) {
                if (!insertOrFind)
                    break; //in the middle; stop testing priority
            }
            if (p <= mm)
                right = mid;
            else
                left = mid;
        }


        for (int i = left; i < right; i++) {
            short ii = items[i];
            if (insertOrFind) {
                if (p < f.valueOf(ii))
                    return i;
            } else {
                if (x == ii)
                    return i;
            }
        }

        //TODO exclude the range scanned already
        return insertOrFind ?
                right
                :
                indexOfExhaustive(x);
    }


    private int indexOfExhaustive(short e) {
        short[] items = this.items;
        int s = Math.min(items.length, this.size);
        return ArrayUtil.indexOf(items, e, 0, s);
    }


    /**
     * Returns the first (lowest) element currently in this list.
     */
    public final short first() {
        return items[0];
    }

    /**
     * Returns the last (highest) element currently in this list.
     */
    public final short last() {
        return items[this.size - 1];
    }

    public void forEach(ShortProcedure action) {
        short[] items = this.items;
        int s = Math.min(items.length, size);
        for (int i = 0; i < s; i++)
            action.value(items[i]);
    }

    public final boolean whileEach(ShortPredicate action) {
        return whileEach(-1, action);
    }

    public final boolean whileEach(int n, ShortPredicate action) {
        int s0 = this.size;
        int s = (n == -1) ? s0 : Math.min(s0, n);
        if (s > 0) {
            short[] ii = items;
            for (int i = 0; i < s; i++) {
                if (!action.accept(ii[i]))
                    return false;
            }
        }
        return true;
    }

    public final void removeRange(int start, int end, ShortProcedure a) {

        int removed = end - start;
        if (removed <= 0) return;

        short[] items = this.items;
        for (int i = start; i < end; i++)
            a.value(items[i]);

        ArrayUtil.shiftTailOverGap(items, size, start, end);
//        ArrayUtil.shiftTailOverGap(items, size, start, end, Short.MIN_VALUE); //for debug
        size -= removed;
    }

    public void update(short item, float priBefore, float priAfter, ShortToFloatFunction f) {

        int posPrev = indexOf(item, -priBefore, f, false); //assert(posPrev >= 0);
        int posNext = indexOf((short)-1, -priAfter, f, true);
        if (posNext > posPrev) posNext--; //adjust as if it were not present since it remaining there affects the calculation

        if (posNext == posPrev) {
            // no change
        } else if (Math.abs(posNext - posPrev)==1) {
            swap(posNext, posPrev);
        } else {
            boolean removed = removeFast(item, posPrev); //assert (removed);
            int index = insert(item, posNext); //assert (index >= 0);
        }
    }

    private void swap(int posBefore, int posAfter) {
        ArrayUtil.swapShort(items, posBefore, posAfter);
    }

    public void replace(ShortToShortFunction each) {
        short[] items = this.items;
        int s = this.size;
        for (int i = 0; i < s; i++)
            items[i] = each.valueOf(items[i]);
    }


    /** for diagnostic purposes */
    public boolean isSorted(ShortToFloatFunction model) {
        int s = size();
        float prev = model.valueOf(get(0));
        for (int i = 1; i < s; i++) {
            float next = model.valueOf(get(i));
            if (next < prev - Prioritized.EPSILON*2)
                return false;
            prev = next;
        }
        return true;


    }

}