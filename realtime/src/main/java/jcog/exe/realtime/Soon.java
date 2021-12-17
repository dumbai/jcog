package jcog.exe.realtime;

import java.util.concurrent.TimeUnit;

/** lighweight one-time procedure */
public abstract class Soon extends AbstractTimedFuture<Object> {

    /** immediately */
    protected Soon() {
        this(0);
    }

    protected Soon(int rounds) {
        super(rounds);
    }

    /** wont need rescheduled, executes immediately */
    @Override
    public int offset(long resolution) {
        return 0;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public final Object get() {
        run();
        return null;
    }

    @Override
    public final Object get(long timeout, TimeUnit unit) {
        run();
        return null;
    }

    /** wraps a Runnable */
    static final class Run extends Soon {

        private final Runnable runnable;

        Run(Runnable runnable) {
            super();
            this.runnable = runnable;
        }

        @Override
        public void run() {
            runnable.run();
        }
    }
}
