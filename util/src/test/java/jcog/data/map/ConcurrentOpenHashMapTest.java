package jcog.data.map;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
public class ConcurrentOpenHashMapTest {

    @Test
    public void testConstructor() {
        try {
            new ConcurrentOpenHashMap<String, String>(0);
            fail("should have thrown exception");
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            new ConcurrentOpenHashMap<String, String>(16, 0);
            fail("should have thrown exception");
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            new ConcurrentOpenHashMap<String, String>(4, 8);
            fail("should have thrown exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    @Test
    public void simpleInsertions() {
        ConcurrentOpenHashMap<String, String> map = new ConcurrentOpenHashMap<>(16);

        assertTrue(map.isEmpty());
        assertNull(map.put("1", "one"));
        assertFalse(map.isEmpty());

        assertNull(map.put("2", "two"));
        assertNull(map.put("3", "three"));

        assertEquals(3, map.size());

        assertEquals("one", map.get("1"));
        assertEquals(3, map.size());

        assertEquals("one", map.remove("1"));
        assertEquals(2, map.size());
        assertNull(map.get("1"));
        assertNull(map.get("5"));
        assertEquals(2, map.size());

        assertNull(map.put("1", "one"));
        assertEquals(3, map.size());
        assertEquals("one", map.put("1", "uno"));
        assertEquals(3, map.size());
    }

    @Test
    public void testRemove() {
        ConcurrentOpenHashMap<String, String> map = new ConcurrentOpenHashMap<>();

        assertTrue(map.isEmpty());
        assertNull(map.put("1", "one"));
        assertFalse(map.isEmpty());

        assertFalse(map.remove("0", "zero"));
        assertFalse(map.remove("1", "uno"));

        assertFalse(map.isEmpty());
        assertTrue(map.remove("1", "one"));
        assertTrue(map.isEmpty());
    }

    @Test
    public void testRehashing() {
        int n = 16;
        ConcurrentOpenHashMap<String, Integer> map = new ConcurrentOpenHashMap<>(n / 2, 1);
        assertEquals(n, map.capacity());
        assertEquals(0, map.size());

        for (int i = 0; i < n; i++) {
            map.put(Integer.toString(i), i);
        }

        assertEquals(map.capacity(), 2 * n);
        assertEquals(n, map.size());
    }

    @Test
    public void testRehashingWithDeletes() {
        int n = 16;
        ConcurrentOpenHashMap<Integer, Integer> map = new ConcurrentOpenHashMap<>(n / 2, 1);
        assertEquals(n, map.capacity());
        assertEquals(0, map.size());

        for (int i = 0; i < n / 2; i++) {
            map.put(i, i);
        }

        for (int i = 0; i < n / 2; i++) {
            map.remove(i);
        }

        for (int i = n; i < (2 * n); i++) {
            map.put(i, i);
        }

        assertEquals(map.capacity(), 2 * n);
        assertEquals(n, map.size());
    }

    @Test
    public void testConcurrentInsertionsOpenHashMapConcurrency1_2() {
        concurrentInsertions(new ConcurrentOpenHashMap<>(16, 1), 1);
    }
    @Test
    public void testConcurrentInsertionsOpenHashMapConcurrency1_16() {
        concurrentInsertions(new ConcurrentOpenHashMap<>(16, 1), 16);
    }
    @Test
    public void testConcurrentInsertionsOpenHashMapConcurrency4_16() {
        Map<Long, String> map = new ConcurrentOpenHashMap<>(16, 4);
        concurrentInsertions(map, 16);
    }

    @Test
    public void testConcurrentInsertionsCustomConcurrentHashMap() {
        Map<Long, String> map = new ConcurrentOpenHashMap<>(16, 1);
        concurrentInsertions(map, 16);
    }

    static void concurrentInsertions(Map<Long, String> map, int nThreads)  {
        ExecutorService executor = Executors.newCachedThreadPool();

        final int N = 100_000;
        String value = "value";

        Collection<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < nThreads; i++) {
            int threadIdx = i;
            Future<?> submit = executor.submit(() -> {
                Random random = new Random();

                for (int j = 0; j < N; j++) {
                    long key = random.nextLong();
                    // Ensure keys are uniques
                    key -= key % (threadIdx + 1);

                    map.put(key, value);
                }
            });
            futures.add(submit);
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                fail(e::toString);
            }
        }
        assertEquals(N * nThreads, map.size());

        executor.shutdown();
    }

    @Test
    void concurrentInsertionsAndReadsConcurrentOpenHashMap() {
        concurrentInsertionsAndReads(new ConcurrentOpenHashMap<>(), 16);
    }

    static void concurrentInsertionsAndReads(ConcurrentOpenHashMap<Long, String> map, int nThreads)  {

        ExecutorService executor = Executors.newCachedThreadPool();

        final int N = 100_000;
        String value = "value";

        Collection<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < nThreads; i++) {
            int threadIdx = i;
            Future<?> submit = executor.submit(() -> {
                Random random = new Random();

                for (int j = 0; j < N; j++) {
                    long key = random.nextLong();
                    // Ensure keys are uniques
                    key -= key % (threadIdx + 1);

                    map.put(key, value);
                }
            });
            futures.add(submit);
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                fail(e);
            }
        }

