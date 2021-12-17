/*
 * Copyright (c) 2017, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


package jcog.data.map;


import jcog.Util;

import java.util.Iterator;
import java.util.function.BiFunction;

/**
 * custom modifications from jdk/internal/vm/compiler/collections/EconomicMapImpl
 *
 * Implementation of a map with a memory-efficient structure that always preserves insertion order
 * when iterating over keys. Particularly efficient when number of entries is 0 or smaller equal
 * {@link #INITIAL_CAPACITY} or smaller 256.
 *
 * The key/value pairs are kept in an expanding flat object array with keys at even indices and
 * values at odd indices. If the map has smaller or equal to {@link #HASH_THRESHOLD} entries, there
 * is no additional hash data structure and comparisons are done via linear checking of the
 * key/value pairs. For the case where the equality check is particularly cheap (e.g., just an
 * object identity comparison), this limit below which the map is without an actual hash table is
 * higher and configured at {@link #HASH_THRESHOLD_IDENTITY_COMPARE}.
 *
 * When the hash table needs to be constructed, the field {@link #hashArray} becomes a new hash
 * array where an entry of 0 means no hit and otherwise denotes the entry number in the
 * {@link #entries} array. The hash array is interpreted as an actual byte array if the indices fit
 * within 8 bit, or as an array of short values if the indices fit within 16 bit, or as an array of
 * integer values in other cases.
 *
 * Hash collisions are handled by chaining a linked list of {@link CollisionLink} objects that take
 * the place of the values in the {@link #entries} array.
 *
 * Removing entries will put {@code null} into the {@link #entries} array. If the occupation of the
 * map falls below a specific threshold, the map will be compressed via the
 * {@link #maybeCompress(int)} method.
 */
final class CheapMap<K, V> implements Iterable<K> /*EconomicMap<K, V>, EconomicSet<K>*/ {

    /**
     * Initial number of key/value pair entries that is allocated in the first entries array.
     */
    private static final int INITIAL_CAPACITY = 4;

    /**
     * Maximum number of entries that are moved linearly forward if a key is removed.
     */
    private static final int COMPRESS_IMMEDIATE_CAPACITY = 8;

    /**
     * Minimum number of key/value pair entries added when the entries array is increased in size.
     */
    private static final int MIN_CAPACITY_INCREASE = 8;

    /**
     * Number of entries above which a hash table is created.
     */
    private static final int HASH_THRESHOLD = 4;

    /**
     * Number of entries above which a hash table is created when equality can be checked with
     * object identity.
     */
    private static final int HASH_THRESHOLD_IDENTITY_COMPARE = 8;

    /**
     * Maximum number of entries allowed in the map.
     */
    private static final int MAX_ELEMENT_COUNT = Integer.MAX_VALUE >> 1;

    /**
     * Number of entries above which more than 1 byte is necessary for the hash index.
     */
    private static final int LARGE_HASH_THRESHOLD = ((1 << Byte.SIZE) << 1);

    /**
     * Number of entries above which more than 2 bytes are are necessary for the hash index.
     */
    private static final int VERY_LARGE_HASH_THRESHOLD = (LARGE_HASH_THRESHOLD << Byte.SIZE);

    /**
     * Total number of entries (actual entries plus deleted entries).
     */
    private int totalEntries;

    /**
     * Number of deleted entries.
     */
    private int deletedEntries;

    /**
     * Entries array with even indices storing keys and odd indices storing values.
     */
    private Object[] entries;

    /**
     * Hash array that is interpreted either as byte or short or int array depending on number of
     * map entries.
     */
    private byte[] hashArray;

    /**
     * The strategy used for comparing keys or {@code null} for denoting special strategy
     * {@link Equivalence#IDENTITY}.
     */
    private final Equivalence strategy;

