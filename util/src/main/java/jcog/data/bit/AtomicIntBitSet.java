package jcog.data.bit;

import jcog.data.list.FastAtomicIntegerArray;

/** from: https://stackoverflow.com/a/12425007 */
public class AtomicIntBitSet {

    //TODO volatile int capacity; etc..

    public static final AtomicIntBitSet EMPTY = new AtomicIntBitSet(0);

    private final FastAtomicIntegerArray array;

    public AtomicIntBitSet(int length) {
        int intLength = (length + 31) /32; // unsigned / 32
        array = new FastAtomicIntegerArray(intLength);
    }

    public void set(int n) {
        int bit = 1 << n;
        int i = n / 32;
        FastAtomicIntegerArray a = this.array;
        int prev, next;
        do {
            prev = a.getAcquire(i);
            next = prev | bit;
        } while (prev != next && !a.weakCompareAndSetRelease(i, prev, next));
    }

    /** clears all */
    public void clear() {
        array.fill(0);
    }

    public void clear(int n) {
        int bit = ~(1 << n);
        int i = n / 32;
        FastAtomicIntegerArray a = this.array;
        int next, prev;
        do {
            prev = a.getAcquire(i);
            next = prev & bit;
        } while (prev != next && !a.weakCompareAndSetRelease(i, prev, next));
    }

    private boolean unclearWeak(int n) {
        int bit = 1 << n;
        int i = n / 32;
        FastAtomicIntegerArray a = this.array;
        int prev = a.getAcquire(i);
        int next = prev | bit;
        return prev == next || a.weakCompareAndSetRelease(i, prev, next);
    }


    public boolean test(int n) {
        return (array.get(n / 32) & (1 << n)) != 0;
    }

    public int setNext(int to) {
        for (int i = 0; i < to; ) {
            int b = nextClearBit(i, to);
            if (b < 0) break; //none found
            else if (unclearWeak(b)) return b;
            else { i = b+1; if (i >= to) i = 0; /* wrap-around */ }
        }
        return -1;
    }

    /** from java.util.BitSet */
    private int nextClearBit(int from, int cap) {
        int u = from / 32;
        int mask = 0xffffffff << from;

        FastAtomicIntegerArray a = this.array;
        cap = Math.min(a.length(), cap); //HACK in case array changed in the call

        long word = ~a.get(u) & mask;

        while (true) {
            if (word != 0)
                return (u * 32) + Long.numberOfTrailingZeros(word);
            if (++u >= cap)
                return -1;
            word = ~a.get(u);
        }
    }

    public int size() {
        FastAtomicIntegerArray a = this.array;
        int n = a.length();
        int c = 0;
        for (int i= 0; i < n; i++)
            c += Integer.bitCount( a.getOpaque(i) );
        return c;
    }
}