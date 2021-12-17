package jcog.data.map;

import jcog.Texts;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ConcurrentHashmapBenchmark {

    final static int Iterations = 20;
    final static int ReadIterations = 20;
    final static int N = 128 * 1024;

    static void bench(Supplier<Map<Long, String>> m) {
        Map map = m.get();
        long start = System.nanoTime();

        for (long i = 0; i < Iterations; i++) {
            for (int j = 0; j < N; j++) {
                map.put((j+i)%N, "value");
            }

            for (long h = 0; h < ReadIterations; h++) {
                for (int j = 0; j < N; j++)
                    map.get(j);
            }

            for (int j = 0; j < N; j++) {
                map.remove((j+i)%N);
            }
        }

        assert(map.isEmpty());
        long end = System.nanoTime();

        System.out.println(map.getClass() + " " + Texts.timeStr(end - start));
    }


    public static void benchConcurrentOpenHashMap(int concurrency, float loadFactor) {
        bench(()->new ConcurrentOpenHashMap<>(N,  concurrency, loadFactor));
    }
    public static void benchConcurrentHashMap(float loadFactor, int concurrency) {
        bench(()->new ConcurrentHashMap<>(N, loadFactor, concurrency));
    }
    public static void benchCustomConcurrentHashMap() {
        bench(()->new CustomConcurrentHashMap<>(N));
    }

    static void benchHashMap(float loadFactor) {
        bench(()->new HashMap<>(N, loadFactor));
    }
    static void benchUnifiedMap(float loadFactor) {
        bench(()->new UnifiedMap<>(N, loadFactor));
    }
    static void benchNBHM() {
        bench(()->new NonBlockingHashMap<>(N));
    }

    public static void main(String[] args) {

        for (int i = 0; i < 4; i++) {
            benchHashMap(0.3f);
            benchHashMap(0.5f);
            benchHashMap(0.66f);
            benchHashMap(0.95f);
            benchUnifiedMap(0.3f);
            benchUnifiedMap(0.5f);
            benchUnifiedMap(0.66f);
            benchUnifiedMap(0.95f);

            benchNBHM();

            benchConcurrentHashMap(0.66f, 1);
            benchConcurrentHashMap(0.95f, 1);
//        benchConcurrentHashMap(0.66f, 8);
//        benchConcurrentHashMap(0.66f, 16);
            benchConcurrentOpenHashMap(1, 0.3f);
            benchConcurrentOpenHashMap(1, 0.666f);
            benchConcurrentOpenHashMap(1, 0.9f);

//        benchConcurrentOpenHashMap(8);
//        benchConcurrentOpenHashMap(16);
            benchCustomConcurrentHashMap();

            System.out.println();
        }
    }
}
