package jcog.data.map;

import java.util.Arrays;
import java.util.function.Supplier;

/** TODO not tested yet */
public class BucketBucketByteMap<V> implements LazyMap<Byte, V> {

    final Object[][][] map;
    private final V[] template;
    final byte superstride, stride;

    public BucketBucketByteMap(int superstride, int stride, V[] template) {
        this.template = template;
        this.stride = (byte) stride;
        this.superstride = (byte) superstride;
        //assert(template.length == 0);
        map = new Object[256 / (superstride * stride)][][];
    }

    @Override
    public V get(Object key) {
        return get((byte) key);
    }

    public V get(byte K) {
        int k = Byte.toUnsignedInt(K);
        Object[][] c = map[k / (stride * superstride)];
        if (c == null) return null;
        int kk = k % (superstride * stride);
        Object[] cc = c[kk / stride];
        return cc == null ? null : (V) cc[kk % stride];
    }

    @Override
    public final V computeIfAbsent(Byte K, Supplier<? extends V> f) {
        return col(f, Byte.toUnsignedInt(K));
    }


    private V col(Supplier<? extends V> f, int k) {
        V[] cc = row(k);
        int r = k % stride;
        V exist = cc[r];
        return exist != null ? exist : (cc[r] = f.get());
    }

    private V[] row(int k) {
        int ks = k / (stride * superstride);
        Object[][] c = map[ks];
        if (c == null)
            map[ks] = c = new Object[superstride][];

        int kks = (k - ks) % superstride;
        V[] cc = (V[]) c[kks];
        if (cc == null)
            c[kks] = cc = Arrays.copyOf(template, stride);
        return cc;
    }
}