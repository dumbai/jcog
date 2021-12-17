/*
 * Here comes the text of your license
 * Each line should be prefixed with  *
 */
package jcog.signal;


import jcog.TODO;

import java.util.function.DoubleSupplier;

/**
 * A mutable <code>double</code> wrapper.
 *
 * @version $Id: MutableDouble.java 618693 2008-02-05 16:33:29Z sebb $
 * @see Double
 * @since 2.1
 */
public class MutableDouble extends NumberX implements DoubleSupplier {

    private volatile double value;

    public MutableDouble() {
    }

    public MutableDouble(double value) {
        this.value = value;
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public float floatValue() {
        return (float) value;
    }

    @Override
    public final double doubleValue() {
        return value;
    }

    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public final float add(float x) {
        set(doubleValue() + x);
        return x;
    }
    @Override
    public float mul(float x) {
        throw new TODO();
    }

    @Override
    public final void set(float v) {
        set((double)v);
    }

    public void set(double v) {
        this.value = v;
    }

    @Override
    public final double getAsDouble() {
        return doubleValue();
    }

    public double get() {
        return doubleValue();
    }
}