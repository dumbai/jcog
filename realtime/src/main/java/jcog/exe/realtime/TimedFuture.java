package jcog.exe.realtime;

import java.util.concurrent.Delayed;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface TimedFuture<T> extends RunnableScheduledFuture<T> {

//	TimedFuture[] EmptyArray = new TimedFuture[0];

	int rounds();


    int CANCELLED = -1;
    int PENDING = 0;
    int READY = 1;

    /** mutability: dont call this except from WheelModel's */
    int queueState();

    /**
     * Get the offset of the Registration relative to the current cursor position
     * to make it fire timely.
     *
     * @return the offset of current Registration
     */
    int offset(long resolution);

    long getDelay(TimeUnit unit);

    @Override
    default int compareTo(Delayed o) {
        if (o == this) return 0;
        TimedFuture other = (TimedFuture) o;

        long r1 = rounds();
        long r2 = other.rounds();
		return r1 == r2 ? Integer.compare(System.identityHashCode(this), System.identityHashCode(other)) : Long.compare(r1, r2);
    }

    default void execute(AbstractTimer t) {
        t.execute(this);
    }

}