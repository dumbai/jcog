package jcog.random;

import jcog.data.bit.FixedPoint;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static java.lang.Float.MIN_NORMAL;

/** buffers 64-bits at a time from a delegate Random generator.
 *  for efficient sub-word generation.
 *  ex: if you only need nextBoolean()'s, then it can generate 64 results from each internal RNG call.
 *  not thread-safe.
 *
 *  also provides extra utility methods not present in java.util.Random
 */
public class RandomBits {

    public final Random rng;

    /** up to 64 bits to be dequeued */
    private long buffer;

    /** bits remaining */
    byte bit;

    public RandomBits(Random rng) {
        this.rng = rng;
        this.bit = 0;
    }

    @Override public String toString() {
        return Long.toBinaryString(buffer) + " @ " + bit;
    }

    public int nextBits(int bits) {
        assert(bits < 31 /* maybe 32 */ && bits > 0);
        return _next(bits);
    }

    public boolean nextBoolean() {
        return _next(1) != 0;
    }

    private int _next(int bits) {
        if (bit < bits)
            refresh();
        int r = (int) (buffer & ((1 << bits) - 1));
        buffer >>>= bits;
        bit -= bits;
        return r;
    }

    private void refresh() {
        buffer = rng.nextLong();
        bit = 64;
    }

    public int nextInt(int i) {
        return nextBits(bits(i)) % i;
    }

    private static int bits(int i) {
        return 32 - Integer.numberOfLeadingZeros(i);
    }

    public void setSeed(long seed) {
        rng.setSeed(seed);
        bit = 0; //force refresh
    }

    public float nextFloat() {
        return rng.nextFloat(); //TODO
    }

    /** lower-precision boolean probability */
    public final boolean nextBooleanFast(float probTrue, int bits) {
        return switch (bits) {
            case 8 -> nextBooleanFast8(probTrue);
            case 16 -> nextBooleanFast16(probTrue);
            case 32 -> nextBoolean(probTrue);
            default -> throw new UnsupportedOperationException();
        };
    }

    public boolean nextBooleanFast8(float probTrue) {
        int a = nextBoolAbs(probTrue); return a!=0 ? (a==+1) :
            probTrue >= FixedPoint.unitByteToFloat(_next(8));
    }

    public boolean nextBooleanFast16(float probTrue) {
        int a = nextBoolAbs(probTrue); return a!=0 ? (a==+1) :
            probTrue >= FixedPoint.unitShortToFloat(_next(16));
    }

    public boolean nextBooleanFast24(float probTrue) {
        int a = nextBoolAbs(probTrue); return a!=0 ? (a==+1) :
                probTrue >= FixedPoint.unit24ToFloat(_next(24));
    }
    public final boolean nextBoolean(float probTrue) {
        int a = nextBoolAbs(probTrue); return a!=0 ? (a==+1) :
            probTrue >= nextFloat();
    }

    /** absolute special cases */
    private int nextBoolAbs(float probTrue) {
        if (probTrue == 0.5f) return nextBoolean() ? +1 : -1;
        if (probTrue <= MIN_NORMAL) return -1;
        if (probTrue >= 1 - MIN_NORMAL) return +1;
        return 0;
    }

    public final int nextBooleanAsInt() {
         return nextBoolean() ? 0 : 1;
    }

    /** fractional ceiling; any integer remainder decides randomly to increment */
    public int floor(float max) {
        int i = (int) max;
        float fract = max - i;

        //remainder: fractional part
        return (fract > MIN_NORMAL && nextBoolean(fract)) ? i + 1 : i;
    }

    @Nullable public <X> X get(X[] x) {
        int n = x.length;
        return switch (n) {
            case 0 -> null;
            case 1 -> x[0];
            default -> x[nextInt(n)];
        };
    }


    public long nextLong(long a, long b) {
        if (b < a) throw new UnsupportedOperationException();
        long r = b - a;
        if (r == 0) return a;
        long s;
        if (r < Integer.MAX_VALUE)
            s = nextInt((int)r);
        else
            s = Math.abs(rng.nextLong()) % r;

        return a + s;
    }
}