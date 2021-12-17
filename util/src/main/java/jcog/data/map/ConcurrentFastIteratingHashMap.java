package jcog.data.map;

import jcog.TODO;
import jcog.data.iterator.ArrayIterator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;
import java.util.stream.Stream;

public class ConcurrentFastIteratingHashMap<X, Y> extends AbstractMap<X, Y>  {

    private final Y[] empty;

    private final Map<X, Y> map =
            //new ConcurrentOpenHashMap<>();
            new ConcurrentHashMap<>();

    /** double buffer live copy */
    private final AtomicBoolean invalid = new AtomicBoolean(false);
    private volatile Y[] values;

    public ConcurrentFastIteratingHashMap(Y[] empty) {
        this.empty = empty;
        this.values = empty;
    }

    @Override
    public final Y put(X key, Y value) {
        if (value == null)
            throw new NullPointerException();

        Y prev = map.put(key, value);
        if (prev!=value)
            invalidate();
        return prev;
    }

    public final boolean removeIf(Predicate<? super Y> filter) {
        if (map.values().removeIf(filter)) {
            invalidate();
            return true;
        }
        return false;
    }


    public final boolean removeIf(BiPredicate<X, ? super Y> filter) {
        if (map.entrySet().removeIf((e) -> filter.test(e.getKey(), e.getValue()))) {
            invalidate();
            return true;
        }
        return false;
    }

    public final void clear(Consumer<Y> each) {
        removeIf((y)-> {
            each.accept(y);
            return true;
        });
    }

    /**
     * without synchronizing this entire method, the best this can do is
     * a near atomic invalidation of the list after the hashmap method returns
     */
    @Override
    public final Y putIfAbsent(X key, Y value) {
        if (value == null)
            throw new NullPointerException();
        Y r;
        if ((r = map.putIfAbsent(key, value)) != value) {
            invalidate();
            return null;
        }
        return r;
    }

    /**
     * without synchronizing this entire method, the best this can do is
     * a near atomic invalidation of the list after the hashmap method returns
     */
    @Override
    public final Y remove(Object key) {
        Y r = map.remove(key);
        if (r != null)
            invalidate();
        return r;
    }

    @Override
    public final boolean remove(Object key, Object value) {
        if (map.remove(key, value)) {
            invalidate();
            return true;
        }
        return false;
    }

    @Override
    public final void clear() {
        values = empty;
        map.clear();
        invalidate();
    }

    @Override
    public final int size() {
        return valueArray().length;
    }

    @Override
    public final boolean isEmpty() {
        return size()==0;
    }

    public final List<Y> asList() {
        return MyAbstractList;
    }

    @Override
    public final void forEach(BiConsumer<? super X, ? super Y> action) {
        this.map.forEach(action);
    }


    /**
     * this is the fast value iterating method
     */
    public final void forEachValue(Consumer<? super Y> action) {
        for (Y y : valueArray()) {
            if (y !=null)
                action.accept(y);
        }
    }
    public final <Z> void forEachValueWith(BiConsumer<? super Y, Z> action, Z z) {
        for (Y y : valueArray()) {
            if (y !=null)
                action.accept(y, z);
        }
    }

    @Override
    public final Y compute(X key, BiFunction<? super X, ? super Y, ? extends Y> remappingFunction) {
        boolean[] changed = {false};
        Y v = map.compute(key, (k, pv) -> {
            Y next = remappingFunction.apply(k, pv);
            if (next != pv)
                changed[0] = true;
            return next;
        });
        if (changed[0])
            invalidate();
        return v;
    }

    @Override
    public final Y computeIfAbsent(X key, Function<? super X, ? extends Y> mappingFunction) {
        boolean[] changed = {false};
        Y v = map.computeIfAbsent(key, p -> {
            Y next = mappingFunction.apply(p);
            changed[0] = true;
            return next;
        });
        if (changed[0])
            invalidate();
        return v;
    }


    protected final void invalidate() {
        invalid.set(true);
    }

    public boolean whileEachValue(Predicate<? super Y> action) {
        Y[] x = valueArray();
        for (Y xi : x) {
            if (!action.test(xi))
                return false;
        }
        return true;
    }

    public boolean whileEachValueReverse(Predicate<? super Y> action) {
        Y[] x = valueArray();
        for (int i = x.length - 1; i >= 0; i--) {
            if (!action.test(x[i]))
                return false;
        }
        return true;
    }

    @Override
    public final Y get(Object key) {
        return map.get(key);
    }

    @Override
    public final Set<Entry<X, Y>> entrySet() {
        return map.entrySet();
    }

    @Override
    public final Set<X> keySet() {
        return map.keySet();
    }

    @Override
    public final Collection<Y> values() {
        return MyAbstractList;
    }

    @Override
    public final boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public final boolean containsValue(Object value) {
        return map.containsValue(value);
    }


    public final Iterator<Y> valueIterator() {
        return ArrayIterator.iterate(valueArray());
    }
    public final Stream<Y> valueStream() {
        return ArrayIterator.stream(valueArray());
    }

    public ListIterator<Y> valueListIterator() {
        throw new TODO();
    }


    public final Y[] valueArray() {
        if (invalid.compareAndSet(true,false)) {
            values = map.values().toArray(empty);
        }
        return values;
    }

//    private Y[] _valueArray(Y[] prev) {
//        Y[] next;
//        if (map instanceof ConcurrentOpenHashMap) { //HACK @Deprecated
//            next = ((ConcurrentOpenHashMap<?, Y>) map).values(prev, i -> Arrays.copyOf(emptyArray, i));
//        } else {
//            int s = map.size();
//            if (s== 0)
//                return emptyArray;
//
//
//            if (s > prev.length || (prev.length - extraThreshold > s)) {
//                next = Arrays.copyOf(emptyArray, s);
//            } else {
//                next = prev;
//            }
//
//            final int[] j = {0};
//            ((ConcurrentHashMap<X,Y>)map).forEachValue(1, x->{
//                assert(x!=null);
//                //if (x!=null) {
//                    int jj = j[0];
//                    if (jj < next.length) {
//                        next[jj] = x;
//                        j[0]++;
//                    }
//                //}
//            });
//
//            if (j[0] < next.length-1)
//                Arrays.fill(next, j[0], next.length, null);
//
//            //next = map.values().toArray(prev);
//        }
//        return next;
//    }





    private final AbstractList<Y> MyAbstractList = new AbstractList<>() {

        @Override
        public int size() {
            return ConcurrentFastIteratingHashMap.this.size();
        }

        @Override
        public Y get(int i) {
           return getIndex(i);
        }

        @Override
        public Iterator<Y> iterator() {
            return ConcurrentFastIteratingHashMap.this.valueIterator();
        }

    };

    public final Y getIndex(int i) {
        Y[] l = valueArray();
        return l.length > i ? l[i] : null;
    }
    public final Y getIndex(Random rng) {
        Y[] l = valueArray();
        return l.length > 0 ? l[rng.nextInt(l.length)] : null;
    }

}