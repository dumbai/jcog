package jcog.data.map;

import java.util.Arrays;
import java.util.function.Supplier;

/** TODO softref/weakref version */
public abstract class BucketByteMap<X> implements LazyMap<Byte, X> {

    final Object[][] map;

    public BucketByteMap() {
        //assert(template.length == 0);
        map = new Object[256/stride()][];
    }

    abstract byte stride();

    @Override public void clear() {
        Arrays.fill(map, null);
    }

    @Override
    public X get(Object key) {
        return get((byte) key);
    }

    public X get(byte k) {
        int stride = stride();
        Object[] c = map[ Byte.toUnsignedInt(k) / stride];
        if (c == null) return null;
        return (X) c[k % stride];
    }

    @Override
    public X computeIfAbsent(Byte K, Supplier<? extends X> f) {
        int k = Byte.toUnsignedInt(K);
        int stride = stride();
        int ks = k / stride;
        Object[] c = map[ks];
        if (c == null)
            map[ks] = c = new Object[stride];

        int r = k % stride;
        Object exist = c[r];
        return (X) (exist != null ? exist : (c[r] = f.get()));
    }
}