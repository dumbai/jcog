package jcog.func;

@FunctionalInterface public interface ObjectObjectToIntFunction<X,Y> {
    int intValueOf(X x, Y y);
}
