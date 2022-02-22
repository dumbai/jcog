package jcog.signal;

import jcog.Util;
import jcog.math.NumberException;

public class IntRange extends MutableInteger {

    public final int max;
    public final int min;

    public IntRange(int value, int min, int max) {
        super(value);
        if (value > max || value < min)
            throw new NumberException("out of bounds: " + min + ".." + max, value);
        this.min = min;
        this.max = max;
    }

    @Override
    public final void set(int next) {
        super.set(post(next));
    }

    @Override
    public int getAndSet(int r) {
        return super.getAndSet(post(r));
    }

    private int post(int next) {
        return Util.clamp(next, min, max);
    }

    public void setLerp(float s) {
        set(Util.lerpSafe(s, min, max));
    }

}