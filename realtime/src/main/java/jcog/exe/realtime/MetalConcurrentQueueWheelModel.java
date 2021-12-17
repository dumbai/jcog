package jcog.exe.realtime;

import jcog.Util;
import jcog.data.list.MetalConcurrentQueue;
import jcog.data.list.MetalRing;

import java.util.Arrays;
import java.util.function.ToIntFunction;

import static jcog.exe.realtime.TimedFuture.*;

/** where each wheel is simply its own concurrent queue
 * TODO this should not involve both poll and offer if the item remains in the wheel
 * */
public  class  MetalConcurrentQueueWheelModel extends WheelModel {

    /** the wheels (array of queues) */
    private final MetalRing<jcog.exe.realtime.TimedFuture>[] q;

    public MetalConcurrentQueueWheelModel(int wheels, int queueCapacity, long resolution) {
        super(wheels, resolution);
        assert(wheels > 1);
        q = new MetalRing[wheels];
        for (int i = 0; i < wheels; i++)
            q[i] = new MetalConcurrentQueue<>(queueCapacity);
    }

    @Override
    public int run(int c) {
        int result = 1;
        MetalRing<jcog.exe.realtime.TimedFuture> q = this.q[c];

        int n = q.size();
        switch (n) {
            case 0:
                result = 0;
                break;
            case 1:
                //special optimized case: the only element can be peek'd without poll/offer in case it remains pending
            {
                var r = q.peek();
                switch (r.state()) {
                    case CANCELLED:
                        q.poll();
                        break;
                    case READY:
                        q.poll();
                        r.execute(timer);
                        break;
                    case PENDING:
                        break; //<--- ideally most common path
                }
            }
            break;
            default:

                //TODO if n=2 and the previous or next queue is empty try moving one of the items there. this will distribute items across wheels so each has an ideal 0 or 1 size

                for (int i = 0; i < n; i++) {
                    {
                        TimedFuture timedFuture = q.poll();
                        switch (timedFuture.state()) {
                            case CANCELLED:
                                break;
                            case READY:
                                timedFuture.execute(timer);
                                break;
                            case PENDING:
                                q.offer(timedFuture); //re-insert
                                break;
                        }
                    }
                }
                break;
        }
        if (result == 1) {
            result = n;
        }
        return result;
    }

    @Override
    public boolean accept(TimedFuture<?> r, HashedWheelTimer t) {
        return t.reschedule(r); //immediately
    }

    @Override
    public boolean reschedule(int wheel, TimedFuture r) {

        int remain = q.length - 1;
        do {
            if (q[wheel].offer(r))
                return true;
            if (++wheel == q.length) wheel = 0;
        } while (--remain > 0);

        return false;
    }

    @Override
    public int size() {
        return Util.sum((ToIntFunction<MetalRing>) MetalRing::size, q);
    }

    @Override
    public boolean isEmpty() {
        return Arrays.stream(q).allMatch(MetalRing::isEmpty);
    }


}