    private abstract static class Equivalence {
        public static final Equivalence DEFAULT = new Equivalence() {
            public boolean equals(Object a, Object b) {
                return a.equals(b);
            }

            public int hashCode(Object o) {
                return o.hashCode();
            }
        };
        public static final Equivalence IDENTITY = new Equivalence() {
            public boolean equals(Object a, Object b) {
                return a == b;
            }

            public int hashCode(Object o) {
                return o.hashCode();
            }
        };
        public static final Equivalence IDENTITY_WITH_SYSTEM_HASHCODE = new Equivalence() {
            public boolean equals(Object a, Object b) {
                return a == b;
            }

            public int hashCode(Object o) {
                return System.identityHashCode(o);
            }
        };

        protected Equivalence() {
        }

        public abstract boolean equals(Object a, Object b);

        public abstract int hashCode(Object o);
    }

    /**
     * Intercept method for debugging purposes.
     */
    private static <K, V> CheapMap<K, V> intercept(CheapMap<K, V> map) {
        return map;
    }

    public static <K, V> CheapMap<K, V> create(Equivalence strategy) {
        return intercept(new CheapMap<>(strategy));
    }

    public static <K, V> CheapMap<K, V> create(Equivalence strategy, int initialCapacity) {
        return intercept(new CheapMap<>(strategy, initialCapacity));
    }

//    public static <K, V> CheapMap<K, V> create(Equivalence strategy, UnmodifiableEconomicMap<K, V> other, boolean isSet) {
//        return intercept(new CheapMap<>(strategy, other, isSet));
//    }
//
//    public static <K, V> CheapMap<K, V> create(Equivalence strategy, UnmodifiableEconomicSet<K> other, boolean isSet) {
//        return intercept(new CheapMap<>(strategy, other, isSet));
//    }

    private CheapMap(Equivalence strategy) {
        this.strategy = strategy == Equivalence.IDENTITY ? null : strategy;
    }

    private CheapMap(Equivalence strategy, int initialCapacity) {
        this(strategy);
        init(initialCapacity);
    }

//    private CheapMap(Equivalence strategy, UnmodifiableEconomicMap<K, V> other, boolean isSet) {
//        this(strategy, isSet);
//        if (!initFrom(other)) {
//            init(other.size());
//            putAll(other);
//        }
//    }

//    private CheapMap(Equivalence strategy, UnmodifiableEconomicSet<K> other, boolean isSet) {
//        this(strategy, isSet);
//        if (!initFrom(other)) {
//            init(other.size());
//            addAll(other);
//        }
//    }

    @SuppressWarnings("unchecked")
    private boolean initFrom(Object o) {
        if (o instanceof CheapMap) {
            CheapMap<K, V> otherMap = (CheapMap<K, V>) o;
            // We are only allowed to directly copy if the strategies of the two maps are the same.
            if (strategy == otherMap.strategy) {
                totalEntries = otherMap.totalEntries;
                deletedEntries = otherMap.deletedEntries;
                if (otherMap.entries != null) {
                    entries = otherMap.entries.clone();
                }
                if (otherMap.hashArray != null) {
                    hashArray = otherMap.hashArray.clone();
                }
                return true;
            }
        }
        return false;
    }

    private void init(int size) {
        if (size > INITIAL_CAPACITY) {
            entries = new Object[size << 1];
        }
    }

    /**
     * Links the collisions. Needs to be immutable class for allowing efficient shallow copy from
     * other map on construction.
     */
    private static final class CollisionLink {

        CollisionLink(Object value, int next) {
            this.value = value;
            this.next = next;
        }

        final Object value;

        /**
         * Index plus one of the next entry in the collision link chain.
         */
        final int next;
    }

    @SuppressWarnings("unchecked")
    public V get(K key) {
        assertNotNull(key);

        int index = find(key);
        if (index != -1) {
            return (V) getValue(index);
        }
        return null;
    }

    private int find(K key) {
        if (hasHashArray()) {
            return findHash(key);
        } else {
            return findLinear(key);
        }
    }

    private int findLinear(K key) {
        for (int i = 0; i < totalEntries; i++) {
            Object entryKey = entries[i << 1];
            if (entryKey != null && compareKeys(key, entryKey)) {
                return i;
            }
        }
        return -1;
    }

