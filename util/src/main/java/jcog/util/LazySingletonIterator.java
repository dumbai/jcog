package jcog.util;

import java.util.Iterator;
import java.util.function.Supplier;

public final class LazySingletonIterator<X> implements Iterator<X> {

	private X n;

	Supplier<X> sup;

	public LazySingletonIterator(Supplier<X> sup) {
		this.sup = sup;
	}

	@Override
	public boolean hasNext() {
		if (sup!=null) {
			n = sup.get();
			sup = null;
			return true;
		} else {
			n = null;
			return false;
		}
	}

	@Override
	public X next() {
		return n;
	}
}