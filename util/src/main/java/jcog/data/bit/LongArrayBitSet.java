package jcog.data.bit;

import com.google.common.base.Joiner;
import jcog.TODO;
import jcog.Util;
import jcog.util.ArrayUtil;

import java.util.Arrays;

/**
 * TODO implement better bulk setAt(start,end,v) impl
 */
public class LongArrayBitSet extends MetalBitSet {
    private long[] data;

    public LongArrayBitSet(long[] data) {
        assert data.length > 0;
        this.data = data;
    }

    public LongArrayBitSet(int bits) {
        resize(bits);
    }

    public int next(boolean what, int from, int to) {
        long[] data = this.data;
        for (int i = from; i < to; i++) {
            if (i % 64 == 0) {
                //determine skip ahead by highest index
                long d = data[i / 64];
                int skip = Long.numberOfTrailingZeros(what ? d : ~d);
                if (skip == 0)
                    return i;

                i += skip;
                if (skip == 64) {
                    i--;
                    continue;
                }
                return i;
            }

            if (test(i) == what)
                return i;
        }
        return -1;
    }

    public int capacity() {
        return data.length * 64;
    }

    @Override
    public MetalBitSet clone() {
        return new LongArrayBitSet(data.clone());
    }

    @Override
    public MetalBitSet negateThis() {
        throw new TODO();
    }

    private void resize(long bits) {
        long[] prev = data;

        if (bits == 0)
            data = ArrayUtil.EMPTY_LONG_ARRAY;
        else {
            data = new long
                    [Math.max(1, (int) Math.ceil(((double) bits) / Long.SIZE))];
                    //[Math.max(1, Util.longToInt(bits / Long.SIZE))];

            if (prev != null)
                System.arraycopy(prev, 0, data, 0, Math.min(data.length, prev.length));
        }
    }

    public void clear() {
        Arrays.fill(data, 0);
    }


    /**
     * number of bits set to true
     */
    public int cardinality() {
        int sum = 0;
        for (long l : data)
            sum += Long.bitCount(l);
        return sum;
    }

    @Override
    public boolean isEmpty() {
        for (long l : data)
            if (l != 0)
                return false;
        return true;
    }

    @Override
    public final void set(int i, boolean v) {
        long m = (1L << i);
        long[] d = this.data;
        int I = i >>> 6;
        if (v)  d[I] |=  m;
        else    d[I] &= ~m;
    }

    /**
     * sets 2 contiguous bits; warning should be aligned so that they are on the same word
     */
    void _set2(int i, boolean a, boolean b) {
        long[] d = this.data;
        int I = i >>> 6;
        long dI = d[I];
        long m = (1L << i);
        long x = a ? (dI | m) : (dI & ~m);
        m <<= 1;
        d[I] = b ? (x | m) : (x & ~m);
    }

    public boolean getAndSet(int i, boolean next) {
        long[] d = this.data;

        int I = i >>> 6;

        long di = d[I];

        long j = (1L << i);
        boolean prev = (di & j) != 0;

        if (prev != next)
            d[I] = next ? (di | j) : (di & ~j);

        return prev;
    }

    @Override
    public void setAll(boolean b) {
        Arrays.fill(data, b ? ~0L : 0L);
    }

    /**
     * Returns true if the bit is set in the specified index.
     */
    @Override
    public boolean test(int i) {
        return (data[i >>> 6] & (1L << i)) != 0;
    }


    final int dibitIfFirstTrue(int i) {
        long[] d = this.data;

        int l = i >>> 6;
        long W = d[l];
        if ((W & (1L << i)) == 0) //first bit?
            return 0;

        //if the index hasnt changed,
        //avoid loading another 64-bits from array
        int h = (++i) >>> 6;

        return ((l == h ? W : d[h]) & (1L << i)) == 0 ?
                1 : 3; //2nd bit?
    }


    public long bitSize() {
        return (long) data.length * Long.SIZE;
    }

    /**
     * Combines the two BitArrays using bitwise OR.
     */
    public void putAll(LongArrayBitSet array) {
        long[] d = this.data;
        int n = d.length;
        assert n == array.data.length :
                "BitArrays must be of equal length (" + n + "!= " + array.data.length + ')';
        for (int i = 0; i < n; i++)
            d[i] |= array.data[i];
    }


    @Override
    public String toString() {
        return Joiner.on(" ").join(Util.arrayOf(i -> Long.toHexString(data[data.length - 1 - i]), new String[data.length]));
    }
}