package jcog.data.map;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.function.Function;
import java.util.function.Supplier;

/** minimal metadata map interface */
public interface MetaMap {
    /** Map.get */
    <X> X meta(String key);

    /** Map.put */
    <X> X meta(String key, Object value);

    /** Map.computeIfAbsent */
    <X> X meta(String key, Function<String, X> valueIfAbsent);

    default <X> X meta(String key, Supplier<X> valueIfAbsent) {
        return meta(key, (s)->valueIfAbsent.get());
    }

    default <X> X metaWeak(String key, boolean softOrWeak, Function<String,X> valueIfAbsent) {
        Reference<X> r = ((Reference<X>) (meta(key, (k) -> {
            X v = valueIfAbsent.apply(k);
            if (v != null)
                return softOrWeak ? new SoftReference(v) : new WeakReference(v);
            else
                return null;
        })));
        if (r == null)
            return null;
        return r.get();
    }

    default <X> X metaWeak(String key, boolean softOrWeak, Supplier<X> valueIfAbsent) {
        return metaWeak(key, softOrWeak, s->valueIfAbsent.get());
    }
}
