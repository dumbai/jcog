package jcog.signal.tensor;

import jcog.Util;
import jcog.util.PriReturn;
import org.eclipse.collections.api.block.function.primitive.FloatFloatToFloatFunction;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.VarHandle;
import java.util.StringJoiner;

import static jcog.Str.n4;
import static jcog.data.bit.FixedPoint.unitShort;
import static jcog.data.bit.FixedPoint.unitShortToFloat;

/**
 * stores 4 (four) 16-bit fixed-point numbers, covering a unit range [0..1.0]
 * TODO use VarHandle with offsets to CAS the specific sub-vector.  see
 *             XX = MethodHandles.arrayElementVarHandle(X[].class);
 *             .getAndSet(a, (cap - 1) & --s, null)
 *             VarHandleBytes
 * TODO test
 * TODO make an array version of this using common static methods
 */
public class AtomicFixedPoint4x16bitVector implements WritableTensor {

    //TODO atomic addAt methods
    private static final int[] QUAD_16_SHAPE = { 4 };

//    private static final MetalAtomicLongFieldUpdater<AtomicFixedPoint4x16bitVector> X =
//        new MetalAtomicLongFieldUpdater<>(AtomicFixedPoint4x16bitVector.class, "x");

    private static final VarHandle X = Util.VAR(AtomicFixedPoint4x16bitVector.class, "x", long.class);
    private volatile long x;

    /**
     * @param c quad selector: 0, 1, 2, 3
     */
    private static float toFloat(long x, int c) {
        return unitShortToFloat(shortAt(x, c));
    }

    private static int shortAt(long x, int c) {
        return ((int) (x >>> (c * 16))) & '\uffff';
    }

    @Override
    public void fill(float x) {
        int component = unitShort(x);
        long pattern =              component;
        pattern <<= 16;  pattern |= component;
        pattern <<= 16;  pattern |= component;
        pattern <<= 16;  pattern |= component;
        data(pattern);
        //data(pattern | (pattern << 16) | (pattern << 32) | (pattern << 48) );
    }

    @Override
    public double sumValues() {
        long x = data();
        int s = 0;
        for (int i = 0; i < 4; i++)
            s += shortAt(x, i);

        return unitShortToFloat(s);
    }

    @Override
    public void writeTo(float[] target, int offset) {
        //optimized writeTo impl
        long x = data(); //volatile read
        for (int i = 0; i < 4; i++)
            target[i+offset] = toFloat(x, i);
    }
    @Override
    public void sumTo(float[] target, int offset) {
        long x = data(); //volatile read
        for (int i = 0; i < 4; i++)
            target[i+offset] += toFloat(x, i);
    }

    @Override
    public String toString() {
        StringJoiner j = new StringJoiner(",");
        for (int i = 0; i < 4; i++)
            j.add(toString(i));
        return j.toString();
    }

    private String toString(int component) {
        return n4(getAt(component));
    }

    @Override public final float merge(int c, float arg, FloatFloatToFloatFunction F, @Nullable PriReturn returning) {
        int shift = c * 16;
        long mask = ~((((long)('\uffff'))) << shift);
        long lx, ly;
        float x, y;

        do {
            lx = data();

            int xi = (int) (lx >> shift) & '\uffff'; // == shortAt(_x, c)
            x = unitShortToFloat(xi);

            y = F.valueOf(x, arg);

            int yi = unitShort(y);
            if (xi == yi) {
                y = x; //no change
                break;
            }

            ly = (lx & mask) | (((long)yi) << shift);

        } while (lx!=ly &&
                !
                /*X.compareAndSet(this, lx, ly)*/
                X.weakCompareAndSetAcquire(this, lx, ly)
        );

        return returning!=null ? returning.apply(arg, x, y) : Float.NaN;
    }


    @Override
    public final void setAt(int index, float f) {
        int shift = index * 16;
        long mask = ~((((long)('\uffff'))) << shift);
        long b = ((long) unitShort(f))<<shift;
        long x, y;
        do {
            y = ((x = data()) & mask) | b;
        } while(x!=y && !
                /*X.compareAndSet(this, x, y)*/
                X.weakCompareAndSetAcquire(this, x, y)
        );
    }

    @Override
    public final float getAt(int index) {
        return toFloat(data(), index);
    }

    @Override
    public final int[] shape() {
        return QUAD_16_SHAPE;
    }

    public final long data() {
        return (long)
                X.getOpaque(this)
                //X.getAcquire(this)
                ;
    }

    public final void data(long y) {
        X.setOpaque(this, y);
    }

    /** TODO rewrite */
    public void commit(org.eclipse.collections.api.block.function.primitive.FloatToFloatFunction m) {
        merge(0, 0, 0, 0, (a,ignored)-> m.valueOf(a));
    }

    public void merge(float b, float q, float g, float Q, FloatFloatToFloatFunction m) {
        merge(0, b, m, null);
        merge(1, q, m, null);
        merge(2, g, m, null);
        merge(3, Q, m, null);
    }
}