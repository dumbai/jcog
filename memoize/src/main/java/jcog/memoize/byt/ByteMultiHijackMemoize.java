package jcog.memoize.byt;

import com.google.common.base.Joiner;
import jcog.memoize.Memoize;

import java.util.function.Function;

public abstract class ByteMultiHijackMemoize<X extends ByteKeyExternal,Y> implements Memoize<X,Y> {

    final ByteHijackMemoize<X,Y>[] level;
    protected int capacity;

    public ByteMultiHijackMemoize(Function<X, Y> f, int capacity, boolean soft, int levels) {
        this.level = new ByteHijackMemoize[levels];
        this.capacity = capacity;
        for (int i = 0; i < levels; i++) {
            level[i] = new ByteHijackMemoize<>(f, capacity(i), reprobes(i), soft);
        }
    }

    abstract int capacity(int level);
    abstract int reprobes(int level);
    abstract int level(ByteKey key);


    @Override
    public String summary() {
        return Joiner.on("\n\t").join(level);
    }

    @Override
    public void clear() {
        for (ByteHijackMemoize m : level)
            m.clear();
    }

    @Override
    public Y apply(X x) {
        int l = level(x);
        return level[l].apply(x);
    }

}
