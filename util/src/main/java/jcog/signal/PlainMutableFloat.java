package jcog.signal;

import jcog.math.FloatSupplier;

public class PlainMutableFloat extends NumberX implements FloatSupplier {


    @SuppressWarnings("unused")
    protected float v;

    @Override
    public float asFloat() {
        return v;
    }

    @Override
    public float add(float x) {
        return v += x;
    }

    @Override
    public float mul(float x) {
        return v *= x;
    }

    public void set(float value) {
        this.v = post(value);
    }

    protected float post(float x) {
        return x;
    }

    /**
     * Sets the value from any Number instance.
     *
     * @param value  the value to setAt, not null
     * @throws NullPointerException if the object is null
     */
    public final void set(Number value) {
        set(value.floatValue());
    }

    /**
     * Checks whether the float value is the special NaN value.
     *
     * @return true if NaN
     */
    public final boolean isNaN() {
        return Float.isNaN(asFloat());
    }

    /**
     * Checks whether the float value is infinite.
     *
     * @return true if infinite
     */
    public final boolean isInfinite() {
        return Float.isInfinite(asFloat());
    }

    public final boolean isFinite() {
        return Float.isFinite(asFloat());
    }


    /**
     * Returns the value of this MutableFloat as an int.
     *
     * @return the numeric value represented by this object after conversion to type int.
     */
    @Override
    public final int intValue() {
        return Math.round(asFloat());
        //return (int) asFloat();
    }

    /**
     * Returns the value of this MutableFloat as a long.
     *
     * @return the numeric value represented by this object after conversion to type long.
     */
    @Override
    public final long longValue() {
        return Math.round(asFloat());
    }

    /**
     * Returns the value of this MutableFloat as a float.
     *
     * @return the numeric value represented by this object after conversion to type float.
     */
    @Override
    public final float floatValue() {
        return asFloat();
    }

    /**
     * Returns the value of this MutableFloat as a double.
     *
     * @return the numeric value represented by this object after conversion to type double.
     */
    @Override
    public final double doubleValue() {
        return asFloat();
    }

    /**
     * Returns the String value of this mutable.
     *
     * @return the mutable value as a string
     */
    @Override
    public String toString() {
        return String.valueOf(asFloat());
    }
}