    private boolean compareKeys(Object key, Object entryKey) {
        if (key == entryKey) {
            return true;
        }
        if (strategy != null && strategy != Equivalence.IDENTITY_WITH_SYSTEM_HASHCODE) {
            if (strategy == Equivalence.DEFAULT) {
                return key.equals(entryKey);
            } else {
                return strategy.equals(key, entryKey);
            }
        }
        return false;
    }

    private int findHash(K key) {
        int index = getHashArray(getHashIndex(key)) - 1;
        if (index != -1) {
            Object entryKey = getKey(index);
            if (compareKeys(key, entryKey)) {
                return index;
            } else {
                Object entryValue = getRawValue(index);
                if (entryValue instanceof CollisionLink) {
                    return findWithCollision(key, (CollisionLink) entryValue);
                }
            }
        }

        return -1;
    }

    private int findWithCollision(K key, CollisionLink initialEntryValue) {
        int index;
        Object entryKey;
        CollisionLink entryValue = initialEntryValue;
        while (true) {
            CollisionLink collisionLink = entryValue;
            index = collisionLink.next;
            entryKey = getKey(index);
            if (compareKeys(key, entryKey)) {
                return index;
            } else {
                Object value = getRawValue(index);
                if (value instanceof CollisionLink) {
                    entryValue = (CollisionLink) getRawValue(index);
                } else {
                    return -1;
                }
            }
        }
    }

    private int getHashArray(int index) {
        byte[] a = this.hashArray;
        int l = entries.length;
        if (l < LARGE_HASH_THRESHOLD) {
            return (a[index] & 0xFF);
        } else if (l < VERY_LARGE_HASH_THRESHOLD) {
            int adjustedIndex = index << 1;
            return (a[adjustedIndex] & 0xFF) | ((a[adjustedIndex + 1] & 0xFF) << 8);
        } else {
            int adjustedIndex = index << 2;
            return (a[adjustedIndex] & 0xFF) | ((a[adjustedIndex + 1] & 0xFF) << 8) | ((a[adjustedIndex + 2] & 0xFF) << 16) | ((a[adjustedIndex + 3] & 0xFF) << 24);
        }
    }

    private void setHashArray(int index, int value) {
        byte[] a = this.hashArray;
        int l = entries.length;
        if (l < LARGE_HASH_THRESHOLD) {
            a[index] = (byte) value;
        } else if (l < VERY_LARGE_HASH_THRESHOLD) {
            int adjustedIndex = index << 1;
            a[adjustedIndex] = (byte) value;
            a[adjustedIndex + 1] = (byte) (value >> 8);
        } else {
            int adjustedIndex = index << 2;
            a[adjustedIndex] = (byte) value;
            a[adjustedIndex + 1] = (byte) (value >> 8);
            a[adjustedIndex + 2] = (byte) (value >> 16);
            a[adjustedIndex + 3] = (byte) (value >> 24);
        }
    }

    private int findAndRemoveHash(Object key) {
        int hashIndex = getHashIndex(key);
        int index = getHashArray(hashIndex) - 1;
        if (index != -1) {
            Object entryKey = getKey(index);
            if (compareKeys(key, entryKey)) {
                Object value = getRawValue(index);
                int nextIndex = -1;
                if (value instanceof CollisionLink collisionLink) {
                    nextIndex = collisionLink.next;
                }
                setHashArray(hashIndex, nextIndex + 1);
                return index;
            } else {
                Object entryValue = getRawValue(index);
                if (entryValue instanceof CollisionLink) {
                    return findAndRemoveWithCollision(key, (CollisionLink) entryValue, index);
                }
            }
        }

        return -1;
    }

    private int findAndRemoveWithCollision(Object key, CollisionLink initialEntryValue, int initialIndexValue) {
        int index;
        Object entryKey;
        CollisionLink entryValue = initialEntryValue;
        int lastIndex = initialIndexValue;
        while (true) {
            CollisionLink collisionLink = entryValue;
            index = collisionLink.next;
            entryKey = getKey(index);
            if (compareKeys(key, entryKey)) {
                Object value = getRawValue(index);
                Object V = collisionLink.value;
                setRawValue(lastIndex, value instanceof CollisionLink ?
                        new CollisionLink(V, ((CollisionLink) value).next) :
                        V
                );
                return index;
            } else {
                Object value = getRawValue(index);
                if (value instanceof CollisionLink) {
                    entryValue = (CollisionLink) getRawValue(index);
                    lastIndex = index;
                } else {
                    return -1;
                }
            }
        }
    }

