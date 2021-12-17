package jcog.memoize;

import jcog.data.map.CustomConcurrentHashMap;
import jcog.util.ArrayUtil;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;

/*>>>
import org.checkerframework.checker.interning.qual.*;
import org.checkerframework.checker.lock.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;
*/

/**
 * Utilities for interning objects. Interning is also known as canonicalization or hash-consing: it
 * returns a single representative object that {@link Object#equals} the object, and the client
 * discards the argument and uses the result instead. Since only one object exists for every set of
 * equal objects, space usage is reduced. Time may also be reduced, since it is possible to use
 * {@code ==} instead of {@code .equals()} for comparisons.
 * <p>
 * <p>Java builds in interning for Strings, but not for other objects. The methods in this class
 * extend interning to all Java objects.
 * <p>
 * https:
 */
public final class Intern {

    /**
     * This class is a collection of methods; it does not represent anything.
     */
    private Intern() {
        throw new Error("do not instantiate");
    }

    /**
     * Whether assertions are enabled.
     */
    private static boolean assertsEnabled = false;

    static {
        assert assertsEnabled = true; 
        
    }

    /**
     * Hasher is intended to work like Comparable: it is an optional argument to a hashing data
     * structure (such as a HashSet, HashMap, or WeakHashMap) which specifies the hashCode() and
     * equals() methods.
     * <p>
     * <p>If no Hasher is provided, then clients should act as if the following Hasher were provided:
     * <p>
     * <pre>
     *   class DefaultHasher {
     *     int hashCode(Object o) { return o.hashCode(); }
     *     boolean equals(Object o, Object o2) { return o.equals(o2); }
     *   }
     * </pre>
     */
    public interface Hasher<X> extends CustomConcurrentHashMap.Equivalence<X> {



















    }
    
    
    

