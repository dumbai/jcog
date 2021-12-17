package jcog.exe.realtime;

import jcog.Util;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.nanoTime;

/**
 * thread-based timer
 */
public class ThreadTimer implements AbstractTimer, Runnable {

    private static final int IDLE_MS = 50;
    private static final int threadPriority = Thread.MIN_PRIORITY;

    @Nullable
    public transient TimedFuture exe;
    private long periodNS;

    private transient Thread thread;

    private final AtomicBoolean sched = new AtomicBoolean(false);

    public ThreadTimer() {
        setPeriodMS(IDLE_MS);
    }

    @Override
    public <D> TimedFuture<D> schedule(TimedFuture<D> r) {
        if (!sched.compareAndSet(false, true))
            return r;

        try {
            this.exe = r;

            FixedRateTimedFuture fr = (FixedRateTimedFuture) r;
            fr.rounds = 1;
            setPeriodMS((Util.longToInt(fr.periodNS.longValue() / 1_000_000L)));

            if (this.thread == null)
                start();
        } finally {
            sched.set(false);
        }
        return r;
    }

    public synchronized void start() {
        Thread tt = new Thread(this);
        tt.setUncaughtExceptionHandler((T, e) -> {
            e.printStackTrace();
            this.thread = null;
        });
        this.thread = tt;

        tt.setPriority(threadPriority);

        tt.start();
    }

    @Override
    public void execute(Runnable r) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void run() {
        try {
            long end = nanoTime() - periodNS;
            while (true) {
                TimedFuture exe = this.exe;
                if (exe != null) {
                    exe.run();
                    if (exe.isCancelled() || exe.isDone())
                        break;
                }

                long i = this.periodNS;
                if (i < 0)
                    break; //stopped

                long now = System.nanoTime();
                i -= (now - end);

                if (i > 0)
                    Util.sleepNS(i);

                end = now;
            }
        } finally {
            exe = null;
            thread = null;
            setPeriodMS(IDLE_MS);
        }
    }

    public void setPeriodMS(int periodMS) {
        this.periodNS = periodMS * 1_000_000L;
    }
}