package jcog.reflect.access;

public abstract class Accessor {
	public abstract Object get(Object container);
	public abstract void set(Object container, Object value);
}
