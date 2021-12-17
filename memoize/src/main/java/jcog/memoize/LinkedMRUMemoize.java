package jcog.memoize;

import jcog.data.map.MRUMap;

import java.util.function.Function;

import static jcog.Str.n2;


public class LinkedMRUMemoize<X, Y> extends MRUMap<X, Y> implements Memoize<X, Y> {

    final Function<X, Y> f;

    public LinkedMRUMemoize( Function<X, Y> f, int capacity) {
        super(capacity);
        this.f = f;
//        .andThen(y->{
//            hit--;
//            miss++;
//            return y;
//        });
    }

//    @Override
//    public String summary() {
//        return "size=" + super.size();
//    }

//    @Override
//    public Y apply(X x) {
//        hit++;
//        return computeIfAbsent(x, f);
//    }
    @Override
    public Y apply(X x) {
        Y y;
        if ((y= get(x))==null) {
            miss++;
            y = this.f.apply(x);
            put(x, y);
        } else {
            hit++;
        }
        return y;
    }
    public long hit = 0;
    long miss = 0;

    public float hitRate() {
        return ((float) hit) / (hit + miss);
    }

    @Override
    public String summary() {
        return getClass() + " " + n2(hitRate() * 100f) + "% x " + (hit+miss) + ", size=" + size();
    }

    @Override
    public void clear() {
        hit = miss = 0;
        super.clear();
    }

    /**
     * drop-in replacement for LinkedMRUMemoize which captures statistics
     * suitable for recursive invocations, using split get/put calls,
     *      rather using Map.computeIfAbsent which would cause ConcurrentModificationException
     */
    public static class LinkedMRUMemoizeRecurseable<X, Y> extends LinkedMRUMemoize<X, Y> {


        public LinkedMRUMemoizeRecurseable(Function<X, Y> f, int capacity) {
            super(f, capacity);
        }





    }
}
