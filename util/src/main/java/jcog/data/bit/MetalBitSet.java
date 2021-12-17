package jcog.data.bit;

import jcog.TODO;
import jcog.data.array.IntComparator;
import jcog.data.iterator.AbstractIntIterator;
import org.eclipse.collections.api.iterator.IntIterator;

import java.util.Random;
import java.util.function.IntPredicate;

/**
 * Bare Metal Fixed-Size BitSets
 * <p>
 * for serious performance. implementations will not check index bounds
 * nor grow in capacity
 *
 * TODO methods from: http://hg.openjdk.java.net/jdk/jdk/file/2cc1ae79b303/src/java.xml/share/classes/com/sun/org/apache/xalan/internal/xsltc/dom/BitArray.java
 */
public abstract class MetalBitSet implements IntPredicate {

    public static MetalBitSet bits(boolean[] arr) {
        MetalBitSet b = bits(arr.length);
        for (int i = 0, arrLength = arr.length; i < arrLength; i++) {
            if (arr[i])
                b.set(i);
        }
        return b;
    }

    public abstract MetalBitSet clone();

    public abstract void set(int i, boolean v);

    public final void set(int i) {
        set(i, true);
    }

    public final void clear(int i) {
        set(i, false);
    }

    public final MetalBitSet set(int... ii) {
        assert(ii.length>0);
        for (int i : ii)
            set(i);
        return this;
    }


    public abstract void clear();

    public abstract int cardinality();

    public boolean isEmpty() {
        return cardinality() == 0;
    }




    public void setRange(boolean v, int start, int end) {
        if (v) {
            for (int i = start; i < end; i++)
                set(i);
        } else {
            for (int i = start; i < end; i++)
                clear(i);
        }
    }


    /** use caution if capacity exceeds what you expect */
    public int first(boolean what) {
        return next(what, 0, capacity());
    }


    /**
     * finds the next bit matching 'what' between from (inclusive) and to (exclusive), or -1 if nothing found
     * TODO use Integer bit methods
     */
    public int next(boolean what, int from, int to) {
        for (int i = from; i < to; i++) {
            if (test(i) == what)
                return i;
        }
        return -1;
    }

    /** modifies this by OR-ing the values with another bitset of equivalent (or smaller?) capacity */
    public void orThis(MetalBitSet other) {
        throw new TODO();
    }

    /** modifies this by AND-ing the values with another bitset of equivalent (or smaller?) capacity */
    public void andThis(MetalBitSet other) {
        throw new TODO();
    }

    public abstract int capacity();

    public final IntComparator indexComparator() {
        return (a,b) -> Boolean.compare(test(a), test(b));
    }

    public final IntComparator indexComparatorReverse() {
        return (a,b) -> Boolean.compare(test(b), test(a));
    }

    public void swap(int a, int b) {
        if (a!=b) {
            boolean A = test(a), B = test(b);
            if (A!=B) {
                set(a, B);
                set(b, A);
            }
        }
    }

    /** modifies this instance by inverting all the bit values
     *  warning this may modify bits beyond the expected range, causing unexpected cardinality changes
     *  returns this instance
     * */
    public abstract MetalBitSet negateThis();

    public void negateBit(int b) {
        set(b, !test(b));
    }

    public MetalBitSet negateAll(int lowestBits) {
        for (int b = 0; b < lowestBits; b++)
            negateBit(b);
        return this;
    }


    /** TODO optimized impl in subclasses */
    public MetalBitSet and(MetalBitSet other) {
        int c = cardinality();
        assert(c == other.cardinality());
        if (this == other) return this;

        //TODO if (this.equals(other)

        MetalBitSet m = bits(c);
        for (int i = 0; i < c; i++)
            m.set(i, test(i) & other.test(i));

        //TODO if (m.equals(this)) else if m.equals(...)
        return m;
    }

    @Override
    public boolean equals(Object obj) {
        throw new TODO();
    }

    //    public void setAll(int bitVector, int o) {
//        assert(o < 32);
//        for (int i = 0; bitVector!=0 && i < o; i++) {
//            if ((bitVector & 1) != 0)
//                set(i);
//            bitVector >>= 1;
//        }
//    }

    public int random(Random rng) {
        int c = cardinality();
        if (c <= 1) {
            return first(true);
        } else {
            int which = rng.nextInt(c);
            int at = -1;
            for ( ; which >= 0; which--)
                at = next(true, at+1, Integer.MAX_VALUE);
            return at;
        }
    }

    public boolean getAndSet(int i, boolean b) {
        boolean was = test(i);
        if (b) set(i); else clear(i);
        return was;
    }

    /** TODO optimized impl in subclasses */
    public int count(boolean b) {
        int n = cardinality();
        int s = 0;
        for (int i = 0; i < n; i++) {
            if (test(i)==b) s++;
        }
        return s;
    }


    static final IntIterator EmptyIntIterator = new IntIterator() {
        @Override public int next() {
            throw new UnsupportedOperationException();
        }
        @Override public boolean hasNext() {
            return false;
        }
    };

    /** interator */
    public IntIterator iterator(int from, int to) {
        int start = MetalBitSet.this.next(true, from, to);
        if (start < 0 || start >= to)
            return EmptyIntIterator;

        //TODO if (start < 0) return new EmptyIntIterator();
        return new MetalBitSetIntIterator(start, to);
    }


    public static MetalBitSet full() {
        return bits(32);
    }

    public static MetalBitSet bits(int size) {
		return size <= 32 ?
                new IntBitSet() :
                //TODO IntArrayBitSet for 32-bit systems
                new LongArrayBitSet(size);

    }

    /**
     * @param from starting index, inclusive
     * @param to ending index, exclusive
     * @return cardinality in the range
     */
    public int cardinality(int from, int to) {
        int count = 0;
        for (int i = from; i < to; i++) {
            if (test(i)) count++;
        }
        return count;
    }

    public void toArray(int fromBit, int toBit, short[] tgt) {
        IntIterator hh = iterator(fromBit, toBit);
        int j = 0;
        while (hh.hasNext()) {
            tgt[j++] = (short) hh.next();
        }
        //assert(j==c);
    }

    public int setNext(int to) {

        for (int x = 0; x < to; ) {
            int y = //nextClearBit(i, to);
                    next(false, x, to);
            if (y < 0)
                break; //none found
            else {
//                if (test(y))
//                    throw new WTF(); //TEMPORARY
                set(y);
                return y;//if (unclearWeak(b)) return b;
            }
            //else { i = b+1; if (i >= to) i = 0; /* wrap-around */ }
        }
        return -1;
    }

    abstract public void setAll(boolean b);

    private final class MetalBitSetIntIterator extends AbstractIntIterator {
        private final int to;

        MetalBitSetIntIterator(int start, int to) {
            super(start, to);
            this.to = to;
        }

        @Override protected int next(int next) {
            return MetalBitSet.this.next(true, next + 1, to);
        }
    }
}