package jcog.signal;

import jcog.Util;

public class DoubleRange extends MutableDouble {
    public final double min, max;

    public DoubleRange(double initValue, double min, double max) {
        this.min = min;
        this.max = max;
        set(initValue);
    }

    @Override
    public void set(double value) {
        super.set(Util.clamp(value, min, max));
    }

    public static DoubleRange unit(double initialValue) {
        return new DoubleRange(initialValue, 0, 1);
    }

    public final void setLerp(double x) {
        set(Util.lerp(x, min, max));
    }

}