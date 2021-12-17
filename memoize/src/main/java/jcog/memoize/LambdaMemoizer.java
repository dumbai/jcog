package jcog.memoize;

import jcog.Util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


public enum LambdaMemoizer {
	;


	private static final AtomicInteger serial = new AtomicInteger();


    public static <V> Function<Object[], V> memoize(Class klass, String methodName, Class[] paramTypes, MemoizeBuilder<V> m) {
        try {
            Method method = klass.getDeclaredMethod(methodName, paramTypes);
            method.trySetAccessible();
            return memoize(m, method);

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <V> Function<Object[], V> memoize(MemoizeBuilder<V> m, Method method) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            MethodHandle h = lookup.unreflect(method)
                    .asSpreader(Object[].class, method.getParameterCount());

            int methodID = serial.getAndIncrement();

            Function<ArgKey, V> compute = x -> {
                try {
                    return (V)(h.invoke(x.args));
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            };

            Memoize<ArgKey, V> memoizedCalls = m.apply(compute);

            return args -> memoizedCalls.apply(new ArgKey(methodID, args));

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * TODO make work like TermKey, etc
     */
    public static final class ArgKey {

        final int methodID;

        public final Object[] args;
        private final int hash;

        protected ArgKey(int methodID, Object[] args) {
            this.methodID = methodID;
            this.args = args;
            this.hash = Util.hashCombine(methodID, args != null ? Arrays.hashCode(args) : 1);
        }

        @Override
        public boolean equals(Object o) {
            ArgKey argList = (ArgKey) o;
            return hash == argList.hash && Arrays.equals(args, argList.args);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    @FunctionalInterface
    interface MemoizeBuilder<V> extends Function<Function<ArgKey, V>, Memoize<ArgKey, V>> {

    }

}
