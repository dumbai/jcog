package jcog.data.set;

import java.util.*;
import java.util.stream.Stream;

/**
 * A simple HashSet, save 25% memory.
 * http:
 *
 * http:
 *
 * @author srain@php.net
 */
public class SimpleHashSet<T> extends AbstractSet<T> implements Cloneable {

    private static final int MINIMUM_CAPACITY = 4;
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private static final SimpleHashSetEntry[] EMPTY_TABLE = new SimpleHashSetEntry[MINIMUM_CAPACITY >>> 1];
    transient SimpleHashSetEntry<T>[] mTable;
    transient int mSize;
    private transient int threshold;

    public SimpleHashSet() {
        mTable = EMPTY_TABLE;
        
        threshold = -1;
    }

    public SimpleHashSet(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity: " + capacity);
        }

        if (capacity == 0) {
            SimpleHashSetEntry<T>[] tab = EMPTY_TABLE;
            mTable = tab;
            threshold = -1; 
            return;
        }

        if (capacity < MINIMUM_CAPACITY) {
            capacity = MINIMUM_CAPACITY;
        } else if (capacity > MAXIMUM_CAPACITY) {
            capacity = MAXIMUM_CAPACITY;
        } else {
            capacity = roundUpToPowerOfTwo(capacity);
        }
        makeTable(capacity);
    }

    public SimpleHashSet(Collection<? extends T> collection) {
        this(collection.size() < 6 ? 11 : collection.size() * 2);
        addAll(collection);
    }

    public static int roundUpToPowerOfTwo(int i) {
        
        i--;

        
        i |= i >>> 1;
        i |= i >>> 2;
        i |= i >>> 4;
        i |= i >>> 8;
        i |= i >>> 16;

        return i + 1;
    }

    public static int secondaryHash(Object key) {
        int hash = key.hashCode();
        hash ^= (hash >>> 20) ^ (hash >>> 12);
        hash ^= (hash >>> 7) ^ (hash >>> 4);
        return hash;
    }

    @Override
    public Iterator<T> iterator() {
        return new HashSetIterator();
    }

    @Override
    public int size() {
        return mSize;
    }

    @Override
    public boolean remove(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }
        int hash = secondaryHash(key);
        SimpleHashSetEntry<T>[] tab = mTable;
        int index = hash & (tab.length - 1);
        for (SimpleHashSetEntry<T> e = tab[index], prev = null; e != null; prev = e, e = e.mNext) {
            if (e.mHash == hash && key.equals(e.mKey)) {
                if (prev == null) {
                    tab[index] = e.mNext;
                } else {
                    prev.mNext = e.mNext;
                }
                mSize--;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean add(T key) {
        if (key == null) {
            throw new NullPointerException();
        }

        int hash = secondaryHash(key);
        SimpleHashSetEntry<T>[] tab = mTable;
        int index = hash & (tab.length - 1);
        for (SimpleHashSetEntry<T> e = tab[index]; e != null; e = e.mNext) {
            if (e.mKey == key || (e.mHash == hash && e.mKey.equals(key))) {
                return false;
            }
        }

        
        if (mSize++ > threshold) {
            tab = grow(2);
            index = hash & (tab.length - 1);
        }
        tab[index] = new SimpleHashSetEntry<>(hash, key);
        return true;
    }

    /**
     * Allocate a table of the given capacity and set the threshold accordingly.
     *
     * @param newCapacity must be a power of two
     */
    private SimpleHashSetEntry<T>[] makeTable(int newCapacity) {
        @SuppressWarnings("unchecked")
        SimpleHashSetEntry<T>[] newTable = (SimpleHashSetEntry<T>[]) new SimpleHashSetEntry[newCapacity];
        mTable = newTable;
        threshold = (newCapacity >> 1) + (newCapacity >> 2); 
        return newTable;
    }

    /**
     * Doubles the capacity of the hash table. Existing entries are placed in
     * the correct bucket on the enlarged table. If the current capacity is,
     * MAXIMUM_CAPACITY, this method is a no-op. Returns the table, which
     * will be new unless we were already at MAXIMUM_CAPACITY.
     */
    private SimpleHashSetEntry<T>[] grow(float factor) {
        SimpleHashSetEntry<T>[] oldTable = mTable;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            return oldTable;
        }
        int newCapacity = (int)Math.floor(oldCapacity * factor);
        SimpleHashSetEntry<T>[] newTable = makeTable(newCapacity);
        if (mSize == 0) {
            return newTable;
        }

        for (int j = 0; j < oldCapacity; j++) {
            /*
             * Rehash the bucket using the minimum number of field writes.
             * This is the most subtle and delicate code in the class.
             */
            SimpleHashSetEntry<T> e = oldTable[j];
            if (e == null) {
                continue;
            }
            int highBit = e.mHash & oldCapacity;
            newTable[j | highBit] = e;
            SimpleHashSetEntry<T> broken = null;
            for (SimpleHashSetEntry<T> n = e.mNext; n != null; e = n, n = n.mNext) {
                int nextHighBit = n.mHash & oldCapacity;
                if (nextHighBit != highBit) {
                    if (broken == null) {
                        newTable[j | nextHighBit] = n;
                    } else {
                        broken.mNext = n;
                    }
                    broken = e;
                    highBit = nextHighBit;
                }
            }
            if (broken != null)
                broken.mNext = null;
        }
        return newTable;
    }

    @Override
    public boolean contains(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }
        int hash = secondaryHash(key);
        SimpleHashSetEntry<T>[] tab = mTable;
        return Stream.iterate(tab[hash & (tab.length - 1)], Objects::nonNull, e -> e.mNext).anyMatch(e -> e.mKey == key || (e.mHash == hash && e.mKey.equals(key)));
    }

    @Override
    public void clear() {
        if (mSize != 0) {
            Arrays.fill(mTable, null);
            mSize = 0;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        /*
         * This could be made more efficient. It unnecessarily hashes all of
         * the elements in the map.
         */
        SimpleHashSet<T> result;
        try {
            result = (SimpleHashSet<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }

        result.makeTable(mTable.length);
        result.mSize = 0;

        result.addAll(this);
        return result;
    }

    private static class SimpleHashSetEntry<T> {

        private final int mHash;
        private final T mKey;
        private SimpleHashSetEntry<T> mNext;

        private SimpleHashSetEntry(int hash, T key) {
            mHash = hash;
            mKey = key;
        }
    }

    private class HashSetIterator implements Iterator<T> {

        int nextIndex;
        SimpleHashSetEntry<T> nextEntry;
        SimpleHashSetEntry<T> lastEntryReturned;

        private HashSetIterator() {

                SimpleHashSetEntry<T>[] tab = mTable;
                SimpleHashSetEntry<T> next = null;
                while (next == null && nextIndex < tab.length) {
                    next = tab[nextIndex++];
                }
                nextEntry = next;

        }

        @Override
        public boolean hasNext() {
            return nextEntry != null;
        }

        @Override
        public T next() {


            SimpleHashSetEntry<T> entryToReturn = nextEntry;
            SimpleHashSetEntry<T>[] tab = mTable;
            SimpleHashSetEntry<T> next = entryToReturn.mNext;
            while (next == null && nextIndex < tab.length) {
                next = tab[nextIndex++];
            }
            nextEntry = next;
            lastEntryReturned = entryToReturn;
            return entryToReturn.mKey;
        }

        @Override
        public void remove() {
            if (lastEntryReturned == null) {
                throw new IllegalStateException();
            }
            SimpleHashSet.this.remove(lastEntryReturned.mKey);
            lastEntryReturned = null;
        }
    }
}