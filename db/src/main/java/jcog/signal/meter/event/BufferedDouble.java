package jcog.signal.meter.event;

import com.google.common.util.concurrent.AtomicDouble;
import jcog.math.FloatSupplier;
import jcog.signal.meter.Metered;

public class BufferedDouble extends Metered.AbstractMetric implements FloatSupplier {

    private final AtomicDouble val = new AtomicDouble();

    double out;

    public BufferedDouble(String id) {
        super(id);
    }

    /** returns the previous value, or NaN if none were set  */
    public void set(double x) {
        val.set(x);
    }

    public void increment(double x) {
        val.addAndGet(x);
    }

    public void commit(double next) {
        out = val.getAndSet(next);
    }


    /** current stored value; optionally triggering any overridden update procedures */
    public final double get() { return out; }


    @Override
    public void accept(MeterReader reader) {
        reader.set(out);
    }

    @Override
    public final float asFloat() {
        return (float)out;
    }

}