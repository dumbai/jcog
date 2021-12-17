package jcog.exe.realtime;

import java.util.concurrent.TimeUnit;

public abstract class AbstractTimedRunnable extends AbstractTimedFuture<Void> {

    private final Runnable run;

    protected AbstractTimedRunnable() {
        super();
        run = this;
    }

    protected AbstractTimedRunnable(int rounds, Runnable run) {
        super(rounds);
        this.run = run;
    }

    @Override
    public String toString() {
        return run.toString();
    }

    @Override
    public void run() {
        run.run();
    }

    @Override
    public Void get() {
        return null;
    }

    @Override
    public Void get(long timeout, TimeUnit unit) {
        return null;
    }

}