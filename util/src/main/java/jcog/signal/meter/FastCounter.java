package jcog.signal.meter;

import jcog.math.FloatSupplier;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/** NOTE: this can extend EITHER AtomicLong or LongAdder */
public class FastCounter extends AtomicLong implements FloatSupplier, LongSupplier {

    private final String name;

    public FastCounter(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + '=' + super.toString();
    }

    public final void add(long x) {
        //unsafe:
        addAndGet(x);
    }

    public final void increment() {
        //unsafe:
        incrementAndGet();

        //safe:
        //long l = incrementAndGet(); if (l == Long.MAX_VALUE-1) throw new TODO("clear me");
    }

    @Override
    public float asFloat() {
        return get();
    }

    @Override
    public long getAsLong() {
        return get();
    }

    @Override
    public double getAsDouble() {
        return get();
    }
}