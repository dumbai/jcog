package jcog.sort;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import jcog.Util;
import jcog.util.SingletonIterator;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;

import java.util.AbstractCollection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.random.RandomGenerator;

/**
 * @see com.google.common.collect.TopKSelector (guava)
 */
public class Top2<T> extends AbstractCollection<T> implements Consumer<T> {

    private final FloatFunction<T> rank;
    public T a, b;
    public float aa = Float.NEGATIVE_INFINITY, bb = Float.NEGATIVE_INFINITY;

    public Top2(FloatFunction<T> rank) {
        this.rank = rank;
    }

    public Top2(FloatFunction<T> rank, Iterable<T> from) {
        this(rank);
        for (T t : from)
            add(t);
    }


    @Override public void clear() {
        aa = bb = Float.NEGATIVE_INFINITY;
        a = b = null;
    }

    /**
     * resets the best values, effectively setting a the minimum entry requirement
     */
    public Top2<T> min(float min) {
        this.aa = this.bb = min;
        return this;
    }

    @Override
    public boolean add(T x) {
        float xx = rank.floatValueOf(x);

        if (xx != xx)
            return false;

        if (xx > aa) {
            b = a;
            bb = aa; 
            a = x;
            aa = xx;
            return true;
        } else if (xx > bb) {
            b = x;
            bb = xx;
            return true;
        }
        return false;
    }


    public List<T> toList() {
        if (a != null && b != null) {
            return Lists.newArrayList(a, b);
        } else if (b == null && a != null) {
            return Collections.singletonList(a);
        } else {
            return Collections.EMPTY_LIST;
        }
    }
//
//    public Top2<T> of(Iterator<T> iterator) {
//        iterator.forEachRemaining(this::add);
//        return this;
//    }

    @Override
    public Iterator<T> iterator() {
        if (a == null)
            return Util.emptyIterator;
        else if (b == null)
            return new SingletonIterator<>(a);
        else
            return Iterators.forArray(a, b);
    }

    @Override
    public int size() {
        if (a == null) return 0;
        else if (b == null) return 1;
        else return 2;
    }

    @Override
    public boolean isEmpty() {
        return a == null;
    }

    @Override
    public final void accept(T t) {
        add(t);
    }

    public void sample(Consumer<T> target, FloatFunction<T> value, RandomGenerator random) {
        float wa = value.floatValueOf(a);
        float wb = value.floatValueOf(b);
        target.accept(
                random.nextFloat() < (wa/Math.max(Float.MIN_NORMAL, wa+wb)) ? a : b
        );
    }

}