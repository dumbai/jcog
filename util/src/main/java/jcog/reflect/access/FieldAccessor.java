package jcog.reflect.access;

import java.lang.reflect.Field;

/** TODO use VarHandle or something faster than reflect.Field */
public class FieldAccessor extends Accessor {
	public final Field field;

	public FieldAccessor(Field field) {
		this.field = field;
	}

	@Override
	public String toString() {
		return field.getName();
	}

	@Override
	public Object get(Object container) {
		try {
			return field.get(container);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object container, Object value) {
		try {
			field.set(container, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
