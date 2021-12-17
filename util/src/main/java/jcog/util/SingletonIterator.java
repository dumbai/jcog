package jcog.util;

import java.util.Iterator;

public final class SingletonIterator<X> implements Iterator<X> {

	private X x;

	public SingletonIterator(X x) {
		this.x = x;
	}

	@Override
	public boolean hasNext() {
		return x !=null;
	}

	@Override
	public X next() {
		X y = x;
		this.x = null;
		return y;
	}
}
