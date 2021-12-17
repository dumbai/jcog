package jcog.data.map;

import java.util.HashMap;

/**
 * allows no puts, gets return null, etc..
 */
public class BlackHoleMap<K, V> extends HashMap<K, V> {

    public BlackHoleMap() {
        super(1);
    }

    @Override
    public V put(K key, V value) {
        
        
        return null;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public V get(Object key) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }
}
