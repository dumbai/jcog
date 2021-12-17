package jcog.lab;

import com.google.common.base.Joiner;
import com.google.common.primitives.Primitives;
import jcog.Log;
import jcog.data.graph.ObjectGraph;
import jcog.data.list.Lst;
import jcog.lab.util.Experiment;
import jcog.lab.var.FloatVar;
import jcog.reflect.access.Accessor;
import jcog.reflect.access.FieldAccessor;
import jcog.signal.FloatRange;
import jcog.signal.IntRange;
import jcog.table.DataTable;
import jcog.util.Range;
import org.eclipse.collections.api.block.function.primitive.BooleanFunction;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;
import org.eclipse.collections.api.block.function.primitive.ObjectFloatToFloatFunction;
import org.eclipse.collections.api.block.procedure.primitive.ObjectFloatProcedure;
import org.eclipse.collections.api.block.procedure.primitive.ObjectIntProcedure;
import org.eclipse.collections.api.tuple.Pair;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * the Lab constructs Experiment's
 * it is a mutable representation of the state necessary to
 * "compile" individual experiment instances, and such
 * is the entry point to performing them.
 *
 * @param X subject type
 * @param E experiment type. may be the same as X in some cases but other times
 *          there is a reason to separate the subject from the experiment
 */
public class Lab<X> {

    private static final Logger logger = Log.log(Lab.class);

    private static final int DEFAULT_DEPTH = 7;
    final Supplier<X> subject;
    final Map<String, Sensor<X, ?>> sensors = new ConcurrentHashMap<>();
    public final Map<String, Var<X, ?>> vars = new ConcurrentHashMap<>();
    public final Map<String, Object> hints = new HashMap();

    public Lab() {
        this((Supplier<X>)null);
    }


