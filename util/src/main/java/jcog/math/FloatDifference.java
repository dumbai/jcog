package jcog.math;

import java.util.function.LongSupplier;

import static jcog.math.LongInterval.TIMELESS;

/**
 * First-(1st)-Order difference
 * TODO abstract other difference models
 */
public class FloatDifference implements FloatSupplier {

    final FloatSupplier in;
    private final LongSupplier clock;
    double xPrev;
    double dx = 0;
    long dt = 0;
    float y = Float.NaN;
    private long tPrev;

    /**
     * whether to report the first-order difference after each change,
     * or to divide that difference by the amount of time since last frame
     * (estimates 1st order derivative)
     *
     * by time could produce inaccurate effects when a sensor can be paused
     * or becomes silent.  so first-order mode is simpler to work with.
     */
    Mode mode = Mode.FirstOrder;

    public enum Mode {
        FirstOrder,
        FirstOrderPerTime,
        PctChange
    }
    public FloatDifference(FloatSupplier in, LongSupplier clock) {
        this.in = in;
        this.clock = clock;
        this.tPrev = TIMELESS;
        this.xPrev = Float.NaN;
    }
    public FloatDifference(FloatSupplier in, Mode mode, LongSupplier clock) {
        this(in, clock);
        this.mode = mode;
    }

    @Override
    public float asFloat() {
        long t = clock.getAsLong();
        long before = tPrev;
        boolean init = before == TIMELESS || before > t /* rewind */;
        long dt = init ? 1 : t - before;
        if (dt > 0) {

            this.tPrev = t;

            double x = in.getAsDouble();

            double xPrev = this.xPrev;

            this.xPrev = x;

            if (xPrev == xPrev) { //has previous value?
                this.dx = x - xPrev;
            } else {
                this.dx = 0; //initialize to zero
            }

            this.dt = init ? 1 : dt;
            this.y = (float) switch (mode) {
                case FirstOrder -> dx;
                case FirstOrderPerTime -> (dx / dt);
                case PctChange ->
                    dx / Math.abs(xPrev);
                    //dx / Math.max(Math.abs(x), Math.abs(xPrev));
            };
        }
        return this.y;
    }

}