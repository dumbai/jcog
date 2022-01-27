/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http:
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package jcog.thing;

import com.google.common.util.concurrent.MoreExecutors;
import jcog.Log;
import jcog.TODO;
import jcog.WTF;
import jcog.event.ListTopic;
import jcog.event.Topic;
import jcog.util.ArrayUtil;
import org.eclipse.collections.api.tuple.primitive.ObjectBooleanPair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static jcog.thing.Thing.ServiceState.*;
import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;

/**
 * Some thing composed of Parts
 * <p>
 * CONTRAINER / OBJENOME
 * <p>
 * A collection or container of 'parts'.
 * Smart Dependency Injection (DI) container with:
 * <p>
 * autowiring
 * type resolution assisted by CastGraph, with diverse of builtin transformers
 * <p>
 * hints from commandline args, env variables, or constructor string:
 * <p>
 * "parts={<key>:<value>,[<key>:<value>] }"
 * key = interface name | variable name
 * value = JSON-parseable java constant
 * <p>
 * the hints keys and values are fuzzy matchable, with levenshtein dist as a decider in case of ambiguity
 * <p>
 * note: such syntax should be parseable both in JSON and NAL
 *
 * @param P parts key
 * @param T thing context
 */
public class Thing<T/* context */, P /* service key */> {


    public final Topic<ObjectBooleanPair<Part<T>>> eventOnOff = new ListTopic<>();
    public final Executor executor;
    //(Exe.concurrent() ? ForkJoinPool.commonPool() : MoreExecutors.directExecutor());

    protected final ConcurrentMap<P, Part<T>> parts = new ConcurrentHashMap<>();
    public final T id;

    public Thing() {
        this(null, null);
    }

    public Thing(@Nullable Executor executor) {
        this(null, executor);
    }

    /**
     * Constructs a new instance for managing the given services.
     *
     * @param services The services to manage
     * @param x
     * @throws IllegalArgumentException if not all services are {@linkplain ServiceState#NEW new} or if there
     *                                  are any duplicate services.
     */
    public Thing(@Nullable T id, @Nullable Executor executor) {
        if (id == null)
            id = (T) this; //attempts cast

        this.id = id;

        this.executor = executor != null ? executor :
                MoreExecutors.directExecutor();

    }

    public Thing(@Nullable T id) {
        this(id, null);
    }


    /**
     * restart an already added part
     */
    public final boolean restart(P key) {
        return tryStart(the(key));
    }


    public final Part<T> the(P key) {
        return parts.get(key);
    }


    /**
     * add and starts it
     */
    public final boolean add(P key, Part<T> instance) {
        //return set(key, instance, true);
        return add(key, instance, true);
    }

    /**
     * tries to add the new instance, replacing any existing one, but doesnt start
     */
    public final boolean add(P key, Part<T> instance, boolean autoStart) {
        return set(key, instance, autoStart);
    }

    public final boolean remove(P p) {
        return set(p, (Part) null, false);
    }

    public boolean stop(P p) {
        //return set(p, part(p), false);
        Part<T> P = parts.get(p);
        if (P != null) {
            tryStop(P, null);
            return true;
        } else
            return false;
    }

    public final boolean stop(Part<T> p) {
        //HACK TODO improve
        return stop(term(p));
    }

    public final boolean remove(Part<T> p) {
        //HACK TODO improve
        P k = term(p);
        return remove(k);
    }

    /**
     * reverse lookup by instance.  this default impl is an exhaustive search.  improve in subclasses
     */
    public @Nullable P term(Part<T> p) {
        //HACK TODO improve
        for (Map.Entry<P, Part<T>> z : entrySet()) {
            if (z.getValue() == p) {
                return z.getKey();
            }
        }
        return null;
    }

    public final Part<T> add(P key, Class<? extends Part<T>> instanceOf) {
        return add(key, instanceOf, true);
    }

    public final Part<T> add(P key, Class<? extends Part<T>> instanceOf, boolean autoStart) {
        return set(key, instanceOf, autoStart);
    }

    public final Part<T> set(P key, Class<? extends Part<T>> instanceOf, boolean start) {
        Part<T> p = build(key, instanceOf).get();
        if (set(key, p, start))
            return p;
        else
            throw new WTF();
    }


    /**
     * stops all parts (but does not remove them)
     */
    public Thing<T, P> stopAll() {
        parts.keySet().forEach(this::stop);
        return this;
    }

    /*@Override*/
    public void delete() {
        eventOnOff.clear();

        for (P p : parts.keySet())
            remove(p);

        assert(parts.isEmpty());
    }

    public final Stream<Part<T>> partStream() {
        return parts.values().stream();
    }

    public final Stream<SubPart> subPartStream() {
        return partStream().flatMap(z ->
            z instanceof Parts ? ((Parts)z).subs() : Stream.empty());
    }

    public final Set<Map.Entry<P, Part<T>>> entrySet() {
        return parts.entrySet();
    }

