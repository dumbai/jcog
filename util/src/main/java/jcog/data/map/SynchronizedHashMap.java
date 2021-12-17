//package jcog.data.map;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.util.HashMap;
//import java.util.function.BiFunction;
//import java.util.function.Function;
//
///** synchronized in a few methods for NARS bag purposes.
// *
// *  should perform a little better than UnifiedMap, since it has the Java8 lambda compute() etc
// *  the downside is somewhat higher memory usage
// */
//public final class SynchronizedHashMap<K, V> extends HashMap<K, V> {
//
//    public SynchronizedHashMap() {
//        super();
//    }
//
//    public SynchronizedHashMap(int cap) {
//        super(cap);
//    }
//
//    public SynchronizedHashMap(int cap, float loadFactor) {
//        super(cap, loadFactor);
//    }
//
//    @Override
//    public V remove(@NotNull Object key) {
//        synchronized (this) {
//            return super.remove(key);
//        }
//    }
//
//    @Override
//    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
//        synchronized (this) {
//            return super.computeIfAbsent(key, mappingFunction);
//        }
//    }
//
//    @Override
//    public V compute(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
//        synchronized (this) {
//            return super.compute(key, remappingFunction);
//        }
//    }
//}
