package jcog.exe.realtime;

import jcog.Util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** reschedules after each execution.  at that point it has the option
 * to use a fixed delay or to subtract from it the duty cycle
 * consumed since being invoked, to approximate fixed rate.
 *
 * TODO AbstracdtTimedRunnable
 */
public class FixedDelayTimedFuture<T> extends AbstractTimedCallable<T> {

    private final Consumer<TimedFuture<?>> rescheduleCallback;
    private long periodNS;
    @Deprecated private final int wheels;
    @Deprecated private final long resolution;
    private final int phase;

    public FixedDelayTimedFuture(
                                 Callable<T> callable,
                                 long periodNS,
                                 long resolution,
                                 int wheels,
                                 Consumer<TimedFuture<?>> rescheduleCallback) {
        super(1, callable);
        this.periodNS = periodNS;
        this.resolution = resolution;
        this.wheels = wheels;
        this.rescheduleCallback = rescheduleCallback;
        this.phase = Math.max(1, Util.longToInt((1 + (periodNS / (resolution * wheels))) % resolution));
        reset();
    }

    @Override
    public boolean isPeriodic() {
        return true;
    }


    @Override public int offset(long resolution) {
        //return (int) Math.round(((double)Math.max(resolution, periodNS)) / resolution);
        //return (int) (periodNS % resolution);
        return phase;
    }

    private void reset() {
        this.rounds = (int) Math.min(Integer.MAX_VALUE - 1,
            Math.round((((double) periodNS) / (resolution * wheels)))
        );
    }

    @Override
    public void run() {
        super.run();
        if (!isCancelled()) {
            reset();
            rescheduleCallback.accept(this);
        }
    }

}