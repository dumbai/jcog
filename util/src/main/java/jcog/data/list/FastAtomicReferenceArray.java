package jcog.data.list;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;


/**
 * An array of object references in which elements may be updated
 * atomically.  See the {@link VarHandle} specification for
 * descriptions of the properties of atomic accesses.
 * @since 1.5
 * @author Doug Lea
 * @param <E> The base class of elements held in this array
 */
public class FastAtomicReferenceArray {

    private static final VarHandle AA =
        MethodHandles.arrayElementVarHandle(Object[].class)
                //.withInvokeExactBehavior()
    ;

    private final Object[] array;// = ArrayUtil.EMPTY_OBJECT_ARRAY;

    /**
     * Creates a new AtomicReferenceArray of the given length, with all
     * elements initially null.
     *
     * @param length the length of the array
     */
    public FastAtomicReferenceArray(int length) {
//        resize(length);
//    }
//
//    public void resize(int l) {
//        Object[] a = this.array;
//        if (a.length!=l) {
            this.array = new Object[length];//Arrays.copyOf(a, l);
//            arrayGet = AA.toMethodHandle(VarHandle.AccessMode.GET_OPAQUE).bindTo(array);
//        }
    }

    /**
     * Returns the length of the array.
     *
     * @return the length of the array
     */
    public final int length() {
        return array.length;
    }

    /**
     * Returns the current value of the element at index {@code i},
     * with memory effects as specified by {@link VarHandle#getVolatile}.
     *
     * @param i the index
     * @return the current value
     */
    @SuppressWarnings("unchecked")
    public final Object get(int i) {
        return AA.getVolatile(array, i);
    }

    /**
     * Sets the element at index {@code i} to {@code newValue},
     * with memory effects as specified by {@link VarHandle#setVolatile}.
     *
     * @param i the index
     * @param newValue the new value
     */
    public final void set(int i, Object newValue) {
        AA.setVolatile(array, i, newValue);
    }

    /**
     * Atomically sets the element at index {@code i} to {@code
     * newValue} and returns the old value,
     * with memory effects as specified by {@link VarHandle#getAndSet}.
     *
     * @param i the index
     * @param newValue the new value
     * @return the previous value
     */
    @SuppressWarnings("unchecked")
    public final Object getAndSet(int i, Object newValue) {
        return AA.getAndSet(array, i, newValue);
    }

    /**
     * Atomically sets the element at index {@code i} to {@code newValue}
     * if the element's current value {@code == expectedValue},
     * with memory effects as specified by {@link VarHandle#compareAndSet}.
     *
     * @param i the index
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return {@code true} if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final boolean compareAndSet(int i, Object expectedValue, Object newValue) {
        return AA.compareAndSet(array, i, expectedValue, newValue);
    }

    /**
     * Possibly atomically sets the element at index {@code i} to
     * {@code newValue} if the element's current value {@code == expectedValue},
     * with memory effects as specified by {@link VarHandle#weakCompareAndSetPlain}.
     *
     * @deprecated This method has plain memory effects but the method
     * name implies volatile memory effects (see methods such as
     * {@link #compareAndExchange} and {@link #compareAndSet}).  To avoid
     * confusion over plain or volatile memory effects it is recommended that
     * the method {@link #weakCompareAndSetPlain} be used instead.
     *
     * @param i the index
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return {@code true} if successful
     * @see #weakCompareAndSetPlain
     */
    @Deprecated(since="9")
    public final boolean weakCompareAndSet(int i, Object expectedValue, Object newValue) {
        return AA.weakCompareAndSetPlain(array, i, expectedValue, newValue);
    }

    /**
     * Possibly atomically sets the element at index {@code i} to
     * {@code newValue} if the element's current value {@code == expectedValue},
     * with memory effects as specified by {@link VarHandle#weakCompareAndSetPlain}.
     *
     * @param i the index
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return {@code true} if successful
     * @since 9
     */
    public final boolean weakCompareAndSetPlain(int i, Object expectedValue, Object newValue) {
        return AA.weakCompareAndSetPlain(array, i, expectedValue, newValue);
    }

    /**
     * Atomically updates (with memory effects as specified by {@link
     * VarHandle#compareAndSet}) the element at index {@code i} with
     * the results of applying the given function, returning the
     * previous value. The function should be side-effect-free, since
     * it may be re-applied when attempted updates fail due to
     * contention among threads.
     *
     * @param i the index
     * @param updateFunction a side-effect-free function
     * @return the previous value
     * @since 1.8
     */
    public final Object getAndUpdate(int i, UnaryOperator<Object> updateFunction) {
        Object prev = get(i), next = null;
        for (boolean haveNext = false;;) {
            if (!haveNext)
                next = updateFunction.apply(prev);
            if (weakCompareAndSetVolatile(i, prev, next))
                return prev;
            haveNext = (prev == (prev = get(i)));
        }
    }

