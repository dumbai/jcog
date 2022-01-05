package jcog.data.list;

import jcog.TODO;
import jcog.Util;
import jcog.data.bit.MetalBitSet;
import jcog.data.iterator.ArrayIterator;
import jcog.random.RandomBits;
import jcog.sort.QuickSort;
import jcog.util.ArrayUtil;
import jcog.util.SingletonIterator;
import org.eclipse.collections.api.block.function.primitive.DoubleFunction;
import org.eclipse.collections.api.block.function.primitive.IntToDoubleFunction;
import org.eclipse.collections.api.block.function.primitive.LongFunction;
import org.eclipse.collections.api.block.function.primitive.*;
import org.eclipse.collections.api.block.predicate.primitive.IntObjectPredicate;
import org.eclipse.collections.api.block.procedure.Procedure2;
import org.eclipse.collections.api.map.primitive.MutableObjectFloatMap;
import org.eclipse.collections.impl.block.factory.Functions;
import org.eclipse.collections.impl.block.factory.HashingStrategies;
import org.eclipse.collections.impl.list.mutable.MutableListIterator;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectFloatHashMapWithHashingStrategy;
import org.eclipse.collections.impl.utility.ListIterate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.*;
import java.util.stream.Stream;

import static java.lang.System.arraycopy;

/**
 * Less-safe faster FastList with direct array access
 * <p>
 * TODO override the array creation to create an array
 * of the actual type necessary, so that .array()
 * can provide the right array when casted
 *
 * see: https://github.com/cliffclick/aa/blob/closure/src/main/java/com/cliffc/aa/util/Ary.java
 */
public class Lst<X> implements List<X>, RandomAccess {

    private static final int INITIAL_SIZE_IF_GROWING_FROM_EMPTY = 4;
    protected int size;
    protected transient X[] items;

    public Lst() {
        this(0);
    }

    public Lst(int initialCapacity) {
        this(0, (X[]) (initialCapacity > 0 ? new Object[initialCapacity] : ArrayUtil.EMPTY_OBJECT_ARRAY));
    }

    public Lst(Iterable<X> copy) {
        this(copy, copy instanceof Collection ? ((Collection<X>)copy).size() : 0);
    }

    public Lst(Iterator<X> copy) {
        this(copy, INITIAL_SIZE_IF_GROWING_FROM_EMPTY);
    }

    public Lst(Iterator<X> copy, int sizeEstimate) {
        this(sizeEstimate);
        while (copy.hasNext())
            add(copy.next());
    }

    public Lst(Iterable<X> copy, int sizeEstimate) {
        this(sizeEstimate);
        for (X x : copy)
            add(x);
    }


    /**
     * uses array directly
     */
    public Lst(int size, X[] x) {
        this.items = x;
        this.size = size;
        if (size > x.length)
            ensureCapacity(size);
    }

    public Lst(X[] x) {
        this(x.length, x);
    }

    private static int sizePlusFiftyPercent(int oldSize) {
        return oldSize + (oldSize / 2) + 1;
        //return result < oldSize ? (Integer.MAX_VALUE - 8) : result;
    }

    public static <T> T max(T[] array, int size, Comparator<? super T> comparator) {
        return min(array, size, comparator.reversed());
    }

    public static <T> T min(T[] array, int size, Comparator<? super T> comparator) {
        if (size == 0) throw new NoSuchElementException();
        T min = array[0];
        for(int i = 1; i < size; ++i) {
            T x = array[i];
            if (comparator.compare(x, min) < 0) min = x;
        }
        return min;
    }

