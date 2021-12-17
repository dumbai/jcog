package jcog.util;

@FunctionalInterface
public interface ObjectLongLongFunction<X,Y> {
    Y apply(X x, long a, long b);
}
