package jcog.data.map;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Implementation which lets me keep an optimal number of elements in memory.
 *
 * The point is that I do not need to keep track of what objects are
 * currently being used since I'm using a combination of a LinkedHashMap
 * for the MRU objects and a WeakHashMap for the LRU objects.
 *
 * So the cache capacity is no less than MRU size plus whatever the
 * GC lets me keep. Whenever objects fall off the MRU they go to the
 * LRU for as long as the GC will have them.
 *
 * http:
 */
public class RUCache<K, V> {
    final Map<K, V> mru;
    final Map<K, V> lru;

    public RUCache(int capacity) {
        lru = new WeakHashMap<>(capacity);

        mru = new MRUMap<>(capacity) {
            @Override
            protected void onEvict(Map.Entry<K, V> entry) {
                lru.put(entry.getKey(), entry.getValue());
            }
        };
    }

    public V get(K k) {
        synchronized (mru) {
            return mru.compute(k, (key, value) -> {
                V value1 = value;
                if (value1 != null) {
				}
                else {
                    if ((value1 = lru.remove(key)) != null)
                        mru.put(key, value1);
				}
				return value1;
			});
        }
    }

    public void put(K key, V value) {
        synchronized (mru) {
            lru.remove(key);
            mru.put(key, value);
        }
    }


}