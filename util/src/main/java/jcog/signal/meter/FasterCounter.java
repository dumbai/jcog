package jcog.signal.meter;

import jcog.math.FloatSupplier;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongSupplier;

/** buffered LongAdder */
public class FasterCounter extends LongAdder implements FloatSupplier, LongSupplier {

    private final String name;

    private volatile long current;

    public FasterCounter(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + '=' + super.toString();
    }

    @Override
    public float asFloat() {
        return current;
    }

    @Override
    public long getAsLong() {
        return current;
    }

    @Override
    public double getAsDouble() {
        return current;
    }

    public void commit() {
        current = sumThenReset();
    }
}