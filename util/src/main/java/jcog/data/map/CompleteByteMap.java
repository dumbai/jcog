package jcog.data.map;

import java.util.function.Supplier;


public class CompleteByteMap<V> implements LazyMap<Byte,V> {

    final V[] map;

    public CompleteByteMap(V[] map) {
        this.map = map;
    }

    @Override
    public V get(Object key) {
        return map[(byte)key];
    }

    @Override
    public V computeIfAbsent(Byte k, Supplier<? extends V> f) {
        V exist = map[k];
        if (exist!=null) return exist;
        else return map[k] = f.get();
    }

}