    /**
     * Atomically updates (with memory effects as specified by {@link
     * VarHandle#compareAndSet}) the element at index {@code i} with
     * the results of applying the given function, returning the
     * updated value. The function should be side-effect-free, since it
     * may be re-applied when attempted updates fail due to contention
     * among threads.
     *
     * @param i the index
     * @param updateFunction a side-effect-free function
     * @return the updated value
     * @since 1.8
     */
    public final Object updateAndGet(int i, UnaryOperator<Object> updateFunction) {
        Object prev = get(i), next = null;
        for (boolean haveNext = false;;) {
            if (!haveNext)
                next = updateFunction.apply(prev);
            if (weakCompareAndSetVolatile(i, prev, next))
                return next;
            haveNext = (prev == (prev = get(i)));
        }
    }

    /**
     * Atomically updates (with memory effects as specified by {@link
     * VarHandle#compareAndSet}) the element at index {@code i} with
     * the results of applying the given function to the current and
     * given values, returning the previous value. The function should
     * be side-effect-free, since it may be re-applied when attempted
     * updates fail due to contention among threads.  The function is
     * applied with the current value of the element at index {@code i}
     * as its first argument, and the given update as the second
     * argument.
     *
     * @param i the index
     * @param x the update value
     * @param accumulatorFunction a side-effect-free function of two arguments
     * @return the previous value
     * @since 1.8
     */
    public final Object getAndAccumulate(int i, Object x,
                                         BinaryOperator<Object> accumulatorFunction) {
        Object prev = get(i), next = null;
        for (boolean haveNext = false;;) {
            if (!haveNext)
                next = accumulatorFunction.apply(prev, x);
            if (weakCompareAndSetVolatile(i, prev, next))
                return prev;
            haveNext = (prev == (prev = get(i)));
        }
    }

    /**
     * Atomically updates (with memory effects as specified by {@link
     * VarHandle#compareAndSet}) the element at index {@code i} with
     * the results of applying the given function to the current and
     * given values, returning the updated value. The function should
     * be side-effect-free, since it may be re-applied when attempted
     * updates fail due to contention among threads.  The function is
     * applied with the current value of the element at index {@code i}
     * as its first argument, and the given update as the second
     * argument.
     *
     * @param i the index
     * @param x the update value
     * @param accumulatorFunction a side-effect-free function of two arguments
     * @return the updated value
     * @since 1.8
     */
    public final Object accumulateAndGet(int i, Object x,
                                         BinaryOperator<Object> accumulatorFunction) {
        Object prev = get(i), next = null;
        for (boolean haveNext = false;;) {
            if (!haveNext)
                next = accumulatorFunction.apply(prev, x);
            if (weakCompareAndSetVolatile(i, prev, next))
                return next;
            haveNext = (prev == (prev = get(i)));
        }
    }

//    /**
//     * Returns the String representation of the current values of array.
//     * @return the String representation of the current values of array
//     */
//    public String toString() {
//        int iMax = array.length - 1;
//        if (iMax == -1)
//            return "[]";
//
//        StringBuilder b = new StringBuilder();
//        b.append('[');
//        for (int i = 0; ; i++) {
//            b.append(get(i));
//            if (i == iMax)
//                return b.append(']').toString();
//            b.append(',').append(' ');
//        }
//    }
//
//    /**
//     * Reconstitutes the instance from a stream (that is, deserializes it).
//     * @param s the stream
//     * @throws ClassNotFoundException if the class of a serialized object
//     *         could not be found
//     * @throws java.io.IOException if an I/O error occurs
//     */
//    private void readObject(java.io.ObjectInputStream s)
//        throws java.io.IOException, ClassNotFoundException {
//        // Note: This must be changed if any additional fields are defined
//        Object a = s.readFields().get("array", null);
//        if (a == null || !a.getClass().isArray())
//            throw new java.io.InvalidObjectException("Not array type");
//        if (a.getClass() != Object[].class)
//            a = Arrays.copyOf((Object[])a, Array.getLength(a), Object[].class);
//        Field arrayField = java.security.AccessController.doPrivileged(
//            (java.security.PrivilegedAction<Field>) () -> {
//                try {
//                    Field f = FastAtomicReferenceArray.class
//                        .getDeclaredField("array");
//                    f.setAccessible(true);
//                    return f;
//                } catch (ReflectiveOperationException e) {
//                    throw new Error(e);
//                }});
//        try {
//            arrayField.set(this, a);
//        } catch (IllegalAccessException e) {
//            throw new Error(e);
//        }
//    }

    // jdk9

    /**
     * Returns the current value of the element at index {@code i},
     * with memory semantics of reading as if the variable was declared
     * non-{@code volatile}.
     *
     * @param i the index
     * @return the value
     * @since 9
     */
    public final Object getPlain(int i) {
        return AA.get(array, i);
    }

    /**
     * Sets the element at index {@code i} to {@code newValue},
     * with memory semantics of setting as if the variable was
     * declared non-{@code volatile} and non-{@code final}.
     *
     * @param i the index
     * @param newValue the new value
     * @since 9
     */
    public final void setPlain(int i, Object newValue) {
        AA.set(array, i, newValue);
    }