    private int getHashIndex(Object key) {
        int hash;
        if (strategy != null && strategy != Equivalence.DEFAULT) {
            hash = strategy == Equivalence.IDENTITY_WITH_SYSTEM_HASHCODE ?
                    System.identityHashCode(key) : strategy.hashCode(key);
        } else {
            hash = key.hashCode();
        }
        //hash = hash ^ (hash >>> 16);
        return Util.spreadHash(hash) & (tableSize() - 1);
    }

    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        assertNotNull(key);
        int index = find(key);
        if (index != -1) {
            Object oldValue = getValue(index);
            if (oldValue!=value)
                setValue(index, value);
            return (V) oldValue;
        }

        int nextEntryIndex = totalEntries;
        if (entries == null) {
            entries = new Object[INITIAL_CAPACITY << 1];
        } else if (entries.length == nextEntryIndex << 1) {
            grow();

            assert entries.length > totalEntries << 1;
            // Can change if grow is actually compressing.
            nextEntryIndex = totalEntries;
        }

        setKey(nextEntryIndex, key);
        setValue(nextEntryIndex, value);
        totalEntries++;

        if (hasHashArray()) {
            // Rehash on collision if hash table is more than three quarters full.
            int s = size();
            boolean rehashOnCollision = (tableSize() < (s + (s >> 1)));
            putHashEntry(key, nextEntryIndex, rehashOnCollision);
        } else if (totalEntries > hashThreshold()) {
            hash();
        }

