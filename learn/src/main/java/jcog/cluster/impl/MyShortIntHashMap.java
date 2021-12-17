package jcog.cluster.impl;

import jcog.TODO;
import org.eclipse.collections.api.LazyShortIterable;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.ShortIterable;
import org.eclipse.collections.api.block.function.primitive.*;
import org.eclipse.collections.api.block.predicate.primitive.IntPredicate;
import org.eclipse.collections.api.block.predicate.primitive.ShortIntPredicate;
import org.eclipse.collections.api.block.procedure.Procedure;
import org.eclipse.collections.api.block.procedure.Procedure2;
import org.eclipse.collections.api.block.procedure.primitive.ObjectIntProcedure;
import org.eclipse.collections.api.block.procedure.primitive.ShortIntProcedure;
import org.eclipse.collections.api.block.procedure.primitive.ShortProcedure;
import org.eclipse.collections.api.collection.primitive.MutableIntCollection;
import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.api.iterator.MutableShortIterator;
import org.eclipse.collections.api.map.primitive.ImmutableShortIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntShortMap;
import org.eclipse.collections.api.map.primitive.MutableShortIntMap;
import org.eclipse.collections.api.map.primitive.ShortIntMap;
import org.eclipse.collections.api.set.primitive.MutableShortSet;
import org.eclipse.collections.api.tuple.primitive.ShortIntPair;
import org.eclipse.collections.impl.SpreadFunctions;
import org.eclipse.collections.impl.lazy.AbstractLazyIterable;
import org.eclipse.collections.impl.list.mutable.primitive.ShortArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.AbstractMutableIntValuesMap;
import org.eclipse.collections.impl.map.mutable.primitive.AbstractSentinelValues;
import org.eclipse.collections.impl.map.mutable.primitive.MutableShortKeysMap;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;

import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * accelerated short int hashmap. not thread safe
 */
public class MyShortIntHashMap extends AbstractMutableIntValuesMap implements MutableShortIntMap, Externalizable, MutableShortKeysMap {
    private static final int EMPTY_VALUE = 0;
    private static final long serialVersionUID = 1L;
    private static final short EMPTY_KEY = 0;
    private static final short REMOVED_KEY = 1;
    private static final int CACHE_LINE_SIZE = 64;
    private static final int KEY_SIZE = 2;
    private static final int INITIAL_LINEAR_PROBE = 16;
    private static final int DEFAULT_INITIAL_CAPACITY = 8;
    private short[] keys;
    private int[] values;
    private int occupiedWithData;
    private int occupiedWithSentinels;
    private SentinelValues sentinelValues;
    private boolean copyKeysOnWrite;
    private final SentinelValues _sentinel = new SentinelValues();

    public MyShortIntHashMap() {
        this.allocateTable(16);
    }

