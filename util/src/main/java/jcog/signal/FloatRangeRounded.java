package jcog.signal;

import jcog.Util;

public class FloatRangeRounded extends FloatRange {

    float epsilon;

    public FloatRangeRounded(float value, float min, float max, float epsilon) {
        super(value, min, max);
        this.epsilon = epsilon;
        set(value); 
    }

    @Override
    protected float post(float x) {
        return super.post(Util.round(x, epsilon));
    }
}