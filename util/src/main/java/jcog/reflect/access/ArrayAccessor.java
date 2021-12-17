package jcog.reflect.access;

import java.lang.reflect.Array;

public class ArrayAccessor extends Accessor {
	final Class type;
	final int index;

	public ArrayAccessor(Class type, int index) {
		this.type = type;
		this.index = index;
	}

	@Override
	public String toString() {
		return type + "[" + index + ']';
	}

	@Override
	public Object get(Object container) {
		return Array.get(container, index);
	}

	@Override
	public void set(Object container, Object value) {
		Array.set(container, index, value);
	}
}
