package jcog.data.map;

import jcog.TODO;

import java.util.function.Function;
import java.util.function.Supplier;


public interface LazyMap<K, X> {

    X get(Object key);

    //TODO
//    void set(Object key, V value);

    default X computeIfAbsent(K key, Function<? super K, ? extends X> f) {
        return computeIfAbsent(key, () -> f.apply(key));
    }

    X computeIfAbsent(K key, Supplier<? extends X> f);

    default void clear() {
        throw new TODO();
    }
}
