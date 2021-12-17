//package jcog.util;
//
//import com.google.common.collect.MapMaker;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.Iterator;
//import java.util.concurrent.ConcurrentMap;
//import java.util.function.Supplier;
//
///**
// * from: https:
// * Class extends ThreadLocal to enable user to iterate over all objects
// * held by the ThreadLocal instance.  Note that this is inherently not
// * thread-safe, and violates both the contract of ThreadLocal and much
// * of the benefit of using a ThreadLocal object.  This class incurs all
// * the overhead of a ConcurrentHashMap, perhaps you would prefer to
// * simply use a ConcurrentHashMap directly instead?
// *
// * If you do really want to use this class, be wary of its iterator.
// * While it is as threadsafe as ConcurrentHashMap's iterator, it cannot
// * guarantee that all existing objects in the ThreadLocal are available
// * to the iterator, and it cannot prevent you from doing dangerous
// * things with the returned values.  If the returned values are not
// * properly thread-safe, you will introduce issues.
// */
//public class IterableThreadLocal<T> extends ThreadLocal<T>
//                                    implements Iterable<T> {
//    private final ConcurrentMap<Thread,T> map;
//    private final @Nullable Supplier<? extends T> supplier;
//
//    public IterableThreadLocal() {
//        this(null);
//    }
//
//    public IterableThreadLocal(@Nullable Supplier<? extends T> supplier) {
//        super();
//        this.supplier = supplier;
//        map = new MapMaker().weakKeys().makeMap();
//    }
//
//    @Override
//    public T get() {
//        T val = super.get();
//        if (val == null) {
//            super.set(val = supplier.get());
//        }
//        map.putIfAbsent(Thread.currentThread(), val);
//        return val;
//    }
//
//    @Override
//    public void set(T value) {
//        map.put(Thread.currentThread(), value);
//        super.set(value);
//    }
//
//    /**
//     * Note that this method fundamentally violates the contract of
//     * ThreadLocal, and exposes all objects to the calling thread.
//     * Use with extreme caution, and preferably only when you know
//     * no other threads will be modifying / using their ThreadLocal
//     * references anymore.
//     */
//    @Override
//    public Iterator<T> iterator() {
//        return map.values().iterator();
//    }
//}