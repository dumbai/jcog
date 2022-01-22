package jcog.reflect;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import jcog.TODO;
import jcog.WTF;
import jcog.data.bit.AtomicMetalBitSet;
import jcog.data.list.Lst;
import jcog.func.TriFunction;
import jcog.signal.MutableInteger;
import jcog.util.Reflect;
import org.eclipse.collections.api.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.eclipse.collections.impl.tuple.Tuples.pair;

//import jcog.sort.FloatRank;
//import jcog.sort.RankedN;

/**
 * generic reflective object decorator: constructs representations and multi-representations
 * from materialization abstractions
 */
public class AutoBuilder<X, Y> {

    public final Map<Class, TriFunction/*<Field, Object, Object, Object>*/> annotation;
    public final Map<Class, BiFunction<X, Object /* relation */, Y>>[] onClass;
    public final Map<Predicate, Function<X, Y>> onCondition;

    int maxClassBuilders/* per object*/ = 1;
    boolean recurseOnlyUnmatched = false;

    final AutoBuilding<X, Y> building;
    private final int maxDepth;

    private final Set<Object> seen = Sets.newSetFromMap(new IdentityHashMap());


    public AutoBuilder(int maxDepth, AutoBuilding<X, Y> building, Map<Class, BiFunction<X, Object, Y>>[] onClass) {
        this.building = building;
        this.maxDepth = maxDepth;
        this.annotation = new HashMap();
        this.onClass = onClass;
        if (onClass.length <= 0)
            throw new WTF();
        this.onCondition = new HashMap<>();
    }

    /**
     * builds the root item's representation
     */
    public final Y build(X root) {
        return build(root, null, null, root, 0);
    }

    protected @Nullable <C> Y build(C root, @Nullable Y parentRepr, Object relation, @Nullable X obj, int depth) {
        if (!add(obj))
            return null; //cycle


        Lst<BiFunction<Object, Object, Y>> builders = new Lst(maxClassBuilders);

//        {
//            if (!onCondition.isEmpty()) {
//                onCondition.forEach((Predicate test, Function builder) -> {
//                    if (test.test(x)) {
//                        Y y = (Y) builder.apply(x);
//                        if (y != null)
//                            built.addAt(pair(x,y));
//                    }
//                });
//            }
//        }

        classBuilders(obj, builders); //TODO check subtypes/supertypes etc
        List<Pair<X, Iterable<Y>>> target = new Lst<>();
        if (!builders.isEmpty()) {
            target.add(pair(obj,
                    //builders.stream().map(b -> b.apply(obj, relation)).filter(Objects::nonNull)::iterator
                    builders.stream().map(b -> b.apply(obj, relation)).filter(Objects::nonNull).limit(maxClassBuilders)::iterator
            ));
        }

            if (builders.isEmpty() || !recurseOnlyUnmatched) {

                //if (bb.isEmpty()) {
                if (depth <= maxDepth) {
                    collectFields(root, obj, parentRepr, target, depth + 1);
                }

            }
//        if (obj instanceof Map) {
//            ((Map<?,?>) obj).entrySet().stream()
//                    .map((Map.Entry<?,?> x) ->
//                            Tuples.pair(obj,
//                                (Iterable<Y>)(builders.stream().map(b ->
//                                    b.apply(x, relation)).filter(Objects::nonNull)::iterator)))
//                    .forEach(target::add);
//        }
//        if (obj instanceof List) {
//            ((List<?>)obj).stream()
//                    .map((Object x) ->
//                            pair(obj, (Iterable<Y>)(builders.stream().map(b ->
//                                            b.apply(x, relation)).filter(Objects::nonNull)::iterator)))
//                    .forEach(target::add);
//
//        }


            return building.build(root, target, obj);

    }

    private void classBuilders(X x, Lst<BiFunction</* X */Object, Object, Y>> ll) {
        Class<?> xc = x.getClass();
//        Function<X, Y> exact = onClass.get(xc);
//        if (exact!=null)
//            return exact;

        //exhaustive search
        // TODO cache in a type graph
        for (Map<Class, BiFunction<X, Object, Y>> onClass : this.onClass) {
            for (Map.Entry<Class, BiFunction<X, Object, Y>> entry : onClass.entrySet()) {
                Class k = entry.getKey();
                BiFunction<X, Object, Y> v = entry.getValue();
                if (k.isAssignableFrom(xc)) {
                    if (v == null)
                        break; //an interrupt

                    ll.add((BiFunction) v);
                    if (ll.size() > maxClassBuilders)
                        break;
                }
            }
        }
    }

    public void clear() {
        seen.clear();
    }

    public <A, F> void annotation(Class<? extends A> essenceClass, TriFunction<Field, F, A, Object> o) {
        annotation.put(essenceClass, o);
    }

