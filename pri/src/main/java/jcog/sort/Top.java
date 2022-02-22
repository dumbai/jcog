package jcog.sort;

import jcog.Util;
import jcog.util.SingletonIterator;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

import static java.lang.Float.NEGATIVE_INFINITY;

public class Top<X> implements TopFilter<X> {
    public final FloatRank<X> rank;
    /* TODO private */ public X the;
    public float score;

    public Top(FloatFunction<X> rank) {
        this(FloatRank.the(rank));
    }

    public Top(FloatRank<X> rank) {
        this.rank = rank;
        this.score = NEGATIVE_INFINITY;
    }

    public void clear() {
        score = NEGATIVE_INFINITY;
        the = null;
    }

    @Override
    public int size() {
        return the==null ? 0 : 1;
    }

    @Override
    public float minValueIfFull() {
        return score;
    }

    public final X get() { return the; }

    @Override
    public String toString() {
        return the + "=" + score;
    }

    @Override
    public final void accept(X x) {
       add(x);
    }

    @Override
    public final boolean add(X x) {
        float xs = rank.rank(x, score);
        if (/*xs==xs && [covered]*/ xs > score) {
            the = x;
            score = xs;
            return true;
        }
        return false;
    }

    @Override
    public @Nullable X pop() {
        X x = this.the;
        clear();
        return x;
    }
    //    public Top<T> of(Iterator<T> iterator) {
//        iterator.forEachRemaining(this);
//        return this;
//    }

    @Override
    public boolean isEmpty() {
        return the==null;
    }

    @Override
    public final Iterator<X> iterator() {
        X x = the;
        return x == null ? Util.emptyIterator : new SingletonIterator<>(x);
    }
}