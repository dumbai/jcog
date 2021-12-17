package jcog.util;

@FunctionalInterface
public interface ObjectLongLongPredicate<T> {
    boolean accept(T object, long start, long end);
}