    public static <X> Lab<X> auto(Class<X> subject) {
        try {
            return new Lab(subject).auto();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Lab(Class<X> subject) throws NoSuchMethodException {
        this(subject.getConstructor());
    }

    public Lab(Constructor<X> subject) {
        this(()-> {
            try {
                return subject.newInstance();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Lab(Supplier<X> subject) {
        initDefaults();
        this.subject = subject;
    }

    private static String varReflectedKey(Iterable<Pair<Class, Accessor>> path) {
        return Joiner.on(':').join(StreamSupport.stream(path.spliterator(), false).map(e ->
                e.getOne().getName() + '.' + e.getTwo()).collect(toList()));
    }

    void initDefaults() {
        final float autoInc_default = 5f;
        hints.put("autoInc", autoInc_default);
    }

    /**
     * records all sensors ()
     */
    public static <X> Object[] record(X x, DataTable data, List<Sensor<X, ?>> sensors) {
        Object[] row = row(x, sensors);
        synchronized (data) {
            data.add(row);
        }
        return row;
    }

    public static <X> Object[] rowVars(X x, List<Var<X, ?>> vars) {
        List<Sensor<X, ?>> sensors = new Lst(vars.size());
        for (int i = 0; i < vars.size(); i++)
            sensors.add(vars.get(i).sense());
        return row(x, sensors);
    }

    public static <X> Object[] row(X x, List<Sensor<X, ?>> sensors) {
        Object[] row = new Object[sensors.size()];
        int c = 0;
        for (Sensor<X, ?> sensor : sensors)
            row[c++] = sensor.apply(x);
        return row;
    }

    protected void add(Var<X, ?> v) {
        if (vars.put(v.id, v) != null)
            throw new UnsupportedOperationException(v.id + " already present in variables");
    }


    private TreeSet<String> validate() {

//        Map<String, Float> h;
//        if (!this.hints.isEmpty()) {
//            if (additionalHints.isEmpty()) {
//                h = this.hints;
//            } else {
//
//                h = new HashMap();
//                h.putAll(this.hints);
//                h.putAll(additionalHints);
//            }
//        } else {
//            h = additionalHints;
//        }
//
//
        TreeSet<String> unknowns = new TreeSet<>();
        for (Var<X, ?> t: vars.values()) {
            List<String> u = t.unknown(hints);
            if (!u.isEmpty())
                unknowns.addAll(u);
        }
        return unknowns;
    }



    //
//    public <Y> Optimize<X, Y> optimize(Function<Supplier<X>, Y> procedure, Goal<Y> goal, List<Sensor<Y, ?>> sensors) {
//        List<Var<X, ?>> list = vars.values().stream().filter(Var::ready).collect(toList());
//        return optimize(subject, list, procedure, goal, sensors
//        );
//    }

//    @SafeVarargs
//    public final Optilive<X, X> optilive(FloatFunction<X>... goals) {
//        assert(goals.length>0);
//        return optilive(Supplier::get, goals);
//    }
//
//    @SafeVarargs
//    public final <Y> Optilive<X, Y> optilive(Function<Supplier<X>, Y> procedure, FloatFunction<Y>... goal) {
//        List<Goal<Y>> list = Arrays.stream(goal).map(Goal::new).collect(toList());
//        return optilive(procedure, list, Collections.EMPTY_LIST);
//    }
//
//
//    public <Y> Optilive<X, Y> optilive(Function<Supplier<X>, Y> procedure, List<Goal<Y>> goal, List<Sensor<Y, ?>> sensors) {
//        List<Var<X, ?>> list = vars.values().stream().filter(Var::ready).collect(toList());
//        return new Optilive<>(subject,
//                procedure, goal,
//                list, sensors);
//    }
//
//    public <Y> Optimize<X, Y> optimize(Function<Supplier<X>, Y> procedure, Goal<Y> goal, Lab<Y> sensors) {
//        List<Var<X, ?>> list = vars.values().stream().filter(Var::ready).collect(toList());
//        return optimize(subject, list,
//                procedure, goal, new Lst(sensors.sensors.values())
//        );
//    }

//
//    /**
//     * simple usage method
//     * provies procedure and goal; no additional experiment sensors
//     */
//    public Opti<X> optimize(Consumer<X> procedure, FloatFunction<X> goal) {
//        return new Opti<>(optimize(procedure,
//                new Goal<>(goal)));
//    }


//    /**
//     * simple usage method
//     * provies procedure and goal; no additional experiment sensors
//     */
//    public <E> Optimize<X, E> optimize(Function<Supplier<X>, E> procedure, FloatFunction<E> goal) {
//        return optimize(procedure, new Goal<>(goal), Collections.EMPTY_LIST);
//    }
//    public <E> Optimize<X, E> optimize(Function<Supplier<X>, E> procedure, ToDoubleFunction<E> goal) {
//        return optimize(procedure, new Goal<>(goal), Collections.EMPTY_LIST);
//    }

    /**
     * simple usage method
     * provies procedure and goal; no additional experiment sensors
     */
    public Opti<X> optimize(Goal<X> goal) {
        List<Var<X, ?>> vars = this.vars.values().stream().filter(Var::ready).collect(toList());

        if (vars.isEmpty())
            throw new UnsupportedOperationException("no Var's defined");

        return new Opti<>(subject, goal, vars, new Lst<>(sensors.values()));
    }

//    public final Opti<X> optimize(int repeats, boolean parallel, FloatFunction<Supplier<X>> goal) {
//        return optimize_(Optimize.repeat(goal, repeats, parallel));
//    }

    public final Opti<X> optimizeEach(FloatFunction<X> goal) {
        return optimize(X ->goal.floatValueOf(X.get()));
    }

    public Opti<X> optimize(FloatFunction<Supplier<X>> goal) {
        return optimize(new Goal<>(goal));
    }

    protected Optimize.OptimizationStrategy optimizer(int maxIter) {
        final int v = vars.size();
        if (v == 0)
            throw new UnsupportedOperationException("no variables");

        return v == 1 ?
            new Optimize.SimplexOptimizationStrategy(maxIter) :
            new Optimize.CMAESOptimizationStrategy(maxIter);
    }


    public Lab<X> sense(Sensor sensor) {
        Sensor removed = sensors.putIfAbsent(sensor.id, sensor);
        if (removed != null)
            throw new RuntimeException("sensor name collision");
        return this;
    }

    private Lab<X> senseNumber(String id, Function<X, Number> f) {
        return sense(NumberSensor.ofNumber(id, f));
    }

    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public Lab<X> sense(String id, FloatFunction<X> f) {
        return sense(NumberSensor.of(id, f));
    }
    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public Lab<X> sense(String id, BooleanFunction<X> f) {
        return sense(NumberSensor.of(id, f));
    }
    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public Lab<X> sense(String id, ToDoubleFunction<X> f) {
        return sense(NumberSensor.of(id, f));
    }

    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public Lab<X> sense(String id, ToLongFunction<X> f) {
        return sense(NumberSensor.of(id, f));
    }

    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public Lab<X> sense(String id, ToIntFunction<X> f) {
        return sense(NumberSensor.of(id, f));
    }

    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public Lab<X> sense(String id, Predicate<X> f) {
        return sense(NumberSensor.of(id, f));
    }

    public final Lab<X> auto() {
        varAuto();
        senseAuto();
        return this;
    }

    /**
     * learns how to modify the possibility space of the parameters of a subject
     * (generates accessors via reflection)
     */
    public Lab<X> varAuto() {
        varAuto(DiscoveryFilter.all);
        return this;
    }
    public Lab<X> senseAuto() {
        //TODO
        return this;
    }


    /**
     * auto discovers Vars by reflecting a sample of the subject
     */
    private Lab<X> varAuto(DiscoveryFilter filter) {

        X x = this.subject.get();

        ObjectGraph o = new LabGraph(filter) {
            @Override
            protected void discovered(X root, Lst<Pair<Class, Accessor>> path, Class<?> targetType) {

                path = path.clone();

                String key = varReflectedKey(path);

                varByClass.get(Primitives.wrap(targetType)).learn(root, key, path);

                varAuto(key, path);
            }
        };

        o.add(DEFAULT_DEPTH, x);

        SortedSet<String> unknown = validate();

        if (!unknown.isEmpty()) {
            for (String w: unknown) {
                logger.warn("unknown: {}", w);
            }
        }

//        if (this.vars.isEmpty()) {
//            throw new RuntimeException("tweaks not ready:\n" + Joiner.on('\n').join(unknown));
//        }

        return this;
    }

    /**
     * extract any hints from the path (ex: annotations, etc)
     */
    private void varAuto(String key, Lst<Pair<Class, Accessor>> path) {
        Accessor a = path.getLast().getTwo();
        if (a instanceof FieldAccessor) {
            Field field = ((FieldAccessor) a).field;
            Range r = field.getAnnotation(Range.class);
            if (r != null) {
                double min = r.min();
                if (min == min)
                    hints.put(key + ".min", (float) min);

                double max = r.max();
                if (max == max)
                    hints.put(key + ".max", (float) max);

                double inc = r.step();
                if (inc == inc)
                    hints.put(key + ".inc", (float) inc);
            }
        }

    }

    public Lab<X> var(String key, Function<X, Integer> get, ObjectIntProcedure<X> apply) {
        return var(key, Float.NaN, Float.NaN, Float.NaN, (x) -> get.apply(x).floatValue() /* HACK */, (x, v) -> {
            int i = Math.round(v);
            apply.value(x, i);
            return i;
        });
    }

    public Lab<X> var(String key, int min, int max, int inc, Function<X, Integer> get, ObjectIntProcedure<X> apply) {
        return var(key, min, max, inc < 0 ? Float.NaN : inc,
                (x) -> get!=null ? get.apply(x).floatValue() : null /* HACK */,
                (x, v) -> {
                    int i = Math.round(v);
                    apply.value(x, i);
                    return i;
                });
    }


    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public Lab<X> var(String id, float min, float max, float inc, ObjectFloatProcedure<X> apply) {
        vars.put(id, new FloatVar<>(id, min, max, inc, null, (X x, float v) -> {
            apply.value(x, v);
            return v;
        }));
        return this;
    }

    /** TODO use an IntVar impl */
    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public Lab<X> var(String id, int min, int max, int inc, ObjectIntProcedure<X> apply) {
        vars.put(id, new FloatVar<>(id, min, max, inc, null, (X x, float v) -> {
            int vv = Math.round(v);
            apply.value(x, vv);
            return vv;
        }));
        return this;
    }

    @Deprecated
    public Lab<X> var(String id, float min, float max, float inc, ObjectFloatToFloatFunction<X> set) {
        return var(id, min, max, inc, null, set);
    }

    public Lab<X> var(String id, float min, float max, float inc, Function<X, Float> get, ObjectFloatToFloatFunction<X> set) {
        vars.put(id, new FloatVar<>(id, min, max, inc, get, set));
        return this;
    }

    private boolean contains(Class<?> t) {
        return varByClass.containsKey(Primitives.wrap(t));
    }

    public Experiment<X> experiment(Consumer<X> e) {
        return experiment((x, t)->{
            e.accept(x);
            t.record(x);
        });
    }

    public Experiment<X> experiment(BiConsumer<X, Experiment<X>> proc) {
        return new Experiment<>(subject, sensors.values(), proc);
    }

    @FunctionalInterface
    private interface VarAccess<X2> {
        void learn(X2 sample, String key, Lst<Pair<Class, Accessor>> path);
    }

    public static class DiscoveryFilter {

        public static final DiscoveryFilter all = new DiscoveryFilter();

        protected boolean includeField(Field f) {
            return true;
        }

        protected boolean includeClass(Class<?> targetType) {
            return true;
        }

    }

    private final Map<Class, VarAccess<X>> varByClass = Map.of(

            Boolean.class, (X sample, String k, Lst<Pair<Class, Accessor>> p) -> {
                Function<X, Boolean> get = ObjectGraph.getter(p);
                BiConsumer<X, Boolean> set = ObjectGraph.setter(p);
                var(k, 0, 1, 0.5f,
                        (x)->get.apply(x) ? 1f : 0f,
                        (x, v) -> {
                            boolean b = (v >= 0.5f);
                            set.accept(x, b);
                            return (b) ? 1f : 0f;
                        });
            },

            AtomicBoolean.class, (sample, k, p) -> {
                Function<X, AtomicBoolean> get = ObjectGraph.getter(p);
                AtomicBoolean fr = get.apply(sample);
                var(k, 0, 1, 0.5f, (x, v) -> {
                    boolean b = v >= 0.5f;
                    get.apply(x).set(b);
                    return b ? 1f : 0f;
                });
            },

            Integer.class, (X sample, String k, Lst<Pair<Class, Accessor>> p) -> {
                Function<X, Integer> get = ObjectGraph.getter(p);
                BiConsumer<X, Integer> set = ObjectGraph.setter(p);
                var(k, get, set::accept);
            },
            IntRange.class, (sample, k, p) -> {
                Function<X, IntRange> get = ObjectGraph.getter(p);
                IntRange fr = get.apply(sample);
                var(k, fr.min, fr.max, -1, null /* TODO */, (ObjectIntProcedure<X>) (x, v) -> get.apply(x).set(v));
            },



            Float.class, (X sample, String k, Lst<Pair<Class, Accessor>> p) -> {
                Function<X, Float> get = ObjectGraph.getter(p);
                BiConsumer<X, Float> set = ObjectGraph.setter(p);
                var(k, Float.NaN, Float.NaN, Float.NaN,
                        get,
                        (x,v)->{ set.accept(x,v); return v; });
            },



            FloatRange.class, (sample, k, p) -> {
                Function<X, FloatRange> get = ObjectGraph.getter(p);
                FloatRange fr = get.apply(sample);
                var(k, fr.min, fr.max, Float.NaN,
                        (x)-> get.apply(x).floatValue(),
                        (x, v) -> {
                            get.apply(x).set(v);
                            return v;
                        });
            }

    );


    private class LabGraph extends ObjectGraph {

        private final DiscoveryFilter filter;

        LabGraph(DiscoveryFilter filter) {
            this.filter = filter;
        }

        @Override
        protected boolean access(Object root, Lst<Pair<Class, Accessor>> path, Object target) {
            Class<?> c = target.getClass();
            if (!filter.includeClass(c))
                return false;

            if (nodes.containsKey(target))
                return false;

            if (contains(c))
                discovered((X) root, path, c);

            return !Primitives.unwrap(target.getClass()).isPrimitive();
        }

        /** @param path should be cloned if it's going to be stored */
        protected void discovered(X root, Lst<Pair<Class, Accessor>> path, Class<?> targetType) {
            /* nop */
        }


        @Override
        public boolean recurse(Object x) {
            Class<?> xc = x.getClass();
            return filter.includeClass(xc) && !contains(xc);
        }

        @Override
        public boolean includeValue(Object v) {
            return filter.includeClass(v.getClass());
        }

        @Override
        public boolean includeClass(Class<?> c) {
            return filter.includeClass(c);
        }

        @Override
        public boolean includeField(Field f) {
            int m = f.getModifiers();
            if (!Modifier.isPublic(m) || !filter.includeField(f))
                return false;

            Class<?> t = Primitives.wrap(f.getType());
            boolean primitive = Primitives.unwrap(f.getType()).isPrimitive();
            return contains(t) ? !primitive || !Modifier.isFinal(m) : !primitive;
        }
    }
}