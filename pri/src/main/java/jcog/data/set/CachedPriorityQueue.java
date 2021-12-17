package jcog.data.set;

import com.google.common.collect.Iterators;
import com.google.common.collect.MinMaxPriorityQueue;
import jcog.Util;
import jcog.pri.NLinking;
import jcog.util.SingletonIterator;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractQueue;
import java.util.Iterator;

/** caches float prioritization of a set of items for use in priority queue comparison function.
 *  priorities can be any value, not only 0..1.0 since NLinking<X> is used.
 * */
public class CachedPriorityQueue<X> extends AbstractQueue<X> {


	final MinMaxPriorityQueue<NLinking<X>> queue;
	protected final FloatFunction<X> rank;


	public CachedPriorityQueue(FloatFunction<X> _rank) {
		this.rank = _rank;
		queue = MinMaxPriorityQueue.orderedBy(((NLinking<X> a, NLinking<X> b) -> a == b ? 0 : Float.compare(b.priElse(rank), a.priElse(rank)))).create();
	}

	@Override
	public boolean offer(X x) {
		return queue.offer(link(x));
	}

	protected NLinking<X> link(X x) {
		NLinking<X> l = new NLinking<>();
		l.set(x, Float.NaN);
		return l;
	}

	@Nullable @Override
	public X poll() {
		return id(queue.pollFirst());
	}

	@Nullable public X pollLast() {
		return id(queue.pollLast());
	}

	@Override
	public X peek() {
		return id(queue.peekFirst());
	}

	@Override
	public void clear() {
		queue.clear();
	}

	private static @Nullable <X> X id(@Nullable NLinking<X> p) {
		return p != null ? p.id : null;
	}

	@Override
	public Iterator<X> iterator() {
        return switch (queue.size()) {
            case 0 -> Util.emptyIterator;
            case 1 -> new SingletonIterator(queue.peek().id);
            default -> Iterators.transform(queue.iterator(), x -> x.id);
        };
	}

	@Override
	public int size() {
		return queue.size();
	}

	public void capacity(int maxSize) {
		if (maxSize == 0) {
			clear();
		} else {
			int toRemove = size() - maxSize;
			for (int i = 0; i < toRemove; i++)
				pollLast();
		}
	}

}
