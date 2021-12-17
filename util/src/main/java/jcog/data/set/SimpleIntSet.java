package jcog.data.set;

import jcog.Util;
import jcog.data.bit.MetalBitSet;

import java.io.Serializable;
import java.util.*;
import java.util.function.IntPredicate;

/**
 * A utility class for efficiently storing a set of integers. The implementation
 * is based on Algorithm D (Open addressing with double hashing) from Knuth's
 * TAOCP page 528.
 *
 * @author Edward Raff
 * https:
 * https:
 */
public class SimpleIntSet extends AbstractSet<Integer> implements Serializable {

    private static final int DEFAULT_CAPACITY = 4;
    private static final float loadFactor = 0.75f;

    private int size = 0;
    /**
     * true if occupied, false otherwise (i.e., free)
     */
    private MetalBitSet status;
    private int[] keys;

    /**
     * Creates a new empty integer setAt
     */
    public SimpleIntSet() {
        this(DEFAULT_CAPACITY);
    }


    /**
     * Creates an empty integer set pre-allocated to store a specific number of
     * items
     *
     * @param capacity the number of items to store
     */
    public SimpleIntSet(int capacity) {
        int size = getNextPow2TwinPrime((int) Math.max(capacity / loadFactor, 4));
        alloc(size);
        this.size = 0;
    }

    private void alloc(int size) {
        status = MetalBitSet.bits(size);
        keys = new int[size];
    }

    /**
     * Creates a new set of integers from the given setAt
     *
     * @param set the set of integers to create a copy of
     */
    public SimpleIntSet(Set<Integer> set) {
        this(set.size());
        this.addAll(set);
    }

    /**
     * Creates a set of integers from the given collection
     *
     * @param collection a collection of integers to create a set from
     */
    public SimpleIntSet(Collection<Integer> collection) {
        this();
        this.addAll(collection);
    }


    /**
     * Gets the index of the given key. Based on that {@link #status} variable,
     * the index is either the location to insert OR the location of the key.
     * <p>
     * This method returns 2 integer table in the long. The lower 32 bits are
     * the index that either contains the key, or is the first empty index.
     * <p>
     * The upper 32 bits is the index of the first position marked as
     * {@link #DELETED} either {@link Integer#MIN_VALUE} if no position was
     * marked as DELETED while searching.
     *
     * @param key they key to search for
     * @return the mixed long containing the index of the first DELETED position
     * and the position that the key is in or the first EMPTY position found
     */
    private int index(int key) {

        int hash = key & 0x7fffffff;

        int[] k = this.keys;
        int len = k.length;

        int i = hash % len;


        IntPredicate s = this.status;
        if (!s.test(i) || k[i] == key)
            return i;


        int c = 1 + (hash % (len - 2));

        while (true) {

            i -= c;
            if (i < 0)
                i += len;

            if (!s.test(i) || k[i] == key)
                return i;
        }
    }

    private void enlargeIfNeeded() {
        if (size + 1 < keys.length * loadFactor)
            return;


        IntPredicate oldSatus = status;
        int[] oldKeys = keys;

        int newSize = getNextPow2TwinPrime(keys.length * 3 / 2);
        alloc(newSize);

        size = 0;
        for (int oldIndex = 0; oldIndex < oldKeys.length; oldIndex++)
            if (oldSatus.test(oldIndex))
                add(oldKeys[oldIndex]);
    }

    @Override
    public void clear() {
        size = 0;
        status.clear();
    }

    @Override
    public boolean add(Integer e) {
        return e != null && add(e.intValue());
    }

    /**
     * @param e element to be added to this setAt
     * @return true if this set did not already contain the specified element
     */
    public boolean add(int e) {
        int key = e;
        int pair_index = index(key);


        int valOrFreeIndex = pair_index;

        if (status.test(valOrFreeIndex))
            return false;


        int i = valOrFreeIndex;


        status.set(i);
        keys[i] = key;
        size++;

        enlargeIfNeeded();

        return true;
    }

    public boolean contains(int o) {
        return status.test(index(o));
    }


    @Override
    public boolean contains(Object o) {
		return o instanceof Integer && contains(((Integer) o).intValue());
    }


    @Override
    public Iterator<Integer> iterator() {

        int START = 0;
        while (START < keys.length && !status.test(START))
            START++;
        if (START == keys.length)
            return Util.emptyIterator;

        int startPos = START;

        return new IntegerIterator(startPos);
    }

    @Override
    public int size() {
        return size;
    }


    /**
     * Gets the next twin prime that is near a power of 2 and greater than or
     * equal to the given value
     *
     * @param m the integer to get a twine prime larger than
     * @return the a twin prime greater than or equal to
     */
    static int getNextPow2TwinPrime(int m) {
        int pos = Arrays.binarySearch(twinPrimesP2, m + 1);
        int p = (pos >= 0) ? pos : -pos - 1;
        return twinPrimesP2[p];
    }

    /**
     * This array lits twin primes that are just larger than a power of 2. The
     * prime in the list will be the larger of the twins, so the smaller can be
     * obtained by subtracting 2 from the value stored. The list is stored in
     * sorted order.<br>
     * Note, the last value stored is just under 2<sup>31</sup>, where the other
     * values are just over 2<sup>x</sup> for x &lt; 31
     */
    static final int[] twinPrimesP2 =
            {
                    7,
                    13,
                    19,
                    43,
                    73,
                    139,
                    271,
                    523,
                    1033,
                    2083,
                    4129,
                    8221,
                    16453,
                    32803,
                    65539,
                    131113,
                    262153,
                    524353,
                    1048891,
                    2097259,
                    4194583,
                    8388619,
                    16777291,
                    33554503,
                    67109323,
                    134217781,
                    268435579,
                    536871019,
                    1073741833,
                    2147482951,
            };


    private class IntegerIterator implements Iterator<Integer> {
        int pos;
        int prevPos;

        IntegerIterator(int startPos) {
            pos = startPos;
            prevPos = -1;
        }

        @Override
        public boolean hasNext() {
            return pos < keys.length;
        }

        @Override
        public Integer next() {

            int oldPos = prevPos = pos++;

            int pos = this.pos;
            IntPredicate s = SimpleIntSet.this.status;
            int[] k = SimpleIntSet.this.keys;
            while (pos < k.length && !s.test(pos))
                pos++;
            this.pos = pos;

            return k[oldPos];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}