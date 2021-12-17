package jcog.signal.tensor;

//import jcog.data.atomic.AtomicFloatFieldUpdater;

import jcog.data.list.FastAtomicIntegerArray;
import jcog.util.PriReturn;
import org.eclipse.collections.api.block.function.primitive.FloatFloatToFloatFunction;

import java.util.StringJoiner;

import static java.lang.Float.floatToIntBits;
import static java.lang.Float.intBitsToFloat;

/** stores 32-bit float values in AtomicIntegerArray */
public class AtomicArrayTensor extends AbstractVector implements WritableTensor {

    public static final AtomicArrayTensor Empty = new AtomicArrayTensor(0);

    private final FastAtomicIntegerArray data;

    public AtomicArrayTensor(int length) {
        this(length, 0);
    }

    /** the initial data will be zero */
    public AtomicArrayTensor(int length, float initialValue) {
        this.data = new FastAtomicIntegerArray(length);
        if (initialValue!=0)
            fill(initialValue);
    }


    @Override
    public final float getAt(int linearCell) {
        return intBitsToFloat( data.getOpaque(linearCell) );
    }

    public float getAndSet(int i, float v) {
        return intBitsToFloat( data.getAndSet(i, floatToIntBits(v) ) );
    }

    @Override
    public final void setAt(int linearCell, float newValue) {
        data.setOpaque(linearCell, floatToIntBits(newValue));
    }

    /** @see AtomicFloatFieldUpdater */
    @Override public final float addAt(int linearCell, float x) {
        FastAtomicIntegerArray data = this.data;

        float nextFloat;
        int prev, next;
        do {
            prev = data.getAcquire(linearCell);
            next = floatToIntBits(nextFloat = (intBitsToFloat(prev) + x)); //next = floatToIntBits(f.apply(intBitsToFloat(prev), y));
        } while (prev!=next && data.weakCompareAndSetRelease(linearCell, prev, next));
        return nextFloat;
    }

    @Override public final float merge(int linearCell, float x, FloatFloatToFloatFunction f, PriReturn y) {
        int prevI, nextI;
        float prev, next;
        FastAtomicIntegerArray data = this.data;
        do {
            prevI = data.getAcquire(linearCell);
            prev = intBitsToFloat(prevI);
            next = f.valueOf(prev, x);
            nextI = floatToIntBits(next);
        } while(prevI!=nextI && data.weakCompareAndSetRelease(linearCell, prevI, nextI));

        return y.apply(x, prev, next);
    }

    @Override
    public final int volume() {
        return data.length();
    }

    @Override
    public void fill(float x) {
        int xx = floatToIntBits(x);
        int v = volume();
        FastAtomicIntegerArray data = this.data;
        for (int i = 0; i < v; i++)
            data.set(i, xx);
    }

    @Override
    public String toString() {

        //TODO consistent with ArrayTensor.toString()
        //   return Arrays.toString(shape) + '<' + Texts.n4(data) + '>';

        StringJoiner joiner = new StringJoiner(",");
        int bound = volume();
        for (int x = 0; x < bound; x++)
            joiner.add(Float.toString(getAt(x)));
        return joiner.toString();
    }


    public final int length() {
        return data.length();
    }
}