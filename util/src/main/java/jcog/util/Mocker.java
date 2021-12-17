/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jcog.util;

import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/*
 * from: Chronicle Core
 * Created by Peter Lawrey on 13/12/16.
 *
 * untested
 */
public enum Mocker {
    ;

    public static <T> T logging(Class<T> tClass, String description, PrintStream out) {
        return intercepting(tClass, description, out::println);
    }

    public static <T> T logging(Class<T> tClass, String description, PrintWriter out) {
        return intercepting(tClass, description, out::println);
    }

//    public static <T> T logging(Class<T> tClass, String description, StringWriter out) {
//        return logging(tClass, description, new PrintWriter(out));
//    }

    public static <T> T queuing(Class<T> tClass, String description, BlockingQueue<String> queue) {
        return intercepting(tClass, description, queue::add);
    }

    public static <T> T intercepting(Class<T> tClass, String description, Consumer<String> consumer) {
        return intercepting(tClass, description, consumer, null);
    }

    public static <T> T intercepting(Class<T> tClass, String description, Consumer<String> consumer, T t) {
        return intercepting(tClass,
                (name, args) -> consumer.accept(description + name + (args == null ? "()" : Arrays.toString(args))),
                t);
    }

    public static <T> T intercepting(Class<T> tClass, BiConsumer<String, Object[]> consumer, T t) {
        
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass}, new AbstractInvocationHandler(ConcurrentHashMap::new) {
            @Override
            protected Object doInvoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
                consumer.accept(method.getName(), args);
                if (t != null)
                    method.invoke(t, args);
                return null;
            }
        });
    }

    public static <T> T ignored(Class<T> tClass) {
        
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass}, new AbstractInvocationHandler(ConcurrentHashMap::new) {
            @Override
            protected Object doInvoke(Object proxy, Method method, Object[] args) {
                return null;
            }
        });
    }

    public abstract static class AbstractInvocationHandler implements InvocationHandler {
        
        private static final ClassLocal<MethodHandles.Lookup> PRIVATE_LOOKUP = ClassLocal.withInitial(AbstractInvocationHandler::acquireLookup);
        private static final Object[] NO_ARGS = {};
        
        private Closeable closeable;




        /**
         * @param mapSupplier ConcurrentHashMap::new for thread safe, HashMap::new for single thread, Collections::emptyMap to turn off.
         */
        protected AbstractInvocationHandler(Supplier<Map> mapSupplier) {




        }

        private static MethodHandles.Lookup acquireLookup(Class<?> c) {
            try {
                
                Constructor<MethodHandles.Lookup> lookupConstructor =
                        MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
                if (!lookupConstructor.isAccessible()) {
                    lookupConstructor.setAccessible(true);
                }
                return lookupConstructor.newInstance(c, MethodHandles.Lookup.PRIVATE);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            }
            try {
                
                Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                field.setAccessible(true);
                return (MethodHandles.Lookup) field.get(null);
            } catch (Exception e) {
                
                return MethodHandles.lookup();
            }
        }

        @Override
        public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Class<?> declaringClass = method.getDeclaringClass();
            if (declaringClass == Object.class) {
                return method.invoke(this, args);

            } else if (declaringClass == Closeable.class && "close".equals(method.getName())) {

                closeQuietly(closeable);
                return null;






            }

            if (args == null)
                args = NO_ARGS;

            Object o = doInvoke(proxy, method, args);

            return o == null ? defaultValues.getOrDefault(method.getReturnType(), null) : o;
        }

        static void closeQuietly(@Nullable Object o) {
            if (o instanceof Object[]) {
                for (Object o2 : (Object[]) o) {
                    closeQuietly(o2);
                }
            } else if (o instanceof Closeable) {
                try {
                    ((Closeable) o).close();
                } catch (IOException e) {
                    LoggerFactory.getLogger(AbstractInvocationHandler.class).debug("", e);
                }
            }
        }

        static final Map<Class,Object> defaultValues = Map.of(
                boolean.class, false,
                byte.class, (byte) 0,
                short.class, (short) 0,
                char.class, (char) 0,
                int.class, 0,
                long.class, 0L,
                float.class, 0.0f,
                double.class, 0.0
                );

        /**
         * Default handler for method call.
         */
        protected abstract Object doInvoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException;

        @SuppressWarnings("WeakerAccess")
        static MethodHandle methodHandleForProxy(Object proxy, Method m) {
            try {
                Class<?> declaringClass = m.getDeclaringClass();
                MethodHandles.Lookup lookup = PRIVATE_LOOKUP.get(declaringClass);
                return lookup
                        .in(declaringClass)
                        .unreflectSpecial(m, declaringClass)
                        .bindTo(proxy);
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }

        public void onClose(Closeable closeable) {
            this.closeable = closeable;
        }
    }

}
