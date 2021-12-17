package jcog.data.set;

import jcog.data.list.Lst;
import jcog.pri.NLinking;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;

import java.util.Set;

public class PrioritySet<X> extends CachedPriorityQueue<X> {

	private final Lst<NLinking<X>> tmp = new Lst();
	private final Set<X> set;

	public PrioritySet(FloatFunction<X> rank, Set<X> set) {
		super(rank);
		this.set = set;
	}

	@Override
	public boolean offer(X x) {
		return set.add(x) && super.offer(x);
	}

	@Override
	public boolean add(X x) {
		return !set.add(x) || super.offer(x);
	}

	@Override
	public X poll() {
		X x = super.poll();
		if (x!=null)
			set.remove(x); //assert(removed)
		return x;
	}

	@Override
	public X pollLast() {
		X x = super.pollLast();
		if (x!=null)
			set.remove(x); //assert(removed)
		return x;
	}

	@Override
	public void clear() {
		if (!isEmpty()) {
			super.clear();
			set.clear();
		}
	}

	/** reprioritize elements */
	public void refresh(int capacity) {
		int s = size();
		if (s < 2)
			return;
		tmp.ensureCapacity(s);
		queue.forEach(tmp::addFast);
		queue.clear();

		s = 0;
		for (NLinking<X> x : tmp) {
			x.pri(rank);
			if (queue.offer(x)) {
				if (s >= capacity) {
					pollLast();
				} else {
					s++;
				}
			}
		}
		tmp.clear();
	}
}
