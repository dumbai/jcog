package jcog.data.set;


import jcog.util.ArrayUtil;
import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;

import java.util.Arrays;

@Deprecated public class MetalLongSet extends LongHashSet {

    public MetalLongSet(int capacity) {
        super(capacity);
        //super(1+capacity/2 /* HACK */);
    }

    public MetalLongSet(long[] initial) {
        this(initial.length);
        addAll(initial);
    }

    public long[] toSortedArray() {

        int s = size();
        if (s == 0)
            return ArrayUtil.EMPTY_LONG_ARRAY;

        long[] l = toArray();
        if (s > 1)
            Arrays.sort(l);
        return l;
    }



//    /** note: this will have trouble with 0 since it is the 'null key' used by the superclass.  stamp 0 shouldnt be expected anyway */
//    public long[] sortedSample(int stampCapacity, Random rng) {
//        //sample(stampCapacity, evi.toLongArray(), rng);
//        if (size() <= stampCapacity) {
//            return toSortedArray();
//        }
//
//        long[] x = key;
//        long[] y = new long[stampCapacity];
//        int n = x.length;
//        int ix = rng.nextInt(n); //random start position
//        int iy = 0;
//        for (int i = ix; ; i++) {
//            long xi = x[i];
//            if (xi !=0) y[iy++] = xi;
//            if (iy >= stampCapacity) break;
//            if (i >= n-1) i = 0;
//        }
//        Arrays.sort(y);
//        return y;
//    }


//    /**
//     * A power-of-2 over-sized array holding the integers in the set along with empty values.
//     */
//    public long[] keys;
//    public int size;
//    public final long emptyVal;
//
//    /**
//     * the count at which a rehash should be done
//     */
//    public int rehashCount;
//
//    /**
//     * @param size     The minimum number of elements this set should be able to hold without rehashing
//     *                 (i.e. the slots are guaranteed not to change)
//     * @param emptyVal The integer value to use for EMPTY
//     */
//    public MetalLongSet(int size) {
//        this.emptyVal = emptyVal();
//        int tsize = Math.max(org.apache.lucene.util.BitUtil.nextHighestPowerOfTwo(size), 1);
//        rehashCount = tsize;
////        rehashCount = tsize - (tsize >> 2);
////        if (size >= rehashCount) {  // should be able to hold "size" w/o re-hashing
////            tsize <<= 1;
////            rehashCount = tsize - (tsize >> 2);
////        }
//        keys = new long[tsize];
//        if (emptyVal != 0)
//            clear();
//    }
//
//    public MetalLongSet(long[] x) {
//        this(x.length);
//        addAll(x);
//    }
//
//    protected long emptyVal() {
//        return Long.MIN_VALUE;
//    }
//
//    public void clear() {
//        Arrays.fill(keys, emptyVal);
//        size = 0;
//    }
//
//    /**
//     * (internal) Return the hash for the key. The default implementation just returns the key,
//     * which is not appropriate for general purpose use.
//     */
//    protected int hash(long key) {
//        return Long.hashCode(key);
//    }
//
//    /**
//     * The number of integers in this setAt.
//     */
//    public final int size() {
//        return size;
//    }
//
//    /**
//     * (internal) Returns the slot for this key
//     */
//    private int slot(long key) {
//
//        int h = hash(key);
//        long[] k = this.keys;
//
//        int n = k.length;
//
//        int s = h & (n - 1);
//        long ks = k[s];
//        if (ks == key || ks == emptyVal) return s;
//
//        int increment = (h >> 7) | 1;
//        do {
//            ks = k[(s = (s + increment) & (n - 1))];
//        } while (ks != key && ks != emptyVal);
//        return s;
//    }
//
//    /**
//     * (internal) Returns the slot for this key, or -slot-1 if not found
//     */
//    private int find(long key, boolean acceptEmpty) {
//
//        int h = hash(key);
//        long[] k = this.keys;
//        int n = k.length;
//
//        int s = h & (n - 1);
//        long ks = k[s];
//        if (acceptEmpty && ks == emptyVal) return -s - 1;
//        if (ks == key) return s;
//
//        int increment = (h >> 7) | 1;
//        int r = 1;
//        for (; ; ) {
//            if (r++ >= n)
//                return -1; //not found
//
//            s = (s + increment) & (n - 1);
//            long kks = k[s];
//            if (acceptEmpty && kks == emptyVal) return -s - 1;
//            if (kks == key) return s;
//        }
//    }
//
//    /**
//     * Does this set contain the specified integer?
//     */
//    public boolean contains(long key) {
//        return size() > 0 && find(key, false) >= 0;
//    }
//
//    public final boolean addAt(long x) {
//        if (x == emptyVal)
//            throw new UnsupportedOperationException();
//        int i = find(x, true);
//        if (i >= 0)
//            return false;
//        else {
//            put(i, x);
//            return true;
//        }
//    }
//    public boolean remove(long x) {
//        int s = find(x, false);
//        if (s >= 0) {
//            keys[s] = emptyVal;
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * Puts this integer (key) in the setAt, and returns the slot index it was added to.
//     * It rehashes if adding it would make the set more than 75% full.
//     */
//    private int put(int s, long key) {
//        if (++size >= rehashCount) {
//            rehash();
//            s = slot(key);
//        } else {
//            s = -s - 1;
//        }
//        keys[s] = key;
//        return s;
//    }
//
//    /**
//     * (internal) Rehashes by doubling {@code long[] key} and filling with the old values.
//     */
//    public void rehash() {
//        int newSize = keys.length << 1;
//        long[] oldKeys = keys;
//        keys = new long[newSize];
//        if (emptyVal != 0) Arrays.fill(keys, emptyVal);
//
//        for (long key : oldKeys) {
//            if (key != emptyVal)
//                keys[slot(key)] = key;
//        }
//        rehashCount = newSize - (newSize >> 2);
//    }
//
//    public long[] toArray() {
//        return toArray(null);
//    }
//
//    public long[] toArray(@Nullable long[] l) {
//        int s = size();
//        if (s == 0)
//            return ArrayUtils.EMPTY_LONG_ARRAY;
//        if (l == null || l.length < s)
//            l = new long[s];
//        int i = 0;
//        for (long x : keys) {
//            if (x != emptyVal)
//                l[i++] = x;
//        }
//        return l;
//    }
//
//    public long[] toSortedArray() {
//        return toSortedArray(null);
//    }
//
//    public long[] toSortedArray(@Nullable long[] l) {
//        long[] x = toArray(l);
//        if (x.length > 1) {
//            Arrays.sort(x);
//        }
//        return x;
//    }
//
//    public void addAll(long[] x) {
//        for (long i : x)
//            addAt(i);
//    }
//
//
////
}