package jcog.exe.realtime;

import org.jctools.queues.MessagePassingQueue;

import java.util.concurrent.Executor;

/**
 * scheduler implementation
 */
public abstract class WheelModel implements MessagePassingQueue.Consumer<TimedFuture> {
    public final int wheels;
    public final long resolution;
    HashedWheelTimer timer;

    WheelModel(int wheels, long resolution) {
        this.wheels = wheels;
        this.resolution = resolution;
    }

    @Override public void accept(TimedFuture x) {
        HashedWheelTimer t = this.timer;
        schedule(x, t.cursor(), t);
    }

    public final int idx(int cursor) {
        return cursor % wheels;
    }

    /**
     * returns how approximately how many entries were in the wheel at start.
     * used in part to determine if the entire wheel is empty.
     */
    public abstract int run(int wheel);

    /**
     * return false if unable to schedule
     */
    public abstract boolean accept(TimedFuture<?> r, HashedWheelTimer hashedWheelTimer);

    /**
     * return false if unable to reschedule
     */
    public abstract boolean reschedule(int wheel, TimedFuture r);

    public final void schedule(TimedFuture r, int c, Executor timer) {
        int offset = r.offset(resolution);
        if (offset > -1 || r.isPeriodic())
            reschedule(idx(c + offset), r);
        else
            timer.execute(r);
    }

    /**
     * estimated number of tasks currently in the wheel
     */
    public abstract int size();

    /**
     * allows the model to interrupt the wheel before it decides to sleep
     */
    public abstract boolean isEmpty();

    public void restart(HashedWheelTimer h) {
        this.timer = h;
    }
}