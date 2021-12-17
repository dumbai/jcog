package jcog.constraint.continuous;


import jcog.Util;

import java.util.function.DoubleSupplier;

public class DoubleVar implements DoubleSupplier {

    public final String name;
    private final int hash;

    private double value;

    public DoubleVar(String name) {
        this.name = name; this.hash = name.hashCode();
    }

    public void value(double value) {
        this.value = value;
    }

    public boolean valueIfChanged(double next, double tolerance) {
        double prev = this.value;
        if (!Util.equals(prev, next, tolerance)) {
            this.value = next;
            return true;
        }
        return false;
    }

    @Override
    public double getAsDouble() {
        return value;
    }

    public final float floatValue() {
        return (float) getAsDouble();
    }

    @Override
    public String toString() {
        return name + '=' + value;
    }

    @Override
    public int hashCode() {
        return hash;
    }



}
