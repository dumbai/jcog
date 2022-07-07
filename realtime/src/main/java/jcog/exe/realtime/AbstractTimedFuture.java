package jcog.exe.realtime;

import java.util.concurrent.TimeUnit;

public abstract class AbstractTimedFuture<T> implements TimedFuture<T> {

    int rounds;

    AbstractTimedFuture() {

    }

    /** @param rounds phase offset */
    AbstractTimedFuture(int rounds) {

        this.rounds = rounds;
    }

    @Override
    public final int queueState() {
        return isCancelled() ? CANCELLED : (--this.rounds < 0 ? READY : PENDING);
    }

    @Override
    public final boolean isCancelled() {
        return rounds == Integer.MIN_VALUE;
    }

    @Override
    public final boolean cancel(boolean mayInterruptIfRunning /* ignored */) {
        rounds = Integer.MIN_VALUE;
        return true;
    }

    @Override
    public boolean isDone() {
        return rounds < 0;
    }


    @Override
    public abstract int offset(long resolution);


    @Override
    public long getDelay(TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final int rounds() {
        return rounds;
    }


//    @Override
//    public boolean isPeriodic() {
//        return false;
//    }
}
