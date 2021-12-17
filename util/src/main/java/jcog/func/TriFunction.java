package jcog.func;

@FunctionalInterface
public interface TriFunction<X,Y,Z,W> {
    W apply(X x, Y y, Z z);
}
