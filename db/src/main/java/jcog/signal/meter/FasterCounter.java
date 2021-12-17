package jcog.signal.meter;

import jcog.math.FloatSupplier;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongSupplier;

/** not necessarily faster
 *  @see LongAdder
 */
public class FasterCounter extends LongAdder implements FloatSupplier, LongSupplier {

    private final String name;

    public FasterCounter(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + '=' + super.toString();
    }

//    public final void add(long x) {
//        //unsafe:
//        addAndGet(x);
//    }
//
//    public final void increment() {
//        //unsafe:
//        incrementAndGet();
//
//        //safe:
//        //long l = incrementAndGet(); if (l == Long.MAX_VALUE-1) throw new TODO("clear me");
//    }

    public long get() {
        return longValue();
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
