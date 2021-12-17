package jcog.math;

import java.util.function.DoubleSupplier;

public class CumulativeSum implements DoubleSupplier {
    private final DoubleSupplier o;
    private final int interval;
    double sum;
    int i;
    private double last = Float.NaN;

    public CumulativeSum(DoubleSupplier o, int interval) {
        this.o = o;
        this.interval = interval;
        sum = 0;
        i = 0;
    }

    public final double last() {
        return last;
    }

    @Override
    public double getAsDouble() {
        double v = o.getAsDouble();
        sum += v;

        if (++i % interval == 0) {
            double mean = sum / interval;
            sum = 0;
            return last = mean;
        }
        return last = Double.NaN;
    }
}