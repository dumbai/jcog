package jcog.exe.realtime;

import jcog.TODO;
import jcog.Util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import static jcog.exe.realtime.TimedFuture.*;

/** TODO this should not involve both poll and offer if the item remains in the wheel */
public class QueueWheelModel extends WheelModel {

	/**
	 * the wheels (array of queues)
	 */
	final Queue<TimedFuture>[] q;

	public QueueWheelModel(int wheels, long resolution, Supplier<Queue<TimedFuture>> queueBuilder) {
		super(wheels, resolution);
		assert (wheels > 1);
		q = new Queue[wheels];
		for (int i = 0; i < wheels; i++)
			q[i] = queueBuilder.get();
	}

	@Override
	public int run(int c) {


		//TODO if n=2 and the previous or next queue is empty try moving one of the items there. this will distribute items across wheels so each has an ideal 0 or 1 size

		int n = 0, limit = Integer.MAX_VALUE;
		Queue<TimedFuture> q = this.q[c];

		TimedFuture r;
		while ((r = q.poll()) != null) {
			n++;
			switch (r.state()) {
				case CANCELLED:
					/* nop */
					break;
				case READY:
					r.execute(timer);
					break;
				case PENDING:
				    if (limit == Integer.MAX_VALUE)
                        limit = n + q.size(); //defer calculating queue size until the first reinsert otherwise it will keep polling what is offered in this loop
					//re-insert
					if (!q.offer(r)) {
						//OVERFLOW
						if (!reschedule((c+1)%wheels, r))
							throw new TODO(); //TODO try all other queues in sequence

					}
					break;
			}
			if (n >= limit)
			    break; //exit before polling PENDING tasks recycled to the back of the queue
		}

		return n;
	}

	@Override
	public boolean accept(TimedFuture<?> r, HashedWheelTimer t) {
		return t.reschedule(r); //immediately
	}

	@Override
	public boolean reschedule(int wheel, TimedFuture r) {

		Queue<TimedFuture>[] q = this.q;
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
		return Util.sum((ToIntFunction<Queue>) Queue::size, q);
	}

	@Override
	public boolean isEmpty() {
		return Arrays.stream(q).allMatch(Collection::isEmpty);
	}


}
