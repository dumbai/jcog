package jcog.data.map;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * most recently used cache based on (non-thread_safe) LinkedHashMap
 */
public class MRUMap<K, V> extends LinkedHashMap<K, V> {

    protected int capacity;

    public MRUMap(int capacity, float loadFactor) {
        super(capacity, loadFactor, true);
        this.capacity = capacity;
    }

    public MRUMap(int capacity) {
        this(capacity, 0.75f);
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
        if (this.size() > capacity) {
            onEvict(entry);
            return true;
        }
        return false;
    }

    protected void onEvict(Map.Entry<K, V> entry) {

    }
}
