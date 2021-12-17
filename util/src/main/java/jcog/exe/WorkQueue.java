package jcog.exe;

import jcog.Util;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpmcArrayQueue;

import java.util.function.Consumer;

public class WorkQueue<X> {
    private final MpmcArrayQueue<X> in;

    public WorkQueue(int capacity) {
        in = new MpmcArrayQueue<>(capacity);
    }

    public boolean offer(X x) {
        return in.offer(x);
    }

    public int size() { return in.size(); }

    public X poll() { return in.poll(); }

    public final void acceptAll(MessagePassingQueue.Consumer<X> x) {
        in.drain(x);
    }

    public int accept(Consumer<X> c, float completeness) {
        int batchSize = -1;
        X next;
        int done = 0;

        //CRITICAL
        while ((next = in./*poll*/ relaxedPoll()) != null) {

            c.accept(next);
            done++;

            if (batchSize == -1) {
                //initialization once for subsequent attempts
                int available; //estimate
                if ((available = size()) <= 0)
                    break;

                batchSize = //Util.lerp(throttle,
                        //available, /* all of it if low throttle. this allows most threads to remains asleep while one awake thread takes care of it all */
                        (int) Util.clamp(available, 1, Math.ceil(completeness * available));

            } else if (--batchSize == 0)
                break; //enough
        }

        return done;
    }
}
