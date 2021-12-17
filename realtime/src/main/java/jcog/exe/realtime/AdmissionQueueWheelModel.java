package jcog.exe.realtime;

import jcog.data.list.Lst;
import jcog.util.ArrayUtil;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscArrayQueue;

import java.util.Arrays;
import java.util.Collection;

import static jcog.exe.realtime.TimedFuture.CANCELLED;
import static jcog.exe.realtime.TimedFuture.READY;

/**
 * uses central concurrent admission queue which is drained each cycle.
 * the wheel queues are (hopefully fast) ArrayDeque's safely accessed from one thread only
 */
public class AdmissionQueueWheelModel extends WheelModel {

    /** capacity of incoming admission queue (not the entire wheel) */
    static final int ADMISSION_CAPACITY = 2048;

    private static final int BUCKET_CAPACITY_INITIAL = 32;

    final MessagePassingQueue<TimedFuture> incoming = //new MetalConcurrentQueue<>(ADMISSION_CAPACITY);
        new MpscArrayQueue<>(ADMISSION_CAPACITY);

    final Lst<TimedFuture>[] wheel;


//    /** where incoming temporarily drains to */
//    final TimedFuture[] coming = new TimedFuture[ADMISSION_CAPACITY];

    public AdmissionQueueWheelModel(int wheels, long resolution) {
        super(wheels, resolution);

        this.wheel = new Lst[wheels];
        for (int i = 0; i < wheels; i++)
            wheel[i] = new Lst<>(0, new TimedFuture[BUCKET_CAPACITY_INITIAL]);
    }

    /**
     * HACK TODO note this method isnt fair because it implicitly prioritizes 'tenured' items that were inserted and remained.
     * instead it should behave like ConcurrentQueueWheelModel's impl
     */
    @Override public int run(int c) {
        incoming.drain(this);
//        if (incoming.drain(this) > 0)
//            timer.assertRunning(); //is this necessary? seems so.. TODO why

        Lst<TimedFuture> Q = wheel[c];

        int n = Q.size();
        if (n == 0) return 0;

        TimedFuture[] q = Q.array();

        int removed = 0;
        for (int i = 0; i < n; i++) {

            TimedFuture r = q[i];

            if (switch (r.state()) {
                case CANCELLED -> true;
                case READY -> { r.execute(timer); yield true;  }
                default -> false;
            }) {
                q[i] = null; removed++;
            }
        }
        if (removed > 0) {
            ArrayUtil.sortNullsToEnd(q, 0, n);
            Q.setSize(n -= removed);
        }
        return n;
    }

    @Override
    public boolean isEmpty() {
        return incoming.isEmpty();
    }

    @Override
    public int size() {
        return Arrays.stream(wheel).mapToInt(Collection::size).sum();
    }



    @Override public boolean accept(TimedFuture<?> r, HashedWheelTimer t) {
        return incoming.offer(r);
    }

    @Override public boolean reschedule(int wheel, TimedFuture r) {
        return this.wheel[wheel].add(r);
    }

}