    /**
     * Returns the current value of the element at index {@code i},
     * with memory effects as specified by {@link VarHandle#getOpaque}.
     *
     * @param i the index
     * @return the value
     * @since 9
     */
    public final Object getOpaque(int i) {
        return AA.getOpaque(array, (int)i);
//        try {
//            return arrayGet.invokeExact(i);
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//            return null;
//        }
        //return Util.unsafe.getObjectVolatile(array, i*Util.unsafe.addressSize());
    }

    /**
     * Sets the element at index {@code i} to {@code newValue},
     * with memory effects as specified by {@link VarHandle#setOpaque}.
     *
     * @param i the index
     * @param newValue the new value
     * @since 9
     */
    public final void setOpaque(int i, Object newValue) {
        AA.setOpaque(array, i, newValue);
    }

    /**
     * Returns the current value of the element at index {@code i},
     * with memory effects as specified by {@link VarHandle#getAcquire}.
     *
     * @param i the index
     * @return the value
     * @since 9
     */
    public final Object getAcquire(int i) {
        return AA.getAcquire(array, i);
    }

    /**
     * Sets the element at index {@code i} to {@code newValue},
     * with memory effects as specified by {@link VarHandle#setRelease}.
     *
     * @param i the index
     * @param newValue the new value
     * @since 9
     */
    public final void setRelease(int i, Object newValue) {
        AA.setRelease(array, i, newValue);
    }

    /**
     * Atomically sets the element at index {@code i} to {@code newValue}
     * if the element's current value, referred to as the <em>witness
     * value</em>, {@code == expectedValue},
     * with memory effects as specified by
     * {@link VarHandle#compareAndExchange}.
     *
     * @param i the index
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return the witness value, which will be the same as the
     * expected value if successful
     * @since 9
     */
    public final Object compareAndExchange(int i, Object expectedValue, Object newValue) {
        return AA.compareAndExchange(array, i, expectedValue, newValue);
    }

    /**
     * Atomically sets the element at index {@code i} to {@code newValue}
     * if the element's current value, referred to as the <em>witness
     * value</em>, {@code == expectedValue},
     * with memory effects as specified by
     * {@link VarHandle#compareAndExchangeAcquire}.
     *
     * @param i the index
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return the witness value, which will be the same as the
     * expected value if successful
     * @since 9
     */
    public final Object compareAndExchangeAcquire(int i, Object expectedValue, Object newValue) {
        return AA.compareAndExchangeAcquire(array, i, expectedValue, newValue);
    }

    /**
     * Atomically sets the element at index {@code i} to {@code newValue}
     * if the element's current value, referred to as the <em>witness
     * value</em>, {@code == expectedValue},
     * with memory effects as specified by
     * {@link VarHandle#compareAndExchangeRelease}.
     *
     * @param i the index
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return the witness value, which will be the same as the
     * expected value if successful
     * @since 9
     */
    public final Object compareAndExchangeRelease(int i, Object expectedValue, Object newValue) {
        return AA.compareAndExchangeRelease(array, i, expectedValue, newValue);
    }

    /**
     * Possibly atomically sets the element at index {@code i} to
     * {@code newValue} if the element's current value {@code == expectedValue},
     * with memory effects as specified by
     * {@link VarHandle#weakCompareAndSet}.
     *
     * @param i the index
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return {@code true} if successful
     * @since 9
     */
    public final boolean weakCompareAndSetVolatile(int i, Object expectedValue, Object newValue) {
        return AA.weakCompareAndSet(array, i, expectedValue, newValue);
    }

    /**
     * Possibly atomically sets the element at index {@code i} to
     * {@code newValue} if the element's current value {@code == expectedValue},
     * with memory effects as specified by
     * {@link VarHandle#weakCompareAndSetAcquire}.
     *
     * @param i the index
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return {@code true} if successful
     * @since 9
     */
    public final boolean weakCompareAndSetAcquire(int i, Object expectedValue, Object newValue) {
        return AA.weakCompareAndSetAcquire(array, i, expectedValue, newValue);
    }

    /**
     * Possibly atomically sets the element at index {@code i} to
     * {@code newValue} if the element's current value {@code == expectedValue},
     * with memory effects as specified by
     * {@link VarHandle#weakCompareAndSetRelease}.
     *
     * @param i the index
     * @param expectedValue the expected value
     * @param newValue the new value
     * @return {@code true} if successful
     * @since 9
     */
    public final boolean weakCompareAndSetRelease(int i, Object expectedValue, Object newValue) {
        return AA.weakCompareAndSetRelease(array, i, expectedValue, newValue);
    }

    public boolean nullifyInstance(Object x) {
        double n = length();
        for (int i = 0; i < n; i++) {
            if (compareAndSet(i, x, null))
                return true;
        }
        return false;
    }
}