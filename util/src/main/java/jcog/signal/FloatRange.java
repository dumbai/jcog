package jcog.signal;

import jcog.Util;
import jcog.math.FloatSupplier;
import jcog.math.NumberException;


public class FloatRange extends MutableFloat {

    public final float min, max;

    public FloatRange(float value, float min, float max) {
        if (value > max || value < min)
            throw new NumberException("out of expected range", value);

        this.min = min;
        this.max = max;
        set(value);
    }

    @Override
    protected float post(float x) {
        return Util.clampSafe(x, min, max);
    }

    public final void set(double value) {
        set((float)value);
    }

    public static FloatRange unit(float initialValue) {
        return new FloatRange(initialValue, 0, 1);
    }

    public static FloatRange unit(FloatSupplier initialValue) {
        return unit(initialValue.asFloat());
    }

    public final void setLerp(float x) {
        setLerp(x, min, max);
    }

//    public static FloatRange mapRange(float mapMin, float mapMax) {
//        throw new TODO();
//    }


}