package jcog.math;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * buffers a FloatSupplier's value until supplied clock signal changes
 */
public class FloatCached implements FloatSupplier {

    final FloatSupplier in;
    final LongSupplier clock;
    /*volatile*/ float current = Float.NaN;
    final AtomicLong lastTime = new AtomicLong(Long.MIN_VALUE);

    public FloatCached(FloatSupplier in, LongSupplier clock) {
        this.in = in;
        this.clock = clock;
    }

    @Override
    public float asFloat() {
        long next = clock.getAsLong();
        if (next != lastTime.getAndSet(next)) {
            current = in.asFloat();
        }
        return current;
    }
}