    public int size() {
        return parts.size();
    }

    /**
     * TODO construct a table, using TableSaw of the following schema, and pretty print the table instance:
     * K key
     * state
     * Part value
     * Class valueClass
     */
    public void print(PrintStream out) {
        for (Map.Entry<P, Part<T>> entry : parts.entrySet()) {
            P p = entry.getKey();
            Part<T> s = entry.getValue();
            out.println(s.state() + "\t" + p + "\t" + s + "\t" + s.getClass());
        }
    }

    void error(@Nullable Part part, Throwable e, String what) {
        if (part != null)
            logger.error("{} {} {} {}", part, what, this, e);
        else
            logger.error("{} {} {}", what, this, e);
    }


    private boolean tryStart(Part<T> x) {

        if (x.state.compareAndSet(Off, OffToOn)) {
            executor.execute(x.start(this));
            return true;
        } else {
            //logger.info("{} already starting or started", x);
            return false;
        }
    }

    private boolean tryStop(Part<T> x, @Nullable Runnable afterOff) {

        if (!x.state.compareAndSet(On, OnToOff)) {
            //logger.info("{} already stopping or stopped", x);
            return false;
        }

        executor.execute(() -> {
            try {

                if (x instanceof Parts) ((Parts)x).stopSubs(); //stop sub-parts

                x.stop(id);

                boolean nowOff = x.state.compareAndSet(OnToOff, Off); assert (nowOff);

                eventOnOff.emit(pair(x, false)/*, executor*/);

                if (afterOff != null && x.isOff())
                    afterOff.run();

            } catch (RuntimeException e) {
                x.state.set(Off);
                error(x, e, "stop");
            }
        });

        return true;
    }


    public final Set<P> keySet() {
        return parts.keySet();
    }


    /**
     * returns true if a state change could be attempted; not whether it was actually successful (since it is invoked async)
     */
    public boolean set(P key, @Nullable Part<T> x, boolean start) {

        if (x == null && start)
            throw new WTF();

        Part<T> removed = x != null ? parts.put(key, x) : parts.remove(key);

        if (x != removed) {
            //something removed
            if (removed != null) {
                tryStop(removed, start ? () -> tryStart(x) : null);
                return true;
            } else {
                return !start || tryStart(x);
            }

        } else {
            if (start) {
                return tryStart(x);
            } else if (x != null) {
                return tryStop(x, null);
            } else
                return false;
        }
    }


    public final <X extends Part<T>> Supplier<X> build(Class<X> klass) {
        return build(null, klass);
    }

    public <X extends Part<T>> Supplier<X> build(@Nullable P key, Class<X> klass) {
		//concrete class, attempt constructor injection
		return klass.isInterface() || Modifier.isAbstract(klass.getModifiers()) ? new PartResolveByClass(key, klass) : new PartResolveByConstructorInjection(key, klass);
    }

    public enum ServiceState {

        OffToOn() {
            @Override
            public String toString() {
                return "-+";
            }
        },
        On() {
            @Override
            public String toString() {
                return "+";
            }
        },
        OnToOff() {
            @Override
            public String toString() {
                return "+-";
            }
        },
        Off() {
            @Override
            public String toString() {
                return "-";
            }
        };
        public final boolean onOrStarting;

        ServiceState() {
            this.onOrStarting = this.ordinal() < 2;
        }
    }

    class PartResolveByClass<X extends Part<T>> implements Supplier<X> {

        private PartResolveByClass(P key, Class<X> klass) {
        }

        @Override
        public X get() {
            throw new TODO();
        }
    }

    class PartResolveByConstructorInjection<X extends Part<T>> implements Supplier<X> {

        private final Class<X> klass;

        PartResolveByConstructorInjection(P key, Class<X> klass) {
            this.klass = klass;
        }

        @Override
        public X get() {

            Constructor[] constructors = klass.getConstructors();


            int constructor = -1;

            //TODO try new Part(key, thisContext) constructors

            //TODO try new Part(key) constructors

            //try new Part(thisContext) constructors
            Object[] args = null;
            int partsIDSettable = ArrayUtil.indexOf(constructors, c -> c.getParameterTypes().length == 1 && c.getParameterTypes()[0].isAssignableFrom(id.getClass()));
            if (partsIDSettable != -1) {
                constructor = partsIDSettable;
                args = new Object[]{id};
            }

            //try no-arg constructors
            if (args == null) {
                int noArgConstructor = ArrayUtil.indexOf(constructors, c -> c.getParameterTypes().length == 0);
                if (noArgConstructor != -1) {
                    constructor = noArgConstructor;
                    args = ArrayUtil.EMPTY_OBJECT_ARRAY;
                }
            }
            if (constructor>=0) {
                try {
                    Constructor cc = constructors[constructor];
                    if (cc.trySetAccessible())
                        return (X) cc.newInstance(args);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            throw new TODO();
        }
    }

    private static final Logger logger = Log.log(Thing.class);

}