/*
 * Copyright (c) 2018 Goldman Sachs.
 * MODIFIED BY FRACTIONAL RESERVE USURY SCAM
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package jcog.data.map;

import org.eclipse.collections.api.block.function.Function0;
import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.block.predicate.Predicate2;
import org.eclipse.collections.api.block.procedure.Procedure;
import org.eclipse.collections.api.block.procedure.Procedure2;
import org.eclipse.collections.api.block.procedure.primitive.ObjectIntProcedure;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.UnsortedMapIterable;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.block.factory.Functions;
import org.eclipse.collections.impl.block.factory.Predicates;
import org.eclipse.collections.impl.block.procedure.MapCollectProcedure;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.AbstractMutableMap;
import org.eclipse.collections.impl.parallel.BatchIterable;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.tuple.ImmutableEntry;
import org.eclipse.collections.impl.tuple.Tuples;
import org.eclipse.collections.impl.utility.ArrayIterate;
import org.eclipse.collections.impl.utility.Iterate;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * PATCHED with customizations
 * <p>
 * UnifiedMap stores key/value pairs in a single array, where alternate slots are keys and values. This is nicer to CPU caches as
 * consecutive memory addresses are very cheap to access. Entry objects are not stored in the table like in java.util.HashMap.
 * Instead of trying to deal with collisions in the main array using Entry objects, we put a special object in
 * the key slot and put a regular Object[] in the value slot. The array contains the key value pairs in consecutive slots,
 * just like the main array, but it's a linear list with no hashing.
 * <p>
 * The final result is a Map implementation that's leaner than java.util.HashMap and faster than Trove's THashMap.
 * The best of both approaches unified together, and thus the name UnifiedMap.
 */

