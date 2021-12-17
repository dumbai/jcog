//package jcog.data.map;
//
//import java.lang.ref.ReferenceQueue;
//import java.lang.ref.WeakReference;
//import java.util.Collection;
//import java.util.concurrent.ConcurrentMap;
//
///** from: https://dzone.com/articles/letting-garbage-collector-do-c */
//public class GarbageMap<K, V> {
//
//    private static final ReferenceQueue referenceQueue = new ReferenceQueue();
//
//    static {
//        new CleanupThread().start();
//    }
//
//    private final ConcurrentMap<K, GarbageReference<K, V>> map;
//
//    public GarbageMap(ConcurrentMap<K, GarbageReference<K, V>> map) {
//        this.map = map;
//    }
//
//    public void clear() {
//        map.clear();
//    }
//
//    public V get(K key) {
//        GarbageReference<K, V> ref = map.get(key);
//        return ref == null ? null : ref.value;
//    }
//
//    public Object getGarbageObject(K key){
//        GarbageReference<K,V> ref=map.get(key);
//        return ref == null ? null : ref.get();
//    }
//
//    public Collection<K> keySet() {
//        return map.keySet();
//    }
//
//    public void put(K key, V value, Object garbageObject) {
//        if (key == null || value == null || garbageObject == null) throw new NullPointerException();
//        if (key == garbageObject)
//            throw new IllegalArgumentException("key can't be equal to garbageObject for gc to work");
//        if (value == garbageObject)
//            throw new IllegalArgumentException("value can't be equal to garbageObject for gc to work");
//
//        GarbageReference reference = new GarbageReference(garbageObject, key, value, map);
//        map.put(key, reference);
//    }
//
//    static class GarbageReference<K, V> extends WeakReference {
//        final K key;
//        final V value;
//        final ConcurrentMap<K, V> map;
//
//        GarbageReference(Object referent, K key, V value, ConcurrentMap<K, V> map) {
//            super(referent, referenceQueue);
//            this.key = key;
//            this.value = value;
//            this.map = map;
//        }
//    }
//
//    static class CleanupThread extends Thread {
//        CleanupThread() {
//            setPriority(Thread.MAX_PRIORITY);
//            setName("GarbageCollectingConcurrentMap-cleanupthread");
//            setDaemon(true);
//        }
//
//        public void run() {
//            while (true) {
//                try {
//                    GarbageReference ref = (GarbageReference) referenceQueue.remove();
//                    while (true) {
//                        ref.map.remove(ref.key);
//                        ref = (GarbageReference) referenceQueue.remove();
//                    }
//                } catch (InterruptedException e) {
//
//                }
//            }
//        }
//    }
//}