    public final boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return ArrayUtil.indexOf(items, o, 0, size)!=-1;
    }

    /**
     * use with caution
     */
    public void setArray(X[] v) {
        items = v;
        size = v!=null ? v.length : 0;
    }

    @Override public Stream<X> stream() {
        return ArrayIterator.stream(items, size);
    }

    public Lst<X> sortThisByLong(LongFunction<? super X> function) {
        if (size > 1) sort(Functions.toLongComparator(function));
        return this;
    }

    public Lst<X> sortThisByFloat(FloatFunction<? super X> f) {
        return sortThisByFloat(f, false);
//        int size = this.size;
//        if (size > 1) {
//            //sort(Functions.toFloatComparator(function));
//            QuickSort.sort(items, 0, size, (int i) -> f.floatValueOf(items[i]));
//        }
//        return this;
    }
    public Lst<X> sortThisByFloat(FloatFunction<? super X> f, boolean cached) {
        int size = this.size;
        if (size > 1) {
            IntToDoubleFunction each = size > 2 && cached ?
                new FloatValueCacher(size, f) :
                (int i) -> f.floatValueOf(items[i]);

            QuickSort.sort(items, 0, size, each);
        }
        return this;
    }


    public final void setNull(int i) {
        setFast(i, null);
    }

    public Lst<X> sortThisByDouble(DoubleFunction<? super X> function) {
        int size = this.size;
        if (size > 1) {
            //sort(Functions.toDoubleComparator(function));
            QuickSort.sort(items, 0, size, (int i) -> function.doubleValueOf(items[i]));
        }
        return this;
    }

    public Lst<X> sortThisByInt(ToIntFunction<? super X> function) {
        int size = this.size;
        if (size > 1) {
            //sort(Comparator.comparingInt(function));
            QuickSort.sort(items, 0, size, (int i) -> function.applyAsInt(items[i]));
        }
        return this;
    }

    public Lst<X> sortThis() {
        sort(null);
        return this;
    }

    public Lst<X> reverseThis() {
        int s = this.size;
        if (s > 1) { //superclass doesnt test for this condition
            ArrayUtil.reverse(items, 0, s);
        }

        return this;
    }

    public void assertNoNulls() {
        ArrayUtil.assertNoNulls(items, 0, size);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        ListIterate.appendString(this, sb, "[", ", ", "]");
        return sb.toString();
    }
//
//    @Override
//    public FastList<X> toSortedList() {
//        //TODO size=2 simple case
//        return size > 1 ? super.toSortedList() : this;
//    }
//
//    @Override
//    public <V extends Comparable<? super V>> MutableList<X> toSortedListBy(Function<? super X, ? extends V> function) {
//        //TODO size=2 simple case
//        return size > 1 ? super.toSortedListBy(function) : this;
//    }
//
//    @Override
//    public FastList<X> toSortedList(Comparator<? super X> comparator) {
//        //TODO size=2 simple case
//        return size > 1 ? super.toSortedList(comparator) : this;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Lst<?> l) {
            return _equals(l);
        } else {
            return o instanceof List && ListIterate.equals(((List)o), this);
        }
    }

    private boolean _equals(Lst<?> lst) {
        int s = this.size;
        if (s != lst.size) return false;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return s==0 || Arrays.equals(items, 0, s, lst.items, 0, s);
    }

    /** adapted from AbstractList<X> */
    @Override public int hashCode() {
        int h = 1;
        X[] ii = this.items;
        for (int i = 0, s = this.size(); i < s; i++) {
            X e = ii[i];
            h = 31 * h + (e == null ? 0 : e.hashCode());
        }
        return h;
    }

    public final void ensureCapacityForAdditional(int num, boolean grow) {
        int s = this.size + num;
        int l = this.items.length;
        if (l < s)
            ensureCapacity(grow ? (l == 0 ? s : sizePlusFiftyPercent(s)) : s);
    }

    public void ensureCapacity(int minCapacity) {
        if (minCapacity > this.items.length)
            this.items = newArray(minCapacity);
    }

    public void clearCapacity(int newCapacity) {
        size = 0;
        items = newArray(newCapacity);
    }

//    public void clearHard() {
//        this.size = 0;
//        this.items = (X[]) ArrayUtils.EMPTY_OBJECT_ARRAY;
//    }

    @Override
    public final int size() {
        return size;
    }

    /**
     * pop()
     */
    public X removeLast() {
        X[] ii = this.items;
        int s;
        X x = ii[s = --size];
        ii[s] = null;
        return x;
    }

    /**
     * pop()
     */
    public void removeLastFast() {
        items[--size] = null;
    }

    /**
     * removes last item or returns null if empty, similar to Queue.poll()
     * TODO Queue.poll invokes removeFirst(), not removeLast(
     */
    @Nullable public final X poll() {
        return size == 0 ? null : removeLast();
    }

    @Nullable @Override public final X get(int index) {
        return items[index];
    }

    @Override public void replaceAll(UnaryOperator<X> operator) {
        int s = size;
        X[] ii = this.items;
        for (int i = 0; i < s; i++)
            ii[i] = operator.apply(ii[i]);
    }

    @Override
    public final X set(int index, X next) {
        X prev = get(index);
        setFast(index, next);
        return prev;
    }

    public X getLast() {
        int s = this.size;
        if (s <= 0) throw new NoSuchElementException();
        return get(s - 1);
    }

    public X getFirst() {
        int s = this.size;
        if (s <= 0) throw new NoSuchElementException();
        return get(0);
    }

    @Deprecated public final @Nullable X get(Random random) {
        int s = this.size;
        X[] ii = this.items;
        return switch (s) {
            case 0 -> null;
            case 1 -> ii[0];
            default -> ii[random.nextInt(Math.min(s, ii.length))];
        };
    }

