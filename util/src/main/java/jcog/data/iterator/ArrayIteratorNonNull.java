package jcog.data.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/** TODO test, may need a pre-buffering stategy to absolutely contain any Null's */
public class ArrayIteratorNonNull<E> implements Iterator<E>, Iterable<E> {

	private final int limit;
	E next;
	protected final E[] array;
	protected int index = -1;

	public ArrayIteratorNonNull(E[] array) {
		this(array, array.length);
	}

	public ArrayIteratorNonNull(E[] array, int limit) {
		this.array = array;
		this.limit = Math.min(array.length, limit);
	}

	@Override
	public boolean hasNext() {
		return next != null || (next = find()) != null;
	}

	private E find() {
		E next = null;
		int index = this.index;
		int limit = this.limit;
		while (++index < limit) {
			if ((next = this.array[index])!=null)
				break;
		}
		this.index = index;
		return this.next = next;
	}

	@Override
	public E next() {
		E n = next;
		if (n == null) {
			//called next without prior hasNext, so call that now.
			if ((n = find())==null)
				throw new NoSuchElementException();
		}
		this.next = null; //force update if not calling hasNext() for next iteration
		return n;
	}

	@Override
	public void forEachRemaining(Consumer<? super E> action) {
		forEach(Math.max(0, index), action);
	}

	private void forEach(int i, Consumer<? super E> action) {
		int l = limit;
		for (; i < l; i++) {
			E x = array[i];
			if (x!=null)
				action.accept(x);
		}
		this.index = i;
	}

	@Override
	public void forEach(Consumer<? super E> action) {
		forEach(0, action);
	}

	/** if already started, return a fresh copy */
	@Override public Iterator<E> iterator() {
		return (index == -1) ? this : clone();
		//return (index != -1 && !(index==0 && next!=null)) ? clone() : this;
	}

	protected ArrayIteratorNonNull<E> clone() {
		return new ArrayIteratorNonNull<>(array);
	}
}