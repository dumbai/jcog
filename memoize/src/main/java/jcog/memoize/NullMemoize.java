package jcog.memoize;

import java.util.function.Function;

/** pass-through, computes on stack */
public final class NullMemoize<K,V> implements Memoize<K,V> {

    private final Function<K, V> f;

    public NullMemoize(Function<K,V> f) {
        this.f = f;
    }

    @Override
    public String summary() {
        return "";
    }

    @Override
    public void clear() {

    }

    @Override
    public V apply(K k) {
        return f.apply(k);
    }
}
