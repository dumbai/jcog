package jcog.func;

/**
 * two argument non-variable integer functor (convenience method)
 */
@FunctionalInterface
public interface IntIntToIntFunction {
    int apply(int x, int y);
}