        assertEquals(N * nThreads, map.size());

        executor.shutdown();
    }

    @Test
    public void testIteration() {
        ConcurrentOpenHashMap<Long, String> map = new ConcurrentOpenHashMap<>();

        assertEquals(Collections.emptyList(), map.keys());
        assertEquals(Collections.emptyList(), map.values());

        map.put(0L, "zero");

        assertEquals(Lists.newArrayList(0L), map.keys());
        assertEquals(Lists.newArrayList("zero"), map.values());

        map.remove(0L);

        assertEquals(Collections.emptyList(), map.keys());
        assertEquals(Collections.emptyList(), map.values());

        map.put(0L, "zero");
        map.put(1L, "one");
        map.put(2L, "two");

        List<Long> keys = map.keys();
        keys.sort(null);
        assertEquals(Lists.newArrayList(0L, 1L, 2L), keys);

        List<String> values = map.values();
        values.sort(null);
        assertEquals(Lists.newArrayList("one", "two", "zero"), values);

        map.put(1L, "uno");

        keys = map.keys();
        keys.sort(null);
        assertEquals(Lists.newArrayList(0L, 1L, 2L), keys);

        values = map.values();
        values.sort(null);
        assertEquals(Lists.newArrayList("two", "uno", "zero"), values);

        map.clear();
        assertTrue(map.isEmpty());
    }

    @Test
    public void testHashConflictWithDeletion() {
        final int Buckets = 16;

        // Pick 2 keys that fall into the same bucket
        long key1 = 1;
        long key2 = 27;

        int bucket1 = ConcurrentOpenHashMap.signSafeMod(ConcurrentOpenHashMap.hash(key1), Buckets);
        int bucket2 = ConcurrentOpenHashMap.signSafeMod(ConcurrentOpenHashMap.hash(key2), Buckets);
        assertEquals(bucket1, bucket2);

        ConcurrentOpenHashMap<Long, String> map = new ConcurrentOpenHashMap<>(Buckets, 1);
        assertNull(map.put(key1, "value-1"));
        assertNull(map.put(key2, "value-2"));
        assertEquals(2, map.size());

        assertEquals("value-1", map.remove(key1));
        assertEquals(1, map.size());

        assertNull(map.put(key1, "value-1-overwrite"));
        assertEquals(2, map.size());

        assertEquals("value-1-overwrite", map.remove(key1));
        assertEquals(1, map.size());

        assertEquals("value-2", map.put(key2, "value-2-overwrite"));
        assertEquals("value-2-overwrite", map.get(key2));

        assertEquals(1, map.size());
        assertEquals("value-2-overwrite", map.remove(key2));
        assertTrue(map.isEmpty());
    }

    @Test
    public void testPutIfAbsent() {
        ConcurrentOpenHashMap<Long, String> map = new ConcurrentOpenHashMap<>();
        assertNull(map.putIfAbsent(1L, "one"));
        assertEquals("one", map.get(1L));

        assertEquals("one", map.putIfAbsent(1L, "uno"));
        assertEquals("one", map.get(1L));
    }

    @Test
    public void testComputeIfAbsent() {
        ConcurrentOpenHashMap<Integer, Integer> map = new ConcurrentOpenHashMap<>(16, 1);
        AtomicInteger counter = new AtomicInteger();
        UnaryOperator<Integer> provider = key -> counter.getAndIncrement();

        assertEquals(0, map.computeIfAbsent(0, provider).intValue());
        assertEquals(0, map.get(0).intValue());

        assertEquals(1, map.computeIfAbsent(1, provider).intValue());
        assertEquals(1, map.get(1).intValue());

        assertEquals(1, map.computeIfAbsent(1, provider).intValue());
        assertEquals(1, map.get(1).intValue());

        assertEquals(2, map.computeIfAbsent(2, provider).intValue());
        assertEquals(2, map.get(2).intValue());
    }

    @Test
    public void testEqualsKeys() {
        class T {
            int value;

            T(int value) {
                this.value = value;
            }

            @Override
            public int hashCode() {
                return Integer.hashCode(value);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof T) {
                    return value == ((T) obj).value;
                }

                return false;
            }
        }

        T t1 = new T(1);
        T t1_b = new T(1);
        T t2 = new T(2);

        assertEquals(t1, t1_b);
        assertNotEquals(t1, t2);
        assertNotEquals(t1_b, t2);

        ConcurrentOpenHashMap<T, String> map = new ConcurrentOpenHashMap<>();
        assertNull(map.put(t1, "t1"));
        assertEquals("t1", map.get(t1));
        assertEquals("t1", map.get(t1_b));
        assertNull(map.get(t2));

        assertEquals("t1", map.remove(t1_b));
        assertNull(map.get(t1));
        assertNull(map.get(t1_b));
    }




}