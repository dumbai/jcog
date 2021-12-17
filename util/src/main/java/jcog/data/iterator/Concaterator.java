package jcog.data.iterator;

import com.google.common.collect.Iterators;

import java.util.Iterator;

import static jcog.Util.emptyIterable;
import static jcog.Util.emptyIterator;

/**
 * modified from eclipse collections "CompositeIterator"
 */
public final class Concaterator<E> implements Iterator<E> {

	private final Iterator meta;
	private Iterator<E> inner;

	public static Iterator concat(Iterator... ii) {
		switch (ii.length) {
			case 0: return emptyIterator;
			case 1: return ii[0];
			case 2:
				if (ii[0] == emptyIterator) return ii[1];
				else if (ii[1] == emptyIterator) return ii[0];
				break;
			//TODO 3-ary tests
		}

		return new Concaterator<>((Object[])ii);
	}
	public static <E> Iterator<E> concat(Object... ii) {
		switch (ii.length) {
			case 0: return emptyIterator;
			case 1: return wrap(ii[0]);
			case 2: {
				Object a = ii[0], b = ii[1];
				if (a == emptyIterator || a == emptyIterable) return wrap(b);
				else if (b == emptyIterator || b == emptyIterable) return wrap(a);
				break;
			}
			//TODO 3-ary tests
		}

		return new Concaterator<>(ii);
	}

	public static <E> Iterator<E> wrap(Object x) {
		if (x instanceof Iterator)
			return (Iterator<E>) x;
		else if (x instanceof Iterable)
			return ((Iterable<E>) x).iterator();
		else
			return Iterators.singletonIterator((E)x);
	}

	private Concaterator(Object... ii) {
		meta = ArrayIterator.iterate(ii);
		inner = null;
	}

	@Override
	public boolean hasNext() {
		while (true) {

			if (inner!=null && this.inner.hasNext())
				return true;

			if (!meta.hasNext()) {
				inner = null;
				return false;
			}

			this.inner = wrap(meta.next());
		}
	}

	@Override
	public E next() {
		return this.inner.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
