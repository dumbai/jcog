package jcog.math;

import jcog.Str;

import java.util.Arrays;

/**
 * simple delay line; shifts data on each access to the right; newest data will be at index 0
 */
public class FloatDelay implements FloatSupplier {

    final FloatSupplier input;

    public final float[] data;

    public FloatDelay(FloatSupplier input, int history) {
        assert(history > 0);
        this.input = input;
        this.data = new float[history];
        Arrays.fill(data, input.asFloat()); 
    }

    @Override
    public String toString() {
        return input + "=" + Str.n4(data);
    }

    @Override
    public float asFloat() {
        return data[data.length-1];
    }

    public void next() {
        System.arraycopy(data, 0, data, 1, data.length-1);
        data[0] = input.asFloat();
    }
}
