package jcog.util;

import java.util.function.Function;

/**
 * from Chronicle Core
 * @param <V>
 */
public class ClassLocal<V> extends ClassValue<V> {
    private final Function<Class<?>, V> classVFunction;

    private ClassLocal(Function<Class<?>, V> classVFunction) {
        this.classVFunction = classVFunction;
    }

    /**
     * Function to create a value to cache information associated with a Class
     *
     * @param classVFunction to generate the associated value.
     * @param <V>            the type of value in this ClassLocal
     * @return the ClassLocal
     */
    public static <V> ClassLocal<V> withInitial(Function<Class<?>, V> classVFunction) {
        return new ClassLocal<>(classVFunction);
    }

    @Override
    protected V computeValue(Class<?> type) {
        return classVFunction.apply(type);
    }
}