        return null;
    }

    /**
     * Number of entries above which a hash table should be constructed.
     */
    private int hashThreshold() {
        if (strategy == null || strategy == Equivalence.IDENTITY_WITH_SYSTEM_HASHCODE) {
            return HASH_THRESHOLD_IDENTITY_COMPARE;
        } else {
            return HASH_THRESHOLD;
        }
    }

    private void grow() {
        int entriesLength = entries.length;
        int newSize = (entriesLength >> 1) + Math.max(MIN_CAPACITY_INCREASE, entriesLength >> 2);
        if (newSize > MAX_ELEMENT_COUNT) {
            throw new UnsupportedOperationException("map grown too large!");
        }
        Object[] newEntries = new Object[newSize << 1];
        System.arraycopy(entries, 0, newEntries, 0, entriesLength);
        entries = newEntries;
        if ((entriesLength < LARGE_HASH_THRESHOLD && newEntries.length >= LARGE_HASH_THRESHOLD) ||
                        (entriesLength < VERY_LARGE_HASH_THRESHOLD && newEntries.length > VERY_LARGE_HASH_THRESHOLD)) {
            // Rehash in order to change number of bits reserved for hash indices.
            hash();
        }
    }

    /**
     * Compresses the graph if there is a large number of deleted entries and returns the translated
     * new next index.
     */
    private int maybeCompress(int nextIndex) {
        if (entries.length != INITIAL_CAPACITY << 1 && deletedEntries >= (totalEntries >> 1) + (totalEntries >> 2)) {
            return compressLarge(nextIndex);
        }
        return nextIndex;
    }

    /**
     * Compresses the graph and returns the translated new next index.
     */
    private int compressLarge(int nextIndex) {
        int size = INITIAL_CAPACITY;
        int remaining = totalEntries - deletedEntries;

        while (size <= remaining) {
            size += Math.max(MIN_CAPACITY_INCREASE, size >> 1);
        }

        Object[] newEntries = new Object[size << 1];
        int z = 0;
        int newNextIndex = remaining;
        for (int i = 0; i < totalEntries; ++i) {
            if (i == nextIndex)
                newNextIndex = z;

            Object key = getKey(i);
            if (key != null) {
                newEntries[z << 1] = key;
                newEntries[(z << 1) + 1] = getValue(i);
                z++;
            }
        }

        this.entries = newEntries;
        totalEntries = z;
        deletedEntries = 0;
        if (z <= hashThreshold()) {
            this.hashArray = null;
        } else {
            hash();
        }
        return newNextIndex;
    }

    private int tableSize() {
		int l = hashArray.length;
		int e = entries.length;
        if (e < LARGE_HASH_THRESHOLD) {
            return l;
        } else if (e < VERY_LARGE_HASH_THRESHOLD) {
            return l >> 1;
        } else {
            return l >> 2;
        }
    }

    private void hash() {
        int entryCount = size();

        // Calculate smallest 2^n that is greater number of entries.
        int size = hashThreshold();
        while (size <= entryCount) {
            size <<= 1;
        }

        // Give extra size to avoid collisions.
        size <<= 1;

        if (this.entries.length >= VERY_LARGE_HASH_THRESHOLD) {
            // Every entry has 4 bytes.
            size <<= 2;
        } else if (this.entries.length >= LARGE_HASH_THRESHOLD) {
            // Every entry has 2 bytes.
            size <<= 1;
        } else {
            // Entries are very small => give extra size to further reduce collisions.
            size <<= 1;
        }

        hashArray = new byte[size];
        int e = this.totalEntries;
        for (int i = 0; i < e; i++) {
            Object entryKey = getKey(i);
            if (entryKey != null)
                putHashEntry(entryKey, i, false);
        }
    }

    private void putHashEntry(Object key, int entryIndex, boolean rehashOnCollision) {
        int hashIndex = getHashIndex(key);
        int oldIndex = getHashArray(hashIndex) - 1;
        if (oldIndex != -1 && rehashOnCollision) {
            this.hash();
            return;
        }
        setHashArray(hashIndex, entryIndex + 1);
        Object value = getRawValue(entryIndex);
        if (oldIndex != -1) {
            assert entryIndex != oldIndex : "this cannot happend and would create an endless collision link cycle";
            if (value instanceof CollisionLink collisionLink) {
                setRawValue(entryIndex, new CollisionLink(collisionLink.value, oldIndex));
            } else {
                setRawValue(entryIndex, new CollisionLink(getRawValue(entryIndex), oldIndex));
            }
        } else {
            if (value instanceof CollisionLink collisionLink) {
                setRawValue(entryIndex, collisionLink.value);
            }
        }
    }

    public int size() {
        return totalEntries - deletedEntries;
    }

    public boolean containsKey(K key) {
        return find(key) != -1;
    }

    public void clear() {
        entries = null;
        hashArray = null;
        totalEntries = deletedEntries = 0;
    }

    private boolean hasHashArray() {
        return hashArray != null;
    }

    @SuppressWarnings("unchecked")
    public V removeKey(K key) {
        assertNotNull(key);
        int index;
        if (hasHashArray()) {
            index = this.findAndRemoveHash(key);
        } else {
            index = this.findLinear(key);
        }

        if (index != -1) {
            Object value = getValue(index);
            remove(index);
            return (V) value;
        }
        return null;
    }

    private static void assertNotNull(Object key) {
        if (key == null) throw new UnsupportedOperationException("key must be non-null");
    }

    /**
     * Removes the element at the specific index and returns the index of the next element. This can
     * be a different value if graph compression was triggered.
     */
    private int remove(int indexToRemove) {
        int index = indexToRemove;
        int entriesAfterIndex = totalEntries - index - 1;
        int result = index + 1;

        // Without hash array, compress immediately.
        if (entriesAfterIndex <= COMPRESS_IMMEDIATE_CAPACITY && !hasHashArray()) {
            while (index < totalEntries - 1) {
                setKey(index, getKey(index + 1));
                setRawValue(index, getRawValue(index + 1));
                index++;
            }
            result--;
        }

        setKey(index, null);
        setRawValue(index, null);
        if (index == totalEntries - 1) {
            // Make sure last element is always non-null.
            totalEntries--;
            while (index > 0 && getKey(index - 1) == null) {
                totalEntries--;
                deletedEntries--;
                index--;
            }
        } else {
            deletedEntries++;
            result = maybeCompress(result);
        }

        return result;
    }

    private abstract class SparseMapIterator<E> implements Iterator<E> {

        protected int current;

        @Override
        public boolean hasNext() {
            return current < totalEntries;
        }

        @Override
        public void remove() {
            if (hasHashArray()) {
                CheapMap.this.findAndRemoveHash(getKey(current - 1));
            }
            current = CheapMap.this.remove(current - 1);
        }
    }

    public Iterable<V> getValues() {
        return () -> new SparseMapIterator<V>() {
            @SuppressWarnings("unchecked")
            @Override
            public V next() {
                Object result;
                while (true) {
                    result = getValue(current);
                    if (result == null && getKey(current) == null) {
                        // values can be null, double-check if key is also null
                        current++;
                    } else {
                        current++;
                        break;
                    }
                }
                return (V) result;
            }
        };
    }

    public Iterable<K> getKeys() {
        return this;
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

//    @Override
//    public MapCursor<K, V> getEntries() {
//        return new MapCursor<K, V>() {
//            int current = -1;
//
//            @Override
//            public boolean advance() {
//                current++;
//                if (current >= totalEntries) {
//                    return false;
//                } else {
//                    while (CheapMap.this.getKey(current) == null) {
//                        // Skip over null entries
//                        current++;
//                    }
//                    return true;
//                }
//            }
//
//            @SuppressWarnings("unchecked")
//            @Override
//            public K getKey() {
//                return (K) CheapMap.this.getKey(current);
//            }
//
//            @SuppressWarnings("unchecked")
//            @Override
//            public V getValue() {
//                return (V) CheapMap.this.getValue(current);
//            }
//
//            @Override
//            public void remove() {
//                if (hasHashArray()) {
//                    CheapMap.this.findAndRemoveHash(CheapMap.this.getKey(current));
//                }
//                current = CheapMap.this.remove(current) - 1;
//            }
//        };
//    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        for (int i = 0; i < totalEntries; i++) {
            Object entryKey = getKey(i);
            if (entryKey != null) {
                Object x = getValue(i);
                V y = function.apply((K) entryKey, (V) x);
                if (x!=y) setValue(i, y);
            }
        }
    }

    private Object getKey(int index) {
        return entries[index << 1];
    }

    private void setKey(int index, Object newValue) {
        entries[index << 1] = newValue;
    }

    private void setValue(int index, Object newValue) {
        Object oldValue = getRawValue(index);
        if (oldValue instanceof CollisionLink collisionLink) {
            setRawValue(index, new CollisionLink(newValue, collisionLink.next));
        } else {
            setRawValue(index, newValue);
        }
    }

    private void setRawValue(int index, Object newValue) {
        entries[(index << 1) + 1] = newValue;
    }

    private Object getRawValue(int index) {
        return entries[(index << 1) + 1];
    }

    private Object getValue(int index) {
        Object object = getRawValue(index);
        if (object instanceof CollisionLink) {
            return ((CollisionLink) object).value;
        }
        return object;
    }



//    @Override
//    public String toString() {
//        StringBuilder builder = new StringBuilder();
//        builder.append(isSet ? "set(size=" : "map(size=").append(size()).append(", {");
//        String sep = "";
//
//        MapCursor<K, V> cursor = getEntries();
//        while (cursor.advance()) {
//            builder.append(sep);
//            if (isSet) {
//                builder.append(cursor.getKey());
//            } else {
//                builder.append("(").append(cursor.getKey()).append(",").append(cursor.getValue()).append(")");
//            }
//            sep = ",";
//        }
//        builder.append("})");
//        return builder.toString();
//    }

    @Override
    public Iterator<K> iterator() {
        return new CheapMapIterator();
    }

    public boolean contains(K element) {
        return containsKey(element);
    }

    @SuppressWarnings("unchecked")
    public boolean add(K element) {
        return put(element, (V) element) == null;
    }

    public void remove(K element) {
        removeKey(element);
    }

    private final class CheapMapIterator extends SparseMapIterator<K> {
        @Override
        public K next() {
            Object result;
            while ((result = getKey(current++)) == null) {
                // skip null entries
            }
            return (K) result;
        }
    }
}