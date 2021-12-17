package jcog.memoize;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.function.Function;

import static jcog.Str.n2;

public class CaffeineMemoize<K, V> implements Memoize<K, V> {

    private final Cache<K, V> cache;
    private final Function<K, V> func;

    CaffeineMemoize(Cache<K, V> cache, Function<K, V> compute) {
        this.cache = cache;
        this.func = compute;
    }

    public static <K, V> CaffeineMemoize<K, V> build(Function<K, V> compute, int capacity, boolean stats) {
        Caffeine b = Caffeine.newBuilder();

        if (capacity < 1)
            b.softValues();
        else
            b.maximumSize(capacity);

        b.executor(MoreExecutors.directExecutor());

        if (stats)
            b.recordStats();
        return new CaffeineMemoize<K,V>(b.build(), compute);
    }

    @Override
    public V apply(K k) {
        return cache.get(k, func);
    }

    @Override
    public String summary() {
        CacheStats stats = cache.stats();
        String a;
		a = stats.hitCount() > 0 ? n2(stats.hitRate() * 100f) + "% hits, " : "";
        return a + cache.estimatedSize() + " size";
    }

    @Override
    public void clear() {
        cache.invalidateAll();
        cache.cleanUp();
    }
}