@SuppressWarnings("ObjectEquality")
public class UnifriedMap<K, V> extends AbstractMutableMap<K, V>
        implements Externalizable, BatchIterable<V> {


    private static final Object CHAINED_KEY = new Object() {
        @Override
        public boolean equals(Object obj) {
            throw new RuntimeException("Possible corruption through unsynchronized concurrent modification.");
        }

        @Override
        public int hashCode() {
            throw new RuntimeException("Possible corruption through unsynchronized concurrent modification.");
        }

        @Override
        public String toString() {
            return "UnifiedMap.CHAINED_KEY";
        }
    };

    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private static final int DEFAULT_INITIAL_CAPACITY = 4;


    private transient Object[] t;

    private transient int size;

    private float loadFactor = DEFAULT_LOAD_FACTOR;

    private int maxSize;

    public UnifriedMap() {
        this.allocate(DEFAULT_INITIAL_CAPACITY);
    }

    public UnifriedMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public UnifriedMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException("initial capacity cannot be less than 0");
        if (loadFactor <= 0.0) throw new IllegalArgumentException("load factor cannot be less than or equal to 0");
        if (loadFactor > 1.0) throw new IllegalArgumentException("load factor cannot be greater than 1");

        this.loadFactor = loadFactor;
        this.init(UnifriedMap.fastCeil(initialCapacity / loadFactor));
    }

    public UnifriedMap(Map<? extends K, ? extends V> map) {
        this(Math.max(map.size(), DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);

        this.putAll(map);
    }

    public UnifriedMap(Pair<K, V>[] pairs) {
        this(Math.max(pairs.length, DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
        ArrayIterate.forEach(pairs, new MapCollectProcedure<Pair<K, V>, K, V>(
                this,
                Functions.firstOfPair(),
                Functions.secondOfPair()));
    }

    public static <K, V> UnifriedMap<K, V> newMap() {
        return new UnifriedMap<>();
    }

    public static <K, V> UnifriedMap<K, V> newMap(int size) {
        return new UnifriedMap<>(size);
    }

    public static <K, V> UnifriedMap<K, V> newMap(int size, float loadFactor) {
        return new UnifriedMap<>(size, loadFactor);
    }

    private static int fastCeil(float x) {
        int y = (int) x;
        return x - y > 0.0F ? y + 1 : y;
    }

    private static int chainedHashCode(Object[] chain) {
        int hashCode = 0;
        int cl = chain.length;
        for (int i = 0; i < cl; i += 2) {
            Object cur = chain[i];
            if (cur == null) return hashCode;
            Object value = chain[i + 1];
            hashCode += cur.hashCode() ^ (value == null ? 0 : value.hashCode());
        }
        return hashCode;
    }


    @Override
    public UnifriedMap<K, V> clone() {
        return new UnifriedMap<>(this);
    }

    @Override
    public MutableMap<K, V> newEmpty() {
        return new UnifriedMap<>();
    }

    @Override
    public MutableMap<K, V> newEmpty(int capacity) {
        return new UnifriedMap<>(capacity, this.loadFactor);
    }

    void init(int initialCapacity) {
        int capacity = 1;
        while (capacity < initialCapacity) capacity <<= 1;

        this.allocate(capacity);
    }

    void allocate(int capacity) {

        // the table size is twice the capacity to handle both keys and values
        int next = capacity << 1;
        if (t == null || t.length != next) {
            this.t = new Object[next];
            this.maxSize = Math.min(capacity - 1, (int) (capacity * this.loadFactor));
        }

    }

    int index(Object key) {
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        int h = key == null ? 0 : key.hashCode();
        h ^= h >>> 20 ^ h >>> 12;
        h ^= h >>> 7 ^ h >>> 4;
        return (h & (this.t.length >> 1) - 1) << 1;
    }

    @Override
    public void clear() {
        if (this.size == 0) return;
        this.size = 0;

        Object[] set = this.t;
        allocate(DEFAULT_INITIAL_CAPACITY);

        Arrays.fill(set, null);
    }

    @Override
    public V put(K key, V value) {
        int index = this.index(key);
        Object[] t = this.t;
        Object cur = t[index];
        if (cur == null) {
            rehashGrow(index, key, value, t);
            return null;
        } else if (cur != CHAINED_KEY && cur.equals(key)) {
            Object result = t[index + 1];
            t[index + 1] = value;
            return (V)result;
        } else
            return this.chainedPut(key, index, value);
    }

    private V chainedPut(K key, int index, V value) {
        if (this.t[index] == CHAINED_KEY) {
            Object[] chain = (Object[]) this.t[index + 1];
            int cl = chain.length;
            for (int i = 0; i < cl; i += 2) {
                if (chain[i] == null) {
                    rehashGrow(i, key, value, chain);
                    return null;
                } else if (chain[i].equals(key)) {
                    V result = (V) chain[i + 1];
                    chain[i + 1] = value;
                    return result;
                }
            }
            Object[] newChain = new Object[cl + 4];
            System.arraycopy(chain, 0, newChain, 0, cl);
            this.t[index + 1] = newChain;
            rehashGrow(cl, key, value, newChain);
        } else {
            chainingGrow(key, index, value);
        }
        return null;
    }

    private void rehashGrow() {
        if (++this.size > this.maxSize) {
            int oldLength = this.t.length;
            Object[] old = this.t;
            this.allocate(this.t.length);
            this.size = 0;

            for (int i = 0; i < oldLength; i += 2) {
                Object cur = old[i];
                if (cur == CHAINED_KEY) {
                    Object[] chain = (Object[]) old[i + 1];
                    int cl = chain.length;
                    for (int j = 0; j < cl; j += 2)
                        if (chain[j] != null)
                            this.put((K) chain[j], (V) chain[j + 1]);
                } else
                    if (cur != null)
                        this.put((K) cur, (V) old[i + 1]);
            }
        }
    }

    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public V updateValue(K key, Function0<? extends V> factory, Function<? super V, ? extends V> function) {
        int index = this.index(key);
        Object[] t = this.t;
        Object cur = t[index];
        if (cur == null) {
            V result = function.apply(factory.value());
            t[index] = key;
            t[index + 1] = result;
            ++this.size;
            return result;
        }
        if (cur != CHAINED_KEY && cur.equals(key)) {
            V newValue = function.apply((V) t[index + 1]);
            t[index + 1] = newValue;
            return newValue;
        }
        return this.chainedUpdateValue(key, index, factory, function);
    }

    private V chainedUpdateValue(K key, int index, Function0<? extends V> factory, Function<? super V, ? extends V> function) {
        if (this.t[index] == CHAINED_KEY) {
            Object[] chain = (Object[]) this.t[index + 1];
            for (int i = 0; i < chain.length; i += 2) {
                Object ci = chain[i];
                if (ci == null) {
                    V result = function.apply(factory.value());
                    rehashGrow(i, key, result, chain);
                    return result;
                }
                if (ci.equals(key)) {
                    V result = function.apply((V) chain[i + 1]);
                    chain[i + 1] = result;
                    return result;
                }
            }
            Object[] newChain = new Object[chain.length + 4];
            System.arraycopy(chain, 0, newChain, 0, chain.length);
            this.t[index + 1] = newChain;
            newChain[chain.length] = key;
            V result = function.apply(factory.value());
            newChain[chain.length + 1] = result;
            rehashGrow();
            return result;
        }
        V result = function.apply(factory.value());
        chainingGrow(key, index, result);
        return result;
    }

    @Override
    public <P> V updateValueWith(K key, Function0<? extends V> factory, Function2<? super V, ? super P, ? extends V> function, P parameter) {
        int index = this.index(key);
        Object[] t = this.t;
        Object cur = t[index];
        if (cur == null) {
            V result = function.value(factory.value(), parameter);
            t[index] = key;
            t[index + 1] = result;
            ++this.size;
            return result;
        }
        if (cur != CHAINED_KEY && cur.equals(key)) {
            V newValue = function.value((V) t[index + 1], parameter);
            t[index + 1] = newValue;
            return newValue;
        }
        return this.chainedUpdateValueWith(key, index, factory, function, parameter);
    }

    private <P> V chainedUpdateValueWith(
            K key,
            int index,
            Function0<? extends V> factory,
            Function2<? super V, ? super P, ? extends V> function,
            P parameter) {
        if (this.t[index] == CHAINED_KEY) {
            Object[] chain = (Object[]) this.t[index + 1];
            int cl = chain.length;
            for (int i = 0; i < cl; i += 2) {
                if (chain[i] == null) {
                    V result = function.value(factory.value(), parameter);
                    rehashGrow(i, key, result, chain);
                    return result;
                }
                if (chain[i].equals(key)) {
                    V result = function.value((V) chain[i + 1], parameter);
                    chain[i + 1] = result;
                    return result;
                }
            }
            Object[] newChain = new Object[cl + 4];
            System.arraycopy(chain, 0, newChain, 0, cl);
            this.t[index + 1] = newChain;
            newChain[cl] = key;
            V result = function.value(factory.value(), parameter);
            newChain[cl + 1] = result;
            rehashGrow();
            return result;
        }

        V result = function.value(factory.value(), parameter);
        chainingGrow(key, index, result);
        return result;
    }


    public final V computeIfAbsent(K key, Function<? super K, ? extends V> f) {
        int index = this.index(key);
        Object[] t = this.t;
        Object cur = t[index];
        if (cur == null) {
            V result = f.apply(key);
            rehashGrow(index, key, result, t);
            return result;
        } else {
            return (V) (cur != CHAINED_KEY && cur.equals(key) ?
                t[index + 1] :
                this.chainedGetIfAbsentPutWith(key, index, f, key));
        }
    }

    public void rehashGrow(int index, K k, Object/*V*/ v, Object[] t) {
        t[index] = k;
        t[index + 1] = v;
        rehashGrow();
    }


    @Override
    public V getIfAbsentPut(K key, Function0<? extends V> function) {
        int index = this.index(key);
        Object cur = this.t[index];

        if (cur == null) {
            V result = function.value();
            rehashGrow(index, key, result, this.t);
            return result;
        }
        return cur != CHAINED_KEY && cur.equals(key) ?
                (V) this.t[index + 1] :
                this.chainedGetIfAbsentPut(key, index, function);
    }

    private V chainedGetIfAbsentPut(K key, int index, Function0<? extends V> function) {
        V result = null;
        if (this.t[index] == CHAINED_KEY) {
            Object[] chain = (Object[]) this.t[index + 1];
            int i = 0;
            for (; i < chain.length; i += 2) {
                if (chain[i] == null) {
                    result = function.value();
                    rehashGrow(i, key, result, chain);
                    break;
                }
                if (chain[i].equals(key)) {
                    result = (V) chain[i + 1];
                    break;
                }
            }
            if (i == chain.length) {
                result = function.value();
                Object[] newChain = new Object[chain.length + 4];
                System.arraycopy(chain, 0, newChain, 0, chain.length);
                newChain[i] = key;
                newChain[i + 1] = result;
                this.t[index + 1] = newChain;
                rehashGrow();
            }
        } else {
            result = function.value();
            chainingGrow(key, index, result);
        }
        return result;
    }

    private void chainingGrow(K key, int index, Object/*V*/ result) {
        chaining(key, index, result);
        rehashGrow();
    }

    private void chaining(K key, int index, Object/*V*/ value) {
        Object[] t = this.t;
        Object a = t[index], b = t[index + 1];
        t[index] = CHAINED_KEY;
        t[index + 1] = new Object[] {
            a, b, key, value
        };
    }

    @Nullable
    @Override
    public V putIfAbsent(K key, V value) {
        return putIfAbsent(key, value, false);
    }

    @Override
    public V getIfAbsentPut(K key, V value) {
        return putIfAbsent(key, value, true);
    }

    private V putIfAbsent(K key, V value, boolean get) {
        int index = index(key);
        Object[] t = this.t;
        Object cur = t[index];
        if (cur == null) {
            rehashGrow(index, key, value, t);
            return get ? value : null;
        } else {
            return cur != CHAINED_KEY && cur.equals(key) ?
                    (V) t[index + 1] :
                    chainedGetIfAbsentPut(key, index, value);
        }
    }

    private V chainedGetIfAbsentPut(K key, int index, V value) {
        V result = value;
        if (this.t[index] == CHAINED_KEY) {
            Object[] chain = (Object[]) this.t[index + 1];
            int i = 0;
            int l = chain.length;
            for (; i < l; i += 2) {
                if (chain[i] == null) {
                    rehashGrow(i, key, value, chain);
                    break;
                }
                if (chain[i].equals(key)) {
                    result = (V) chain[i + 1];
                    break;
                }
            }
            if (i == l) {
                Object[] newChain = new Object[l + 4];
                System.arraycopy(chain, 0, newChain, 0, l);
                newChain[i] = key;
                newChain[i + 1] = value;
                t[index + 1] = newChain;
                rehashGrow();
            }
        } else {
            chainingGrow(key, index, value);
        }
        return result;
    }


    private <P> Object chainedGetIfAbsentPutWith(K key, int index, Function<? super P, ? extends V> function, P parameter) {
        Object result = null;
        if (this.t[index] == CHAINED_KEY) {
            Object[] chain = (Object[]) this.t[index + 1];
            int i = 0;
            int l = chain.length;
            for (; i < l; i += 2) {
                Object ci = chain[i];
                if (ci == null) {
                    rehashGrow(i, key, result = function.apply(parameter), chain);
                    break;
                } else if (ci.equals(key)) {
                    result = chain[i + 1];
                    break;
                }
            }
            if (i == l) {
                result = function.apply(parameter);
                Object[] newChain = new Object[l + 4];
                System.arraycopy(chain, 0, newChain, 0, l);
                newChain[i] = key;
                newChain[i + 1] = result;
                this.t[index + 1] = newChain;
                rehashGrow();
            }
        } else {
            chainingGrow(key, index, result = function.apply(parameter));
        }
        return result;
    }

//    public int getCollidingBuckets() {
//        int count = 0;
//        final Object[] t = this.t;
//        final int l = t.length;
//        for (int i = 0; i < l; i += 2)
//            if (t[i] == CHAINED_KEY) count++;
//        return count;
//    }

//    /**
//     * Returns the number of JVM words that is used by this map. A word is 4 bytes in a 32bit VM and 8 bytes in a 64bit
//     * VM. Each array has a 2 word header, thus the formula is:
//     * words = (internal table length + 2) + sum (for all chains (chain length + 2))
//     *
//     * @return the number of JVM words that is used by this map.
//     */
//    public int getMapMemoryUsedInWords() {
//        int headerSize = 2;
//        int sizeInWords = this.t.length + headerSize;
//        for (int i = 0; i < this.t.length; i += 2)
//            if (this.t[i] == CHAINED_KEY) sizeInWords += headerSize + ((Object[]) this.t[i + 1]).length;
//        return sizeInWords;
//    }

    @Override
    public final V get(Object key) {
        int index = this.index(key);
        Object[] t = this.t;
        Object cur = t[index];
        if (cur != null) {
            Object val = t[index + 1];
            if (cur == CHAINED_KEY) return (V)this.getFromChain((Object[]) val, (K) key);
            if (cur.equals(key)) return (V)val;
        }
        return null;
    }

    static private Object getFromChain(Object[] chain, Object key) {
        int l = chain.length;
        for (int i = 0; i < l; i += 2) {
            Object k = chain[i];
            if (k == null) return null;
            else if (k.equals(key)) return chain[i + 1];
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        int index = this.index(key);
        Object[] t = this.t;
        Object cur = t[index];
        if (cur == null)
            return false;
        else if (cur != CHAINED_KEY)
            return cur.equals(key);
        else
            return chainContains((Object[]) t[index + 1], key);
    }

    private static boolean chainContains(Object[] chain, Object key) {
        int l = chain.length;
        for (int i = 0; i < l; i += 2) {
            Object k = chain[i];
            if (k == null) return false;
            else if (k.equals(key)) return true;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        Object[] t = this.t;
        int l = t.length;
        for (int i = 0; i < l; i += 2)
            if (t[i] == CHAINED_KEY) {
                if (chainContains((Object[]) t[i + 1], value)) return true;
            } else if (t[i] != null && value.equals(t[i + 1]))
                return true;
        return false;
    }



    @Override
    public void forEach(BiConsumer<? super K, ? super V> procedure) {
        Object[] t = this.t;
        int l = t.length;
        for (int i = 0; i < l; i += 2) {
            Object cur = t[i];
            if (cur == CHAINED_KEY) this.chainedForEachEntry((Object[]) t[i + 1], procedure);
            else if (cur != null) procedure.accept((K) cur, (V) t[i + 1]);
        }
    }

    @Override
    public final void forEachKeyValue(Procedure2<? super K, ? super V> procedure) {
        forEach(procedure);
    }

    @Override
    public V getFirst() {
        Object[] t = this.t;
        int l = t.length;
        for (int i = 0; i < l; i += 2) {
            Object cur = t[i];
            if (cur == CHAINED_KEY)
                return (V) ((Object[]) t[i + 1])[1];
            else if (cur != null)
                return (V) t[i + 1];
        }
        return null;
    }

    @Override
    public <E> MutableMap<K, V> collectKeysAndValues(
            Iterable<E> iterable,
            org.eclipse.collections.api.block.function.Function<? super E, ? extends K> keyFunction,
            org.eclipse.collections.api.block.function.Function<? super E, ? extends V> valueFunction) {
        Iterate.forEach(iterable, new MapCollectProcedure<>(this, keyFunction, valueFunction));
        return this;
    }

    @Override
    public V removeKey(K key) {
        return this.remove(key);
    }

    @Override
    public boolean removeIf(Predicate2<? super K, ? super V> predicate) {
        int previousOccupied = this.size;
        for (int index = 0; index < this.t.length; index += 2) {
            Object cur = this.t[index];
            if (cur == CHAINED_KEY) {
                Object[] chain = (Object[]) this.t[index + 1];
                for (int chIndex = 0; chIndex < chain.length; ) {
                    if (chain[chIndex] == null) break;
                    K key = (K) chain[chIndex];
                    V value = (V) chain[chIndex + 1];
                    if (predicate.accept(key, value))
                        this.overwriteWithLastElementFromChain(chain, index, chIndex);
                    else chIndex += 2;
                }
            } else if (cur!=null) {
                K key = (K) cur;
                V value = (V) this.t[index + 1];
                if (predicate.accept(key, value)) {
                    this.t[index] = null;
                    this.t[index + 1] = null;
                    this.size--;
                }
            }
        }
        return previousOccupied > this.size;
    }

    private static void chainedForEachEntry(Object[] chain, BiConsumer procedure) {
        int l = chain.length;
        for (int i = 0; i < l; i += 2) {
            Object cur = chain[i];
            if (cur == null) return;
            procedure.accept(cur, chain[i + 1]);
        }
    }

    @Override
    public int getBatchCount(int batchSize) {
        return Math.max(1, this.t.length / 2 / batchSize);
    }

    @Override
    public void batchForEach(Procedure<? super V> procedure, int sectionIndex, int sectionCount) {
        int sectionSize = this.t.length / sectionCount;
        int start = sectionIndex * sectionSize;
        int end = sectionIndex == sectionCount - 1 ? this.t.length : start + sectionSize;
        if (start % 2 == 0) start++;
        for (int i = start; i < end; i += 2) {
            Object value = this.t[i];
            if (value instanceof Object[]) this.chainedForEachValue((Object[]) value, procedure);
            else if (value != null || this.t[i - 1] != null) procedure.value((V) value);
        }
    }

    @Override
    public void forEachKey(Procedure<? super K> procedure) {
        for (int i = 0; i < this.t.length; i += 2) {
            Object cur = this.t[i];
            if (cur == CHAINED_KEY) this.chainedForEachKey((Object[]) this.t[i + 1], procedure);
            else if (cur != null) procedure.value((K) cur);
        }
    }

    private void chainedForEachKey(Object[] chain, Procedure<? super K> procedure) {
        for (int i = 0; i < chain.length; i += 2) {
            Object cur = chain[i];
            if (cur == null) return;
            procedure.value((K) cur);
        }
    }

    @Override
    public void forEachValue(Procedure<? super V> procedure) {
        for (int i = 0; i < this.t.length; i += 2) {
            Object cur = this.t[i];
            if (cur == CHAINED_KEY) this.chainedForEachValue((Object[]) this.t[i + 1], procedure);
            else if (cur != null) procedure.value((V) this.t[i + 1]);
        }
    }

    private void chainedForEachValue(Object[] chain, Procedure procedure) {
        for (int i = 0; i < chain.length; i += 2) {
            Object cur = chain[i];
            if (cur == null) return;
            procedure.value(chain[i + 1]);
        }
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        if (map instanceof UnifriedMap<?, ?>) this.copyMap((UnifriedMap<K, V>) map);
        else if (map instanceof UnsortedMapIterable) {
            MapIterable<K, V> mapIterable = (MapIterable<K, V>) map;
            mapIterable.forEachKeyValue(this::put);
        } else for (Entry<? extends K, ? extends V> entry : map.entrySet()) this.put(entry.getKey(), entry.getValue());
    }

    void copyMap(UnifriedMap<K, V> unifiedMap) {
        Object[] t = unifiedMap.t;
        for (int i = 0; i < t.length; i += 2) {
            Object cur = t[i];
            if (cur == CHAINED_KEY) this.copyChain((Object[]) t[i + 1]);
            else if (cur != null) this.put((K) cur, (V) t[i + 1]);
        }
    }

    private void copyChain(Object[] chain) {
        int cl = chain.length;
        for (int j = 0; j < cl; j += 2) {
            Object cur = chain[j];
            if (cur == null) break;
            this.put((K) cur, (V) chain[j + 1]);
        }
    }

    @Override
    public V remove(Object key) {
        int index = this.index(key);
        Object[] t = this.t;
        Object cur = t[index];
        if (cur != null) {
            Object val = t[index + 1];
            if (cur == CHAINED_KEY)
                return this.removeFromChain((Object[]) val, (K) key, index);
            if (cur.equals(key)) {
                t[index] = null;
                t[index + 1] = null;
                this.size--;
                return (V) val;
            }
        }
        return null;
    }

    private V removeFromChain(Object[] chain, K key, int index) {
        int cl = chain.length;
        for (int i = 0; i < cl; i += 2) {
            Object k = chain[i];
            if (k == null) return null;
            else if (k.equals(key)) {
                Object val = chain[i + 1];
                this.overwriteWithLastElementFromChain(chain, index, i);
                return (V)val;
            }
        }
        return null;
    }

    private void overwriteWithLastElementFromChain(Object[] chain, int index, int i) {
        int j = chain.length - 2;
        for (; j > i; j -= 2)
            if (chain[j] != null) {
                chain[i] = chain[j];
                chain[i + 1] = chain[j + 1];
                break;
            }
        chain[j] = null;
        chain[j + 1] = null;
        if (j == 0) {
            t[index] = null;
            t[index + 1] = null;
        }
        this.size--;
    }

    @Override
    public final int size() {
        return this.size;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    @Override
    public Collection<V> values() {
        return new ValuesCollection();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;

        if (!(object instanceof Map<?, ?> other)) return false;

        if (this.size() != other.size()) return false;

        Object[] t = this.t;
        for (int i = 0; i < t.length; i += 2) {
            Object cur = t[i];
            if (cur == CHAINED_KEY) {
                if (!chainedEquals((Object[]) t[i + 1], other)) return false;
            } else if (cur != null) {
                Object otherValue = other.get(cur);
                if (otherValue==null || !otherValue.equals(t[i + 1]))
                    return false;
            }
        }

        return true;
    }

    private static boolean chainedEquals(Object[] chain, Map<?, ?> other) {
        for (int i = 0; i < chain.length; i += 2) {
            Object cur = chain[i];
            if (cur == null) return true;
            if (!other.get(cur).equals(chain[i + 1]))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (int i = 0; i < this.t.length; i += 2) {
            Object cur = this.t[i];
            if (cur == CHAINED_KEY) hashCode += UnifriedMap.chainedHashCode((Object[]) this.t[i + 1]);
            else if (cur != null) {
                Object value = this.t[i + 1];
                hashCode += cur.hashCode() ^ (value == null ? 0 : value.hashCode());
            }
        }
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('{');

        this.forEachKeyValue(new Procedure2<>() {
            private boolean first = true;

            public void value(K key, V value) {
                if (this.first) this.first = false;
                else builder.append(", ");

                builder.append(key == UnifriedMap.this ? "(this Map)" : key);
                builder.append('=');
                builder.append(value == UnifriedMap.this ? "(this Map)" : value);
            }
        });

        builder.append('}');
        return builder.toString();
    }

    public boolean trimToSize() {
        if (this.t.length <= UnifriedMap.fastCeil(this.size / this.loadFactor) << 2) return false;

        Object[] temp = this.t;
        this.init(UnifriedMap.fastCeil(this.size / this.loadFactor));
        if (this.isEmpty()) return true;

        int mask = this.t.length - 1;
        for (int j = 0; j < temp.length; j += 2) {
            Object key = temp[j];
            if (key == CHAINED_KEY) {
                Object[] chain = (Object[]) temp[j + 1];
                for (int i = 0; i < chain.length; i += 2) {
                    Object cur = chain[i];
                    if (cur != null) this.putForTrim((K) cur, (V) chain[i + 1], j, mask);
                }
            } else if (key != null) this.putForTrim((K) key, (V) temp[j + 1], j, mask);
        }
        return true;
    }

    private void putForTrim(K key, V value, int oldIndex, int mask) {
        int index = oldIndex & mask;
        Object cur = this.t[index];
        if (cur == null) {
            this.t[index] = key;
            this.t[index + 1] = value;
            return;
        }
        this.chainedPutForTrim(key, index, value);
    }

    private void chainedPutForTrim(K key, int index, V value) {
        if (this.t[index] == CHAINED_KEY) {
            Object[] chain = (Object[]) this.t[index + 1];
            for (int i = 0; i < chain.length; i += 2)
                if (chain[i] == null) {
                    chain[i] = key;
                    chain[i + 1] = value;
                    return;
                }
            Object[] newChain = new Object[chain.length + 4];
            System.arraycopy(chain, 0, newChain, 0, chain.length);
            this.t[index + 1] = newChain;
            newChain[chain.length] = key;
            newChain[chain.length + 1] = value;
            return;
        }
        chaining(key, index, value);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        this.loadFactor = in.readFloat();
        this.init(Math.max(
                (int) (size / this.loadFactor) + 1,
                DEFAULT_INITIAL_CAPACITY));
        for (int i = 0; i < size; i++) this.put((K) in.readObject(), (V) in.readObject());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(this.size());
        out.writeFloat(this.loadFactor);
        for (int i = 0; i < this.t.length; i += 2) {
            Object o = this.t[i];
            if (o != null) if (o == CHAINED_KEY) UnifriedMap.writeExternalChain(out, (Object[]) this.t[i + 1]);
            else {
                out.writeObject(o);
                out.writeObject(this.t[i + 1]);
            }
        }
    }

    private static void writeExternalChain(ObjectOutput out, Object[] chain) throws IOException {
        for (int i = 0; i < chain.length; i += 2) {
            Object cur = chain[i];
            if (cur == null) return;
            out.writeObject(cur);
            out.writeObject(chain[i + 1]);
        }
    }

    @Override
    public void forEachWithIndex(ObjectIntProcedure<? super V> objectIntProcedure) {
        int index = 0;
        for (int i = 0; i < this.t.length; i += 2) {
            Object cur = this.t[i];
            if (cur == CHAINED_KEY)
                index = this.chainedForEachValueWithIndex((Object[]) this.t[i + 1], objectIntProcedure, index);
            else if (cur != null) objectIntProcedure.value((V) this.t[i + 1], index++);
        }
    }

    private int chainedForEachValueWithIndex(Object[] chain, ObjectIntProcedure<? super V> objectIntProcedure, int index) {
        for (int i = 0; i < chain.length; i += 2) {
            Object cur = chain[i];
            if (cur == null) return index;
            objectIntProcedure.value((V) chain[i + 1], index++);
        }
        return index;
    }

    @Override
    public <P> void forEachWith(Procedure2<? super V, ? super P> procedure, P parameter) {
        for (int i = 0; i < this.t.length; i += 2) {
            Object cur = this.t[i];
            if (cur == CHAINED_KEY) this.chainedForEachValueWith((Object[]) this.t[i + 1], procedure, parameter);
            else if (cur != null) procedure.value((V) this.t[i + 1], parameter);
        }
    }

    private <P> void chainedForEachValueWith(
            Object[] chain,
            Procedure2<? super V, ? super P> procedure,
            P parameter) {
        for (int i = 0; i < chain.length; i += 2) {
            Object cur = chain[i];
            if (cur == null) return;
            procedure.value((V) chain[i + 1], parameter);
        }
    }

    @Override
    public <R> MutableMap<K, R> collectValues(Function2<? super K, ? super V, ? extends R> function) {
        UnifriedMap<K, R> target = (UnifriedMap<K, R>) this.newEmpty();
        target.loadFactor = this.loadFactor;
        target.size = this.size;
        target.allocate(this.t.length >> 1);

        for (int i = 0; i < target.t.length; i += 2) {
            target.t[i] = this.t[i];

            if (this.t[i] == CHAINED_KEY) {
                Object[] chainedTable = (Object[]) this.t[i + 1];
                Object[] chainedTargetTable = new Object[chainedTable.length];
                for (int j = 0; j < chainedTargetTable.length; j += 2)
                    if (chainedTable[j] != null) {
                        chainedTargetTable[j] = chainedTable[j];
                        chainedTargetTable[j + 1] = function.value((K) chainedTable[j], (V) chainedTable[j + 1]);
                    }
                target.t[i + 1] = chainedTargetTable;
            } else if (this.t[i] != null)
                target.t[i + 1] = function.value((K) this.t[i], (V) this.t[i + 1]);
        }

        return target;
    }

    @Override
    public Pair<K, V> detect(Predicate2<? super K, ? super V> predicate) {
        for (int i = 0; i < this.t.length; i += 2)
            if (this.t[i] == CHAINED_KEY) {
                Object[] chainedTable = (Object[]) this.t[i + 1];
                for (int j = 0; j < chainedTable.length; j += 2)
                    if (chainedTable[j] != null) {
                        K key = (K) chainedTable[j];
                        V value = (V) chainedTable[j + 1];
                        if (predicate.accept(key, value)) return Tuples.pair(key, value);
                    }
            } else if (this.t[i] != null) {
                K key = (K) this.t[i];
                V value = (V) this.t[i + 1];

                if (predicate.accept(key, value)) return Tuples.pair(key, value);
            }

        return null;
    }



    private boolean shortCircuit(
            Predicate/*<? super V>*/ predicate,
            boolean expected,
            boolean onShortCircuit,
            boolean atEnd) {
        Object[] t = this.t;
        for (int i = 0; i < t.length; i += 2) {
            if (t[i] == CHAINED_KEY) {
                Object[] chainedTable = (Object[]) t[i + 1];
                final int ctl = chainedTable.length;
                for (int j = 0; j < ctl; j += 2)
                    if (chainedTable[j] != null) {
                        if (predicate.accept(chainedTable[j + 1]) == expected)
                            return onShortCircuit;
                    }
            } else if (t[i] != null) {
                if (predicate.accept(t[i + 1]) == expected)
                    return onShortCircuit;
            }
        }

        return atEnd;
    }

//    private <P> boolean shortCircuitWith(
//            BiPredicate<? super V, ? super P> predicate,
//            P parameter,
//            boolean expected,
//            boolean onShortCircuit,
//            boolean atEnd) {
//        Object[] t = this.t;
//        for (int i = 0; i < t.length; i += 2)
//            if (t[i] == CHAINED_KEY) {
//                Object[] chainedTable = (Object[]) t[i + 1];
//                for (int j = 0; j < chainedTable.length; j += 2)
//                    if (chainedTable[j] != null) {
//                        if (predicate.test((V) chainedTable[j + 1], parameter) == expected) return onShortCircuit;
//                    }
//            } else if (t[i] != null) {
//                if (predicate.test((V) t[i + 1], parameter) == expected) return onShortCircuit;
//            }
//
//        return atEnd;
//    }

    @Override
    public boolean anySatisfy(Predicate<? super V> predicate) {
        return this.shortCircuit(predicate, true, true, false);
    }

//    @Override
//    public <P> boolean anySatisfyWith(Predicate2<? super V, ? super P> predicate, P parameter) {
//        return this.shortCircuitWith(predicate, parameter, true, true, false);
//    }
//
//    @Override
//    public boolean allSatisfy(Predicate<? super V> predicate) {
//        return this.shortCircuit(predicate, false, false, true);
//    }
//
//    @Override
//    public <P> boolean allSatisfyWith(Predicate2<? super V, ? super P> predicate, P parameter) {
//        return this.shortCircuitWith(predicate, parameter, false, false, true);
//    }

    @Override
    public boolean noneSatisfy(Predicate<? super V> predicate) {
        return this.shortCircuit(predicate, true, false, true);
    }
//
//    @Override
//    public <P> boolean noneSatisfyWith(Predicate2<? super V, ? super P> predicate, P parameter) {
//        return this.shortCircuitWith(predicate, parameter, true, false, true);
//    }

    public void delete() {
        this.t = null;
        this.size = -1;
    }

    static class WeakBoundEntry<K, V> implements Map.Entry<K, V> {
        final K key;
        final WeakReference<UnifriedMap<K, V>> holder;
        V value;

        WeakBoundEntry(K key, V value, WeakReference<UnifriedMap<K, V>> holder) {
            this.key = key;
            this.value = value;
            this.holder = holder;
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public V getValue() {
            return this.value;
        }

        @Override
        public V setValue(V value) {
            this.value = value;
            UnifriedMap<K, V> map = this.holder.get();
            if (map != null && map.containsKey(this.key)) return map.put(this.key, value);
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Entry<?, ?> other) {
                K otherKey = (K) other.getKey();
                V otherValue = (V) other.getValue();
                return this.key.equals(otherKey)
                        && this.value.equals(otherValue);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (this.key == null ? 0 : this.key.hashCode())
                    ^ (this.value == null ? 0 : this.value.hashCode());
        }

        @Override
        public String toString() {
            return this.key + "=" + this.value;
        }
    }

    protected class KeySet implements Set<K>, Serializable, BatchIterable<K> {
//        private static final long serialVersionUID = 1L;

        @Override
        public boolean add(K key) {
            throw new UnsupportedOperationException("Cannot call add() on " + this.getClass().getSimpleName());
        }

        @Override
        public boolean addAll(Collection<? extends K> collection) {
            throw new UnsupportedOperationException("Cannot call addAll() on " + this.getClass().getSimpleName());
        }

        @Override
        public void clear() {
            UnifriedMap.this.clear();
        }

        @Override
        public boolean contains(Object o) {
            return UnifriedMap.this.containsKey(o);
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            for (Object aCollection : collection) if (!UnifriedMap.this.containsKey(aCollection)) return false;
            return true;
        }

        @Override
        public boolean isEmpty() {
            return UnifriedMap.this.isEmpty();
        }

        @Override
        public Iterator<K> iterator() {
            return new KeySetIterator();
        }

        @Override
        public boolean remove(Object key) {
            int oldSize = UnifriedMap.this.size;
            UnifriedMap.this.remove(key);
            return UnifriedMap.this.size != oldSize;
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            int oldSize = UnifriedMap.this.size;
            for (Object object : collection) UnifriedMap.this.remove(object);
            return oldSize != UnifriedMap.this.size;
        }

        void putIfFound(Object key, Map<K, V> other) {
            int index = UnifriedMap.this.index(key);
            Object cur = UnifriedMap.this.t[index];
            if (cur != null) {
                Object val = UnifriedMap.this.t[index + 1];
                if (cur == CHAINED_KEY) {
                    this.putIfFoundFromChain((Object[]) val, (K) key, other);
                    return;
                }
                if (cur.equals(key))
                    other.put((K) cur, (V) val);
            }
        }

        private void putIfFoundFromChain(Object[] chain, K key, Map<K, V> other) {
            for (int i = 0; i < chain.length; i += 2) {
                Object k = chain[i];
                if (k == null) return;
                if (k.equals(key))
                    other.put((K) k, (V) chain[i + 1]);
            }
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            int retainedSize = collection.size();
            UnifriedMap<K, V> retainedCopy = (UnifriedMap<K, V>) UnifriedMap.this.newEmpty(retainedSize);
            for (Object key : collection) this.putIfFound(key, retainedCopy);
            if (retainedCopy.size() < this.size()) {
                UnifriedMap.this.maxSize = retainedCopy.maxSize;
                UnifriedMap.this.size = retainedCopy.size;
                UnifriedMap.this.t = retainedCopy.t;
                return true;
            }
            return false;
        }

        @Override
        public int size() {
            return UnifriedMap.this.size();
        }

        @Override
        public void forEach(Procedure<? super K> procedure) {
            UnifriedMap.this.forEachKey(procedure);
        }

        @Override
        public int getBatchCount(int batchSize) {
            return UnifriedMap.this.getBatchCount(batchSize);
        }

        @Override
        public void batchForEach(Procedure<? super K> procedure, int sectionIndex, int sectionCount) {
            Object[] map = UnifriedMap.this.t;
            int sectionSize = map.length / sectionCount;
            int start = sectionIndex * sectionSize;
            int end = sectionIndex == sectionCount - 1 ? map.length : start + sectionSize;
            if (start % 2 != 0) start++;
            for (int i = start; i < end; i += 2) {
                Object cur = map[i];
                if (cur == CHAINED_KEY) UnifriedMap.this.chainedForEachKey((Object[]) map[i + 1], procedure);
                else if (cur != null) procedure.value((K) cur);
            }
        }

        void copyKeys(Object[] result) {
            Object[] table = UnifriedMap.this.t;
            int count = 0;
            for (int i = 0; i < table.length; i += 2) {
                Object x = table[i];
                if (x != null) if (x == CHAINED_KEY) {
                    Object[] chain = (Object[]) table[i + 1];
                    for (int j = 0; j < chain.length; j += 2) {
                        Object cur = chain[j];
                        if (cur == null) break;
                        result[count++] = cur;
                    }
                } else result[count++] = x;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Set<?> other) {
                if (other.size() == this.size()) return this.containsAll(other);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hashCode = 0;
            Object[] table = UnifriedMap.this.t;
            for (int i = 0; i < table.length; i += 2) {
                Object x = table[i];
                if (x != null) if (x == CHAINED_KEY) {
                    Object[] chain = (Object[]) table[i + 1];
                    for (int j = 0; j < chain.length; j += 2) {
                        Object cur = chain[j];
                        if (cur == null) break;
                        hashCode += cur.hashCode();
                    }
                } else hashCode += x.hashCode();
            }
            return hashCode;
        }

        @Override
        public String toString() {
            return Iterate.makeString(this, "[", ", ", "]");
        }

        @Override
        public Object[] toArray() {
            int size = UnifriedMap.this.size();
            Object[] result = new Object[size];
            this.copyKeys(result);
            return result;
        }

        @Override
        public <T> T[] toArray(T[] result) {
            int size = UnifriedMap.this.size();
            if (result.length < size) result = (T[]) Array.newInstance(result.getClass().getComponentType(), size);
            this.copyKeys(result);
            if (size < result.length) result[size] = null;
            return result;
        }

        protected Object writeReplace() {
            UnifiedSet<K> replace = UnifiedSet.newSet(UnifriedMap.this.size());
            for (int i = 0; i < UnifriedMap.this.t.length; i += 2) {
                Object cur = UnifriedMap.this.t[i];
                if (cur == CHAINED_KEY) this.chainedAddToSet((Object[]) UnifriedMap.this.t[i + 1], replace);
                else if (cur != null) replace.add((K) cur);
            }
            return replace;
        }

        private void chainedAddToSet(Object[] chain, UnifiedSet<K> replace) {
            for (int i = 0; i < chain.length; i += 2) {
                Object cur = chain[i];
                if (cur == null) return;
                replace.add((K) cur);
            }
        }
    }

    abstract class PositionalIterator<T> implements Iterator<T> {
        int count;
        int position;
        int chainPosition;
        boolean lastReturned;

        @Override
        public boolean hasNext() {
            return this.count < UnifriedMap.this.size();
        }

        @Override
        public void remove() {
            if (!this.lastReturned) throw new IllegalStateException("next() must be called as many times as remove()");
            this.count--;
            UnifriedMap.this.size--;

            if (this.chainPosition != 0) {
                this.removeFromChain();
            } else {
                int pos = this.position - 2;
                Object[] t = UnifriedMap.this.t;
                if (t[pos] == CHAINED_KEY) {
                    this.removeLastFromChain((Object[]) t[pos + 1], pos);
                } else {
                    t[pos] = null;
                    t[pos + 1] = null;
                    this.position = pos;
                    this.lastReturned = false;
                }
            }
        }

        void removeFromChain() {
            Object[] chain = (Object[]) UnifriedMap.this.t[this.position + 1];
            int pos = this.chainPosition - 2;
            int replacePos = this.chainPosition;
            while (replacePos < chain.length - 2 && chain[replacePos + 2] != null) replacePos += 2;
            chain[pos] = chain[replacePos];
            chain[pos + 1] = chain[replacePos + 1];
            chain[replacePos] = null;
            chain[replacePos + 1] = null;
            this.chainPosition = pos;
            this.lastReturned = false;
        }

        void removeLastFromChain(Object[] chain, int tableIndex) {
            int pos = chain.length - 2;
            while (chain[pos] == null) pos -= 2;
            if (pos == 0) {
                UnifriedMap.this.t[tableIndex] = null;
                UnifriedMap.this.t[tableIndex + 1] = null;
            } else {
                chain[pos] = null;
                chain[pos + 1] = null;
            }
            this.lastReturned = false;
        }
    }

    class KeySetIterator extends PositionalIterator<K> {
        K nextFromChain() {
            Object[] chain = (Object[]) UnifriedMap.this.t[this.position + 1];
            Object cur = chain[this.chainPosition];
            this.chainPosition += 2;
            if (this.chainPosition >= chain.length
                    || chain[this.chainPosition] == null) {
                this.chainPosition = 0;
                this.position += 2;
            }
            this.lastReturned = true;
            return (K) cur;
        }

        @Override
        public K next() {
            if (!this.hasNext()) throw new NoSuchElementException("next() called, but the iterator is exhausted");
            this.count++;
            Object[] table = UnifriedMap.this.t;
            if (this.chainPosition != 0) return this.nextFromChain();
            while (table[this.position] == null) this.position += 2;
            Object cur = table[this.position];
            if (cur == CHAINED_KEY) return this.nextFromChain();
            this.position += 2;
            this.lastReturned = true;
            return (K) cur;
        }
    }

    protected class EntrySet implements Set<Entry<K, V>>, Serializable, BatchIterable<Entry<K, V>> {
//        private static final long serialVersionUID = 1L;
        private transient WeakReference<UnifriedMap<K, V>> holder = new WeakReference<>(UnifriedMap.this);

        @Override
        public boolean add(Entry<K, V> entry) {
            throw new UnsupportedOperationException("Cannot call add() on " + this.getClass().getSimpleName());
        }

        @Override
        public boolean addAll(Collection<? extends Entry<K, V>> collection) {
            throw new UnsupportedOperationException("Cannot call addAll() on " + this.getClass().getSimpleName());
        }

        @Override
        public void clear() {
            UnifriedMap.this.clear();
        }

        boolean containsEntry(Entry<?, ?> entry) {
            return this.getEntry(entry) != null;
        }

        private Entry<K, V> getEntry(Entry<?, ?> entry) {
            K key = (K) entry.getKey();
            V value = (V) entry.getValue();
            int index = UnifriedMap.this.index(key);

            Object cur = UnifriedMap.this.t[index];
            Object curValue = UnifriedMap.this.t[index + 1];
            if (cur == CHAINED_KEY) return this.chainGetEntry((Object[]) curValue, key, value);
            if (cur == null) return null;
            if (cur.equals(key)) if (value.equals(curValue))
                return ImmutableEntry.of((K) cur, (V) curValue);
            return null;
        }

        private Entry<K, V> chainGetEntry(Object[] chain, K key, V value) {
            for (int i = 0; i < chain.length; i += 2) {
                Object cur = chain[i];
                if (cur == null) return null;
                if (cur.equals(key)) {
                    Object curValue = chain[i + 1];
                    if (value.equals(curValue))
                        return ImmutableEntry.of((K) cur, (V) curValue);
                }
            }
            return null;
        }

        @Override
        public boolean contains(Object o) {
            return o instanceof Entry && this.containsEntry((Entry<?, ?>) o);
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            for (Object obj : collection) if (!this.contains(obj)) return false;
            return true;
        }

        @Override
        public boolean isEmpty() {
            return UnifriedMap.this.isEmpty();
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntrySetIterator(this.holder);
        }

        @Override
        public boolean remove(Object e) {
            if (!(e instanceof Entry<?, ?> entry)) return false;
            K key = (K) entry.getKey();
            V value = (V) entry.getValue();

            int index = UnifriedMap.this.index(key);

            Object cur = UnifriedMap.this.t[index];
            if (cur != null) {
                Object val = UnifriedMap.this.t[index + 1];
                if (cur == CHAINED_KEY) return this.removeFromChain((Object[]) val, key, value, index);
                if (cur.equals(key) && value.equals(val)) {
                    UnifriedMap.this.t[index] = null;
                    UnifriedMap.this.t[index + 1] = null;
                    UnifriedMap.this.size--;
                    return true;
                }
            }
            return false;
        }

        private boolean removeFromChain(Object[] chain, K key, V value, int index) {
            for (int i = 0; i < chain.length; i += 2) {
                Object k = chain[i];
                if (k == null) return false;
                if (k.equals(key)) {
                    V val = (V) chain[i + 1];
                    if (val.equals(value)) {
                        UnifriedMap.this.overwriteWithLastElementFromChain(chain, index, i);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            boolean changed = false;
            for (Object obj : collection) if (this.remove(obj)) changed = true;
            return changed;
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            int retainedSize = collection.size();
            UnifriedMap<K, V> retainedCopy = (UnifriedMap<K, V>) UnifriedMap.this.newEmpty(retainedSize);

            for (Object obj : collection)
                if (obj instanceof Entry<?, ?> otherEntry) {
                    Entry<K, V> thisEntry = this.getEntry(otherEntry);
                    if (thisEntry != null) retainedCopy.put(thisEntry.getKey(), thisEntry.getValue());
                }
            if (retainedCopy.size() < this.size()) {
                UnifriedMap.this.maxSize = retainedCopy.maxSize;
                UnifriedMap.this.size = retainedCopy.size;
                UnifriedMap.this.t = retainedCopy.t;
                return true;
            }
            return false;
        }

        @Override
        public int size() {
            return UnifriedMap.this.size();
        }

        @Override
        public void forEach(Procedure<? super Entry<K, V>> procedure) {
            for (int i = 0; i < UnifriedMap.this.t.length; i += 2) {
                Object cur = UnifriedMap.this.t[i];
                if (cur == CHAINED_KEY) this.chainedForEachEntry((Object[]) UnifriedMap.this.t[i + 1], procedure);
                else if (cur != null)
                    procedure.value(ImmutableEntry.of((K) cur, (V) t[i + 1]));
            }
        }

        private void chainedForEachEntry(Object[] chain, Procedure<? super Entry<K, V>> procedure) {
            for (int i = 0; i < chain.length; i += 2) {
                Object cur = chain[i];
                if (cur == null) return;
                procedure.value(ImmutableEntry.of((K) cur, (V) chain[i + 1]));
            }
        }

        @Override
        public int getBatchCount(int batchSize) {
            return UnifriedMap.this.getBatchCount(batchSize);
        }

        @Override
        public void batchForEach(Procedure<? super Entry<K, V>> procedure, int sectionIndex, int sectionCount) {
            Object[] map = t;
            int sectionSize = map.length / sectionCount;
            int start = sectionIndex * sectionSize;
            int end = sectionIndex == sectionCount - 1 ? map.length : start + sectionSize;
            if (start % 2 != 0) start++;
            for (int i = start; i < end; i += 2) {
                Object cur = map[i];
                if (cur == CHAINED_KEY) this.chainedForEachEntry((Object[]) map[i + 1], procedure);
                else if (cur != null)
                    procedure.value(ImmutableEntry.of((K) cur, (V) map[i + 1]));
            }
        }

        void copyEntries(Object[] result) {
            Object[] table = UnifriedMap.this.t;
            int count = 0;
            for (int i = 0; i < table.length; i += 2) {
                Object x = table[i];
                if (x != null) if (x == CHAINED_KEY) {
                    Object[] chain = (Object[]) table[i + 1];
                    for (int j = 0; j < chain.length; j += 2) {
                        Object cur = chain[j];
                        if (cur == null) break;
                        result[count++] =
                                new WeakBoundEntry<>((K) cur, (V) chain[j + 1], this.holder);
                    }
                } else
                    result[count++] = new WeakBoundEntry<>((K) x, (V) table[i + 1], this.holder);
            }
        }

        @Override
        public Object[] toArray() {
            Object[] result = new Object[UnifriedMap.this.size()];
            this.copyEntries(result);
            return result;
        }

        @Override
        public <T> T[] toArray(T[] result) {
            int size = UnifriedMap.this.size();
            if (result.length < size) result = (T[]) Array.newInstance(result.getClass().getComponentType(), size);
            this.copyEntries(result);
            if (size < result.length) result[size] = null;
            return result;
        }

        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            this.holder = new WeakReference<>(UnifriedMap.this);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Set<?> other) {
                if (other.size() == this.size()) return this.containsAll(other);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return UnifriedMap.this.hashCode();
        }
    }

    class EntrySetIterator extends PositionalIterator<Entry<K, V>> {
        private final WeakReference<UnifriedMap<K, V>> holder;

        EntrySetIterator(WeakReference<UnifriedMap<K, V>> holder) {
            this.holder = holder;
        }

        Entry<K, V> nextFromChain() {
            Object[] chain = (Object[]) UnifriedMap.this.t[this.position + 1];
            Object cur = chain[this.chainPosition];
            Object value = chain[this.chainPosition + 1];
            this.chainPosition += 2;
            if (this.chainPosition >= chain.length
                    || chain[this.chainPosition] == null) {
                this.chainPosition = 0;
                this.position += 2;
            }
            this.lastReturned = true;
            return new WeakBoundEntry<>((K) cur, (V) value, this.holder);
        }

        @Override
        public Entry<K, V> next() {
            if (!this.hasNext()) throw new NoSuchElementException("next() called, but the iterator is exhausted");
            this.count++;
            Object[] table = UnifriedMap.this.t;
            if (this.chainPosition != 0) return this.nextFromChain();
            while (table[this.position] == null) this.position += 2;
            Object cur = table[this.position];
            Object value = table[this.position + 1];
            if (cur == CHAINED_KEY) return this.nextFromChain();
            this.position += 2;
            this.lastReturned = true;
            return new WeakBoundEntry<>((K) cur, (V) value, this.holder);
        }
    }

    protected class ValuesCollection extends ValuesCollectionCommon<V>
            implements Serializable, BatchIterable<V> {
        private static final long serialVersionUID = 1L;

        @Override
        public void clear() {
            UnifriedMap.this.clear();
        }

        @Override
        public boolean contains(Object o) {
            return UnifriedMap.this.containsValue(o);
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            // todo: this is N^2. if c is large, we should copy the values to a set.
            return Iterate.allSatisfy(collection, Predicates.in(this));
        }

        @Override
        public boolean isEmpty() {
            return UnifriedMap.this.isEmpty();
        }

        @Override
        public Iterator<V> iterator() {
            return new ValuesIterator();
        }

        @Override
        public boolean remove(Object o) {
            // this is so slow that the extra overhead of the iterator won't be noticeable
            if (o == null) {
                for (Iterator<V> it = this.iterator(); it.hasNext(); )
                    if (it.next() == null) {
                        it.remove();
                        return true;
                    }
            } else {
                for (Iterator<V> it = this.iterator(); it.hasNext(); ) {
                    V o2 = it.next();
                    if (o == o2 || o2.equals(o)) {
                        it.remove();
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            // todo: this is N^2. if c is large, we should copy the values to a set.
            boolean changed = false;

            for (Object obj : collection) if (this.remove(obj)) changed = true;
            return changed;
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            boolean modified = false;
            Iterator<V> e = this.iterator();
            while (e.hasNext()) if (!collection.contains(e.next())) {
                e.remove();
                modified = true;
            }
            return modified;
        }

        @Override
        public int size() {
            return UnifriedMap.this.size();
        }

        @Override
        public void forEach(Procedure<? super V> procedure) {
            UnifriedMap.this.forEachValue(procedure);
        }

        @Override
        public int getBatchCount(int batchSize) {
            return UnifriedMap.this.getBatchCount(batchSize);
        }

        @Override
        public void batchForEach(Procedure<? super V> procedure, int sectionIndex, int sectionCount) {
            UnifriedMap.this.batchForEach(procedure, sectionIndex, sectionCount);
        }

        void copyValues(Object[] result) {
            int count = 0;
            for (int i = 0; i < UnifriedMap.this.t.length; i += 2) {
                Object x = UnifriedMap.this.t[i];
                if (x != null) if (x == CHAINED_KEY) {
                    Object[] chain = (Object[]) UnifriedMap.this.t[i + 1];
                    for (int j = 0; j < chain.length; j += 2) {
                        Object cur = chain[j];
                        if (cur == null) break;
                        result[count++] = chain[j + 1];
                    }
                } else result[count++] = UnifriedMap.this.t[i + 1];
            }
        }

        @Override
        public Object[] toArray() {
            int size = UnifriedMap.this.size();
            Object[] result = new Object[size];
            this.copyValues(result);
            return result;
        }

        @Override
        public <T> T[] toArray(T[] result) {
            int size = UnifriedMap.this.size();
            if (result.length < size) result = (T[]) Array.newInstance(result.getClass().getComponentType(), size);
            this.copyValues(result);
            if (size < result.length) result[size] = null;
            return result;
        }

        protected Object writeReplace() {
            FastList<V> replace = FastList.newList(UnifriedMap.this.size());
            for (int i = 0; i < UnifriedMap.this.t.length; i += 2) {
                Object cur = UnifriedMap.this.t[i];
                if (cur == CHAINED_KEY) this.chainedAddToList((Object[]) UnifriedMap.this.t[i + 1], replace);
                else if (cur != null) replace.add((V) UnifriedMap.this.t[i + 1]);
            }
            return replace;
        }

        private void chainedAddToList(Object[] chain, FastList<V> replace) {
            for (int i = 0; i < chain.length; i += 2) {
                Object cur = chain[i];
                if (cur == null) return;
                replace.add((V) chain[i + 1]);
            }
        }

        @Override
        public String toString() {
            return Iterate.makeString(this, "[", ", ", "]");
        }
    }

    class ValuesIterator extends PositionalIterator<V> {
        V nextFromChain() {
            Object[] chain = (Object[]) UnifriedMap.this.t[this.position + 1];
            Object val = chain[this.chainPosition + 1];
            this.chainPosition += 2;
            if (this.chainPosition >= chain.length
                    || chain[this.chainPosition] == null) {
                this.chainPosition = 0;
                this.position += 2;
            }
            this.lastReturned = true;
            return (V)val;
        }

        @Override
        public V next() {
            if (!this.hasNext()) throw new NoSuchElementException("next() called, but the iterator is exhausted");
            this.count++;
            if (this.chainPosition != 0) return this.nextFromChain();

            int p = this.position;
            Object[] table = UnifriedMap.this.t;
            while (table[p] == null) p += 2;
            Object cur = table[p];
            this.position = p;
            if (cur == CHAINED_KEY)
                return this.nextFromChain();
            else {
                Object val = table[p + 1];
                this.position += 2;
                this.lastReturned = true;
                return (V) val;
            }
        }
    }
}