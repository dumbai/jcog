package jcog.signal;

import jcog.Util;

/** extends Number with mutable methods
 * TODO add more mutation methods
 * */
public abstract class NumberX extends Number {

    public abstract float add(float x);
    public abstract float mul(float x);

    public abstract void set(float v);

    public float getAndSet(float r) {
        set(r);
        return floatValue();
    }

    public final void subtract(float x) {
        add(-x);
    }

    public final void subtract(Number operand) {
        add(-operand.floatValue());
    }

    public final void setLerp(float x, float min, float max) {
        set(Util.lerp(x, min, max));
    }

    public boolean setIfChanged(float x) {
        return getAndSet(x)!=x;
    }
}