    public MyShortIntHashMap(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initial capacity cannot be less than 0");
        } else {
            int capacity = MyShortIntHashMap.smallestPowerOfTwoGreaterThan(initialCapacity << 1);
            this.allocateTable(capacity);
        }
    }


    public void addToValues(int d) {

        if (sentinelValues != null) {
            if (sentinelValues.containsZeroKey)
                sentinelValues.zeroValue += d;
            if (sentinelValues.containsOneKey)
                sentinelValues.oneValue += d;
        }

        for (short key : this.keys) {
            if (isNonSentinel(key)) {
                int index = this.probe(key);
                short keyAtIndex = this.keys[index];
                if (keyAtIndex == key) {
                    this.values[index] += d;
                }
            }
        }

    }

    public void filter(IntPredicate toKeep) {
        int ss = size();
        if (ss == 0)
            return;

        SentinelValues sv = this.sentinelValues;
        if (sv != null && sv.containsZeroKey) {
            if (!toKeep.accept(sv.zeroValue)) {
                removeKey((short) 0);
                sv = this.sentinelValues; 
            }
        }
        if (sv != null && sv.containsOneKey) {
            if (!toKeep.accept(sv.oneValue)) {
                removeKey((short) 1);
                sv = this.sentinelValues; 
            }
        }


        ShortArrayList tmp = new ShortArrayList(ss / 2 /* ESTIMATE */);

        short[] keys = this.keys;
        int sizeBefore = keys.length;
        int[] values = this.values;
        for (int i = 0; i < sizeBefore; ++i) {
            short k = keys[i];
            if (isNonSentinel(k) && !toKeep.accept(values[i])) {
                tmp.add(k);
            }
        }

        int s = tmp.size();
        if (s > 0) {
            tmp.forEach(this::removeKey);
        }
    }


    public static MyShortIntHashMap newWithKeysValues(short key1, int value1) {
        return (new MyShortIntHashMap(1)).withKeyValue(key1, value1);
    }

    public static MyShortIntHashMap newWithKeysValues(short key1, int value1, short key2, int value2) {
        return (new MyShortIntHashMap(2)).withKeysValues(key1, value1, key2, value2);
    }

    public static MyShortIntHashMap newWithKeysValues(short key1, int value1, short key2, int value2, short key3, int value3) {
        return (new MyShortIntHashMap(3)).withKeysValues(key1, value1, key2, value2, key3, value3);
    }

    public static MyShortIntHashMap newWithKeysValues(short key1, int value1, short key2, int value2, short key3, int value3, short key4, int value4) {
        return (new MyShortIntHashMap(4)).withKeysValues(key1, value1, key2, value2, key3, value3, key4, value4);
    }

    private static int smallestPowerOfTwoGreaterThan(int n) {
        return n > 1 ? Integer.highestOneBit(n - 1) << 1 : 1;
    }

    @Override
    protected int getOccupiedWithData() {
        return this.occupiedWithData;
    }

    @Override
    protected final AbstractMutableIntValuesMap.SentinelValues getSentinelValues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        SentinelValues s = this.sentinelValues;
        return this.occupiedWithData == 0 && (s == null || s.size() == 0);
    }

    @Override
    public final int size() {
        SentinelValues s = this.sentinelValues;
        return this.occupiedWithData + (s == null ? 0 : s.size());
    }


    @Override
    protected void setSentinelValuesNull() {
        this.sentinelValues = null;
    }

    @Override
    protected int getEmptyValue() {
        return 0;
    }

    @Override
    protected int getTableSize() {
        return this.values.length;
    }

    @Override
    protected int getValueAtIndex(int index) {
        return this.values[index];
    }

    public int hashCode() {
        int result = 0;
        if (this.sentinelValues != null) {
            if (this.sentinelValues.containsZeroKey) {
                result += this.sentinelValues.zeroValue;
            }

            if (this.sentinelValues.containsOneKey) {
                result += 1 ^ this.sentinelValues.oneValue;
            }
        }

        for (int i = 0; i < this.keys.length; ++i) {
            if (isNonSentinel(this.keys[i]))
                result += this.keys[i] ^ this.values[i];
        }

        return result;
    }

    public String toString() {
        StringBuilder appendable = new StringBuilder();
        appendable.append('{');
        boolean first = true;
        if (this.sentinelValues != null) {
            if (this.sentinelValues.containsZeroKey) {
                appendable.append(0).append('=').append(this.sentinelValues.zeroValue);
                first = false;
            }

            if (this.sentinelValues.containsOneKey) {
                if (!first) {
                    appendable.append(", ");
                }

                appendable.append(1).append('=').append(this.sentinelValues.oneValue);
                first = false;
            }
        }

        for (int i = 0; i < this.keys.length; ++i) {
            short key = this.keys[i];
            if (isNonSentinel(key)) {
                if (!first) {
                    appendable.append(", ");
                }

                appendable.append(key).append('=').append(this.values[i]);
                first = false;
            }
        }

        appendable.append('}');
        return appendable.toString();
    }

    @Override
    public ImmutableShortIntMap toImmutable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MutableShortSet keySet() {
        throw new UnsupportedOperationException();
    }

    /*
    public MutableIntIterator intIterator() {
        return new MyShortIntHashMap.InternalIntIterator(null);
    }
    */

    @Override
    public void clear() {
        this.sentinelValues = null;
        this.occupiedWithData = 0;
        this.occupiedWithSentinels = 0;
        if (this.copyKeysOnWrite) {
            this.copyKeys();
        }

        Arrays.fill(this.keys, (short) 0);
        Arrays.fill(this.values, 0);
    }

    @Override
    public MutableIntIterator intIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T injectInto(T t, ObjectIntToObjectFunction<? super T, ? extends T> objectIntToObjectFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void put(short key, int value) {
        if (isEmptyKey(key)) {
            this.putForEmptySentinel(value);
        } else if (isRemovedKey(key)) {
            this.putForRemovedSentinel(value);
        } else {
            int index = this.probe(key);
            short keyAtIndex = this.keys[index];
            if (keyAtIndex == key) {
                this.values[index] = value;
            } else {
                this.addKeyValueAtIndex(key, value, index);
            }

        }
    }

    private void putForRemovedSentinel(int value) {
        sentinelize();

        this.addRemovedKeyValue(value);
    }

    private void sentinelize() {
        if (this.sentinelValues == null) {
            this.sentinelValues = newSentinel();
        }
    }

    private void putForEmptySentinel(int value) {
        sentinelize();

        this.addEmptyKeyValue(value);
    }

    @Override
    protected void addEmptyKeyValue(int value) {
        this.sentinelValues.containsZeroKey = true;
        this.sentinelValues.zeroValue = value;
    }


    @Override
    protected void removeEmptyKey() {
        if (this.sentinelValues.containsOneKey) {
            this.sentinelValues.containsZeroKey = false;
            this.sentinelValues.zeroValue = this.getEmptyValue();
        } else {
            this.setSentinelValuesNull();
        }

    }

    @Override
    protected void addRemovedKeyValue(int value) {
        this.sentinelValues.containsOneKey = true;
        this.sentinelValues.oneValue = value;
    }

    @Override
    protected void removeRemovedKey() {
        if (this.sentinelValues.containsZeroKey) {
            this.sentinelValues.containsOneKey = false;
            this.sentinelValues.oneValue = this.getEmptyValue();
        } else {
            this.setSentinelValuesNull();
        }

    }


    @Override
    public void putAll(ShortIntMap map) {
        map.forEachKeyValue((ShortIntProcedure) MyShortIntHashMap.this::put);
    }

    @Override
    public final void removeKey(short key) {
        if (isEmptyKey(key)) {
            if (this.sentinelValues != null && this.sentinelValues.containsZeroKey) {
                this.removeEmptyKey();
            }
        } else if (isRemovedKey(key)) {
            if (this.sentinelValues != null && this.sentinelValues.containsOneKey) {
                this.removeRemovedKey();
            }
        } else {
            int index = this.probe(key);
            if (this.keys[index] == key) {
                this.removeKeyAtIndex(index);
            }

        }
    }

    @Override
    public void remove(short key) {
        this.removeKey(key);
    }

    @Override
    public int removeKeyIfAbsent(short key, int value) {
        int index;
        if (isEmptyKey(key)) {
            if (this.sentinelValues != null && this.sentinelValues.containsZeroKey) {
                index = this.sentinelValues.zeroValue;
                this.removeEmptyKey();
                return index;
            } else {
                return value;
            }
        } else if (isRemovedKey(key)) {
            if (this.sentinelValues != null && this.sentinelValues.containsOneKey) {
                index = this.sentinelValues.oneValue;
                this.removeRemovedKey();
                return index;
            } else {
                return value;
            }
        } else {
            index = this.probe(key);
            if (this.keys[index] == key) {
                int oldValue = this.values[index];
                this.removeKeyAtIndex(index);
                return oldValue;
            } else {
                return value;
            }
        }
    }

    @Override
    public int getIfAbsentPut(short key, int value) {
        if (isEmptyKey(key)) {
            SentinelValues sv1 = this.sentinelValues;
            if (sv1 == null) {
                this.sentinelValues = newSentinel();
                this.addEmptyKeyValue(value);
                return value;
            } else if (sv1.containsZeroKey) {
                return sv1.zeroValue;
            } else {
                this.addEmptyKeyValue(value);
                return value;
            }
        } else if (isRemovedKey(key)) {
            if (this.sentinelValues == null) {
                this.sentinelValues = newSentinel();
                this.addRemovedKeyValue(value);
                return value;
            } else if (this.sentinelValues.containsOneKey) {
                return this.sentinelValues.oneValue;
            } else {
                this.addRemovedKeyValue(value);
                return value;
            }
        } else {
            int index = this.probe(key);
            if (this.keys[index] == key) {
                return this.values[index];
            } else {
                this.addKeyValueAtIndex(key, value, index);
                return value;
            }
        }
    }

    private SentinelValues newSentinel() {
        _sentinel.clear();
        return _sentinel;
    }

    @Override
    public int getIfAbsentPut(short key, IntFunction0 function) {
        int index;
        if (isEmptyKey(key)) {
            if (this.sentinelValues == null) {
                index = function.value();
                this.sentinelValues = newSentinel();
                this.addEmptyKeyValue(index);
                return index;
            } else if (this.sentinelValues.containsZeroKey) {
                return this.sentinelValues.zeroValue;
            } else {
                index = function.value();
                this.addEmptyKeyValue(index);
                return index;
            }
        } else if (isRemovedKey(key)) {
            if (this.sentinelValues == null) {
                index = function.value();
                this.sentinelValues = newSentinel();
                this.addRemovedKeyValue(index);
                return index;
            } else if (this.sentinelValues.containsOneKey) {
                return this.sentinelValues.oneValue;
            } else {
                index = function.value();
                this.addRemovedKeyValue(index);
                return index;
            }
        } else {
            index = this.probe(key);
            if (this.keys[index] == key) {
                return this.values[index];
            } else {
                int value = function.value();
                this.addKeyValueAtIndex(key, value, index);
                return value;
            }
        }
    }

    @Override
    public <P> int getIfAbsentPutWith(short key, IntFunction<? super P> function, P parameter) {
        int index;
        if (isEmptyKey(key)) {
            if (this.sentinelValues == null) {
                index = function.intValueOf(parameter);
                this.sentinelValues = newSentinel();
                this.addEmptyKeyValue(index);
                return index;
            } else if (this.sentinelValues.containsZeroKey) {
                return this.sentinelValues.zeroValue;
            } else {
                index = function.intValueOf(parameter);
                this.addEmptyKeyValue(index);
                return index;
            }
        } else if (isRemovedKey(key)) {
            if (this.sentinelValues == null) {
                index = function.intValueOf(parameter);
                this.sentinelValues = newSentinel();
                this.addRemovedKeyValue(index);
                return index;
            } else if (this.sentinelValues.containsOneKey) {
                return this.sentinelValues.oneValue;
            } else {
                index = function.intValueOf(parameter);
                this.addRemovedKeyValue(index);
                return index;
            }
        } else {
            index = this.probe(key);
            if (this.keys[index] == key) {
                return this.values[index];
            } else {
                int value = function.intValueOf(parameter);
                this.addKeyValueAtIndex(key, value, index);
                return value;
            }
        }
    }

    @Override
    public int getIfAbsentPutWithKey(short key, ShortToIntFunction function) {
        int index;
        if (isEmptyKey(key)) {
            if (this.sentinelValues == null) {
                index = function.valueOf(key);
                this.sentinelValues = newSentinel();
                this.addEmptyKeyValue(index);
                return index;
            } else if (this.sentinelValues.containsZeroKey) {
                return this.sentinelValues.zeroValue;
            } else {
                index = function.valueOf(key);
                this.addEmptyKeyValue(index);
                return index;
            }
        } else if (isRemovedKey(key)) {
            if (this.sentinelValues == null) {
                index = function.valueOf(key);
                this.sentinelValues = newSentinel();
                this.addRemovedKeyValue(index);
                return index;
            } else if (this.sentinelValues.containsOneKey) {
                return this.sentinelValues.oneValue;
            } else {
                index = function.valueOf(key);
                this.addRemovedKeyValue(index);
                return index;
            }
        } else {
            index = this.probe(key);
            if (this.keys[index] == key) {
                return this.values[index];
            } else {
                int value = function.valueOf(key);
                this.addKeyValueAtIndex(key, value, index);
                return value;
            }
        }
    }

    @Override
    public int addToValue(short key, int toBeAdded) {
        if (isEmptyKey(key)) {
            if (this.sentinelValues == null) {
                this.sentinelValues = newSentinel();
                this.addEmptyKeyValue(toBeAdded);
            } else if (this.sentinelValues.containsZeroKey) {
                this.sentinelValues.zeroValue += toBeAdded;
            } else {
                this.addEmptyKeyValue(toBeAdded);
            }

            return this.sentinelValues.zeroValue;
        } else if (isRemovedKey(key)) {
            if (this.sentinelValues == null) {
                this.sentinelValues = newSentinel();
                this.addRemovedKeyValue(toBeAdded);
            } else if (this.sentinelValues.containsOneKey) {
                this.sentinelValues.oneValue += toBeAdded;
            } else {
                this.addRemovedKeyValue(toBeAdded);
            }

            return this.sentinelValues.oneValue;
        } else {
            int index = this.probe(key);
            if (this.keys[index] == key) {
                this.values[index] += toBeAdded;
            } else {
                this.addKeyValueAtIndex(key, toBeAdded, index);
            }
            return this.values[index];
        }
    }

    private void addKeyValueAtIndex(short key, int value, int index) {
        if (this.keys[index] == 1) {
            --this.occupiedWithSentinels;
        }

        if (this.copyKeysOnWrite) {
            this.copyKeys();
        }

        this.keys[index] = key;
        this.values[index] = value;
        ++this.occupiedWithData;
        if (this.occupiedWithData + this.occupiedWithSentinels > this.maxOccupiedWithData()) {
            this.rehashAndGrow();
        }

    }

    private void removeKeyAtIndex(int index) {
        if (this.copyKeysOnWrite) {
            this.copyKeys();
        }

        this.keys[index] = 1;
        this.values[index] = 0;
        --this.occupiedWithData;
        ++this.occupiedWithSentinels;
    }

    private void copyKeys() {
        short[] copy = new short[this.keys.length];
        System.arraycopy(this.keys, 0, copy, 0, this.keys.length);
        this.keys = copy;
        this.copyKeysOnWrite = false;
    }

    @Override
    public void updateValues(ShortIntToIntFunction shortIntToIntFunction) {
        throw new TODO();
    }

    @Override
    public int updateValue(short key, int initialValueIfAbsent, IntToIntFunction function) {
        if (isEmptyKey(key)) {
            if (this.sentinelValues == null) {
                this.sentinelValues = newSentinel();
                this.addEmptyKeyValue(function.valueOf(initialValueIfAbsent));
            } else if (this.sentinelValues.containsZeroKey) {
                this.sentinelValues.zeroValue = function.valueOf(this.sentinelValues.zeroValue);
            } else {
                this.addEmptyKeyValue(function.valueOf(initialValueIfAbsent));
            }

            return this.sentinelValues.zeroValue;
        } else if (isRemovedKey(key)) {
            if (this.sentinelValues == null) {
                this.sentinelValues = newSentinel();
                this.addRemovedKeyValue(function.valueOf(initialValueIfAbsent));
            } else if (this.sentinelValues.containsOneKey) {
                this.sentinelValues.oneValue = function.valueOf(this.sentinelValues.oneValue);
            } else {
                this.addRemovedKeyValue(function.valueOf(initialValueIfAbsent));
            }

            return this.sentinelValues.oneValue;
        } else {
            int index = this.probe(key);
            if (this.keys[index] == key) {
                this.values[index] = function.valueOf(this.values[index]);
                return this.values[index];
            } else {
                int value = function.valueOf(initialValueIfAbsent);
                this.addKeyValueAtIndex(key, value, index);
                return value;
            }
        }
    }

    @Override
    public MutableIntShortMap flipUniqueValues() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public MutableShortIntMap select(ShortIntPredicate shortIntPredicate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MutableShortIntMap reject(ShortIntPredicate shortIntPredicate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MyShortIntHashMap withKeyValue(short key1, int value1) {
        this.put(key1, value1);
        return this;
    }

    public MyShortIntHashMap withKeysValues(short key1, int value1, short key2, int value2) {
        this.put(key1, value1);
        this.put(key2, value2);
        return this;
    }

    public MyShortIntHashMap withKeysValues(short key1, int value1, short key2, int value2, short key3, int value3) {
        this.put(key1, value1);
        this.put(key2, value2);
        this.put(key3, value3);
        return this;
    }

    public MyShortIntHashMap withKeysValues(short key1, int value1, short key2, int value2, short key3, int value3, short key4, int value4) {
        this.put(key1, value1);
        this.put(key2, value2);
        this.put(key3, value3);
        this.put(key4, value4);
        return this;
    }

    @Override
    public MyShortIntHashMap withoutKey(short key) {
        this.removeKey(key);
        return this;
    }

    @Override
    public MyShortIntHashMap withoutAllKeys(ShortIterable keys) {
        keys.forEach((ShortProcedure) MyShortIntHashMap.this::removeKey);
        return this;
    }

    @Override
    public MutableShortIntMap asUnmodifiable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MutableShortIntMap asSynchronized() {
        throw new UnsupportedOperationException();
    }













    @Override
    public int get(short key) {
        return this.getIfAbsent(key, 0);
    }

    @Override
    public int getIfAbsent(short key, int ifAbsent) {
        return !isEmptyKey(key) && !isRemovedKey(key) ? (this.occupiedWithSentinels == 0 ? this.fastGetIfAbsent(key, ifAbsent) : this.slowGetIfAbsent(key, ifAbsent)) : this.getForSentinel(key, ifAbsent);
    }

    private int getForSentinel(short key, int ifAbsent) {
        return isEmptyKey(key) ? (this.sentinelValues != null && this.sentinelValues.containsZeroKey ? this.sentinelValues.zeroValue : ifAbsent) : (this.sentinelValues != null && this.sentinelValues.containsOneKey ? this.sentinelValues.oneValue : ifAbsent);
    }

    private int slowGetIfAbsent(short key, int ifAbsent) {
        int index = this.probe(key);
        return this.keys[index] == key ? this.values[index] : ifAbsent;
    }

    private int fastGetIfAbsent(short key, int ifAbsent) {
        int index = this.mask(key);

        for (int i = 0; i < 16; ++i) {
            short keyAtIndex = this.keys[index];
            if (keyAtIndex == key) {
                return this.values[index];
            }

            if (keyAtIndex == 0) {
                return ifAbsent;
            }

            index = index + 1 & this.keys.length - 1;
        }

        return this.slowGetIfAbsentTwo(key, ifAbsent);
    }

    private int slowGetIfAbsentTwo(short key, int ifAbsent) {
        int index = this.probeTwo(key, -1);
        return this.keys[index] == key ? this.values[index] : ifAbsent;
    }

    @Override
    public int getOrThrow(short key) {
        if (isEmptyKey(key)) {
            if (this.sentinelValues != null && this.sentinelValues.containsZeroKey) {
                return this.sentinelValues.zeroValue;
            } else {
                throw new IllegalStateException("Key " + key + " not present.");
            }
        } else if (isRemovedKey(key)) {
            if (this.sentinelValues != null && this.sentinelValues.containsOneKey) {
                return this.sentinelValues.oneValue;
            } else {
                throw new IllegalStateException("Key " + key + " not present.");
            }
        } else {
            int index = this.probe(key);
            if (isNonSentinel(this.keys[index])) {
                return this.values[index];
            } else {
                throw new IllegalStateException("Key " + key + " not present.");
            }
        }
    }

    @Override
    public boolean containsKey(short key) {
        return isEmptyKey(key) ? this.sentinelValues != null && this.sentinelValues.containsZeroKey : (isRemovedKey(key) ? this.sentinelValues != null && this.sentinelValues.containsOneKey : this.keys[this.probe(key)] == key);
    }

    @Override
    public void forEachKey(ShortProcedure procedure) {
        SentinelValues s = this.sentinelValues;
        if (s != null) {
            if (s.containsZeroKey) {
                procedure.value((short) 0);
            }

            if (s.containsOneKey) {
                procedure.value((short) 1);
            }
        }

        short[] kk = this.keys;
        int l = kk.length;
        for (short k : kk) {
            if (isNonSentinel(k)) {
                procedure.value(k);
            }
        }

    }

    @Override
    public void forEachKeyValue(ShortIntProcedure procedure) {
        SentinelValues s = this.sentinelValues;
        if (s != null) {
            if (s.containsZeroKey) {
                procedure.value((short) 0, s.zeroValue);
            }

            if (s.containsOneKey) {
                procedure.value((short) 1, s.oneValue);
            }
        }

        short[] kk = this.keys;
        int[] values = this.values;
        int l = kk.length;
        for (int i = 0; i < l; ++i) {
            short key = kk[i];
            if (isNonSentinel(key)) {
                procedure.value(key, values[i]);
            }
        }

    }

    @Override
    public LazyShortIterable keysView() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RichIterable<ShortIntPair> keyValuesView() {
        throw new UnsupportedOperationException();
    }



















































































    public void compact() {

        this.rehash(MyShortIntHashMap.smallestPowerOfTwoGreaterThan(this.size()));
    }

    private void rehashAndGrow() {
        this.rehash(this.keys.length << 1);
    }

    private void rehash(int newCapacity) {
        int oldLength = this.keys.length;
        short[] old = this.keys;
        int[] oldValues = this.values;
        this.allocateTable(newCapacity);
        this.occupiedWithData = 0;
        this.occupiedWithSentinels = 0;

        for (int i = 0; i < oldLength; ++i) {
            if (isNonSentinel(old[i])) {
                this.put(old[i], oldValues[i]);
            }
        }

    }

    int probe(short element)
    {
        int index = this.mask(element);
        short keyAtIndex = this.keys[index];

        if (keyAtIndex == element || keyAtIndex == EMPTY_KEY)
        {
            return index;
        }

        int removedIndex = keyAtIndex == REMOVED_KEY ? index : -1;
        for (int i = 1; i < INITIAL_LINEAR_PROBE; i++)
        {
            int nextIndex = (index + i) & (this.keys.length - 1);
            keyAtIndex = this.keys[nextIndex];
            if (keyAtIndex == element)
            {
                return nextIndex;
            }
            if (keyAtIndex == EMPTY_KEY)
            {
                return removedIndex == -1 ? nextIndex : removedIndex;
            }
            if (keyAtIndex == REMOVED_KEY && removedIndex == -1)
            {
                removedIndex = nextIndex;
            }
        }
        return this.probeTwo(element, removedIndex);
    }

    int probeTwo(short element, int removedIndex) {
        int index = this.spreadTwoAndMask(element);
        short[] keys = this.keys;
        int kl = keys.length;

        for (int i = 0; i < 16; ++i) {

            int nextIndex = index + i & kl - 1;
            short keyAtIndex = keys[nextIndex];
            if (keyAtIndex == element) {
                return nextIndex;
            }

            if (keyAtIndex == 0) {
                return removedIndex == -1 ? nextIndex : removedIndex;
            }

            if (keyAtIndex == 1 && removedIndex == -1) {
                removedIndex = nextIndex;
            }
        }

        return this.probeThree(element, removedIndex);
    }

    int probeThree(short element, int removedIndex) {
        int nextIndex = SpreadFunctions.shortSpreadOne(element);
        int spreadTwo = Integer.reverse(SpreadFunctions.shortSpreadTwo(element)) | 1;

        short[] keys = this.keys;

        while (true) {
            nextIndex = this.mask(nextIndex + spreadTwo);

            short keyAtIndex = keys[nextIndex];
            if (keyAtIndex == element) {
                return nextIndex;
            }

            if (keyAtIndex == 0) {
                return removedIndex == -1 ? nextIndex : removedIndex;
            }

            if (keyAtIndex == 1 && removedIndex == -1) {
                removedIndex = nextIndex;
            }
        }
    }

    int spreadAndMask(short element) {
        int code = SpreadFunctions.shortSpreadOne(element);
        return this.mask(code);
    }

    int spreadTwoAndMask(short element) {
        int code = SpreadFunctions.shortSpreadTwo(element);
        return this.mask(code);
    }

    private int mask(int spread) {
        return spread & this.keys.length - 1;
    }

    private void allocateTable(int sizeToAllocate) {
        this.keys = new short[sizeToAllocate];
        this.values = new int[sizeToAllocate];
    }

    private static boolean isEmptyKey(short key) {
        return key == 0;
    }

    private static boolean isRemovedKey(short key) {
        return key == 1;
    }





    private static boolean isNonSentinel(short k) {
        
        return k > 1 || k < 0;
        
    }

    @Override
    protected boolean isNonSentinelAtIndex(int index) {
        short k = this.keys[index];
        return isNonSentinel(keys[index]);
    }

    private int maxOccupiedWithData() {
        return this.keys.length >> 1;
    }

    private int maxOccupiedWithSentinels() {
        return this.keys.length >> 2;
    }

    @Override
    public MutableIntCollection values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeExternal(ObjectOutput out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readExternal(ObjectInput in) {
        throw new UnsupportedOperationException();
    }

    public float density() {
        int kl = capacity();
        return kl == 0 ? 0 : size() / ((float) kl);
    }

    public int capacity() {
        return keys.length;
    }









    private class KeyValuesView extends AbstractLazyIterable<ShortIntPair> {
        private KeyValuesView() {
        }

        @Override
        public void each(Procedure<? super ShortIntPair> procedure) {
            if (MyShortIntHashMap.this.sentinelValues != null) {
                if (MyShortIntHashMap.this.sentinelValues.containsZeroKey) {
                    procedure.value(PrimitiveTuples.pair((short) 0, MyShortIntHashMap.this.sentinelValues.zeroValue));
                }

                if (MyShortIntHashMap.this.sentinelValues.containsOneKey) {
                    procedure.value(PrimitiveTuples.pair((short) 1, MyShortIntHashMap.this.sentinelValues.oneValue));
                }
            }

            for (int i = 0; i < MyShortIntHashMap.this.keys.length; ++i) {
                if (MyShortIntHashMap.isNonSentinel(MyShortIntHashMap.this.keys[i])) {
                    procedure.value(PrimitiveTuples.pair(MyShortIntHashMap.this.keys[i], MyShortIntHashMap.this.values[i]));
                }
            }

        }

        @Override
        public void forEachWithIndex(ObjectIntProcedure<? super ShortIntPair> objectIntProcedure) {
            int index = 0;
            if (MyShortIntHashMap.this.sentinelValues != null) {
                if (MyShortIntHashMap.this.sentinelValues.containsZeroKey) {
                    objectIntProcedure.value(PrimitiveTuples.pair((short) 0, MyShortIntHashMap.this.sentinelValues.zeroValue), index);
                    ++index;
                }

                if (MyShortIntHashMap.this.sentinelValues.containsOneKey) {
                    objectIntProcedure.value(PrimitiveTuples.pair((short) 1, MyShortIntHashMap.this.sentinelValues.oneValue), index);
                    ++index;
                }
            }

            for (int i = 0; i < MyShortIntHashMap.this.keys.length; ++i) {
                if (MyShortIntHashMap.isNonSentinel(MyShortIntHashMap.this.keys[i])) {
                    objectIntProcedure.value(PrimitiveTuples.pair(MyShortIntHashMap.this.keys[i], MyShortIntHashMap.this.values[i]), index);
                    ++index;
                }
            }

        }

        @Override
        public <P> void forEachWith(Procedure2<? super ShortIntPair, ? super P> procedure, P parameter) {
            if (MyShortIntHashMap.this.sentinelValues != null) {
                if (MyShortIntHashMap.this.sentinelValues.containsZeroKey) {
                    procedure.value(PrimitiveTuples.pair((short) 0, MyShortIntHashMap.this.sentinelValues.zeroValue), parameter);
                }

                if (MyShortIntHashMap.this.sentinelValues.containsOneKey) {
                    procedure.value(PrimitiveTuples.pair((short) 1, MyShortIntHashMap.this.sentinelValues.oneValue), parameter);
                }
            }

            for (int i = 0; i < MyShortIntHashMap.this.keys.length; ++i) {
                if (MyShortIntHashMap.isNonSentinel(MyShortIntHashMap.this.keys[i])) {
                    procedure.value(PrimitiveTuples.pair(MyShortIntHashMap.this.keys[i], MyShortIntHashMap.this.values[i]), parameter);
                }
            }

        }

        @Override
        public Iterator<ShortIntPair> iterator() {
            return new InternalKeyValuesIterator();
        }

        public class InternalKeyValuesIterator implements Iterator<ShortIntPair> {
            private int count;
            private int position;
            private boolean handledZero;
            private boolean handledOne;

            public InternalKeyValuesIterator() {
            }

            @Override
            public ShortIntPair next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException("next() called, but the iterator is exhausted");
                } else {
                    ++this.count;
                    if (!this.handledZero) {
                        this.handledZero = true;
                        if (MyShortIntHashMap.this.containsKey((short) 0)) {
                            return PrimitiveTuples.pair((short) 0, MyShortIntHashMap.this.sentinelValues.zeroValue);
                        }
                    }

                    if (!this.handledOne) {
                        this.handledOne = true;
                        if (MyShortIntHashMap.this.containsKey((short) 1)) {
                            return PrimitiveTuples.pair((short) 1, MyShortIntHashMap.this.sentinelValues.oneValue);
                        }
                    }

                    short[] keys;
                    for (keys = MyShortIntHashMap.this.keys; !MyShortIntHashMap.isNonSentinel(keys[this.position]); ++this.position) {
                    }

                    ShortIntPair result = PrimitiveTuples.pair(keys[this.position], MyShortIntHashMap.this.values[this.position]);
                    ++this.position;
                    return result;
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Cannot call remove() on " + this.getClass().getSimpleName());
            }

            @Override
            public boolean hasNext() {
                return this.count != MyShortIntHashMap.this.size();
            }
        }
    }

















































































































    private class KeySetIterator implements MutableShortIterator {
        private int count;
        private int position;
        private short lastKey;
        private boolean handledZero;
        private boolean handledOne;
        private boolean canRemove;

        private KeySetIterator() {
        }

        @Override
        public boolean hasNext() {
            return this.count < MyShortIntHashMap.this.size();
        }

        @Override
        public short next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException("next() called, but the iterator is exhausted");
            } else {
                ++this.count;
                this.canRemove = true;
                if (!this.handledZero) {
                    this.handledZero = true;
                    if (MyShortIntHashMap.this.containsKey((short) 0)) {
                        this.lastKey = 0;
                        return this.lastKey;
                    }
                }

                if (!this.handledOne) {
                    this.handledOne = true;
                    if (MyShortIntHashMap.this.containsKey((short) 1)) {
                        this.lastKey = 1;
                        return this.lastKey;
                    }
                }

                short[] keys;
                for (keys = MyShortIntHashMap.this.keys; !MyShortIntHashMap.isNonSentinel(keys[this.position]); ++this.position) {
                }

                this.lastKey = keys[this.position];
                ++this.position;
                return this.lastKey;
            }
        }

        @Override
        public void remove() {
            if (!this.canRemove) {
                throw new IllegalStateException();
            } else {
                MyShortIntHashMap.this.removeKey(this.lastKey);
                --this.count;
                this.canRemove = false;
            }
        }
    }














    private class InternalIntIterator implements MutableIntIterator {
        private int count;
        private int position;
        private short lastKey;
        private boolean handledZero;
        private boolean handledOne;
        private boolean canRemove;

        private InternalIntIterator() {
        }

        @Override
        public boolean hasNext() {
            return this.count < MyShortIntHashMap.this.size();
        }

        @Override
        public int next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException("next() called, but the iterator is exhausted");
            } else {
                ++this.count;
                this.canRemove = true;
                if (!this.handledZero) {
                    this.handledZero = true;
                    if (MyShortIntHashMap.this.containsKey((short) 0)) {
                        this.lastKey = 0;
                        return MyShortIntHashMap.this.get((short) 0);
                    }
                }

                if (!this.handledOne) {
                    this.handledOne = true;
                    if (MyShortIntHashMap.this.containsKey((short) 1)) {
                        this.lastKey = 1;
                        return MyShortIntHashMap.this.get((short) 1);
                    }
                }

                short[] keys;
                for (keys = MyShortIntHashMap.this.keys; !MyShortIntHashMap.isNonSentinel(keys[this.position]); ++this.position) {
                }

                this.lastKey = keys[this.position];
                int result = MyShortIntHashMap.this.values[this.position];
                ++this.position;
                return result;
            }
        }

        @Override
        public void remove() {
            if (!this.canRemove) {
                throw new IllegalStateException();
            } else {
                MyShortIntHashMap.this.removeKey(this.lastKey);
                --this.count;
                this.canRemove = false;
            }
        }
    }

    static final class SentinelValues extends AbstractSentinelValues {

        public boolean containsZeroKey;
        public boolean containsOneKey;


        @Override
        public int size() {
            return (this.containsZeroKey ? 1 : 0) + (this.containsOneKey ? 1 : 0);
        }

        public int zeroValue;
        public int oneValue;

        SentinelValues() {
        }

        public void clear() {
            zeroValue = oneValue = 0;
            containsZeroKey = containsOneKey = false;
        }

        public boolean containsValue(int value) {
            boolean valueEqualsZeroValue = this.containsZeroKey && this.zeroValue == value;
            boolean valueEqualsOneValue = this.containsOneKey && this.oneValue == value;
            return valueEqualsZeroValue || valueEqualsOneValue;
        }

        
        
        
        
        
        
        
        
    }
}