//    public final @Nullable X getSafe(int index) {
//        if (index < 0)
//            return null;
//        X[] items = this.items;
//        return items != null && index < Math.min(items.length, this.size) ? items[index] : null;
//    }


    public @Nullable X get(RandomBits random) {
        X[] ii = this.items;
        int s = this.size;
        return switch (Math.min(s, ii.length)) {
            case 0 -> null;
            case 1 -> ii[0];
            default -> ii[random.nextInt(s)];
        };
    }


    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public final int indexOf(Predicate<X> p) {
        return indexOf(0, p);
    }

    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public int indexOf(int atOrAfter, Predicate<X> p) {
        int s = size;
        if (s > 0) {
            X[] items = this.items;
            for (int i = Math.max(0, atOrAfter); i < s; i++) {
                if (p.test(items[i]))
                    return i;
            }
        }
        return -1;
    }

    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public final int indexOf(IntPredicate p) {
        return indexOf(0, p);
    }

    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public int indexOf(int atOrAfter, IntPredicate p) {
        int s = size;
        if (s > 0) {
            for (int i = Math.max(0, atOrAfter); i < s; i++) {
                if (p.test(i))
                    return i;
            }
        }
        return -1;
    }

    @Override
    public int indexOf(/**/ Object object) {

        if (object == null)
            return firstIndexOfInstance(null);

        int s = size;
        if (s > 0) {
            X[] items = this.items;
            for (int i = 0; i < s; i++)
                if (object.equals(items[i]))
                    return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new TODO();
    }

    @Override
    public ListIterator<X> listIterator() {
        return new MutableListIterator<>(this, 0);
    }

    @Override
    public ListIterator<X> listIterator(int index) {
        return new MutableListIterator<>(this, index);
    }

    @Override
    public List<X> subList(int fromIndex, int toIndex) {
        return fromIndex==toIndex ? Collections.EMPTY_LIST :
                new Lst(Arrays.copyOfRange(items, fromIndex, toIndex));
    }

    public int firstIndexOfInstance(/**/ Object x) {
        int s = size;
        if (s > 0) {
            X[] items = this.items;
            for (int i = 0; i < s; i++)
                if (items[i] == x) return i;
        }
        return -1;
    }

    public X min(Comparator<? super X> comparator) {
        return min(this.items, this.size, comparator);
    }

    public X max(Comparator<? super X> comparator) {
        return max(this.items, this.size, comparator);
    }

    public Lst<X> shuffleThis(Random rng) {

        return shuffleThis(new RandomBits(rng));
    }

    public Lst<X> shuffleThis(RandomBits rng) {
        //return size > 1 ? (FasterList<X>) super.shuffleThis(rnd) : this;
        int s = this.size;
        if (s > 1) {
            for(int i = s; i > 1; --i) //TODO ArrayUtil.shuffle(...)
                swap(i - 1, rng.nextInt(i));
        }
        return this;
    }

    /**
     * use with caution.
     * --this could become invalidated so use it as a snapshot
     * --dont modify it
     * --when iterating, expect to encounter a null
     * at any time, and if this happens, break your loop
     * early
     * *
     */
    public final X[] array() {
        return items;
    }

    public void trimToSize() {
        int s = this.size;
        if (items.length != s) {
            this.items = s == 0 ? (X[]) ArrayUtil.EMPTY_OBJECT_ARRAY : Arrays.copyOf(this.items, s);
        }
    }

    @Override
    public X[] toArray() {
        int s = this.size;
        //return s > 0 ? Arrays.copyOf(items, s) : (X[]) ArrayUtil.EMPTY_OBJECT_ARRAY;
        return Arrays.copyOf(items, s);
    }



    @Override
    public <T> T[] toArray(T[] y) {
        int s = this.size;
        if (y.length < s)
            y = Arrays.copyOf(y, s);
        arraycopy(items, 0, y, 0, size);
        return y;
    }

    /**
     * returns the array directly, or reconstructs it for the target type for the exact size required
     */
    public final <Y> Y[] array(IntFunction<Y[]> arrayBuilder) {
        Object[] i = items;
        int s = size;
        if (i.length != s || i.getClass() == Object[].class) {
            Y[] x = arrayBuilder.apply(s);
            if (s > 0)
                arraycopy(items, 0, x, 0, s);
            return x;
        }
        return (Y[]) i;
    }


    protected final long longify(LongObjectToLongFunction<X> f, long l) {
        int thisSize = this.size;
        if (thisSize > 0) {
            X[] ii = this.items;
            for (int i = 0; i < thisSize; i++)
                l = f.longValueOf(l, ii[i]);
        }
        return l;
    }

//
//    /**
//     * reduce
//     */
//    public float reapply(FloatFunction<? super X> function, FloatFloatToFloatFunction combine) {
//        int n = size;
//        switch (n) {
//            case 0:
//                return Float.NaN;
//            case 1:
//                return function.floatValueOf(items[0]);
//            default:
//                float x = function.floatValueOf(items[0]);
//                for (int i = 1; i < n; i++) {
//                    x = combine.apply(x, function.floatValueOf(items[i]));
//                }
//                return x;
//        }
//    }

//    public int maxIndex(Comparator<? super X> comparator) {
//        Object[] array = items;
//        int size = this.size;
//        if (size == 0) {
//            return -1;
//        }
//
//        X max = (X) array[0];
//        int maxIndex = 0;
//        for (int i = 1; i < size; i++) {
//            X item = (X) array[i];
//            if (comparator.compare(item, max) > 0) {
//                max = item;
//                maxIndex = i;
//            }
//        }
//        return maxIndex;
//    }

    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public float maxValue(FloatFunction<? super X> function) {
        float max = Float.NEGATIVE_INFINITY;
        for (int i = 0, thisSize = this.size(); i < thisSize; i++) {
            float y = function.floatValueOf(this.items[i]);
            if (y > max)
                max = y;
        }
        return max;
    }

    public long minValue(ToLongFunction<? super X> f) {
        long min = Long.MAX_VALUE;
        X[] items = this.items;
        int thisSize = this.size();
        if (thisSize < 1) throw new UnsupportedOperationException();
        for (int i = 0; i < thisSize; i++)
            min = Math.min(f.applyAsLong(items[i]), min);
        return min;
    }

    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public long maxValue(ToLongFunction<? super X> function) {
        return longify((max, x) -> Math.max(max, function.applyAsLong(x)), Long.MIN_VALUE);
    }

//    public X maxBy(float mustExceed, FloatFunction<? super X> function) {
//
//        if (ArrayIterate.isEmpty(items)) {
//            throw new NoSuchElementException();
//        }
//
//        X min = null;
//        float minValue = mustExceed;
//        for (int i = 0; i < size; i++) {
//            X next = items[i];
//            float nextValue = function.floatValueOf(next);
//            if (nextValue > minValue) {
//                min = next;
//                minValue = nextValue;
//            }
//        }
//        return min;
//
//    }


    /** if filter is null, tests for null and removes them */
    @Override public boolean removeIf(@Nullable Predicate<? super X> filter) {
        int s = size;
        X[] a = this.items;
        int removed = ArrayUtil.removeIf(a, 0, s, filter);
        if (removed > 0) {
            this.size = s - removed;
            return true;
        }
        return false;
    }

//    public final boolean removeIf(Predicate<? super X> filter, List<X> displaced) {
//        int s = size();
//        int ps = s;
//        X[] a = this.items;
//        for (int i = 0; i < s; ) {
//            X ai = a[i];
//            if (ai == null || (filter.test(ai) && displaced.add(ai))) {
//                s--;
//                System.arraycopy(a, i + 1, a, i, s - i);
//                Arrays.fill(a, s, ps, null);
//            } else {
//                i++;
//            }
//        }
//        if (ps != s) {
//            this.size = s;
//            return true;
//        }
//
//        return false;
//    }

    //    public final X[] fillArrayNullPadded(X[] array) {
//        int s = size;
//        int l = array.length;
//        if (array == null || array.length < (s + 1)) {
//            array = (X[]) Array.newInstance(array.getClass().getComponentType(), s + 1);
//        }
//        System.arraycopy(items, 0, array, 0, s);
//        if (s < l)
//            Arrays.fill(array, s, l, null);
//        return array;
//    }

    final X[] fillArray(X[] array, boolean nullRemainder) {

        int l = array.length;
        int s = Math.min(l, size);
        arraycopy(items, 0, array, 0, s);
        if (nullRemainder && s < l)
            Arrays.fill(array, s, l, null);
        return array;
    }

    @Override
    public void forEach(Consumer<? super X> c) {
        int s = size;
        if (s > 0) {
            X[] ii = items; s = Math.min(s, ii.length);
            for (int i = 0; i < s; i++) {
                X j = ii[i];
                if (j != null)
                    c.accept(j);
            }
        }
    }

    public final void clearFast() {
        size = 0;
    }

    /**
     * remove, but with Map.remove semantics
     */
    public X removed(/**/ X object) {
        int index = this.indexOf(object);
        if (index >= 0) {
            X r = get(index);
            this.removeFast(index);
            return r;
        }
        return null;
    }

//    private X[] copyItemsWithNewCapacity(int newCapacity) {
//        return Arrays.copyOf(items, newCapacity);
//    }

    public boolean addIf(X x, Predicate<X> p) {
        return p.test(x) && add(x);
    }

    /** adds item xx from x, if p(xx)==true */
    public boolean addAllIf(Iterable<X> x, Predicate<X> p) {
        boolean y = false;
        for (X xx : x)
            y |= addIf(xx, p);
        return y;
    }

    public final boolean addIfNotNull(@Nullable X x) {
        return x != null && add(x);
    }

    @Override
    public boolean add(X x) {
        X[] i = items;
        int s = size++;
        if (s >= i.length)
            items = i = newArray(sizePlusFiftyPercent(s));
        i[s] = x;

//        ensureCapacityForAdditional(1);
//        addFast(x);
        return true;
    }
    public X remove(int index) {
        X[] ii = this.items;
        X previous = ii[index];
        int totalOffset = this.size - index - 1;
        if (totalOffset > 0) {
            arraycopy(ii, index + 1, ii, index, totalOffset);
        }

        ii[--this.size] = null;
        return previous;
    }
    public void add(int index, X element) {
        int sizeBefore = this.size;

        if (index > -1 && index < sizeBefore) {
            this.addAtIndex(index, element);
        } else if (index == sizeBefore) {
            this.add(element);
        } else {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    private void addAtIndex(int index, X element) {
        int oldSize = this.size++;
        X[] ii = this.items;
        if (ii.length == oldSize) {
            X[] newItems = newArray(sizePlusFiftyPercent(oldSize));
            if (index > 0)
                arraycopy(ii, 0, newItems, 0, index);

            arraycopy(ii, index, newItems, index + 1, oldSize - index);
            ii = this.items = newItems;
        } else {
            arraycopy(ii, index, ii, index + 1, oldSize - index);
        }

        ii[index] = element;
    }

    public final int addAndGetSize(X x) {
        ensureCapacityForAdditional(1, true);
        addFast(x);
        return size;
    }

    public final byte addAndGetSizeAsByte(X x) {
        return (byte) addAndGetSize(x);
    }


    private X[] newArray(int newCapacity) {
        return //newCapacity <= 0 ? (X[]) ArrayUtil.EMPTY_OBJECT_ARRAY :
            Arrays.copyOf(items, newCapacity);
    }

//    public final boolean addIfNotNull(Supplier<X> x) {
//        return addIfNotNull(x.get());
//    }
//
//    private boolean addIfNotNull(@Nullable X x) {
//        return x != null && add(x);
//    }

    /**
     * slow: use a setAt
     */
    public final boolean addIfNotPresent(@Nullable X x) {
        if (!contains(x)) {
            add(x);
            return true;
        }
        return false;
    }

    public int forEach(int offset, IntObjectPredicate each) {
        int n = offset;
        for (Object j : items) {
            if (j == null)
                break;
            each.accept(n++, j);
        }
        return size();
    }


    public final void setFast(int index, X t) {
        items[index] = t;
    }

    public void removeFast(int index) {
        X[] ii = items;
        int totalOffset = this.size - index - 1;
        if (totalOffset > 0)
            arraycopy(ii, index + 1, ii, index, totalOffset);
        ii[--size] = null;
    }


    @Nullable public X removeRandom(RandomBits r) {
        final int s = this.size;
        return s > 0 ? remove(r.nextInt(s)) : null;
    }

    @Override
    public boolean remove(Object object) {
        int index = this.indexOf(object);
        if (index >= 0) {
            this.removeFast(index);
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new TODO();
    }


    @Override
    public Iterator<X> iterator() {
        return switch (size) {
            case 0 -> Util.emptyIterator;
            case 1 -> new SingletonIterator<>(items[0]);
            default -> new MutableListIterator<>(this, 0);
        };
    }

    public boolean containsInstance(X x) {
        X[] items = this.items;
        for (int i = 0, thisSize = this.size; i < thisSize; i++) {
            if (items[i] == x)
                return true;
        }
        return false;
    }


    public boolean removeBelow(int index) {
        if (index == 0)
            return false;
        if (index >= size) {
            if (size > 1) {
                clear();
                return true;
            } else
                return false;
        } else {
            //TODO optimize
            for (int i = 0; i < index; i++)
                removeFast(0);
            return true;
        }
    }

    public boolean removeAbove(int index) {
        int s = this.size;
        if (index >= s) {
            return false;
        } else {
            Arrays.fill(items, index, s, null);
            this.size = index;
            return true;
        }
    }

    public int capacity() {
        return items.length;
    }

    @Override
    public Lst<X> clone() {
        return new Lst<>(size, items.length > 0 ? items.clone() : items);
    }

    /**
     * dangerous unless you know the array has enough capacity
     */
    public final void addFast(X x) {
        this.items[this.size++] = x;
    }

    public final void addAll(X a, X b) {
        ensureCapacityForAdditional(2, true);
        addFast(a);
        addFast(b);
    }

    @SafeVarargs
    public final void addAll(X... x) {
        addAll(x.length, x);
    }

    public final void addAll(int n, X[] x) {
        ensureCapacityForAdditional(n, true);
        addFast(x, n);
    }

    public final void addAllFaster(Lst<X> source) {
        int s = source.size();
        if (s > 0) {
            this.ensureCapacityForAdditional(s, false);
            arraycopy(source.array(), 0, this.items, this.size, s);
            this.size += s;
        }
    }

    public final void addFast(X[] x, int n) {
        //if (n > 0) {
        X[] items = this.items;
        int size = this.size;
        if (n >= 0) arraycopy(x, 0, items, size, n);
        this.size += n;
        //}
    }

    public boolean addWithoutResize(X x) {
        X[] i = this.items;
        if (this.size < i.length) {
            i[this.size++] = x;
            return true;
        }
        return false;
    }

    public boolean addWithoutResize(Supplier<X> x) {
        X[] i = this.items;
        if (this.size < i.length) {
            i[this.size++] = x.get();
            return true;
        }
        return false;
    }

    public X[] toArrayRecycled(IntFunction<X[]> ii) {
        X[] a = items;
        int s = size;
        return s == a.length && a.getClass() != Object[].class ? a : fillArray(ii.apply(size), false);
    }

    protected X[] toArrayCopy(@Nullable X[] target, IntFunction<X[]> ii) {
        int s = size;
        if (s != target.length) {
            target = ii.apply(s);
        }
        return fillArray(target, false);
    }


    /**
     * before procedure executes on a cell, it nullifies the cell. equivalent to:
     * forEach(p) ... clear()
     * but faster
     *
     * @param each
     */
    public int clear(Consumer<? super X> each) {
        int sizeBefore = this.size;
        if (sizeBefore > 0) {
            X[] items = this.items;
            for (int i = 0; i < sizeBefore; i++) {
                X ii = items[i];
                if (ii != null) {
                    items[i] = null;
                    each.accept(ii);
                }
            }
            this.size = 0;
        }
        return sizeBefore;
    }

    public <Y> void clearWith(BiConsumer<X, Y> each, Y y) {
        int s = this.size;
        if (s > 0) {
            X[] items = this.items;
            for (int i = 0; i < s; i++) {
                X ii = items[i];
                items[i] = null;
                each.accept(ii, y);
            }
            this.size = 0;
        }
    }

    /**
     * returns this to an unallocated, un-reusable state
     */
    public void delete() {
        if (items!=null) {
            clear();
            items = null;
        }
    }


    @Override
    public void clear() {
        clearIfChanged();
    }


//    public void clearReallocate(int maxSizeToReuse, int sizeIfNew) {
//        assert (sizeIfNew < maxSizeToReuse);
//
//        int s = this.size;
//        if (s == 0)
//            return;
//        else if (s > maxSizeToReuse) {
//            items = Arrays.copyOf(items, sizeIfNew);
//        } else {
//            //re-use, so nullify
//            Arrays.fill(this.items, 0, s, null);
//        }
//        this.size = 0;
//    }

    public boolean clearIfChanged() {
        int s = size;
        if (s != 0) {
            this.size = 0;
            Arrays.fill(this.items, 0, s, null);

            //ArrayUtil.assertNoNonNulls(items, 0, s); //TEMPORARY

            return true;
        }

        //ArrayUtil.assertNoNonNulls(items, 0, s); //TEMPORARY

        return false;
    }

    public boolean removeNulls() {
//
//        if (size == 0)
//            return false;
//
//        X[] items = this.items;
//        if (size == 1) {
//            if (items[0] == null) {
//                size = 0;
//                return true;
//            } else
//                return false;
//        }

        return removeIf(null);

//        ArrayUtil.sortNullsToEnd(items, 0, size);
//        int s;
//        for (s = size - 1; s >= 0; s--) {
//            if (items[s] != null)
//                break;
//        }
//        s++;
//        if (this.size != s) {
//            this.size = s;
//            return true;
//        }
//        return false;


    }
//    public boolean removeNulls() {
//        X[] nextItems = ArrayUtil.removeNulls(items);
//        this.items = nextItems;
//        if (nextItems!=items) {
//            size = items.length;
//            return true;
//        }
//        return false;
//    }

//    @Override
//    @Deprecated public final boolean removeIf(Predicate<? super X> predicate) {
//        return removeIf(predicate!=null ? (Predicate<X>)predicate::test : null);
////
////        int nowFilled = 0;
////        int s0 = this.size;
////        if (s0 == 0)
////            return false;
////        X[] xx = this.items;
////        for (int i = 0; i < s0; i++) {
////            X x = xx[i];
////            if (!predicate.accept(x)) {
////                if (nowFilled != i) {
////                    xx[nowFilled] = x;
////                }
////                nowFilled++;
////            }
////        }
////
////        if (nowFilled < s0) {
////            Arrays.fill(items, this.size = nowFilled, s0, null);
////            return true;
////        }
////        return false;
//    }

    public boolean removeInstances(X x) {
        //return removeIf((Predicate)y -> x == y);
        int s = size;
        MetalBitSet toRemove = null;
        for (int i = 0; i < s; i++) {
            if (items[i] == x) {
                if (toRemove == null) toRemove = MetalBitSet.bits(s);
                toRemove.set(i);
            }
        }
        return toRemove != null && removeAll(toRemove);
    }

    public boolean removeFirstInstance(X x) {
        int i = firstIndexOfInstance(x);
        if (i != -1) {
            removeFast(i);
            return true;
        }
        return false;
    }


    public void swap(int a, int b) {
        ArrayUtil.swap(items, a, b);
    }

    /**
     * use with caution
     */
    public void setSize(int s) {
        this.size = s;
    }


    public boolean removeOnce(X x) {
        int s = this.size;
        if (s > 0) {
            X[] ii = items;
            for (int i = 0; i < s; i++) {
                if (ii[i].equals(x)) {
                    removeFast(i);
                    return true;
                }
            }
        }
        return false;
    }

    public final <P> void forEachWith(Procedure2<? super X, ? super P> procedure, P parameter) {
        int s = this.size;
        if (s > 0) {
            X[] items = this.items;
            for (int i = 0; i < s; i++)
                procedure.value(items[i], parameter);
        }
    }



    public boolean AND(Predicate<? super X> predicate) {
        //return InternalArrayIterate.allSatisfy(this.items, this.size, predicate);
        int s = size;
        if (s > 0) {
            X[] items = this.items;
            for (int i = 0; i < s; i++) {
                if (!predicate.test(items[i]))
                    return false;
            }
        }
        return true;
    }


    public boolean OR(Predicate<? super X> predicate) {
        int s = size;
        if (s > 0) {
            X[] items = this.items;
            if (items!=null) { //if deleted
                s = Math.min(s, items.length);
                for (int i = 0; i < s; i++) {
                    if (predicate.test(items[i]))
                        return true;
                }
            }
        }
        return false;
    }

    public boolean OR(int from, int to, Predicate<? super X> predicate2) {
        int s = size;
        if (s > 0 && s > from) {
            X[] items = this.items;
            for (int i = from; i < to; i++) {
                if (predicate2.test(items[i]))
                    return true;
            }
        }
        return false;
    }

//    public <P> boolean anySatisfyWith(BiPredicate<? super X, ? super P> predicate2, P parameter) {
//        int s = size;
//        if (s > 0) {
//            X[] items = this.items;
//            for (int i = 0; i < s; i++) {
//                if (predicate2.test(items[i], parameter))
//                    return true;
//            }
//        }
//        return false;
//    }
//
//    public <P> boolean allSatisfyWith(BiPredicate<? super X, ? super P> predicate2, P parameter) {
//        int s = size;
//        if (s > 0) {
//            X[] items = this.items;
//            for (int i = 0; i < s; i++) {
//                if (!predicate2.test(items[i], parameter))
//                    return false;
//            }
//        }
//        return true;
//    }

    public MetalBitSet match(Predicate<? super X> p) {
        int s = size();
        X[] items = this.items;
        MetalBitSet b = MetalBitSet.bits(s);
        for (int i= 0; i < s; i++) {
            if (p.test(items[i]))
                b.set(i);
        }
        return b;
    }

    public boolean removeAll(MetalBitSet toRemove) {
        int s = size;
        int n = toRemove.cardinality();
        if (n == 0) return false;

        if (n == 1)
            removeFast(toRemove.first(true)); //fast case
        else
            removeAllN(toRemove, s, n);

        return true;
    }

    private void removeAllN(MetalBitSet toRemove, int s, int n) {
        X[] items = this.items;
        int r = toRemove.first(true);
        do {
            items[r] = null;
        } while (--n > 0 && (r = toRemove.next(true, r + 1, s)) != -1);
        removeNulls();
    }

    public int count(Predicate<? super X> p) {
        X[] ii = this.items;
        int c = 0, n = this.size;
        for (int i = 0; i < n; i++)
            if (p.test(ii[i])) c++;
        return c;
    }
    public boolean countGreaterThan(int limit, Predicate<? super X> p) {
        X[] ii = this.items;
        int c = 0, n = this.size;
        for (int i = 0; i < n; i++) {
            if (p.test(ii[i])) {
                if (++c > limit)
                    return true;
            }
        }
        return false;
    }
    public long sumOfInt(ToIntFunction<X> v) {
        X[] ii = this.items;
        int n = this.size;
        long c = 0;
        for (int i = 0; i < n; i++)
            c += v.applyAsInt(ii[i]);
        return c;
    }
    public double sumOfDouble(ToDoubleFunction<X> v) {
        X[] ii = this.items;
        int n = this.size;
        double c = 0;
        for (int i = 0; i < n; i++)
            c += v.applyAsDouble(ii[i]);
        return c;
    }


    public double[] variance(ToDoubleFunction<X> f) {
        int n = size;
        double average = sumOfDouble(f)  / n;
        double variance = 0;
        for (X x : this)
            variance += Util.sqr(f.applyAsDouble(x) - average);
        variance /= n;
        return new double[]{average, variance};
    }

    public Lst<X> adding(X x) {
        add(x);
        return this;
    }
    @SafeVarargs
    public final Lst<X> adding(X... xx) {
        int n = xx.length;
        if (n > 0) {
            ensureCapacityForAdditional(n, false);
            System.arraycopy(xx, 0, items, size, n);
            size+=n;
        }
        return this;
    }


    public void sort(Comparator<? super X> comparator) {
        int s = size;
        if (s > 1) Arrays.sort(items, 0, s, comparator);
    }

    @Override
    public boolean addAll(Collection<? extends X> c) {
        return addAll(c, 0);
    }

    @Override
    public boolean addAll(int index, Collection<? extends X> c) {
        throw new TODO();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return removeIf(c::contains);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new TODO();
    }

    public boolean addAll(Collection<? extends X> c, int extraSpace) {
        int cSize = c.size();
        if (cSize > 0) {
            ensureCapacityForAdditional(cSize + extraSpace, true);
            if (c instanceof Lst) {
                System.arraycopy(((Lst)c).items, 0, items, size, cSize);
                size += cSize;
            } else {
                for (X x : c) addFast(x);
                //c.forEach(this::addFast);
            }
        }
        return false;
    }

    public void nullify(int i) {
        items[i] = null;
    }

    public boolean isSorted(Comparator<X> c) {
        return ArrayUtil.isSorted(items, size, c);
    }

    /** returns the result of the filter, not whether it was added */
    public Predicate<X> addIfNot(Predicate<X> filter) {
        return x -> {
            if (!filter.test(x)) {
                add(x);
                return false;
            }
            return true;
        };
    }

    /** returns the result of the filter, not whether it was added */
    public Predicate<X> addIf(Predicate<X> filter) {
        return x -> {
            if (filter.test(x)) {
                add(x);
                return true;
            }
            return false;
        };
    }

    public X getAndNull(int i) {
        X[] items = this.items;
        X x = items[i];
        items[i] = null;
        return x;
    }
    @Nullable public X getAndNullIfNotNull(int i) {
        X[] items = this.items;
        if (items.length <= i)
            return null;
        X x = items[i];
        items[i] = null;
        return x;
    }

    public boolean equalsIdentity(Lst s) {
        int size = this.size;
        return size ==s.size && ArrayUtil.equalsIdentity(items, s.array(), size);
    }

    public Lst<X> cleared() {
        clear();
        return this;
    }

    private final class FloatValueCacher implements IntToDoubleFunction {
        private final MutableObjectFloatMap<X> ff;
        private final int size;
        private final FloatFunction<? super X> f;

        FloatValueCacher(int size, FloatFunction<? super X> f) {
            this.size = size;
            this.f = f;
            this.ff = new ObjectFloatHashMapWithHashingStrategy<>(HashingStrategies.identityStrategy(), size);
                    //new ObjectFloatHashMap(size);
        }

        @Override public double valueOf(int i) {
            return ff.getIfAbsentPutWithKey(items[i], f);
        }
    }
}