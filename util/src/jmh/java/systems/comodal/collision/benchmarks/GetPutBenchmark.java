package systems.comodal.collision.benchmarks;

import com.github.benmanes.caffeine.cache.Cache;
import jcog.io.BinTxt;
import jcog.memoize.HijackMemoize;
import org.cache2k.Cache2kBuilder;
import systems.comodal.collision.cache.CollisionCache;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Threads(Threads.MAX)
@Fork(1)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 4, time = 1)
public class GetPutBenchmark {

    @Param({//"256", "1024",
            "16384"})
    private String size;

    private int SIZE, MASK;

    float uniqueFactor = 1.5f;

    @Param({
            "Hijack",
//            "Collision",
//            "Cache2k",
//            "Caffeine"
    })
    private CacheFactory cacheType;

    private GetPutCache<Object, Boolean> cache;
    private Object[] keys;

    @Setup
    public void setup() {
        SIZE = Integer.parseInt(size);
        MASK = SIZE - 1;

        int uniqueKeys = Math.round(SIZE * uniqueFactor);
        keys = new Object[uniqueKeys];

        final int capacity = SIZE;

        cache = cacheType.create(capacity);
        final ScrambledZipfGenerator generator = new ScrambledZipfGenerator(uniqueKeys);
        IntStream.range(0, uniqueKeys).forEach(i -> {
            final Long key = generator.nextValue();
            keys[i] = BinTxt.toString(key);
            cache.put(key, Boolean.TRUE);
        });
    }


    @TearDown
    public void stop() {
        System.out.println(cache);
        if (cacheType == CacheFactory.Hijack)
            System.out.println(cache.summary());
    }


    static final AtomicInteger iter = new AtomicInteger(0);

    @Benchmark
    public Boolean get() {
        return cache.get(keys[iter.getAndIncrement() & MASK]);
    }

    @Benchmark
    public Boolean put() {
        return cache.put(keys[iter.getAndIncrement() & MASK], Boolean.TRUE);
    }

    @Benchmark
    public Boolean putGet() {
        put();
        return get();
    }


    public enum CacheFactory {
        Cache2k {
            @Override
            @SuppressWarnings("unchecked")
            <K, V> GetPutCache<K, V> create(final int capacity) {
                final org.cache2k.Cache<K, V> cache = Cache2kBuilder
                        .forUnknownTypes()
                        .entryCapacity(capacity)
                        .disableStatistics(true)
                        .eternal(true)
                        .build();
                return new GetPutCache<>() {

                    @Override
                    public V get(final K key) {
                        return cache.peek(key);
                    }

                    @Override
                    public V put(final K key, final V val) {
                        cache.put(key, val);
                        return val;
                    }
                };
            }
        },
        Caffeine {
            @Override
            <K, V> GetPutCache<K, V> create(final int capacity) {
                final Cache<K, V> cache = com.github.benmanes.caffeine.cache.Caffeine
                        .newBuilder()
                        .initialCapacity(capacity)
                        .maximumSize(capacity)
                        .build();
                return new GetPutCache<>() {

                    @Override
                    public V get(final K key) {
                        return cache.getIfPresent(key);
                    }

                    @Override
                    public V put(final K key, final V val) {
                        cache.put(key, val);
                        return val;
                    }
                };
            }
        },
        Collision {
            @Override
            <K, V> GetPutCache<K, V> create(final int capacity) {
                final CollisionCache<K, V> cache = CollisionCache
                        .<V>withCapacity(capacity)
                        .setStrictCapacity(true)
                        .buildSparse();
                return new GetPutCache<>() {

                    @Override
                    public V get(final K key) {
                        return cache.getIfPresent(key);
                    }

                    @Override
                    public V put(final K key, final V val) {
                        return cache.putReplace(key, val);
                    }
                };
            }
        },
        Hijack {
            @Override
            <K, V> GetPutCache<K, V> create(int capacity) {

                int REPROBES = 3;

                Function insert = (key) -> {
                    return Boolean.TRUE; //HACK
                };

                final HijackMemoize<K, V> cache = new HijackMemoize<>(insert, capacity, REPROBES);
                return new GetPutCache<>() {

                    @Override
                    public V get(final K key) {
                        return cache.getIfPresent(key);
                    }

                    @Override
                    public V put(final K key, final V val) {
                        return cache.apply(key);
                    }

                    @Override
                    public String summary() {
                        return cache.summary();
                    }
                };
            }

        };

        abstract <K, V> GetPutCache<K, V> create(final int capacity);
    }

    private interface GetPutCache<K, V> {

        V get(final K key);

        V put(final K key, final V val);

        default String summary() {
            return "";
        }
    }
}
