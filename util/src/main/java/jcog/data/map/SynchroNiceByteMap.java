package jcog.data.map;

import jcog.Util;
import org.eclipse.collections.impl.map.mutable.primitive.ByteObjectHashMap;

import java.lang.invoke.VarHandle;
import java.util.function.Supplier;

/** sloppy but fast and wait free.
 *  untested
 * */
public class SynchroNiceByteMap<V> implements LazyMap<Byte,V> {

    final ByteObjectHashMap<V> map;

    public SynchroNiceByteMap() {
        this(0);
    }

    public SynchroNiceByteMap(int initialCap) {
        map = new ByteObjectHashMap<>(initialCap);
    }

    @Override
    public V get(Object key) {
        return map.get((byte)key);
    }

    @Override
    public V computeIfAbsent(Byte k, Supplier<? extends V> f) {
        if (busy()) {
            return f.get();
        } else {
            try {
                return map.getIfAbsentPut(k, f::get);
            } finally {
                available();
            }
        }
    }

    protected final void available() {
        BUSY.setVolatile(this, 0);
    }

    protected final boolean busy() {
        return !BUSY.compareAndSet(this, 0, 1);
    }

    @SuppressWarnings("CanBeFinal")
    private volatile int busy = 0;

    private static final VarHandle BUSY = Util.VAR(SynchroNiceByteMap.class, "busy", int.class);
}
