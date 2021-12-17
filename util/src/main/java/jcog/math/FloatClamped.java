package jcog.math;

import jcog.Util;
import jcog.normalize.FloatNormalized;

public class FloatClamped implements FloatSupplier {
    final float min;
    final float max;
    private final FloatSupplier in;

    public FloatClamped(FloatSupplier in, float min, float max) {
        this.min = min;
        this.max = max;
        this.in = in;
    }

    public static FloatSupplier unitized(FloatNormalized f) {
        return new FloatClamped(f, 0, 1);
    }

    @Override
    public float asFloat() {
        float v = in.asFloat();
        return v==v ? Util.clamp(v, min, max) : Float.NaN;
    }
}