package jcog.exe.realtime;

import jcog.Util;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public abstract class FixedRateTimedFuture extends AbstractTimedFuture<Void> {

    int offset;
    /**
     * adjustable while running
     */
    protected final/* volatile */ AtomicLong periodNS = new AtomicLong();

    protected FixedRateTimedFuture() {
        super();
    }

    FixedRateTimedFuture(int rounds, long periodNS) {
        super(rounds);
        setPeriodNS(periodNS);
    }

    @Override
    public void execute(AbstractTimer t) {
        super.execute(t);
        t.schedule(this);
    }

    @Override
    public final boolean isPeriodic() {
        return true;
    }

    public final void setPeriodMS(long periodMS) {
        setPeriodNS(periodMS * 1_000_000);
    }

    private void setPeriodNS(long periodNS) {
        this.periodNS.set(periodNS);
    }

    public final int offset(long resolution) {
        return offset;
    }

    public final long periodNS() {
        return periodNS.getOpaque();
    }

    @Deprecated public final int periodMS() {
        //return Util.longToInt(Math.round(periodNS()/1.0E6));
        return Util.longToInt(periodNS() / 1_000_000);
    }

    /** period in seconds */
    public final double periodSec() {
        return periodNS()/1.0E9;
    }

    @Override
    public final Void get() {
        return null;
    }

    @Override
    public final Void get(long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public final int compareTo(Delayed o) {
        throw new UnsupportedOperationException();
    }

}