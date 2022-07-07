package jcog.exe;

import jcog.Log;
import jcog.exe.realtime.AbstractTimer;
import jcog.exe.realtime.FixedRateTimedFuture;
import jcog.exe.realtime.ThreadTimer;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

import static jcog.Str.n2;


public abstract class Loop extends FixedRateTimedFuture {

    public static final Logger logger = Log.log(Loop.class);

    /**
     * busy lock
     */
//    private final AtomicBoolean running = new AtomicBoolean(false);
//    public final AtomicBoolean scheduled = new AtomicBoolean(false); //prevents multiple pending schedulings while waiting for next run

    public static final int PENDING = 0;
    public static final int SCHEDULED = 1;
    public static final int RUNNING = 2;
    public final AtomicInteger phase = new AtomicInteger(PENDING);

    public final AbstractTimer timer;

    public static Loop of(Runnable iteration) {
        return new LambdaLoop(iteration);
    }


    /**
     * create but do not start
     */
    public Loop() {
        this(-1);
    }

    /**
     * create and auto-start
     */
    public Loop(float fps) {
        this(fps >= 0 ? fpsToMS(fps) : -1);
    }

//    public Loop(int periodMS) {
//        this(new AtomicInteger(periodMS));
//    }

    public Loop(int periodMS) {
        this(periodMS, Exe.timer());
    }

    /**
     * create and auto-start
     */
    public Loop(int periodMS, AbstractTimer timer) {
        super();

        //int p = periodMS.get();

        //HACK trigger change in period value to trigger start
        //periodMS.set(-1);
        //this.periodMS = periodMS;
        //setPeriodMS(periodMS);
        periodNS.set(periodMS * 1_000_000L);
        this.timer = timer;
    }


    @Override
    public String toString() {
        return getClass() + "@" + System.identityHashCode(this);
    }

    public boolean isRunning() {
        return periodNS() >= 0;
    }

    @Override
    public void execute(AbstractTimer t) {
        if (phase.compareAndSet(PENDING, RUNNING))
            _execute(t);

        t.schedule(this);
    }

    protected void _execute(AbstractTimer t) {
        t.execute(this);
    }

    public final Loop fps(float fps) {
        setPeriodMS(fpsToMS(fps));
        return this;
    }

    public void ready() {
        phase.set(PENDING);
    }

    private static int fpsToMS(float fps) {
        return Math.max(1, (int)Math.round(1000.0 / fps));
    }


    public final boolean setPeriodMS(int nextPeriodMS) {
        long nextPeriodNS = nextPeriodMS * 1_000_000L, prevPeriodNS;
        if ((prevPeriodNS = periodNS.getAndSet(nextPeriodNS)) == nextPeriodNS)
            return false; //no change

        if (nextPeriodMS < 0) {
            _stop();
        } else {

            //logger.info("continue {}fps (each {}ms)", n2(1000f/nextPeriodMS), nextPeriodMS);

            if (prevPeriodNS < 0 /*|| !running.getOpaque()*/) //TODO these need to be checked atomically together
                _start(nextPeriodMS);

        }

        return true;
    }

    private void _start(int nextPeriodMS) {
        if (logger.isDebugEnabled())
            logger.debug("start {} {} fps", this, n2(1000f/nextPeriodMS));

        starting();

        timer.schedule(this);
    }

    private void _stop() {
        logger.debug("stop {}", this);

        cancel(false);

        //synchronized (periodMS) {
        stopping();
        //}
    }


    public final boolean stop() {
        return setPeriodMS(-1);
    }

    /**
     * for subclass overriding; called from the looping thread
     */
    protected void starting() {

    }

    /**
     * for subclass overriding; called from the looping thread
     */
    protected void stopping() {

    }

    /** returns false to stop this loop. */
    protected boolean thrown(Throwable e) {
        logger.error("{} {}", this, e);
        return true;
    }


    @Override
    public final void run() {

        beforeNext();

        boolean kontinue;
        try {
            kontinue = next();
        } catch (Throwable e) {
            kontinue = thrown(e);
        }

        afterNext();

        if (!kontinue)
            stop();
        else if (!async())
            ready();

    }

    /**
     * if iterationAsync, then the executing flag will not be cleared automatically.  then it is the implementation's
     * responsibility to clear it so that the next iteration can proceed.
     */
    protected boolean async() {
        return false;
    }

    protected void beforeNext() {

    }

    protected void afterNext() {

    }

    public abstract boolean next();


    public float fps() {
        long pns = periodNS();
        if (pns > 0 /* isRunning() */) {
            return (float) (1_000_000_000.0 / pns);
        } else {
            return pns == 0 ? Float.POSITIVE_INFINITY : 0;
        }
    }

    public void newThread(int periodMS) {
        ThreadTimer tt = new ThreadTimer();
        tt.setPeriodMS(periodMS);
        periodNS.set(periodMS * 1_000_000L); //HACK
        //tt.schedule(this);
        tt.exe = this;
        tt.start();
    }
}