    private <C> void collectFields(C c, X x, Y parentRepr, Collection<? super Pair<X, Iterable<Y>>> target, int depth) {

        for (Map.Entry<String, Reflect> entry : Reflect.on(x.getClass()).fields(true, false, false).entrySet()) {
//            String s = entry.getKey();
            Reflect ff = entry.getValue();
            try {
                Field f = ff.get();
                Object fVal = f.get(x);
                int fMod = f.getModifiers();
                if (Modifier.isPublic(fMod) && !Modifier.isTransient(fMod)) {
                    for (Map.Entry<Class, TriFunction> e : annotation.entrySet()) {
                        Annotation fe = f.getAnnotation(e.getKey());
                        if (fe != null) {
                            Object v = e.getValue().apply(f, fVal, fe);
                            if (v != null) {
                                Object vv;
                                try {
                                    //HACK
                                    vv = build((X) v);
                                } catch (ClassCastException ce) {
                                    //continue
                                    vv = v;
                                }
                                if (vv != null) {
                                    fVal = vv;
                                    break;
                                }
                            }
                        }
                    }

                    if (fVal != null && fVal != x) {
                        X z = (X) fVal;
                        Y w = build(c, parentRepr, f, z, depth);
                        if (w != null) {
                            List<Y> ww = List.of(w); //HACK
                            target.add(pair(z, ww.subList(0, Math.min(ww.size(), maxClassBuilders))));
                        }
                    }
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
//        for (Field f : cc.getFields()) {
//
//            int mods = f.getModifiers();
//            if (Modifier.isStatic(mods))
//                continue;
//            if (!Modifier.isPublic(mods))
//                continue;
//            if (f.getType().isPrimitive())
//                continue;
//
//            try {
//
//
//                f.trySetAccessible();
//
//
//                Object y = f.get(x);
//                if (y != null && y != x)
//                    collect(y, target, depth, f.getName());
//
//            } catch (Throwable t) {
//                t.printStackTrace();
//            }
//        }


    }

    private boolean add(Object x) {
        return seen.add(x);
    }

    /** TODO */
    enum RelationType {
        Root,
        Field,
        Dereference,
        ListElement,
        MapElement
        //..
    }

//    public <C extends X> AutoBuilder<X, Y> on(Class<C> c, BiFunction<C, /*RelationType*/Object, Y> each) {
//        onClass.put(c, (BiFunction<? super X, Object, Y>) each);
//        return this;
//    }

    public AutoBuilder<X, Y> on(Predicate test, Function<X, Y> each) {
        onCondition.put(test, each);
        return this;
    }

    /** TODO use Deduce interface */
    @FunctionalInterface
    public interface AutoBuilding<X, Y> {
        Y build(Object context, List<Pair<X, Iterable<Y>>> features, X obj);
    }

    public abstract static class Way<X> implements Supplier<X> {
        public String name;
    }

    /** supplies zero or more chocies from a set */
    public static class Some<X> implements Supplier<X[]> {
        final Way<X>[] way;
        final AtomicMetalBitSet enable = new AtomicMetalBitSet();

        public Some(Way<X>[] way) {
            this.way = way;
            assert(way.length > 1 && way.length <= 31 /* AtomicMetalBitSet limit */);
        }

        public Some<X> set(int which, boolean enable) {
            this.enable.set(which, enable);
            return this;
        }

        @Override
        public X[] get() {
            throw new TODO();
        }

        public int size() {
            return way.length;
        }
    }

//    public static class Best<X> extends RankedN implements Supplier<X> {
//        final Some<X> how;
//        final FloatRank<X> rank;
//
//        public Best(Some<X> how, FloatRank<X> rank) {
//            super(new Object[how.size()], rank);
//            this.how = how;
//            this.rank = rank;
//        }
//
//        @Override
//        public X get() {
//            super.clear();
//            X[] xx = how.get();
//            if (xx.length == 0)
//                return null;
//            for (X x : xx)
//                add(x);
//            return (X) top();
//        }
//    }

    /** forces a one or none choice from a set */
    public static class Either<X> implements Supplier<X> {
        final Way<X>[] way;
        volatile int which = -1;

        @SafeVarargs
        public Either(Way<X>... way) {
            assert(way.length > 1);
            this.way = way;
        }

        public Either<X> set(int which) {
            this.which = which;
            return this;
        }

        public final Either<X> disable() {
            set(-1);
            return this;
        }

        @Override
        public X get() {
            int c = this.which;
            return c >=0 ? way[c].get() : null;
        }
    }

    /** essentially a decomposition of a subject into its components,
     *  include a descriptive relations to each */
    @FunctionalInterface
    public interface Deduce<R,X> extends Iterable<Pair<R,X>> {

    }

    public static class DeduceFields<X,R,Y> implements Deduce<R,Y> {

        public DeduceFields(X source) {
        }

        @Override
        public Iterator<Pair<R, Y>> iterator() {
            throw new TODO();
        }
    }

    /** for Iterable's incl. Collections */
    public static class DeduceIterable<X> implements Deduce<MutableInteger,X> {

        public DeduceIterable(Iterable<X> i) {
        }
        @Override
        public Iterator<Pair<MutableInteger, X>> iterator() {
            throw new TODO();
        }
    }

    public static class DeduceMap<X,Y> implements Deduce<X,Y> {

        private final Map<X, Y> m;

        public DeduceMap(Map<X,Y> m) {
            this.m = m;
        }

        @Override
        public Iterator<Pair<X, Y>> iterator() {
            return Iterators.transform(m.entrySet().iterator(), (x) -> pair(x.getKey(), x.getValue()));
        }
    }

    /** inverse of deduce, somehow */
    public interface Induce {
        //TODO
    }

}