    /**
     * Replace each element of the array by its interned version. Side-effects the array, but also
     * returns it.
     *
     * @param a the array whose elements to intern in place
     * @return an interned version of a
     * @see String#intern
     */
    @SuppressWarnings("interning") 
    public static /*@Interned*/ String[] internStrings(String[] a) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != null) {
                a[i] = a[i].intern();
            }
        }
        return a;
    }

    
    
    

    /**
     * Return true if the argument is interned (is canonical among all objects equal to itself).
     *
     * @param value the value to test for interning
     * @return true iff value is interned
     */
    @SuppressWarnings("interning") 
            /*@Pure*/
    public static boolean isInterned(/*@Nullable*/ Object value) {
        if (value == null) {
            
            return true;
        } else if (value instanceof String) {
            return (value == ((String) value).intern());
        } else if (value instanceof String[]) {
            return (value == intern((String[]) value));
        } else if (value instanceof Integer) {
            return (value == intern((Integer) value));
        } else if (value instanceof Long) {
            return (value == intern((Long) value));
        } else if (value instanceof int[]) {
            return (value == intern((int[]) value));
        } else if (value instanceof long[]) {
            return (value == intern((long[]) value));
        } else if (value instanceof Double) {
            return (value == intern((Double) value));
        } else if (value instanceof double[]) {
            return (value == intern((double[]) value));
        } else if (value instanceof Object[]) {
            return (value == intern((Object[]) value));
        } else {
            
            
            return true;
        }
    }

    
    
    

    /**
     * Hasher object which hashes and compares Integers. This is the obvious implementation that uses
     * intValue() for the hashCode.
     *
     * @see Hasher
     */
    private static final class IntegerHasher implements Hasher {
        @Override
        public boolean equal(Object a1, Object a2) {
            return a1.equals(a2);
        }

        @Override
        public int hash(Object o) {
            return (Integer) o;
        }
    }

    /**
     * Hasher object which hashes and compares Longs. This is the obvious implementation that uses
     * intValue() for the hashCode.
     *
     * @see Hasher
     */
    private static final class LongHasher implements Hasher {
        @Override
        public boolean equal(Object a1, Object a2) {
            return a1.equals(a2);
        }

        @Override
        public int hash(Object o) {
            Long i = (Long) o;
            return i.intValue();
        }
    }

    /**
     * Hasher object which hashes and compares int[] objects according to their contents.
     *
     * @see Hasher
     * @see Arrays#equals(int[], int[])
     */
    private static final class IntArrayHasher implements Hasher {
        @Override
        public boolean equal(Object a1, Object a2) {
            return Arrays.equals((int[]) a1, (int[]) a2);
        }

        @Override
        public int hash(Object o) {
            return Arrays.hashCode((int[]) o);
        }
    }

    /**
     * Hasher object which hashes and compares long[] objects according to their contents.
     *
     * @see Hasher
     * @see Arrays#equals (long[], long[])
     */
    private static final class LongArrayHasher implements Hasher {
        @Override
        public boolean equal(Object a1, Object a2) {
            return Arrays.equals((long[]) a1, (long[]) a2);
        }

        @Override
        public int hash(Object o) {
            return Arrays.hashCode((long[]) o);
        }
    }

    private static final int FACTOR = 23;
    
    private static final double DOUBLE_FACTOR = 263;

    /**
     * Hasher object which hashes and compares Doubles.
     *
     * @see Hasher
     */
    private static final class DoubleHasher implements Hasher {
        @Override
        public boolean equal(Object a1, Object a2) {
            return a1.equals(a2);
        }

        @Override
        public int hash(Object o) {
            Double d = (Double) o;
            return d.hashCode();
        }
    }

    /**
     * Hasher object which hashes and compares double[] objects according to their contents.
     *
     * @see Hasher
     * @see Arrays#equals(Object[], Object[])
     */
    private static final class DoubleArrayHasher implements Hasher {
        @Override
        public boolean equal(Object a1, Object a2) {
            
            
            
            double[] da1 = (double[]) a1;
            double[] da2 = (double[]) a2;
            if (da1.length != da2.length) {
                return false;
            }
            return IntStream.range(0, da1.length).noneMatch(i -> (da1[i] != da2[i]) && (!Double.isNaN(da1[i]) || !Double.isNaN(da2[i])));
        }

        @Override
        public int hash(Object o) {
            double[] a = (double[]) o;
            
            
            double running = 0;
            for (int i = 0; i < a.length; i++) {
                double elt = (Double.isNaN(a[i]) ? 0.0 : a[i]);
                running = running * FACTOR + elt * DOUBLE_FACTOR;
            }
            
            long result = Math.round(running);
            return (int) (result % Integer.MAX_VALUE);
        }
    }

    /**
     * Hasher object which hashes and compares String[] objects according to their contents.
     *
     * @see Hasher
     * @see Arrays.equals
     */
    private static final class StringArrayHasher implements Hasher {
        @Override
        public boolean equal(Object a1, Object a2) {
            return Arrays.equals((String[]) a1, (String[]) a2);
        }

        @Override
        public int hash(Object o) {
            return Arrays.hashCode((String[]) o);
        }
    }

    /**
     * Hasher object which hashes and compares Object[] objects according to their contents.
     *
     * @see Hasher
     * @see Arrays#equals(Object[], Object[])
     */
    private static final class ObjectArrayHasher implements Hasher {
        @Override
        public boolean equal(Object a1, Object a2) {
            return Arrays.equals((/*@Nullable*/ Object[]) a1, (/*@Nullable*/ Object[]) a2);
        }

        @Override
        public int hash(Object o) {
            return Arrays.hashCode((Object[]) o);
        }
    }

    
    
    
    
    

    public static class WeakHasherMap<X,Y> extends CustomConcurrentHashMap<X,Y> {
        private final Hasher hasher;

        public WeakHasherMap(Hasher<X> hasher) {
            super(WEAK, hasher, WEAK, IDENTITY, 1024);
            this.hasher = hasher;
        }

        public Hasher getHasher() {
            return hasher;
        }
    }

    private static final WeakHasherMap</*@Interned*/ Integer, WeakReference</*@Interned*/ Integer>>
            internedIntegers = new WeakHasherMap</*@Interned*/ Integer, WeakReference</*@Interned*/ Integer>>(
                    new IntegerHasher());
    private static final WeakHasherMap</*@Interned*/ Long, WeakReference</*@Interned*/ Long>> internedLongs = new WeakHasherMap</*@Interned*/ Long, WeakReference</*@Interned*/ Long>>(new LongHasher());
    private static final WeakHasherMap<int /*@Interned*/[], WeakReference<int /*@Interned*/[]>>
            internedIntArrays = new WeakHasherMap<int /*@Interned*/[], WeakReference<int /*@Interned*/[]>>(
                    new IntArrayHasher());
    private static final WeakHasherMap<long /*@Interned*/[], WeakReference<long /*@Interned*/[]>>
            internedLongArrays = new WeakHasherMap<long /*@Interned*/[], WeakReference<long /*@Interned*/[]>>(
                    new LongArrayHasher());
    private static final WeakHasherMap</*@Interned*/ Double, WeakReference</*@Interned*/ Double>>
            internedDoubles = new WeakHasherMap</*@Interned*/ Double, WeakReference</*@Interned*/ Double>>(
                    new DoubleHasher());
    private static final /*@Interned*/ Double internedDoubleNaN = Double.NaN;
    private static final /*@Interned*/ Double internedDoubleZero = 0.0;
    private static final WeakHasherMap<double /*@Interned*/[], WeakReference<double /*@Interned*/[]>>
            internedDoubleArrays = new WeakHasherMap<double /*@Interned*/[], WeakReference<double /*@Interned*/[]>>(
                    new DoubleArrayHasher());
    private static final WeakHasherMap<
            /*@Nullable*/ /*@Interned*/ String /*@Interned*/[],
            WeakReference</*@Nullable*/ /*@Interned*/ String /*@Interned*/[]>>
            internedStringArrays = new WeakHasherMap<
                    /*@Nullable*/ /*@Interned*/ String /*@Interned*/[],
                    WeakReference</*@Nullable*/ /*@Interned*/ String /*@Interned*/[]>>(
                    new StringArrayHasher());
    private static final WeakHasherMap<
            /*@Nullable*/ /*@Interned*/ Object /*@Interned*/[],
            WeakReference</*@Nullable*/ /*@Interned*/ Object /*@Interned*/[]>>
            internedObjectArrays = new WeakHasherMap<
                    /*@Nullable*/ /*@Interned*/ Object /*@Interned*/[],
                    WeakReference</*@Nullable*/ /*@Interned*/ Object /*@Interned*/[]>>(
                    new ObjectArrayHasher());
    private static final WeakHasherMap<
            SequenceAndIndices<int /*@Interned*/[]>, WeakReference<int /*@Interned*/[]>>
            internedIntSequenceAndIndices = new WeakHasherMap<
                    SequenceAndIndices<int /*@Interned*/[]>, WeakReference<int /*@Interned*/[]>>(
                    new SequenceAndIndicesHasher<int /*@Interned*/[]>());
    private static final WeakHasherMap<
            SequenceAndIndices<long /*@Interned*/[]>, WeakReference<long /*@Interned*/[]>>
            internedLongSequenceAndIndices = new WeakHasherMap<
                    SequenceAndIndices<long /*@Interned*/[]>, WeakReference<long /*@Interned*/[]>>(
                    new SequenceAndIndicesHasher<long /*@Interned*/[]>());
    private static final WeakHasherMap<
            SequenceAndIndices<double /*@Interned*/[]>, WeakReference<double /*@Interned*/[]>>
            internedDoubleSequenceAndIndices = new WeakHasherMap<
                    SequenceAndIndices<double /*@Interned*/[]>, WeakReference<double /*@Interned*/[]>>(
                    new SequenceAndIndicesHasher<double /*@Interned*/[]>());
    private static final WeakHasherMap<
            SequenceAndIndices</*@Nullable*/ /*@Interned*/ Object /*@Interned*/[]>,
            WeakReference</*@Nullable*/ /*@Interned*/ Object /*@Interned*/[]>>
            internedObjectSequenceAndIndices = new WeakHasherMap<
                    SequenceAndIndices</*@Nullable*/ /*@Interned*/ Object /*@Interned*/[]>,
                    WeakReference</*@Nullable*/ /*@Interned*/ Object /*@Interned*/[]>>(
                    new SequenceAndIndicesHasher</*@Nullable*/ /*@Interned*/ Object /*@Interned*/[]>());
    private static final WeakHasherMap<
            SequenceAndIndices</*@Nullable*/ /*@Interned*/ String /*@Interned*/[]>,
            WeakReference</*@Nullable*/ /*@Interned*/ String /*@Interned*/[]>>
            internedStringSequenceAndIndices = new WeakHasherMap<
                    SequenceAndIndices</*@Nullable*/ /*@Interned*/ String /*@Interned*/[]>,
                    WeakReference</*@Nullable*/ /*@Interned*/ String /*@Interned*/[]>>(
                    new SequenceAndIndicesHasher</*@Nullable*/ /*@Interned*/ String /*@Interned*/[]>());


    public static int numIntegers() {
        return internedIntegers.size();
    }

    public static int numLongs() {
        return internedLongs.size();
    }

    public static int numIntArrays() {
        return internedIntArrays.size();
    }

    public static int numLongArrays() {
        return internedLongArrays.size();
    }

    public static int numDoubles() {
        return internedDoubles.size();
    }

    public static int numDoubleArrays() {
        return internedDoubleArrays.size();
    }

    public static int numStringArrays() {
        return internedStringArrays.size();
    }

    public static int numObjectArrays() {
        return internedObjectArrays.size();
    }

    public static Iterator</*@Interned*/ Integer> integers() {
        return internedIntegers.keySet().iterator();
    }

    public static Iterator</*@Interned*/ Long> longs() {
        return internedLongs.keySet().iterator();
    }

    public static Iterator<int /*@Interned*/[]> intArrays() {
        return internedIntArrays.keySet().iterator();
    }

    public static Iterator<long /*@Interned*/[]> longArrays() {
        return internedLongArrays.keySet().iterator();
    }

    public static Iterator</*@Interned*/ Double> doubles() {
        return internedDoubles.keySet().iterator();
    }

    public static Iterator<double /*@Interned*/[]> doubleArrays() {
        return internedDoubleArrays.keySet().iterator();
    }

    public static Iterator</*@Nullable*/ /*@Interned*/ String /*@Interned*/[]> stringArrays() {
        return internedStringArrays.keySet().iterator();
    }

    public static Iterator</*@Nullable*/ /*@Interned*/ Object /*@Interned*/[]> objectArrays() {
        return internedObjectArrays.keySet().iterator();
    }

    /**
     * Interns a String. Delegates to the builtin String.intern() method, but handles {@code null}.
     *
     * @param a the string to intern; may be null
     * @return an interned version of the argument, or null if the argument was null
     */
    /*@Pure*/
    public static /*@Interned*/ /*@PolyNull*/ String intern(/*@PolyNull*/ String a) {
        
        if (a == null) {
            return null;
        }
        return a.intern();
    }

    /**
     * Interns a long. A no-op. Provided for completeness.
     *
     * @param l the long to intern
     * @return an interned version of the argument
     */
    /*@Pure*/
    public static long intern(long l) {
        return l;
    }

    /**
     * Interns a double A no-op. Provided for completeness.
     *
     * @param d the double to intern
     * @return an interned version of the argument
     */
    /*@Pure*/
    public static double intern(double d) {
        return d;
    }

    /**
     * Intern (canonicalize) an Integer. Return a canonical representation for the Integer.
     *
     * @param a an Integer to canonicalize
     * @return a canonical representation for the Integer
     */
    
    
    
    @SuppressWarnings({"interning", "purity"}) 
            /*@Pure*/
    public static /*@Interned*/ Integer intern(Integer a) {
        WeakReference</*@Interned*/ Integer> lookup = internedIntegers.get(a);
        Integer result1 = (lookup != null) ? lookup.get() : null;
        if (result1 != null) {
            return result1;
        } else {
            @SuppressWarnings("cast") 
                    /*@Interned*/ Integer result = a;
            internedIntegers.put(result, new WeakReference<>(result));
            return result;
        }
    }

    

    /**
     * Returns an interned Integer with value i.
     *
     * @param i the value to intern
     * @return an interned Integer with value i
     */
    public static /*@Interned*/ Integer internedInteger(int i) {
        return intern(Integer.valueOf(i));
    }

    

    /**
     * Returns an interned Integer with value parsed from the string.
     *
     * @param s the string to parse
     * @return an interned Integer parsed from s
     */
    public static /*@Interned*/ Integer internedInteger(String s) {
        return intern(Integer.decode(s));
    }

    /**
     * Intern (canonicalize) a Long. Return a canonical representation for the Long.
     *
     * @param a the value to intern
     * @return a canonical representation for the Long
     */
    
    
    
    @SuppressWarnings({"interning", "purity"})
            /*@Pure*/
    public static /*@Interned*/ Long intern(Long a) {
        WeakReference</*@Interned*/ Long> lookup = internedLongs.get(a);
        Long result1 = (lookup != null) ? lookup.get() : null;
        if (result1 != null) {
            return result1;
        } else {
            @SuppressWarnings("cast") 
                    /*@Interned*/ Long result = a;
            internedLongs.put(result, new WeakReference<>(result));
            return result;
        }
    }

    

    /**
     * Returns an interned Long with value i.
     *
     * @param i the value to intern
     * @return an interned Integer with value i
     */
    public static /*@Interned*/ Long internedLong(long i) {
        return intern(Long.valueOf(i));
    }

    

    /**
     * Returns an interned Long with value parsed from the string.
     *
     * @param s the string to parse
     * @return an interned Long parsed from s
     */
    public static /*@Interned*/ Long internedLong(String s) {
        return intern(Long.decode(s));
    }

    
    
    
    

    /**
     * Intern (canonicalize) an int[]. Return a canonical representation for the int[] array. Arrays
     * are compared according to their elements.
     *
     * @param a the array to canonicalize
     * @return a canonical representation for the int[] array
     */
    @SuppressWarnings({"interning", "purity"})
            /*@Pure*/
    public static int /*@Interned*/[] intern(int[] a) {
        
        
        

        WeakReference<int /*@Interned*/[]> lookup = internedIntArrays.get(a);
        int[] result1 = (lookup != null) ? lookup.get() : null;
        if (result1 != null) {
            return result1;
        } else {
            @SuppressWarnings("cast") 
                    /*@Interned*/ int[] result = a;
            internedIntArrays.put(result, new WeakReference<>(result));
            return result;
        }
    }

    /**
     * Intern (canonicalize) a long[]. Return a canonical representation for the long[] array. Arrays
     * are compared according to their elements.
     *
     * @param a the array to canonicalize
     * @return a canonical representation for the long[] array
     */
    @SuppressWarnings({"interning", "purity"})
            /*@Pure*/
    public static long /*@Interned*/[] intern(long[] a) {
        
        
        WeakReference<long /*@Interned*/[]> lookup = internedLongArrays.get(a);
        long[] result1 = (lookup != null) ? lookup.get() : null;
        if (result1 != null) {
            return result1;
        } else {
            @SuppressWarnings("cast") 
                    /*@Interned*/ long[] result = a;
            internedLongArrays.put(result, new WeakReference<>(result));
            return result;
        }
    }

    /**
     * Intern (canonicalize) a Double. Return a canonical representation for the Double.
     *
     * @param a the Double to canonicalize
     * @return a canonical representation for the Double
     */
    
    
    
    @SuppressWarnings({"interning", "purity"})
            /*@Pure*/
    public static /*@Interned*/ Double intern(Double a) {
        
        if (a.isNaN()) {
            return internedDoubleNaN;
        }
        
        if (a == 0) {
            return internedDoubleZero;
        }
        WeakReference</*@Interned*/ Double> lookup = internedDoubles.get(a);
        Double result1 = (lookup != null) ? lookup.get() : null;
        if (result1 != null) {
            return result1;
        } else {
            @SuppressWarnings("cast") 
                    /*@Interned*/ Double result = a;
            internedDoubles.put(result, new WeakReference<>(result));
            return result;
        }
    }

    

    /**
     * Returns an interned Double with value i.
     *
     * @param d the value to intern
     * @return an interned Double with value d
     */
    public static /*@Interned*/ Double internedDouble(double d) {
        return intern(Double.valueOf(d));
    }

    

    /**
     * Returns an interned Double with value parsed from the string.
     *
     * @param s the string to parse
     * @return an interned Double parsed from s
     */
    public static /*@Interned*/ Double internedDouble(String s) {
        return internedDouble(Double.parseDouble(s));
    }

    
    
    
    

    /**
     * Intern (canonicalize) a double[]. Return a canonical representation for the double[] array.
     * Arrays are compared according to their elements.
     *
     * @param a the array to canonicalize
     * @return a canonical representation for the double[] array
     */
    @SuppressWarnings({"interning", "purity"})
            /*@Pure*/
    public static double /*@Interned*/[] intern(double[] a) {
        WeakReference<double /*@Interned*/[]> lookup = internedDoubleArrays.get(a);
        double[] result1 = (lookup != null) ? lookup.get() : null;
        if (result1 != null) {
            return result1;
        } else {
            @SuppressWarnings("cast") 
                    /*@Interned*/ double[] result = a;
            internedDoubleArrays.put(result, new WeakReference<>(result));
            return result;
        }
    }

    /**
     * Intern (canonicalize) an String[]. Return a canonical representation for the String[] array.
     * Arrays are compared according to their elements' equals() methods.
     *
     * @param a the array to canonicalize. Its elements should already be interned.
     * @return a canonical representation for the String[] array
     */
    @SuppressWarnings({
            "interning", 
            "ReferenceEquality",
            "purity",
            "cast"
    }) 
            /*@Pure*/
    public static /*@PolyNull*/ /*@Interned*/ String /*@Interned*/[] intern(
            /*@PolyNull*/ /*@Interned*/ String[] a) {

        
        if (assertsEnabled) {
            for (int k = 0; k < a.length; k++) {
                if (!(a[k] == Intern.intern(a[k]))) {
                    throw new IllegalArgumentException();
                }
            }
        }

        WeakReference</*@Nullable*/ /*@Interned*/ String /*@Interned*/[]> lookup =
                internedStringArrays.get(a);
        /*@Nullable*/ /*@Interned*/
        String /*@Interned*/[] result = (lookup != null) ? lookup.get() : null;
        if (result == null) {
            result = a;
            internedStringArrays.put(
                    result, new WeakReference<>(result));
        }
        @SuppressWarnings(
                "nullness") 
                /*@PolyNull*/ /*@Interned*/ String /*@Interned*/[] polyresult = result;
        return polyresult;
    }

    /**
     * Intern (canonicalize) an Object[]. Return a canonical representation for the Object[] array.
     * Arrays are compared according to their elements. The elements should themselves already be
     * interned; they are compared using their equals() methods.
     *
     * @param a the array to canonicalize
     * @return a canonical representation for the Object[] array
     */
    @SuppressWarnings({
            "interning", 
            "purity",
            "cast"
    }) 
            /*@Pure*/
    public static /*@PolyNull*/ /*@Interned*/ Object /*@Interned*/[] intern(
            /*@PolyNull*/ /*@Interned*/ Object[] a) {
        @SuppressWarnings(
                "nullness") 
                WeakReference</*@Nullable*/ /*@Interned*/ Object /*@Interned*/[]> lookup =
                internedObjectArrays.get(a);
        /*@Nullable*/ /*@Interned*/
        Object /*@Interned*/[] result = (lookup != null) ? lookup.get() : null;
        if (result == null) {
            result = a;
            internedObjectArrays.put(
                    result, new WeakReference<>(result));
        }
        @SuppressWarnings(
                "nullness") 
                /*@PolyNull*/ /*@Interned*/ Object /*@Interned*/[] polyresult = result;
        return polyresult;
    }

    /**
     * Convenince method to intern an Object when we don't know its runtime type. Its runtime type
     * must be one of the types for which we have an intern() method, else an exception is thrown. If
     * the argument is an array, its elements should themselves be interned.
     *
     * @param a an Object to canonicalize
     * @return a canonical version of a
     */
    @SuppressWarnings("purity") 
            /*@Pure*/
    public static /*@Interned*/ /*@PolyNull*/ Object intern(/*@PolyNull*/ Object a) {
        if (a == null) {
            return null;
        } else if (a instanceof String) {
            return intern((String) a);
        } else if (a instanceof @SuppressWarnings("interning")String[] asArray) {
            /*@Interned*/
            /*@Interned*/
            return intern(asArray);
        } else if (a instanceof Integer) {
            return intern((Integer) a);
        } else if (a instanceof Long) {
            return intern((Long) a);
        } else if (a instanceof int[]) {
            return intern((int[]) a);
        } else if (a instanceof long[]) {
            return intern((long[]) a);
        } else if (a instanceof Double) {
            return intern((Double) a);
        } else if (a instanceof double[]) {
            return intern((double[]) a);
        } else if (a instanceof @SuppressWarnings("interning")Object[] asArray) {
            /*@Interned*/
            /*@Interned*/
            return intern(asArray);
        } else {
            throw new IllegalArgumentException(
                    "Arguments of type " + a.getClass() + " cannot be interned");
        }
    }

    /**
     * Return an interned subsequence of seq from start (inclusive) to end (exclusive). The argument
     * seq should already be interned.
     * <p>
     * <p>The result is the same as computing the subsequence and then interning it, but this method
     * is more efficient: if the subsequence is already interned, it avoids computing the subsequence.
     * <p>
     * <p>For example, since derived variables in Daikon compute the subsequence many times, this
     * shortcut saves quite a bit of computation. It saves even more when there may be many derived
     * variables that are non-canonical, since they are guaranteed to be ==.
     *
     * @param seq   the interned sequence whose subsequence should be computed and interned
     * @param start the index of the start of the subsequence to compute and intern
     * @param end   the index of the end of the subsequence to compute and intern
     * @return a subsequence of seq from start to end that is interned
     */
    public static int /*@Interned*/[] internSubsequence(int /*@Interned*/[] seq, int start, int end) {
        if (assertsEnabled && !Intern.isInterned(seq)) {
            throw new IllegalArgumentException();
        }
        SequenceAndIndices<int /*@Interned*/[]> sai =
                new SequenceAndIndices<>(seq, start, end);
        WeakReference<int /*@Interned*/[]> lookup = internedIntSequenceAndIndices.get(sai);
        int[] result1 = (lookup != null) ? lookup.get() : null;
        if (result1 != null) {
            return result1;
        } else {
            int[] subseqUninterned = ArrayUtil.subarray(seq, start, end - start);
            int /*@Interned*/[] subseq = Intern.intern(subseqUninterned);
            internedIntSequenceAndIndices.put(sai, new WeakReference<>(subseq));
            return subseq;
        }
    }

    /**
     * @param seq   the interned sequence whose subsequence should be computed and interned
     * @param start the index of the start of the subsequence to compute and intern
     * @param end   the index of the end of the subsequence to compute and intern
     * @return a subsequence of seq from start to end that is interned
     * @see #internSubsequence(int[], int, int)
     */
    @SuppressWarnings("purity") 
            /*@Pure*/
    public static long /*@Interned*/[] internSubsequence(
            long /*@Interned*/[] seq, int start, int end) {
        if (assertsEnabled && !Intern.isInterned(seq)) {
            throw new IllegalArgumentException();
        }
        SequenceAndIndices<long /*@Interned*/[]> sai =
                new SequenceAndIndices<>(seq, start, end);
        WeakReference<long /*@Interned*/[]> lookup = internedLongSequenceAndIndices.get(sai);
        long[] result1 = (lookup != null) ? lookup.get() : null;
        if (result1 != null) {
            return result1;
        } else {
            long[] subseq_uninterned = ArrayUtil.subarray(seq, start, end - start);
            long /*@Interned*/[] subseq = Intern.intern(subseq_uninterned);
            internedLongSequenceAndIndices.put(sai, new WeakReference<>(subseq));
            return subseq;
        }
    }

    /**
     * @param seq   the interned sequence whose subsequence should be computed and interned
     * @param start the index of the start of the subsequence to compute and intern
     * @param end   the index of the end of the subsequence to compute and intern
     * @return a subsequence of seq from start to end that is interned
     * @see #internSubsequence(int[], int, int)
     */
    @SuppressWarnings("purity") 
            /*@Pure*/
    public static double /*@Interned*/[] internSubsequence(
            double /*@Interned*/[] seq, int start, int end) {
        if (assertsEnabled && !Intern.isInterned(seq)) {
            throw new IllegalArgumentException();
        }
        SequenceAndIndices<double /*@Interned*/[]> sai =
                new SequenceAndIndices<>(seq, start, end);
        WeakReference<double /*@Interned*/[]> lookup = internedDoubleSequenceAndIndices.get(sai);
        double[] result1 = (lookup != null) ? lookup.get() : null;
        if (result1 != null) {
            return result1;
        } else {
            double[] subseq_uninterned = ArrayUtil.subarray(seq, start, end - start);
            double /*@Interned*/[] subseq = Intern.intern(subseq_uninterned);
            internedDoubleSequenceAndIndices.put(sai, new WeakReference<>(subseq));
            return subseq;
        }
    }

    /**
     * @param seq   the interned sequence whose subsequence should be computed and interned
     * @param start the index of the start of the subsequence to compute and intern
     * @param end   the index of the end of the subsequence to compute and intern
     * @return a subsequence of seq from start to end that is interned
     * @see #internSubsequence(int[], int, int)
     */
    @SuppressWarnings("purity") 
            /*@Pure*/
    public static /*@PolyNull*/ /*@Interned*/ Object /*@Interned*/[] internSubsequence(
            /*@PolyNull*/ /*@Interned*/ Object /*@Interned*/[] seq, int start, int end) {
        if (assertsEnabled && !Intern.isInterned(seq)) {
            throw new IllegalArgumentException();
        }
        SequenceAndIndices</*@PolyNull*/ /*@Interned*/ Object /*@Interned*/[]> sai =
                new SequenceAndIndices<>(seq, start, end);
        @SuppressWarnings("nullness") 
                WeakReference</*@PolyNull*/ /*@Interned*/ Object /*@Interned*/[]> lookup =
                internedObjectSequenceAndIndices.get(sai);
        /*@PolyNull*/ /*@Interned*/
        Object[] result1 = (lookup != null) ? lookup.get() : null;
        if (result1 != null) {
            return result1;
        } else {
            /*@PolyNull*/ /*@Interned*/
            Object[] subseq_uninterned = ArrayUtil.subarray(seq, start, end - start);
            /*@PolyNull*/ /*@Interned*/
            Object /*@Interned*/[] subseq = Intern.intern(subseq_uninterned);
            @SuppressWarnings("nullness") 
                    Object
                    ignore = 
                    internedObjectSequenceAndIndices.put(
                            sai,
                            new WeakReference<>(subseq));
            return subseq;
        }
    }

    /**
     * @param seq   the interned sequence whose subsequence should be computed and interned
     * @param start the index of the start of the subsequence to compute and intern
     * @param end   the index of the end of the subsequence to compute and intern
     * @return a subsequence of seq from start to end that is interned
     * @see #internSubsequence(int[], int, int)
     */
    /*@Pure*/
    @SuppressWarnings("purity") 
    public static /*@PolyNull*/ /*@Interned*/ String /*@Interned*/[] internSubsequence(
            /*@PolyNull*/ /*@Interned*/ String /*@Interned*/[] seq, int start, int end) {
        if (assertsEnabled && !Intern.isInterned(seq)) {
            throw new IllegalArgumentException();
        }
        SequenceAndIndices</*@PolyNull*/ /*@Interned*/ String /*@Interned*/[]> sai =
                new SequenceAndIndices<>(seq, start, end);
        @SuppressWarnings("nullness") 
                WeakReference</*@PolyNull*/ /*@Interned*/ String /*@Interned*/[]> lookup =
                internedStringSequenceAndIndices.get(sai);
        /*@PolyNull*/ /*@Interned*/
        String[] result1 = (lookup != null) ? lookup.get() : null;
        if (result1 != null) {
            return result1;
        } else {
            /*@PolyNull*/ /*@Interned*/
            String[] subseq_uninterned = ArrayUtil.subarray(seq, start, end - start);
            /*@PolyNull*/ /*@Interned*/
            String /*@Interned*/[] subseq = Intern.intern(subseq_uninterned);
            @SuppressWarnings("nullness") 
                    Object
                    ignore = 
                    internedStringSequenceAndIndices.put(
                            sai,
                            new WeakReference<>(subseq));
            return subseq;
        }
    }

    /**
     * Data structure for storing triples of a sequence and start and end indices, to represent a
     * subsequence. Requires that the sequence be interned. Used for interning the repeated finding of
     * subsequences on the same sequence.
     */
    private static final class SequenceAndIndices<T /*@Interned*/> {
        public T seq;
        public int start;
        public int end;

        /**
         * @param seq an interned array
         */
        public SequenceAndIndices(T seq, int start, int end) {
            if (assertsEnabled && !Intern.isInterned(seq)) {
                throw new IllegalArgumentException();
            }
            this.seq = seq;
            this.start = start;
            this.end = end;
        }

        @SuppressWarnings("unchecked")
                /*@Pure*/
        @Override
        public boolean equals(
                /*>>>@GuardSatisfied SequenceAndIndices<T> this,*/
                /*@GuardSatisfied*/ /*@Nullable*/ Object other) {
            if (other instanceof SequenceAndIndices<?>) {
                @SuppressWarnings("unchecked")
                SequenceAndIndices<T> other_sai = (SequenceAndIndices<T>) other;
                return equalsSequenceAndIndices(other_sai);
            } else {
                return false;
            }
        }

        /*@Pure*/
        public boolean equalsSequenceAndIndices(
                /*>>>@GuardSatisfied SequenceAndIndices<T> this,*/
                /*@GuardSatisfied*/ SequenceAndIndices<T> other) {
            return ((this.seq == other.seq) && this.start == other.start && this.end == other.end);
        }

        /*@Pure*/
        @Override
        public int hashCode(/*>>>@GuardSatisfied SequenceAndIndices<T> this*/) {
            return seq.hashCode() + start * 30 - end * 2;
        }

        
        /*@SideEffectFree*/
        @Override
        public String toString(/*>>>@GuardSatisfied SequenceAndIndices<T> this*/) {
            return "SAI(" + start + ',' + end + ") from: " + (seq);
        }
    }

    /**
     * Hasher object which hashes and compares String[] objects according to their contents.
     *
     * @see Hasher
     */
    private static final class SequenceAndIndicesHasher<T /*@Interned*/>
            implements Hasher {
        @Override
        public boolean equal(Object a1, Object a2) {
            @SuppressWarnings("unchecked")
            SequenceAndIndices<T> sai1 = (SequenceAndIndices<T>) a1;
            @SuppressWarnings("unchecked")
            SequenceAndIndices<T> sai2 = (SequenceAndIndices<T>) a2;
            
            return sai1.equals(sai2);
        }

        @Override
        public int hash(Object o) {
            return o.hashCode();
        }
    }
}