package jcog.data.pool;

import jcog.data.list.Lst;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Simple object pool implemented by a Deque (ex: ArrayDeque)
 * guarded by a SpinMutex
 * no synchronizes; but should be thread safe
 */
public abstract class MetalPool<X> implements Pool<X> {

    protected final Lst<X> data;
    private int capacity;


    public MetalPool() {
        this(64, Integer.MAX_VALUE);
    }

    /** if maxCapacity==Integer.MAX_VALUE, capacity is not tested on inserts */
    public MetalPool(int initialCapacity, int maxCapacity) {
        data = new Lst<>(initialCapacity);
                //new ConcurrentLinkedDeque<>();

        capacity(maxCapacity);
    }

    /** note: ThreadLocal obviously doesnt require the thread-safe version */
    public static <X> ThreadLocal<MetalPool<X>> threadLocal(Supplier<X> o) {
        //noinspection Convert2Diamond
        return ThreadLocal.withInitial(() -> new MetalPool<X>() {
            @Override
            public X create() {
                return o.get();
            }
        });
    }

    public void prepare(int preallocate) {
        for (int i = 0; i < preallocate; i++)
            put(create());
    }


    public MetalPool<X> capacity(int c) {
        capacity = c;
        return this;
    }

    @Override
    public final void put(X i) {
        //assert (i != null);
        int c = this.capacity;
        if (c == Integer.MAX_VALUE || data.size() < c)
            data.add(i);
    }

    @Override
    public void delete() {
        capacity = 0;
        data.delete();
    }

    @Override
    public final X get() {
        X e = data.poll();
        return e == null ? create() : e;
    }


    public void putAll(Collection<X> c) {
        for (X x : c)
            put(x);
    }

    public int size() {
        return data.size();
    }


//    public void put(X[] items, int size) {
//        int c = this.capacity;
//        if (c != Integer.MAX_VALUE) {
//            size = c - data.size();
//            if (size <= 0)
//                return;
//        }
//
//        data.ensureCapacityForAdditional(size);
//        data.addFast(items, size);
//    }

}
