/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcog.util;


import com.google.common.collect.ForwardingListIterator;
import jcog.Util;
import jcog.data.array.IntComparator;
import jcog.data.bit.MetalBitSet;
import jcog.data.list.Lst;
import org.eclipse.collections.api.block.procedure.primitive.IntIntProcedure;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.*;
import java.util.random.RandomGenerator;

/**
 * <p>Operations on arrays, primitive arrays (like {@code int[]}) and
 * primitive wrapper arrays (like {@code Integer[]}).
 *
 * <p>This class tries to handle {@code null} input gracefully.
 * An exception will not be thrown for a {@code null}
 * array input. However, an Object array that contains a {@code null}
 * element may throw an exception. Each method documents its behaviour.
 */
public enum ArrayUtil {
	;

	/**
	 * An empty immutable {@code Object} array.
	 */
	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	/**
	 * An empty immutable {@code Class} array.
	 */
	public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
	/**
	 * An empty immutable {@code String} array.
	 */
	public static final String[] EMPTY_STRING_ARRAY = new String[0];
	/**
	 * An empty immutable {@code long} array.
	 */
	public static final long[] EMPTY_LONG_ARRAY = new long[0];
	/**
	 * An empty immutable {@code Long} array.
	 */
	public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];
	/**
	 * An empty immutable {@code int} array.
	 */
	public static final int[] EMPTY_INT_ARRAY = new int[0];
	/**
	 * An empty immutable {@code Integer} array.
	 */
	public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];
	/**
	 * An empty immutable {@code short} array.
	 */
	public static final short[] EMPTY_SHORT_ARRAY = new short[0];
	/**
	 * An empty immutable {@code Short} array.
	 */
	public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];
	/**
	 * An empty immutable {@code byte} array.
	 */
	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
	/**
	 * An empty immutable {@code Byte} array.
	 */
	public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];
	/**
	 * An empty immutable {@code double} array.
	 */
	public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
	/**
	 * An empty immutable {@code Double} array.
	 */
	public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];
	/**
	 * An empty immutable {@code float} array.
	 */
	public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
	/**
	 * An empty immutable {@code Float} array.
	 */
	public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];
	/**
	 * An empty immutable {@code boolean} array.
	 */
	public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
	/**
	 * An empty immutable {@code Boolean} array.
	 */
	public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];
	/**
	 * An empty immutable {@code char} array.
	 */
	public static final char[] EMPTY_CHAR_ARRAY = new char[0];
	/**
	 * An empty immutable {@code Character} array.
	 */
	public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];

	public static final double[][] EMPTY_DOUBLE_DOUBLE = new double[0][0];
	public static final Consumer[] EMPTY_CONSUMER_ARRAY = new Consumer[0];
	public static final URL[] EMPTY_URL_ARRAY = new URL[0];

	/** use with caution */
	public static final byte[] BYTE_ARRAY_ONLY_ZERO = {0};

	//	public static final LongObjectPair[] EMPTY_LONGOBJECT_PAIR_ARRAY = new LongObjectPair[0];

	/**
	 * The number of distinct byte values.
	 */
//    private static final int NUM_BYTE_VALUES = 1 << 8;
	private static final byte[] BYTE_ZERO = {0};
	private static final byte[] BYTE_ONE = {1};
	private static final byte[] BYTE_TWO = {2};
	private static final byte[] BYTE_THREE = {3};
	private static final byte[] BYTE_ZERO_ZERO = {0, 0};
	private static final byte[] BYTE_ZERO_ONE = {0, 1};
	private static final byte[] BYTE_ONE_ZERO = {1, 0};
	private static final byte[] BYTE_ONE_ONE = {1, 1};
	private static final byte[] BYTE_ONE_TWO = {1, 2};
	private static final byte[] BYTE_ONE_NEGONE = {1, -1};
	private static final byte[] BYTE_ONE_NEGTWO = {1, -2};
	private static final byte[] BYTE_TWO_ONE = {2, 1};
	private static final byte[] BYTE_TWO_TWO = {2, 2};
	private static final byte[] BYTE_TWO_NEGONE = {2, -1};
	private static final byte[] BYTE_TWO_NEGTWO = {2, -2};
	private static final byte[] BYTE_NEGONE_ONE = {-1, 1};
	private static final byte[] BYTE_NEGONE_TWO = {-1, 2};
	private static final byte[] BYTE_NEGONE_NEGONE = {-1, -1};
	private static final byte[] BYTE_NEGONE_NEGTWO = {-1, -2};

	/** returns how many removed */
	public static int sortNullsToEnd(Object[] x) {
		return sortNullsToEnd(x, 0, x.length);
	}

	/** returns how many removed */
	public static int sortNullsToEnd(Object[] x, int from, int to) {
		return removeIf(x, from, to, null);
	}

	/**
	 * <p>Shallow clones an array returning a typecast result and handling
	 * {@code null}.
	 *
	 * <p>The objects in the array are not cloned, thus there is no special
	 * handling for multi-dimensional arrays.
	 *
	 * <p>This method returns {@code null} for a {@code null} input array.
	 *
	 * @param <T>   the component type of the array
	 * @param array the array to shallow clone, may be {@code null}
	 * @return the cloned array, {@code null} if {@code null} input
	 */
	private static <T> T[] clone(T[] array) {
		return array == null ? null : array.clone();
	}

	/**
	 * <p>Clones an array returning a typecast result and handling
	 * {@code null}.
	 *
	 * <p>This method returns {@code null} for a {@code null} input array.
	 *
	 * @param array the array to clone, may be {@code null}
	 * @return the cloned array, {@code null} if {@code null} input
	 */
	public static long[] clone(long[] array) {
		if (array == null) return null;
		return array.clone();
	}

	/**
	 * <p>Clones an array returning a typecast result and handling
	 * {@code null}.
	 *
	 * <p>This method returns {@code null} for a {@code null} input array.
	 *
	 * @param array the array to clone, may be {@code null}
	 * @return the cloned array, {@code null} if {@code null} input
	 */
	public static int[] clone(int[] array) {
		if (array == null) return null;
		return array.clone();
	}

	/**
	 * <p>Clones an array returning a typecast result and handling
	 * {@code null}.
	 *
	 * <p>This method returns {@code null} for a {@code null} input array.
	 *
	 * @param array the array to clone, may be {@code null}
	 * @return the cloned array, {@code null} if {@code null} input
	 */
	public static short[] clone(short[] array) {
		if (array == null) return null;
		return array.clone();
	}

	/**
	 * <p>Clones an array returning a typecast result and handling
	 * {@code null}.
	 *
	 * <p>This method returns {@code null} for a {@code null} input array.
	 *
	 * @param array the array to clone, may be {@code null}
	 * @return the cloned array, {@code null} if {@code null} input
	 */
	public static char[] clone(char[] array) {
		if (array == null) return null;
		return array.clone();
	}

	/**
	 * <p>Clones an array returning a typecast result and handling
	 * {@code null}.
	 *
	 * <p>This method returns {@code null} for a {@code null} input array.
	 *
	 * @param array the array to clone, may be {@code null}
	 * @return the cloned array, {@code null} if {@code null} input
	 */
	public static byte[] clone(byte[] array) {
		if (array == null) return null;
		return array.clone();
	}

	/**
	 * <p>Clones an array returning a typecast result and handling
	 * {@code null}.
	 *
	 * <p>This method returns {@code null} for a {@code null} input array.
	 *
	 * @param array the array to clone, may be {@code null}
	 * @return the cloned array, {@code null} if {@code null} input
	 */
	public static double[] clone(double[] array) {
		if (array == null) return null;
		return array.clone();
	}

	/**
	 * <p>Clones an array returning a typecast result and handling
	 * {@code null}.
	 *
	 * <p>This method returns {@code null} for a {@code null} input array.
	 *
	 * @param array the array to clone, may be {@code null}
	 * @return the cloned array, {@code null} if {@code null} input
	 */
	public static float[] clone(float[] array) {
		if (array == null) return null;
		return array.clone();
	}

	/**
	 * <p>Clones an array returning a typecast result and handling
	 * {@code null}.
	 *
	 * <p>This method returns {@code null} for a {@code null} input array.
	 *
	 * @param array the array to clone, may be {@code null}
	 * @return the cloned array, {@code null} if {@code null} input
	 */
	public static boolean[] clone(boolean[] array) {
		if (array == null) return null;
		return array.clone();
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @param type  the class representation of the desired array
	 * @param <T>   the class type
	 * @return the same array, {@code public static} empty array if {@code null}
	 * @throws IllegalArgumentException if the type argument is null
	 * @since 3.5
	 */
	public static <T> T[] nullToEmpty(T[] array, Class<T[]> type) {
		if (type == null) throw new IllegalArgumentException("The type must not be null");

		if (array == null) return type.cast(Array.newInstance(type.getComponentType(), 0));
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static Object[] nullToEmpty(Object[] array) {
		if (array.length == 0) return EMPTY_OBJECT_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 3.2
	 */
	public static Class<?>[] nullToEmpty(Class<?>[] array) {
		if (array.length == 0) return EMPTY_CLASS_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static String[] nullToEmpty(String[] array) {
		if (array.length == 0) return EMPTY_STRING_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static long[] nullToEmpty(long[] array) {
		if (array.length == 0) return EMPTY_LONG_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static int[] nullToEmpty(int[] array) {
		if (array.length == 0) return EMPTY_INT_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static short[] nullToEmpty(short[] array) {
		if (array.length == 0) return EMPTY_SHORT_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static char[] nullToEmpty(char[] array) {
		if (array.length == 0) return EMPTY_CHAR_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static byte[] nullToEmpty(byte[] array) {
		if (array.length == 0) return EMPTY_BYTE_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static double[] nullToEmpty(double[] array) {
		if (array.length == 0) return EMPTY_DOUBLE_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static float[] nullToEmpty(float[] array) {
		if (array.length == 0) return EMPTY_FLOAT_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static boolean[] nullToEmpty(boolean[] array) {
		if (array.length == 0) return EMPTY_BOOLEAN_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static Long[] nullToEmpty(Long[] array) {
		if (array.length == 0) return EMPTY_LONG_OBJECT_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static Integer[] nullToEmpty(Integer[] array) {
		if (array.length == 0) return EMPTY_INTEGER_OBJECT_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static Short[] nullToEmpty(Short[] array) {
		if (array.length == 0) return EMPTY_SHORT_OBJECT_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static Character[] nullToEmpty(Character[] array) {
		if (array.length == 0) return EMPTY_CHARACTER_OBJECT_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static Byte[] nullToEmpty(Byte[] array) {
		if (array.length == 0) return EMPTY_BYTE_OBJECT_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static Double[] nullToEmpty(Double[] array) {
		if (array.length == 0) return EMPTY_DOUBLE_OBJECT_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static Float[] nullToEmpty(Float[] array) {
		if (array.length == 0) return EMPTY_FLOAT_OBJECT_ARRAY;
		return array;
	}

	/**
	 * <p>Defensive programming technique to change a {@code null}
	 * reference to an empty one.
	 *
	 * <p>This method returns an empty array for a {@code null} input array.
	 *
	 * <p>As a memory optimizing technique an empty array passed in will be overridden with
	 * the empty {@code public static} references in this class.
	 *
	 * @param array the array to check for {@code null} or empty
	 * @return the same array, {@code public static} empty array if {@code null} or empty input
	 * @since 2.5
	 */
	public static Boolean[] nullToEmpty(Boolean[] array) {
		if (array.length == 0) return EMPTY_BOOLEAN_OBJECT_ARRAY;
		return array;
	}

	/**
	 * <p>Produces a new array containing the elements between
	 * the start and end indices.
	 *
	 * <p>The start index is inclusive, the end index exclusive.
	 * Null array input produces null output.
	 *
	 * <p>The component type of the subarray is always the same as
	 * that of the input array. Thus, if the input is an array of type
	 * {@code Date}, the following usage is envisaged:
	 *
	 * <pre>
	 * Date[] someDates = (Date[])ArrayUtils.subarray(allDates, 2, 5);
	 * </pre>
	 *
	 * @param <T>                 the component type of the array
	 * @param array               the array
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0)
	 *                            is promoted to 0, overvalue (&gt;array.length) results
	 *                            in an empty array.
	 * @param endIndexExclusive   elements up to endIndex-1 are present in the
	 *                            returned subarray. Undervalue (&lt; startIndex) produces
	 *                            empty array, overvalue (&gt;array.length) is demoted to
	 *                            array length.
	 * @return a new array containing the elements between
	 * the start and end indices.
	 * @see Arrays#copyOfRange(Object[], int, int)
	 * @since 2.1
	 */
	public static <T> T[] subarray(T[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return null;
		if (startIndexInclusive < 0) startIndexInclusive = 0;
		if (endIndexExclusive > array.length) endIndexExclusive = array.length;
		int newSize = endIndexExclusive - startIndexInclusive;
		Class<?> type = array.getClass().getComponentType();
		if (newSize <= 0) {
			@SuppressWarnings("unchecked") T[] emptyArray = (T[]) Array.newInstance(type, 0);
			return emptyArray;
		}
		@SuppressWarnings("unchecked") T[] subarray = (T[]) Array.newInstance(type, newSize);
		System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
		return subarray;
	}

	public static <T> T[] subarray(T[] array, int startIndexInclusive, int endIndexExclusive, IntFunction<T[]> builder) {
		if (array == null) return null;
		if (startIndexInclusive < 0) startIndexInclusive = 0;
		if (endIndexExclusive > array.length) endIndexExclusive = array.length;
		int newSize = endIndexExclusive - startIndexInclusive;
		//Class<?> type = array.getClass().getComponentType();
		if (newSize <= 0) return builder.apply(0);
		@SuppressWarnings("unchecked") T[] subarray = builder.apply(newSize);
		System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
		return subarray;
	}

	/**
	 * <p>Produces a new {@code long} array containing the elements
	 * between the start and end indices.
	 *
	 * <p>The start index is inclusive, the end index exclusive.
	 * Null array input produces null output.
	 *
	 * @param array               the array
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0)
	 *                            is promoted to 0, overvalue (&gt;array.length) results
	 *                            in an empty array.
	 * @param endIndexExclusive   elements up to endIndex-1 are present in the
	 *                            returned subarray. Undervalue (&lt; startIndex) produces
	 *                            empty array, overvalue (&gt;array.length) is demoted to
	 *                            array length.
	 * @return a new array containing the elements between
	 * the start and end indices.
	 * @see Arrays#copyOfRange(long[], int, int)
	 * @since 2.1
	 */
	public static long[] subarray(long[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return null;
		if (startIndexInclusive < 0) startIndexInclusive = 0;
		if (endIndexExclusive > array.length) endIndexExclusive = array.length;
		int newSize = endIndexExclusive - startIndexInclusive;
		if (newSize <= 0) return EMPTY_LONG_ARRAY;

		long[] subarray = new long[newSize];
		System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
		return subarray;
	}

	/**
	 * <p>Produces a new {@code int} array containing the elements
	 * between the start and end indices.
	 *
	 * <p>The start index is inclusive, the end index exclusive.
	 * Null array input produces null output.
	 *
	 * @param array               the array
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0)
	 *                            is promoted to 0, overvalue (&gt;array.length) results
	 *                            in an empty array.
	 * @param endIndexExclusive   elements up to endIndex-1 are present in the
	 *                            returned subarray. Undervalue (&lt; startIndex) produces
	 *                            empty array, overvalue (&gt;array.length) is demoted to
	 *                            array length.
	 * @return a new array containing the elements between
	 * the start and end indices.
	 * @see Arrays#copyOfRange(int[], int, int)
	 * @since 2.1
	 */
	public static int[] subarray(int[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return null;
		if (startIndexInclusive < 0) startIndexInclusive = 0;
		if (endIndexExclusive > array.length) endIndexExclusive = array.length;
		int newSize = endIndexExclusive - startIndexInclusive;
		if (newSize <= 0) return EMPTY_INT_ARRAY;

		int[] subarray = new int[newSize];
		System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
		return subarray;
	}

	/**
	 * <p>Produces a new {@code short} array containing the elements
	 * between the start and end indices.
	 *
	 * <p>The start index is inclusive, the end index exclusive.
	 * Null array input produces null output.
	 *
	 * @param array               the array
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0)
	 *                            is promoted to 0, overvalue (&gt;array.length) results
	 *                            in an empty array.
	 * @param endIndexExclusive   elements up to endIndex-1 are present in the
	 *                            returned subarray. Undervalue (&lt; startIndex) produces
	 *                            empty array, overvalue (&gt;array.length) is demoted to
	 *                            array length.
	 * @return a new array containing the elements between
	 * the start and end indices.
	 * @see Arrays#copyOfRange(short[], int, int)
	 * @since 2.1
	 */
	public static short[] subarray(short[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return null;
		if (startIndexInclusive < 0) startIndexInclusive = 0;
		if (endIndexExclusive > array.length) endIndexExclusive = array.length;
		int newSize = endIndexExclusive - startIndexInclusive;
		if (newSize <= 0) return EMPTY_SHORT_ARRAY;

		short[] subarray = new short[newSize];
		System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
		return subarray;
	}

	/**
	 * <p>Produces a new {@code char} array containing the elements
	 * between the start and end indices.
	 *
	 * <p>The start index is inclusive, the end index exclusive.
	 * Null array input produces null output.
	 *
	 * @param array               the array
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0)
	 *                            is promoted to 0, overvalue (&gt;array.length) results
	 *                            in an empty array.
	 * @param endIndexExclusive   elements up to endIndex-1 are present in the
	 *                            returned subarray. Undervalue (&lt; startIndex) produces
	 *                            empty array, overvalue (&gt;array.length) is demoted to
	 *                            array length.
	 * @return a new array containing the elements between
	 * the start and end indices.
	 * @see Arrays#copyOfRange(char[], int, int)
	 * @since 2.1
	 */
	public static char[] subarray(char[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return null;
		if (startIndexInclusive < 0) startIndexInclusive = 0;
		if (endIndexExclusive > array.length) endIndexExclusive = array.length;
		int newSize = endIndexExclusive - startIndexInclusive;
		if (newSize <= 0) return EMPTY_CHAR_ARRAY;

		char[] subarray = new char[newSize];
		System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
		return subarray;
	}

	/**
	 * <p>Produces a new {@code byte} array containing the elements
	 * between the start and end indices.
	 *
	 * <p>The start index is inclusive, the end index exclusive.
	 * Null array input produces null output.
	 *
	 * @param array               the array
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0)
	 *                            is promoted to 0, overvalue (&gt;array.length) results
	 *                            in an empty array.
	 * @param endIndexExclusive   elements up to endIndex-1 are present in the
	 *                            returned subarray. Undervalue (&lt; startIndex) produces
	 *                            empty array, overvalue (&gt;array.length) is demoted to
	 *                            array length.
	 * @return a new array containing the elements between
	 * the start and end indices.
	 * @see Arrays#copyOfRange(byte[], int, int)
	 * @since 2.1
	 */
	public static byte[] subarray(byte[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return null;
		if (startIndexInclusive < 0) startIndexInclusive = 0;
		if (endIndexExclusive > array.length) endIndexExclusive = array.length;
		int newSize = endIndexExclusive - startIndexInclusive;
		if (newSize <= 0) return EMPTY_BYTE_ARRAY;

		byte[] subarray = new byte[newSize];
		System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
		return subarray;
	}

	/**
	 * <p>Produces a new {@code double} array containing the elements
	 * between the start and end indices.
	 *
	 * <p>The start index is inclusive, the end index exclusive.
	 * Null array input produces null output.
	 *
	 * @param array               the array
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0)
	 *                            is promoted to 0, overvalue (&gt;array.length) results
	 *                            in an empty array.
	 * @param endIndexExclusive   elements up to endIndex-1 are present in the
	 *                            returned subarray. Undervalue (&lt; startIndex) produces
	 *                            empty array, overvalue (&gt;array.length) is demoted to
	 *                            array length.
	 * @return a new array containing the elements between
	 * the start and end indices.
	 * @see Arrays#copyOfRange(double[], int, int)
	 * @since 2.1
	 */
	public static double[] subarray(double[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return null;
		if (startIndexInclusive < 0) startIndexInclusive = 0;
		if (endIndexExclusive > array.length) endIndexExclusive = array.length;
		int newSize = endIndexExclusive - startIndexInclusive;
		if (newSize <= 0) return EMPTY_DOUBLE_ARRAY;

		double[] subarray = new double[newSize];
		System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
		return subarray;
	}

	/**
	 * <p>Produces a new {@code float} array containing the elements
	 * between the start and end indices.
	 *
	 * <p>The start index is inclusive, the end index exclusive.
	 * Null array input produces null output.
	 *
	 * @param array               the array
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0)
	 *                            is promoted to 0, overvalue (&gt;array.length) results
	 *                            in an empty array.
	 * @param endIndexExclusive   elements up to endIndex-1 are present in the
	 *                            returned subarray. Undervalue (&lt; startIndex) produces
	 *                            empty array, overvalue (&gt;array.length) is demoted to
	 *                            array length.
	 * @return a new array containing the elements between
	 * the start and end indices.
	 * @see Arrays#copyOfRange(float[], int, int)
	 * @since 2.1
	 */
	public static float[] subarray(float[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return null;
		if (startIndexInclusive < 0) startIndexInclusive = 0;
		if (endIndexExclusive > array.length) endIndexExclusive = array.length;
		int newSize = endIndexExclusive - startIndexInclusive;
		if (newSize <= 0) return EMPTY_FLOAT_ARRAY;

		float[] subarray = new float[newSize];
		System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
		return subarray;
	}

	/**
	 * <p>Produces a new {@code boolean} array containing the elements
	 * between the start and end indices.
	 *
	 * <p>The start index is inclusive, the end index exclusive.
	 * Null array input produces null output.
	 *
	 * @param array               the array
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0)
	 *                            is promoted to 0, overvalue (&gt;array.length) results
	 *                            in an empty array.
	 * @param endIndexExclusive   elements up to endIndex-1 are present in the
	 *                            returned subarray. Undervalue (&lt; startIndex) produces
	 *                            empty array, overvalue (&gt;array.length) is demoted to
	 *                            array length.
	 * @return a new array containing the elements between
	 * the start and end indices.
	 * @see Arrays#copyOfRange(boolean[], int, int)
	 * @since 2.1
	 */
	public static boolean[] subarray(boolean[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return null;
		if (startIndexInclusive < 0) startIndexInclusive = 0;
		if (endIndexExclusive > array.length) endIndexExclusive = array.length;
		int newSize = endIndexExclusive - startIndexInclusive;
		if (newSize <= 0) return EMPTY_BOOLEAN_ARRAY;

		boolean[] subarray = new boolean[newSize];
		System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
		return subarray;
	}

	/**
	 * <p>Returns the length of the specified array.
	 * This method can deal with {@code Object} arrays and with primitive arrays.
	 *
	 * <p>If the input array is {@code null}, {@code 0} is returned.
	 *
	 * <pre>
	 * ArrayUtils.getLength(null)            = 0
	 * ArrayUtils.getLength([])              = 0
	 * ArrayUtils.getLength([null])          = 1
	 * ArrayUtils.getLength([true, false])   = 2
	 * ArrayUtils.getLength([1, 2, 3])       = 3
	 * ArrayUtils.getLength(["a", "b", "c"]) = 3
	 * </pre>
	 *
	 * @param array the array to retrieve the length from, may be null
	 * @return The length of the array, or {@code 0} if the array is {@code null}
	 * @throws IllegalArgumentException if the object argument is not an array.
	 * @since 2.1
	 */
	public static int getLength(Object array) {
		return Array.getLength(array);
	}

	/**
	 * <p>Reverses the order of the given array.
	 *
	 * <p>There is no special handling for multi-dimensional arrays.
	 *
	 * <p>This method does nothing for a {@code null} input array.
	 *
	 * @param array the array to reverse, may be {@code null}
	 */
	public static <X> X[] reverse(X[] array) {
		reverse(array, 0, array.length);
		return array;
	}

	/**
	 * <p>Reverses the order of the given array.
	 *
	 * <p>This method does nothing for a {@code null} input array.
	 *
	 * @param array the array to reverse, may be {@code null}
	 */
	public static void reverse(long[] array) {
		reverse(array, 0, array.length);
	}

	/**
	 * <p>Reverses the order of the given array.
	 *
	 * <p>This method does nothing for a {@code null} input array.
	 *
	 * @param a the array to reverse, may be {@code null}
	 */
	public static int[] reverse(int[] a) {
		int l = a.length;
		switch (l) {
			case 0:
			case 1:
				break;
			case 2: {
				int i = a[0];
				a[0] = a[1];
				a[1] = i;
				break;
			}
			default:
				reverse(a, 0, l);
				break;
		}
		return a;
	}

	/**
	 * <p>Reverses the order of the given array.
	 *
	 * <p>This method does nothing for a {@code null} input array.
	 *
	 * @param array the array to reverse, may be {@code null}
	 */
	public static void reverse(short[] array) {
		if (array == null) return;
		reverse(array, 0, array.length);
	}

	/**
	 * <p>Reverses the order of the given array.
	 *
	 * <p>This method does nothing for a {@code null} input array.
	 *
	 * @param array the array to reverse, may be {@code null}
	 */
	public static void reverse(char[] array) {
		if (array == null) return;
		reverse(array, 0, array.length);
	}

	/**
	 * <p>Reverses the order of the given array.
	 *
	 * <p>This method does nothing for a {@code null} input array.
	 *
	 * @param array the array to reverse, may be {@code null}
	 */
	public static void reverse(byte[] array) {
		if (array == null || array.length < 2)
			return;
		reverse(array, 0, array.length);
	}

	/**
	 * <p>Reverses the order of the given array.
	 *
	 * <p>This method does nothing for a {@code null} input array.
	 *
	 * @param array the array to reverse, may be {@code null}
	 */
	public static void reverse(double[] array) {
		if (array == null) return;
		reverse(array, 0, array.length);
	}

	/**
	 * <p>Reverses the order of the given array.
	 *
	 * <p>This method does nothing for a {@code null} input array.
	 *
	 * @param array the array to reverse, may be {@code null}
	 */
	public static void reverse(float[] array) {
		if (array == null) return;
		reverse(array, 0, array.length);
	}

	/**
	 * <p>Reverses the order of the given array.
	 *
	 * <p>This method does nothing for a {@code null} input array.
	 *
	 * @param array the array to reverse, may be {@code null}
	 */
	public static void reverse(boolean[] array) {
		if (array == null) return;
		reverse(array, 0, array.length);
	}

	/**
	 * <p>
	 * Reverses the order of the given array in the given range.
	 *
	 * <p>
	 * This method does nothing for a {@code null} input array.
	 *
	 * @param array               the array to reverse, may be {@code null}
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0) is promoted to 0, overvalue (&gt;array.length) results in no
	 *                            change.
	 * @param endIndexExclusive   elements up to endIndex-1 are reversed in the array. Undervalue (&lt; start index) results in no
	 *                            change. Overvalue (&gt;array.length) is demoted to array length.
	 * @since 3.2
	 */
	public static void reverse(boolean[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return;
		int i = Math.max(startIndexInclusive, 0);
		int j = Math.min(array.length, endIndexExclusive) - 1;
		while (j > i) {
			boolean tmp = array[j];
			array[j] = array[i];
			array[i] = tmp;
			j--;
			i++;
		}
	}

	/**
	 * <p>
	 * Reverses the order of the given array in the given range.
	 *
	 * <p>
	 * This method does nothing for a {@code null} input array.
	 *
	 * @param array               the array to reverse, may be {@code null}
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0) is promoted to 0, overvalue (&gt;array.length) results in no
	 *                            change.
	 * @param endIndexExclusive   elements up to endIndex-1 are reversed in the array. Undervalue (&lt; start index) results in no
	 *                            change. Overvalue (&gt;array.length) is demoted to array length.
	 * @since 3.2
	 */
	public static void reverse(byte[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return;
		int i = Math.max(startIndexInclusive, 0);
		int j = Math.min(array.length, endIndexExclusive) - 1;
		while (j > i) {
			byte tmp = array[j];
			array[j] = array[i];
			array[i] = tmp;
			j--;
			i++;
		}
	}

	/**
	 * <p>
	 * Reverses the order of the given array in the given range.
	 *
	 * <p>
	 * This method does nothing for a {@code null} input array.
	 *
	 * @param array               the array to reverse, may be {@code null}
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0) is promoted to 0, overvalue (&gt;array.length) results in no
	 *                            change.
	 * @param endIndexExclusive   elements up to endIndex-1 are reversed in the array. Undervalue (&lt; start index) results in no
	 *                            change. Overvalue (&gt;array.length) is demoted to array length.
	 * @since 3.2
	 */
	public static void reverse(char[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return;
		int i = Math.max(startIndexInclusive, 0);
		int j = Math.min(array.length, endIndexExclusive) - 1;
		while (j > i) {
			char tmp = array[j];
			array[j] = array[i];
			array[i] = tmp;
			j--;
			i++;
		}
	}

	/**
	 * <p>
	 * Reverses the order of the given array in the given range.
	 *
	 * <p>
	 * This method does nothing for a {@code null} input array.
	 *
	 * @param array               the array to reverse, may be {@code null}
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0) is promoted to 0, overvalue (&gt;array.length) results in no
	 *                            change.
	 * @param endIndexExclusive   elements up to endIndex-1 are reversed in the array. Undervalue (&lt; start index) results in no
	 *                            change. Overvalue (&gt;array.length) is demoted to array length.
	 * @since 3.2
	 */
	public static void reverse(double[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return;
		int i = Math.max(startIndexInclusive, 0);
		int j = Math.min(array.length, endIndexExclusive) - 1;
		while (j > i) {
			double tmp = array[j];
			array[j] = array[i];
			array[i] = tmp;
			j--;
			i++;
		}
	}

	/**
	 * <p>
	 * Reverses the order of the given array in the given range.
	 *
	 * <p>
	 * This method does nothing for a {@code null} input array.
	 *
	 * @param array               the array to reverse, may be {@code null}
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0) is promoted to 0, overvalue (&gt;array.length) results in no
	 *                            change.
	 * @param endIndexExclusive   elements up to endIndex-1 are reversed in the array. Undervalue (&lt; start index) results in no
	 *                            change. Overvalue (&gt;array.length) is demoted to array length.
	 * @since 3.2
	 */
	public static void reverse(float[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return;
		int i = Math.max(startIndexInclusive, 0);
		int j = Math.min(array.length, endIndexExclusive) - 1;
		while (j > i) {
			float tmp = array[j];
			array[j] = array[i];
			array[i] = tmp;
			j--;
			i++;
		}
	}

	/**
	 * <p>
	 * Reverses the order of the given array in the given range.
	 *
	 * <p>
	 * This method does nothing for a {@code null} input array.
	 *
	 * @param array               the array to reverse, may be {@code null}
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0) is promoted to 0, overvalue (&gt;array.length) results in no
	 *                            change.
	 * @param endIndexExclusive   elements up to endIndex-1 are reversed in the array. Undervalue (&lt; start index) results in no
	 *                            change. Overvalue (&gt;array.length) is demoted to array length.
	 * @since 3.2
	 */
	public static void reverse(int[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null || array.length <= 1 || (endIndexExclusive - startIndexInclusive <= 1)) return;
		int i = Math.max(startIndexInclusive, 0);
		int j = Math.min(array.length, endIndexExclusive) - 1;
		while (j > i) {
			int tmp = array[j];
			array[j--] = array[i];
			array[i++] = tmp;
		}
	}

	/**
	 * <p>
	 * Reverses the order of the given array in the given range.
	 *
	 * <p>
	 * This method does nothing for a {@code null} input array.
	 *
	 * @param array               the array to reverse, may be {@code null}
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0) is promoted to 0, overvalue (&gt;array.length) results in no
	 *                            change.
	 * @param endIndexExclusive   elements up to endIndex-1 are reversed in the array. Undervalue (&lt; start index) results in no
	 *                            change. Overvalue (&gt;array.length) is demoted to array length.
	 * @since 3.2
	 */
	public static void reverse(long[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return;
		int i = Math.max(startIndexInclusive, 0);
		int j = Math.min(array.length, endIndexExclusive) - 1;
		while (j > i) {
			long tmp = array[j];
			array[j] = array[i];
			array[i] = tmp;
			j--;
			i++;
		}
	}

	/**
	 * <p>
	 * Reverses the order of the given array in the given range.
	 *
	 * <p>
	 * This method does nothing for a {@code null} input array.
	 *
	 * @param array               the array to reverse, may be {@code null}
	 * @param startIndexInclusive the starting index. Under value (&lt;0) is promoted to 0, over value (&gt;array.length) results in no
	 *                            change.
	 * @param endIndexExclusive   elements up to endIndex-1 are reversed in the array. Under value (&lt; start index) results in no
	 *                            change. Over value (&gt;array.length) is demoted to array length.
	 * @since 3.2
	 */
	public static void reverse(@Nullable Object[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return;
		int i = Math.max(startIndexInclusive, 0),
			j = Math.min(array.length, endIndexExclusive) - 1;
		while (j > i) {
			_swap(array, j--, i++);
		}
	}

	/**
	 * <p>
	 * Reverses the order of the given array in the given range.
	 *
	 * <p>
	 * This method does nothing for a {@code null} input array.
	 *
	 * @param array               the array to reverse, may be {@code null}
	 * @param startIndexInclusive the starting index. Undervalue (&lt;0) is promoted to 0, overvalue (&gt;array.length) results in no
	 *                            change.
	 * @param endIndexExclusive   elements up to endIndex-1 are reversed in the array. Undervalue (&lt; start index) results in no
	 *                            change. Overvalue (&gt;array.length) is demoted to array length.
	 * @since 3.2
	 */
	public static void reverse(short[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) return;
		int i = Math.max(startIndexInclusive, 0);
		int j = Math.min(array.length, endIndexExclusive) - 1;
		while (j > i) {
			short tmp = array[j];
			array[j] = array[i];
			array[i] = tmp;
			j--;
			i++;
		}
	}

	/**
	 * Swaps a series of elements in the given boolean array.
	 *
	 * <p>This method does nothing for a {@code null} or empty input array or
	 * for overflow indices. Negative indices are promoted to 0(zero). If any
	 * of the sub-arrays to swap falls outside of the given array, then the
	 * swap is stopped at the end of the array and as many as possible elements
	 * are swapped.</p>
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>ArrayUtils.swap([true, false, true, false], 0, 2, 1) -&gt; [true, false, true, false]</li>
	 * <li>ArrayUtils.swap([true, false, true, false], 0, 0, 1) -&gt; [true, false, true, false]</li>
	 * <li>ArrayUtils.swap([true, false, true, false], 0, 2, 2) -&gt; [true, false, true, false]</li>
	 * <li>ArrayUtils.swap([true, false, true, false], -3, 2, 2) -&gt; [true, false, true, false]</li>
	 * <li>ArrayUtils.swap([true, false, true, false], 0, 3, 3) -&gt; [false, false, true, true]</li>
	 * </ul>
	 *
	 * @param array   the array to swap, may be {@code null}
	 * @param a the index of the first element in the series to swap
	 * @param b the index of the second element in the series to swap
	 * @param len     the number of elements to swap starting with the given indices
	 * @since 3.5
	 */
	public static void swap(boolean[] array, int a, int b, int len) {
		if (array == null || array.length == 0 || a >= array.length || b >= array.length) return;
		if (a < 0) a = 0;
		if (b < 0) b = 0;
		len = Math.min(Math.min(len, array.length - a), array.length - b);
		for (int i = 0; i < len; i++, a++, b++) {
			boolean aux = array[a];
			array[a] = array[b];
			array[b] = aux;
		}
	}

	/**
	 * Swaps a series of elements in the given byte array.
	 *
	 * <p>This method does nothing for a {@code null} or empty input array or
	 * for overflow indices. Negative indices are promoted to 0(zero). If any
	 * of the sub-arrays to swap falls outside of the given array, then the
	 * swap is stopped at the end of the array and as many as possible elements
	 * are swapped.</p>
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 2, 1) -&gt; [3, 2, 1, 4]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 0, 1) -&gt; [1, 2, 3, 4]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 2, 0, 2) -&gt; [3, 4, 1, 2]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], -3, 2, 2) -&gt; [3, 4, 1, 2]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 3, 3) -&gt; [4, 2, 3, 1]</li>
	 * </ul>
	 *
	 * @param array   the array to swap, may be {@code null}
	 * @param a the index of the first element in the series to swap
	 * @param b the index of the second element in the series to swap
	 * @param len     the number of elements to swap starting with the given indices
	 * @since 3.5
	 */
	public static void swap(byte[] array, int a, int b, int len) {
		if (len == 1) swapByte(array, a, b);
		else {
			int alen = array.length;
			if (alen <= 1 || a >= alen || b >= alen) return;
			if (a < 0) a = 0;
			if (b < 0) b = 0;
			len = Math.min(Math.min(len, alen - a), alen - b);
			for (int i = 0; i < len; i++, a++, b++)
				swapByte(array, a, b);
		}
	}

	public static void swapByte(byte[] array, int a, int b) {
		if (a == b)
			return;
		byte aux = array[a];
		array[a] = array[b];
		array[b] = aux;
	}

	public static void swapLong(long[] array, int a, int b) {
		if (a == b)
			return;
		long aux = array[a];
		array[a] = array[b];
		array[b] = aux;
	}

	public static void swapInt(int[] array, int a, int b) {
		if (a == b)
			return;
		int aux = array[a];
		array[a] = array[b];
		array[b] = aux;
	}

	public static void swapDouble(double[] array, int a, int b) {
		if (a == b)
			return;
		double aux = array[a];
		array[a] = array[b];
		array[b] = aux;
	}

	public static void swapFloat(float[] array, int a, int b) {
		if (a == b)
			return;
		float aux = array[a];
		array[a] = array[b];
		array[b] = aux;
	}

	public static void swapBool(boolean[] array, int a, int b) {
		if (a == b)
			return;
		boolean aux = array[a];
		array[a] = array[b];
		array[b] = aux;
	}

	public static void swap(Object[] o, int a, int b) {
		if (a != b)
			_swap(o, a, b);
	}

	/** see: Arrays.swap */
	private static void _swap(Object[] o, int a, int b) {
		Object aux = o[a];
		o[a] = o[b];
		o[b] = aux;
	}

	public static void swapObjFloat(Object[] o, float[] f, int a, int b) {
		if (a == b) return;

		_swap(o, a, b);

		float fx = f[a];
		f[a] = f[b];
		f[b] = fx;
	}

	public static void swapObjShort(Object[] o, short[] f, int a, int b) {
		if (a == b) return;

		_swap(o, a, b);

		short fx = f[a];
		f[a] = f[b];
		f[b] = fx;
	}

	public static void swapObjInt(Object[] o, int[] f, int a, int b) {
		if (a == b) return;

		_swap(o, a, b);

		int fx = f[a];
		f[a] = f[b];
		f[b] = fx;
	}

	public static void swapShort(short[] o, int a, int b) {
		if (a != b) {
			short aux = o[a];
			o[a] = o[b];
			o[b] = aux;
		}
	}

	/**
	 * Swaps a series of elements in the given long array.
	 *
	 * <p>This method does nothing for a {@code null} or empty input array or
	 * for overflow indices. Negative indices are promoted to 0(zero). If any
	 * of the sub-arrays to swap falls outside of the given array, then the
	 * swap is stopped at the end of the array and as many as possible elements
	 * are swapped.</p>
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 2, 1) -&gt; [3, 2, 1, 4]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 0, 1) -&gt; [1, 2, 3, 4]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 2, 0, 2) -&gt; [3, 4, 1, 2]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], -3, 2, 2) -&gt; [3, 4, 1, 2]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 3, 3) -&gt; [4, 2, 3, 1]</li>
	 * </ul>
	 *
	 * @param array   the array to swap, may be {@code null}
	 * @param offset1 the index of the first element in the series to swap
	 * @param offset2 the index of the second element in the series to swap
	 * @param len     the number of elements to swap starting with the given indices
	 * @since 3.5
	 */
	public static void swap(long[] array, int offset1, int offset2, int len) {
		if (len == 1) swapLong(array, offset1, offset2);
		else {
			int alen = array.length;
			if (alen <= 1 || offset1 >= alen || offset2 >= alen) return;
			if (offset1 < 0) offset1 = 0;
			if (offset2 < 0) offset2 = 0;
			len = Math.min(Math.min(len, alen - offset1), alen - offset2);
			for (int i = 0; i < len; i++, offset1++, offset2++)
				swapLong(array, offset1, offset2);
		}
	}

	public static void swap(int[] array, int offset1, int offset2, int len) {
		if (len == 1) swapInt(array, offset1, offset2);
		else {
			int alen = array.length;
			if (alen <= 1 || offset1 >= alen || offset2 >= alen) return;
			if (offset1 < 0) offset1 = 0;
			if (offset2 < 0) offset2 = 0;
			len = Math.min(Math.min(len, alen - offset1), alen - offset2);
			for (int i = 0; i < len; i++, offset1++, offset2++)
				swapInt(array, offset1, offset2);
		}
	}

	public static void swap(short[] array, int offset1, int offset2, int len) {
		if (len == 1) swapShort(array, offset1, offset2);
		else {
			int alen = array.length;
			if (alen <= 1 || offset1 >= alen || offset2 >= alen) return;
			if (offset1 < 0) offset1 = 0;
			if (offset2 < 0) offset2 = 0;
			len = Math.min(Math.min(len, alen - offset1), alen - offset2);
			for (int i = 0; i < len; i++, offset1++, offset2++)
				swapShort(array, offset1, offset2);
		}
	}

	/**
	 * Swaps a series of elements in the given char array.
	 *
	 * <p>This method does nothing for a {@code null} or empty input array or
	 * for overflow indices. Negative indices are promoted to 0(zero). If any
	 * of the sub-arrays to swap falls outside of the given array, then the
	 * swap is stopped at the end of the array and as many as possible elements
	 * are swapped.</p>
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 2, 1) -&gt; [3, 2, 1, 4]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 0, 1) -&gt; [1, 2, 3, 4]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 2, 0, 2) -&gt; [3, 4, 1, 2]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], -3, 2, 2) -&gt; [3, 4, 1, 2]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 3, 3) -&gt; [4, 2, 3, 1]</li>
	 * </ul>
	 *
	 * @param array   the array to swap, may be {@code null}
	 * @param offset1 the index of the first element in the series to swap
	 * @param offset2 the index of the second element in the series to swap
	 * @param len     the number of elements to swap starting with the given indices
	 * @since 3.5
	 */
	public static void swap(char[] array, int offset1, int offset2, int len) {
		if (array == null || array.length == 0 || offset1 >= array.length || offset2 >= array.length) return;
		if (offset1 < 0) offset1 = 0;
		if (offset2 < 0) offset2 = 0;
		len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
		for (int i = 0; i < len; i++, offset1++, offset2++) {
			char aux = array[offset1];
			array[offset1] = array[offset2];
			array[offset2] = aux;
		}
	}

	/**
	 * Swaps a series of elements in the given double array.
	 *
	 * <p>This method does nothing for a {@code null} or empty input array or
	 * for overflow indices. Negative indices are promoted to 0(zero). If any
	 * of the sub-arrays to swap falls outside of the given array, then the
	 * swap is stopped at the end of the array and as many as possible elements
	 * are swapped.</p>
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 2, 1) -&gt; [3, 2, 1, 4]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 0, 1) -&gt; [1, 2, 3, 4]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 2, 0, 2) -&gt; [3, 4, 1, 2]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], -3, 2, 2) -&gt; [3, 4, 1, 2]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 3, 3) -&gt; [4, 2, 3, 1]</li>
	 * </ul>
	 *
	 * @param array   the array to swap, may be {@code null}
	 * @param offset1 the index of the first element in the series to swap
	 * @param offset2 the index of the second element in the series to swap
	 * @param len     the number of elements to swap starting with the given indices
	 * @since 3.5
	 */
	public static void swap(double[] array, int offset1, int offset2, int len) {
		if (array == null || array.length == 0 || offset1 >= array.length || offset2 >= array.length) return;
		if (offset1 < 0) offset1 = 0;
		if (offset2 < 0) offset2 = 0;
		len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
		for (int i = 0; i < len; i++, offset1++, offset2++) {
			double aux = array[offset1];
			array[offset1] = array[offset2];
			array[offset2] = aux;
		}
	}

	/**
	 * Swaps a series of elements in the given float array.
	 *
	 * <p>This method does nothing for a {@code null} or empty input array or
	 * for overflow indices. Negative indices are promoted to 0(zero). If any
	 * of the sub-arrays to swap falls outside of the given array, then the
	 * swap is stopped at the end of the array and as many as possible elements
	 * are swapped.</p>
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 2, 1) -&gt; [3, 2, 1, 4]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 0, 1) -&gt; [1, 2, 3, 4]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 2, 0, 2) -&gt; [3, 4, 1, 2]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], -3, 2, 2) -&gt; [3, 4, 1, 2]</li>
	 * <li>ArrayUtils.swap([1, 2, 3, 4], 0, 3, 3) -&gt; [4, 2, 3, 1]</li>
	 * </ul>
	 *
	 * @param array   the array to swap, may be {@code null}
	 * @param offset1 the index of the first element in the series to swap
	 * @param offset2 the index of the second element in the series to swap
	 * @param len     the number of elements to swap starting with the given indices
	 * @since 3.5
	 */
	public static void swap(float[] array, int offset1, int offset2, int len) {
		if (array == null || array.length == 0 || offset1 >= array.length || offset2 >= array.length) return;
		if (offset1 < 0) offset1 = 0;
		if (offset2 < 0) offset2 = 0;
		len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
		for (int i = 0; i < len; i++, offset1++, offset2++) {
			float aux = array[offset1];
			array[offset1] = array[offset2];
			array[offset2] = aux;
		}

	}

	/**
	 * Swaps a series of elements in the given array.
	 *
	 * <p>This method does nothing for a {@code null} or empty input array or
	 * for overflow indices. Negative indices are promoted to 0(zero). If any
	 * of the sub-arrays to swap falls outside of the given array, then the
	 * swap is stopped at the end of the array and as many as possible elements
	 * are swapped.</p>
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>ArrayUtils.swap(["1", "2", "3", "4"], 0, 2, 1) -&gt; ["3", "2", "1", "4"]</li>
	 * <li>ArrayUtils.swap(["1", "2", "3", "4"], 0, 0, 1) -&gt; ["1", "2", "3", "4"]</li>
	 * <li>ArrayUtils.swap(["1", "2", "3", "4"], 2, 0, 2) -&gt; ["3", "4", "1", "2"]</li>
	 * <li>ArrayUtils.swap(["1", "2", "3", "4"], -3, 2, 2) -&gt; ["3", "4", "1", "2"]</li>
	 * <li>ArrayUtils.swap(["1", "2", "3", "4"], 0, 3, 3) -&gt; ["4", "2", "3", "1"]</li>
	 * </ul>
	 *
	 * @param array   the array to swap, may be {@code null}
	 * @param offset1 the index of the first element in the series to swap
	 * @param offset2 the index of the second element in the series to swap
	 * @param len     the number of elements to swap starting with the given indices
	 * @since 3.5
	 */
	public static void swap(Object[] array, int offset1, int offset2, int len) {
		if (len == 1) swap(array, offset1, offset2);
		else {
			int alen = array.length;
			if (alen <= 1 || offset1 >= alen || offset2 >= alen) return;
			if (offset1 < 0) offset1 = 0;
			if (offset2 < 0) offset2 = 0;
			len = Math.min(Math.min(len, alen - offset1), alen - offset2);
			for (int i = 0; i < len; i++, offset1++, offset2++)
				swap(array, offset1, offset2);
		}
	}

	/**
	 * <p>Finds the index of the given object in the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array        the array to search through for the object, may be {@code null}
	 * @param objectToFind the object to find, may be {@code null}
	 * @return the index of the object within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(Object[] array, Object objectToFind) {
		return indexOf(array, objectToFind, 0);
	}

	public static int indexOf(Object[] array, Object objectToFind, int startIndex) {
		return indexOf(array, objectToFind, startIndex, array.length);
	}

	/**
	 * <p>Finds the index of the given object in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex is treated as zero. A startIndex larger than the array
	 * length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
	 *
	 * @param array        the array to search through for the object, may be {@code null}
	 * @param objectToFind the object to find, may be {@code null}
	 * @param startIndex   the index to start searching at
	 * @return the index of the object within the array starting at the index,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(Object[] array, Object objectToFind, int startIndex, int endIndex) {
		for (int i = startIndex; i < endIndex; i++) {
			if (objectToFind.equals(array[i])) return i;
		}
		return -1;
	}
	public static int indexOf(short[] array, short objectToFind, int startIndex, int endIndex) {
		for (int i = startIndex; i < endIndex; i++) {
			if (objectToFind==array[i]) return i;
		}
		return -1;
	}
	public static int indexOfInstance(Object[] array, Object objectToFind, int startIndex, int endIndex) {
		for (int i = startIndex; i < endIndex; i++) {
			if (objectToFind == array[i]) return i;
		}
		return -1;
	}

	public static <X> int indexOf(X[] array, Predicate<X> test) {
		return indexOf(array, test, 0);
	}

	public static <X> int indexOf(X[] array, Predicate<X> test, int startIndex) {

		int n = array.length;
		for (int i = startIndex; i < n; i++) {
			if (test.test(array[i]))
				return i;
		}

		return -1;
	}

	/**
	 * <p>Finds the last index of the given object within the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array        the array to traverse backwards looking for the object, may be {@code null}
	 * @param objectToFind the object to find, may be {@code null}
	 * @return the last index of the object within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(Object[] array, Object objectToFind) {
		return lastIndexOf(array, objectToFind, Integer.MAX_VALUE);
	}

	/**
	 * <p>Finds the last index of the given object in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} ({@code -1}). A startIndex larger than
	 * the array length will search from the end of the array.
	 *
	 * @param array        the array to traverse for looking for the object, may be {@code null}
	 * @param objectToFind the object to find, may be {@code null}
	 * @param startIndex   the start index to traverse backwards from
	 * @return the last index of the object within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(Object[] array, Object objectToFind, int startIndex) {
		if (array == null) {
			return -1;
		}
		if (startIndex < 0) {
			return -1;
		}
		if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		if (objectToFind == null) {
			for (int i = startIndex; i >= 0; i--) {
				if (array[i] == null) {
					return i;
				}
			}
		} else if (array.getClass().getComponentType().isInstance(objectToFind)) {
			for (int i = startIndex; i >= 0; i--) {
				if (objectToFind.equals(array[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * <p>Checks if the object is in the given array.
	 *
	 * <p>The method returns {@code false} if a {@code null} array is passed in.
	 *
	 * @param array        the array to search through
	 * @param objectToFind the object to find
	 * @return {@code true} if the array contains the object
	 */
	public static boolean contains(Object[] array, Object objectToFind) {
		return indexOf(array, objectToFind) != -1;
	}

	/**
	 * <p>Finds the index of the given value in the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(long[] array, long valueToFind) {
		return indexOf(array, valueToFind, 0, array.length);
	}

	/**
	 * <p>Finds the index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex is treated as zero. A startIndex larger than the array
	 * length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the index to start searching at
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(long[] array, long valueToFind, int startIndex, int endIndex) {
		///if (array == null) return INDEX_NOT_FOUND;
		//if (startIndex < 0) startIndex = 0;
		for (int i = startIndex; i < endIndex; i++) {
			if (valueToFind == array[i])
				return i;
		}
		return -1;
	}
	/**
	 * <p>Finds the last index of the given value within the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to traverse backwards looking for the object, may be {@code null}
	 * @param valueToFind the object to find
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(long[] array, long valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	/**
	 * <p>Finds the last index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} ({@code -1}). A startIndex larger than the
	 * array length will search from the end of the array.
	 *
	 * @param array       the array to traverse for looking for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the start index to traverse backwards from
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(long[] array, long valueToFind, int startIndex) {
		if (array == null) {
			return -1;
		}
		if (startIndex < 0) {
			return -1;
		}
		if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Checks if the value is in the given array.
	 *
	 * <p>The method returns {@code false} if a {@code null} array is passed in.
	 *
	 * @param array       the array to search through
	 * @param valueToFind the value to find
	 * @return {@code true} if the array contains the object
	 */
	public static boolean contains(long[] array, long valueToFind) {
		return indexOf(array, valueToFind) != -1;
	}
	public static boolean contains(long[] array, long valueToFind, int start, int end) {
		return indexOf(array, valueToFind, start, end) != -1;
	}

	/**
	 * <p>Finds the index of the given value in the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(int[] array, int valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	/**
	 * <p>Finds the index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex is treated as zero. A startIndex larger than the array
	 * length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the index to start searching at
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(int[] array, int valueToFind, int startIndex) {
		if (array == null) {
			return -1;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * quick search for items by identity
	 * returns first matching index, though others could exist
	 */
	public static int indexOfInstance(Object[] xx, Object y) {
		for (int i = 0; i < xx.length; i++) {
			if (y == xx[i])
				return i;
		}
		return -1;
	}


	/**
	 * <p>Finds the last index of the given value within the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to traverse backwards looking for the object, may be {@code null}
	 * @param valueToFind the object to find
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(int[] array, int valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	/**
	 * <p>Finds the last index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} ({@code -1}). A startIndex larger than the
	 * array length will search from the end of the array.
	 *
	 * @param array       the array to traverse for looking for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the start index to traverse backwards from
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(int[] array, int valueToFind, int startIndex) {
		if (array == null) {
			return -1;
		}
		if (startIndex < 0) {
			return -1;
		}
		if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Checks if the value is in the given array.
	 *
	 * <p>The method returns {@code false} if a {@code null} array is passed in.
	 *
	 * @param array       the array to search through
	 * @param valueToFind the value to find
	 * @return {@code true} if the array contains the object
	 */
	public static boolean contains(int[] array, int valueToFind) {
		return indexOf(array, valueToFind) != -1;
	}

	/**
	 * <p>Finds the index of the given value in the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(short[] array, short valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	/**
	 * <p>Finds the index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex is treated as zero. A startIndex larger than the array
	 * length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the index to start searching at
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(short[] array, short valueToFind, int startIndex) {
		if (array == null) {
			return -1;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Finds the last index of the given value within the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to traverse backwards looking for the object, may be {@code null}
	 * @param valueToFind the object to find
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(short[] array, short valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	/**
	 * <p>Finds the last index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} ({@code -1}). A startIndex larger than the
	 * array length will search from the end of the array.
	 *
	 * @param array       the array to traverse for looking for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the start index to traverse backwards from
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(short[] array, short valueToFind, int startIndex) {
		if (array == null) {
			return -1;
		}
		if (startIndex < 0) {
			return -1;
		}
		if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Checks if the value is in the given array.
	 *
	 * <p>The method returns {@code false} if a {@code null} array is passed in.
	 *
	 * @param array       the array to search through
	 * @param valueToFind the value to find
	 * @return {@code true} if the array contains the object
	 */
	public static boolean contains(short[] array, short valueToFind) {
		return indexOf(array, valueToFind) != -1;
	}
	public static boolean containsAll(short[] container, short[] x) {
		if (container == x) return true;
		if (container.length < x.length) return false;
		for (short xx : x) {
			if (!contains(container, xx)) return false;
		}
		return true;
	}

	/**
	 * <p>Finds the index of the given value in the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 * @since 2.1
	 */
	public static int indexOf(char[] array, char valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	/**
	 * <p>Finds the index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex is treated as zero. A startIndex larger than the array
	 * length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the index to start searching at
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 * @since 2.1
	 */
	public static int indexOf(char[] array, char valueToFind, int startIndex) {
		if (array == null) {
			return -1;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Finds the last index of the given value within the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to traverse backwards looking for the object, may be {@code null}
	 * @param valueToFind the object to find
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 * @since 2.1
	 */
	public static int lastIndexOf(char[] array, char valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	/**
	 * <p>Finds the last index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} ({@code -1}). A startIndex larger than the
	 * array length will search from the end of the array.
	 *
	 * @param array       the array to traverse for looking for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the start index to traverse backwards from
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 * @since 2.1
	 */
	public static int lastIndexOf(char[] array, char valueToFind, int startIndex) {
		if (array == null) {
			return -1;
		}
		if (startIndex < 0) {
			return -1;
		}
		if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Checks if the value is in the given array.
	 *
	 * <p>The method returns {@code false} if a {@code null} array is passed in.
	 *
	 * @param array       the array to search through
	 * @param valueToFind the value to find
	 * @return {@code true} if the array contains the object
	 * @since 2.1
	 */
	public static boolean contains(char[] array, char valueToFind) {
		return indexOf(array, valueToFind) != -1;
	}

	/**
	 * <p>Finds the index of the given value in the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(byte[] array, byte valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	/**
	 * <p>Finds the index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex is treated as zero. A startIndex larger than the array
	 * length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the index to start searching at
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(byte[] array, byte valueToFind, int startIndex, int endIndex) {
//        if (array != null) {
//            if (startIndex < 0)
//                startIndex = 0;
		for (int i = startIndex; i < endIndex; i++) {
			if (valueToFind == array[i])
				return i;
		}
		//        }
		return -1;
	}

	public static int indexOf(byte[] array, byte valueToFind, int startIndex) {
		return indexOf(array, valueToFind, startIndex, array.length);
	}

	/**
	 * <p>Finds the last index of the given value within the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to traverse backwards looking for the object, may be {@code null}
	 * @param valueToFind the object to find
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(byte[] array, byte valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	/**
	 * <p>Finds the last index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} ({@code -1}). A startIndex larger than the
	 * array length will search from the end of the array.
	 *
	 * @param array       the array to traverse for looking for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the start index to traverse backwards from
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(byte[] array, byte valueToFind, int startIndex) {
		if (array == null) {
			return -1;
		}
		if (startIndex < 0) {
			return -1;
		}
		if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Checks if the value is in the given array.
	 *
	 * <p>The method returns {@code false} if a {@code null} array is passed in.
	 *
	 * @param array       the array to search through
	 * @param valueToFind the value to find
	 * @return {@code true} if the array contains the object
	 */
	public static boolean contains(byte[] array, byte valueToFind) {
		return indexOf(array, valueToFind) != -1;
	}

	/**
	 * <p>Finds the index of the given value in the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(double[] array, double valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	/**
	 * <p>Finds the index of the given value within a given tolerance in the array.
	 * This method will return the index of the first value which falls between the region
	 * defined by valueToFind - tolerance and valueToFind + tolerance.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param tolerance   tolerance of the search
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(double[] array, double valueToFind, double tolerance) {
		return indexOf(array, valueToFind, 0, tolerance);
	}

	/**
	 * <p>Finds the index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex is treated as zero. A startIndex larger than the array
	 * length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the index to start searching at
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(double[] array, double valueToFind, int startIndex) {
		if (array.length == 0) {
			return -1;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Finds the index of the given value in the array starting at the given index.
	 * This method will return the index of the first value which falls between the region
	 * defined by valueToFind - tolerance and valueToFind + tolerance.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex is treated as zero. A startIndex larger than the array
	 * length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the index to start searching at
	 * @param tolerance   tolerance of the search
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(double[] array, double valueToFind, int startIndex, double tolerance) {
		if (array.length == 0) {
			return -1;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		double min = valueToFind - tolerance;
		double max = valueToFind + tolerance;
		for (int i = startIndex; i < array.length; i++) {
			if (array[i] >= min && array[i] <= max) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Finds the last index of the given value within the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to traverse backwards looking for the object, may be {@code null}
	 * @param valueToFind the object to find
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(double[] array, double valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	/**
	 * <p>Finds the last index of the given value within a given tolerance in the array.
	 * This method will return the index of the last value which falls between the region
	 * defined by valueToFind - tolerance and valueToFind + tolerance.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param tolerance   tolerance of the search
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(double[] array, double valueToFind, double tolerance) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE, tolerance);
	}

	/**
	 * <p>Finds the last index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} ({@code -1}). A startIndex larger than the
	 * array length will search from the end of the array.
	 *
	 * @param array       the array to traverse for looking for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the start index to traverse backwards from
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(double[] array, double valueToFind, int startIndex) {
		if (array.length == 0) {
			return -1;
		}
		if (startIndex < 0) {
			return -1;
		}
		if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Finds the last index of the given value in the array starting at the given index.
	 * This method will return the index of the last value which falls between the region
	 * defined by valueToFind - tolerance and valueToFind + tolerance.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} ({@code -1}). A startIndex larger than the
	 * array length will search from the end of the array.
	 *
	 * @param array       the array to traverse for looking for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the start index to traverse backwards from
	 * @param tolerance   search for value within plus/minus this amount
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(double[] array, double valueToFind, int startIndex, double tolerance) {
		if (array.length == 0) {
			return -1;
		}
		if (startIndex < 0) {
			return -1;
		}
		if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		double min = valueToFind - tolerance;
		double max = valueToFind + tolerance;
		for (int i = startIndex; i >= 0; i--) {
			if (array[i] >= min && array[i] <= max) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Checks if the value is in the given array.
	 *
	 * <p>The method returns {@code false} if a {@code null} array is passed in.
	 *
	 * @param array       the array to search through
	 * @param valueToFind the value to find
	 * @return {@code true} if the array contains the object
	 */
	public static boolean contains(double[] array, double valueToFind) {
		return indexOf(array, valueToFind) != -1;
	}

	/**
	 * <p>Checks if a value falling within the given tolerance is in the
	 * given array.  If the array contains a value within the inclusive range
	 * defined by (value - tolerance) to (value + tolerance).
	 *
	 * <p>The method returns {@code false} if a {@code null} array
	 * is passed in.
	 *
	 * @param array       the array to search
	 * @param valueToFind the value to find
	 * @param tolerance   the array contains the tolerance of the search
	 * @return true if value falling within tolerance is in array
	 */
	public static boolean contains(double[] array, double valueToFind, double tolerance) {
		return indexOf(array, valueToFind, 0, tolerance) != -1;
	}

	/**
	 * <p>Finds the index of the given value in the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(float[] array, float valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	/**
	 * <p>Finds the index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex is treated as zero. A startIndex larger than the array
	 * length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the index to start searching at
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(float[] array, float valueToFind, int startIndex) {
		if (array.length == 0) {
			return -1;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Finds the last index of the given value within the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to traverse backwards looking for the object, may be {@code null}
	 * @param valueToFind the object to find
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(float[] array, float valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	/**
	 * <p>Finds the last index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} ({@code -1}). A startIndex larger than the
	 * array length will search from the end of the array.
	 *
	 * @param array       the array to traverse for looking for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the start index to traverse backwards from
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(float[] array, float valueToFind, int startIndex) {
		if (array.length == 0) {
			return -1;
		}
		if (startIndex < 0) {
			return -1;
		}
		if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Checks if the value is in the given array.
	 *
	 * <p>The method returns {@code false} if a {@code null} array is passed in.
	 *
	 * @param array       the array to search through
	 * @param valueToFind the value to find
	 * @return {@code true} if the array contains the object
	 */
	public static boolean contains(float[] array, float valueToFind) {
		return indexOf(array, valueToFind) != -1;
	}

	/**
	 * <p>Finds the index of the given value in the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int indexOf(boolean[] array, boolean valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	/**
	 * <p>Finds the index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex is treated as zero. A startIndex larger than the array
	 * length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
	 *
	 * @param array       the array to search through for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the index to start searching at
	 * @return the index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null}
	 * array input
	 */
	public static int indexOf(boolean[] array, boolean valueToFind, int startIndex) {
		if (array.length == 0) {
			return -1;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Finds the last index of the given value within the array.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) if
	 * {@code null} array input.
	 *
	 * @param array       the array to traverse backwards looking for the object, may be {@code null}
	 * @param valueToFind the object to find
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(boolean[] array, boolean valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	/**
	 * <p>Finds the last index of the given value in the array starting at the given index.
	 *
	 * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.
	 *
	 * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} ({@code -1}). A startIndex larger than
	 * the array length will search from the end of the array.
	 *
	 * @param array       the array to traverse for looking for the object, may be {@code null}
	 * @param valueToFind the value to find
	 * @param startIndex  the start index to traverse backwards from
	 * @return the last index of the value within the array,
	 * {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
	 */
	public static int lastIndexOf(boolean[] array, boolean valueToFind, int startIndex) {
		if (array.length == 0) {
			return -1;
		}
		if (startIndex < 0) {
			return -1;
		}
		if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p>Checks if the value is in the given array.
	 *
	 * <p>The method returns {@code false} if a {@code null} array is passed in.
	 *
	 * @param array       the array to search through
	 * @param valueToFind the value to find
	 * @return {@code true} if the array contains the object
	 */
	public static boolean contains(boolean[] array, boolean valueToFind) {
		return indexOf(array, valueToFind) != -1;
	}



	/**
	 * <p>Adds all the elements of the given arrays into a new array.
	 * <p>The new array contains all of the element of {@code array1} followed
	 * by all of the elements {@code array2}. When an array is returned, it is always
	 * a new array.
	 *
	 * <pre>
	 * ArrayUtils.addAll(null, null)     = null
	 * ArrayUtils.addAll(array1, null)   = cloned copy of array1
	 * ArrayUtils.addAll(null, array2)   = cloned copy of array2
	 * ArrayUtils.addAll([], [])         = []
	 * ArrayUtils.addAll([null], [null]) = [null, null]
	 * ArrayUtils.addAll(["a", "b", "c"], ["1", "2", "3"]) = ["a", "b", "c", "1", "2", "3"]
	 * </pre>
	 *
	 * @param <T>    the component type of the array
	 * @param array1 the first array whose elements are added to the new array, may be {@code null}
	 * @param array2 the second array whose elements are added to the new array, may be {@code null}
	 * @return The new array, {@code null} if both arrays are {@code null}.
	 * The type of the new array is the type of the first array,
	 * unless the first array is null, in which case the type is the same as the second array.
	 * @throws IllegalArgumentException if the array types are incompatible
	 * @since 2.1
	 */
	@SafeVarargs
	public static <T> T[] addAll(T[] array1, T... array2) {
		if (array1 == null) return clone(array2);
		if (array2 == null) return clone(array1);
		Class<?> type1 = array1.getClass().getComponentType();
		@SuppressWarnings("unchecked") T[] joinedArray = (T[]) Array.newInstance(type1, array1.length + array2.length);
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
//		try {
			System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
//		} catch (ArrayStoreException ase) {
//
//			/*
//			 * We do this here, rather than before the copy because:
//			 * - it would be a wasted check most of the time
//			 * - safer, in case check turns out to be too strict
//			 */
//			Class<?> type2 = array2.getClass().getComponentType();
//			if (!type1.isAssignableFrom(type2))
//				throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of "
//					+ type1.getName(), ase);
//			throw ase;
//		}
		return joinedArray;
	}

	/**
	 * <p>Adds all the elements of the given arrays into a new array.
	 * <p>The new array contains all of the element of {@code array1} followed
	 * by all of the elements {@code array2}. When an array is returned, it is always
	 * a new array.
	 *
	 * <pre>
	 * ArrayUtils.addAll(array1, null)   = cloned copy of array1
	 * ArrayUtils.addAll(null, array2)   = cloned copy of array2
	 * ArrayUtils.addAll([], [])         = []
	 * </pre>
	 *
	 * @param array1 the first array whose elements are added to the new array.
	 * @param array2 the second array whose elements are added to the new array.
	 * @return The new boolean[] array.
	 * @since 2.1
	 */
	public static boolean[] addAll(boolean[] array1, boolean... array2) {
		if (array1 == null) return clone(array2);
		if (array2 == null) return clone(array1);
		boolean[] joinedArray = new boolean[array1.length + array2.length];
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		return joinedArray;
	}

	/**
	 * <p>Adds all the elements of the given arrays into a new array.
	 * <p>The new array contains all of the element of {@code array1} followed
	 * by all of the elements {@code array2}. When an array is returned, it is always
	 * a new array.
	 *
	 * <pre>
	 * ArrayUtils.addAll(array1, null)   = cloned copy of array1
	 * ArrayUtils.addAll(null, array2)   = cloned copy of array2
	 * ArrayUtils.addAll([], [])         = []
	 * </pre>
	 *
	 * @param array1 the first array whose elements are added to the new array.
	 * @param array2 the second array whose elements are added to the new array.
	 * @return The new char[] array.
	 * @since 2.1
	 */
	public static char[] addAll(char[] array1, char... array2) {
		if (array1 == null) return clone(array2);
		if (array2 == null) return clone(array1);
		char[] joinedArray = new char[array1.length + array2.length];
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		return joinedArray;
	}

	/** from java.util.ArrayList */
	public static void shiftTailOverGap(Object[] es, int size, int lo, int hi) {
		System.arraycopy(es, hi, es, lo, size - hi);
		int to = size;
		size -= hi - lo;
		Arrays.fill(es, size, to, null);
	}

	public static void shiftTailOverGap(short[] es, int size, int lo, int hi, short emptyFiller) {
		int to = size;
		shiftTailOverGap(es, size, lo, hi);
		size -= hi - lo;
		Arrays.fill(es, size, to, emptyFiller);
	}

	public static void shiftTailOverGap(short[] es, int size, int lo, int hi) {
		System.arraycopy(es, hi, es, lo, size - hi);
	}

	/**
	 * <p>Adds all the elements of the given arrays into a new array.
	 * <p>The new array contains all of the element of {@code array1} followed
	 * by all of the elements {@code array2}. When an array is returned, it is always
	 * a new array.
	 *
	 * <pre>
	 * ArrayUtils.addAll(array1, null)   = cloned copy of array1
	 * ArrayUtils.addAll(null, array2)   = cloned copy of array2
	 * ArrayUtils.addAll([], [])         = []
	 * </pre>
	 *
	 * @param array1 the first array whose elements are added to the new array.
	 * @param array2 the second array whose elements are added to the new array.
	 * @return The new byte[] array.
	 * @since 2.1
	 */
	public static byte[] addAll(byte[] array1, byte... array2) {
		if (array1 == null) return clone(array2);
		if (array2 == null) return clone(array1);
		byte[] joinedArray = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		return joinedArray;
	}

	/**
	 * <p>Adds all the elements of the given arrays into a new array.
	 * <p>The new array contains all of the element of {@code array1} followed
	 * by all of the elements {@code array2}. When an array is returned, it is always
	 * a new array.
	 *
	 * <pre>
	 * ArrayUtils.addAll(array1, null)   = cloned copy of array1
	 * ArrayUtils.addAll(null, array2)   = cloned copy of array2
	 * ArrayUtils.addAll([], [])         = []
	 * </pre>
	 *
	 * @param array1 the first array whose elements are added to the new array.
	 * @param array2 the second array whose elements are added to the new array.
	 * @return The new short[] array.
	 * @since 2.1
	 */
	public static short[] addAll(short[] array1, short... array2) {
		if (array1 == null) return clone(array2);
		if (array2 == null) return clone(array1);
		short[] joinedArray = new short[array1.length + array2.length];
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		return joinedArray;
	}

	/**
	 * <p>Adds all the elements of the given arrays into a new array.
	 * <p>The new array contains all of the element of {@code array1} followed
	 * by all of the elements {@code array2}. When an array is returned, it is always
	 * a new array.
	 *
	 * <pre>
	 * ArrayUtils.addAll(array1, null)   = cloned copy of array1
	 * ArrayUtils.addAll(null, array2)   = cloned copy of array2
	 * ArrayUtils.addAll([], [])         = []
	 * </pre>
	 *
	 * @param array1 the first array whose elements are added to the new array.
	 * @param array2 the second array whose elements are added to the new array.
	 * @return The new int[] array.
	 * @since 2.1
	 */
	public static int[] addAll(int[] array1, int... array2) {
		if (array1 == null) return clone(array2);
		if (array2 == null) return clone(array1);
		int[] joinedArray = new int[array1.length + array2.length];
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		return joinedArray;
	}

	/**
	 * <p>Adds all the elements of the given arrays into a new array.
	 * <p>The new array contains all of the element of {@code array1} followed
	 * by all of the elements {@code array2}. When an array is returned, it is always
	 * a new array.
	 *
	 * <pre>
	 * ArrayUtils.addAll(array1, null)   = cloned copy of array1
	 * ArrayUtils.addAll(null, array2)   = cloned copy of array2
	 * ArrayUtils.addAll([], [])         = []
	 * </pre>
	 *
	 * @param array1 the first array whose elements are added to the new array.
	 * @param array2 the second array whose elements are added to the new array.
	 * @return The new long[] array.
	 * @since 2.1
	 */
	public static long[] addAll(long[] array1, long... array2) {
		if (array1 == null) return clone(array2);
		if (array2 == null) return clone(array1);
		long[] joinedArray = new long[array1.length + array2.length];
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		return joinedArray;
	}

	/**
	 * <p>Adds all the elements of the given arrays into a new array.
	 * <p>The new array contains all of the element of {@code array1} followed
	 * by all of the elements {@code array2}. When an array is returned, it is always
	 * a new array.
	 *
	 * <pre>
	 * ArrayUtils.addAll(array1, null)   = cloned copy of array1
	 * ArrayUtils.addAll(null, array2)   = cloned copy of array2
	 * ArrayUtils.addAll([], [])         = []
	 * </pre>
	 *
	 * @param array1 the first array whose elements are added to the new array.
	 * @param array2 the second array whose elements are added to the new array.
	 * @return The new float[] array.
	 * @since 2.1
	 */
	public static float[] addAll(float[] array1, float... array2) {
		if (array1 == null) return clone(array2);
		if (array2 == null) return clone(array1);
		float[] joinedArray = new float[array1.length + array2.length];
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		return joinedArray;
	}

	/**
	 * <p>Adds all the elements of the given arrays into a new array.
	 * <p>The new array contains all of the element of {@code array1} followed
	 * by all of the elements {@code array2}. When an array is returned, it is always
	 * a new array.
	 *
	 * <pre>
	 * ArrayUtils.addAll(array1, null)   = cloned copy of array1
	 * ArrayUtils.addAll(null, array2)   = cloned copy of array2
	 * ArrayUtils.addAll([], [])         = []
	 * </pre>
	 *
	 * @param array1 the first array whose elements are added to the new array.
	 * @param array2 the second array whose elements are added to the new array.
	 * @return The new double[] array.
	 * @since 2.1
	 */
	public static double[] addAll(double[] array1, double... array2) {
		if (array1 == null) return clone(array2);
		if (array2 == null) return clone(array1);
		double[] joinedArray = new double[array1.length + array2.length];
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		return joinedArray;
	}

	/**
	 * <p>Copies the given array and adds the given element at the end of the new array.
	 *
	 * <p>The new array contains the same elements of the input
	 * array plus the given element in the last position. The component type of
	 * the new array is the same as that of the input array.
	 *
	 * <p>If the input array is {@code null}, a new one element array is returned
	 * whose component type is the same as the element, unless the element itself is null,
	 * in which case the return type is Object[]
	 *
	 * <pre>
	 * ArrayUtils.addAt(null, null)      = IllegalArgumentException
	 * ArrayUtils.addAt(null, "a")       = ["a"]
	 * ArrayUtils.addAt(["a"], null)     = ["a", null]
	 * ArrayUtils.addAt(["a"], "b")      = ["a", "b"]
	 * ArrayUtils.addAt(["a", "b"], "c") = ["a", "b", "c"]
	 * </pre>
	 *
	 * @param <T>     the component type of the array
	 * @param x   the array to "addAt" the element to, may be {@code null}
	 * @param element the object to addAt, may be {@code null}
	 * @return A new array containing the existing elements plus the new element
	 * The returned array type will be that of the input array (unless null),
	 * in which case it will have the same type as the element.
	 * If both are null, an IllegalArgumentException is thrown
	 * @throws IllegalArgumentException if both arguments are null
	 * @since 2.1
	 */
	public static <T> T[] add(T[] x, T element) {
		int n = x.length;
		T[] y = Arrays.copyOf(x, n + 1);
		y[n] = element;
		return y;
	}




	public static <X> X[] prepend(X element, X[] x) {
		int len = x.length;
		X[] y = Arrays.copyOf(x, len + 1);
		y[0] = element;
		System.arraycopy(x, 0, y, 1, len);
		return y;
	}

	public static <X> X[] remove(X[] input, int index) {
		int length = input.length;
		X[] output = Arrays.copyOf(input, length - 1);
		if (index > 0) System.arraycopy(input, 0, output, 0, index);
		if (index < length - 1) System.arraycopy(input, index + 1, output, index, length - index - 1);
		return output;
	}


	public static <X> X[] removeAll(X[] x, @Nullable MetalBitSet indices) {
		return indices!=null ? removeAll(x, x.length, indices) : x;
	}

	public static <X> X[] removeAll(X[] x, int n, MetalBitSet indices) {
		int toRemove = indices.cardinality();
		if (toRemove == 0)
			return x.length == n ? x : Arrays.copyOf(x, n);

		int remain = n - toRemove;
		if (remain == 0)
			return (X[]) EMPTY_OBJECT_ARRAY;

		X[] y = Arrays.copyOf(x, remain);
		int j = 0;
		for (int i = 0; i < n; i++)
			if (toRemove == 0 || !indices.test(i))
				y[j++] = x[i];
			else
				toRemove--;
		return y;
	}

	/**
	 * <p>This method checks whether the provided array is sorted according to the class's
	 * {@code compareTo} method.
	 *
	 * @param array the array to check
	 * @param <T>   the datatype of the array to check, it must implement {@code Comparable}
	 * @return whether the array is sorted
	 * @since 3.4
	 */
	public static <T extends Comparable<? super T>> boolean isSorted(T[] array) {
		return isSorted(array, Comparator.naturalOrder());
	}

	/**
	 * <p>This method checks whether the provided array is sorted according to the provided {@code Comparator}.
	 *
	 * @param array      the array to check
	 * @param c the {@code Comparator} to compare over
	 * @param <T>        the datatype of the array
	 * @return whether the array is sorted
	 * @since 3.4
	 */
	public static <T> boolean isSorted(T[] array, Comparator<T> c) {
		return isSorted(array, array.length, c);
	}
	public static <T> boolean isSorted(T[] array, int n, Comparator<T> c) {
		if (c == null) throw new IllegalArgumentException("Comparator should not be null.");

		if (array == null || array.length < 2) return true;

		T previous = array[0];
		for (int i = 1; i < n; i++) {
			T current = array[i];
			if (c.compare(previous, current) > 0) return false;

			previous = current;
		}
		return true;
	}

	/**
	 * <p>This method checks whether the provided array is sorted according to natural ordering.
	 *
	 * @param array the array to check
	 * @return whether the array is sorted according to natural ordering
	 * @since 3.4
	 */
	public static boolean isSorted(double[] array) {
		if (array == null || array.length < 2) return true;

		double previous = array[0];
		int n = array.length;
		for (int i = 1; i < n; i++) {
			double current = array[i];
			if (previous > current)
				return false;
			previous = current;
		}
		return true;
	}

	/**
	 * <p>This method checks whether the provided array is sorted according to natural ordering.
	 *
	 * @param array the array to check
	 * @return whether the array is sorted according to natural ordering
	 * @since 3.4
	 */
	public static boolean isSorted(float[] array) {
		if (array == null || array.length < 2)
			return true;

		int n = array.length;
		float previous = array[0];
		for (int i = 1; i < n; i++) {
			float current = array[i];
			if (previous > current)
				return false;
			previous = current;
		}
		return true;
	}

	public static boolean isSorted(long[] array) {
		if (array == null || array.length < 2)
			return true;

		int n = array.length;
		long previous = array[0];
		for (int i = 1; i < n; i++) {
			long current = array[i];
			if (previous > current)
				return false;
			previous = current;
		}
		return true;
	}



	public static <T> T[] removeNulls(T[] array) {
		int n = array.length;
		if (n == 0)
			return array;
		MetalBitSet bits = null;
		for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
			T t = array[i];
			if (t == null) {
				if (bits == null) bits = MetalBitSet.bits(n);
				bits.set(i);
			}
		}

		return removeAll(array, bits);
	}

	public static <T> T[] removeNulls(T[] array, int nulls) {
		int s = array.length - nulls;
		T[] a = Arrays.copyOf(array, s);
		if (s > 0) {
			int j = 0;
			for (T x : array)
				if (x != null)
					a[j++] = x;
		}
		return a;
	}

	/**
	 * <p>Returns an array containing the string representation of each element in the argument array.</p>
	 *
	 * <p>This method returns {@code null} for a {@code null} input array.</p>
	 *
	 * @param array the {@code Object[]} to be processed, may be null
	 * @return {@code String[]} of the same size as the source with its element's string representation,
	 * {@code null} if null array input
	 * @throws NullPointerException if array contains {@code null}
	 * @since 3.6
	 */
	public static String[] toStringArray(Object[] array) {
		if (array == null) return null;
		if (array.length == 0) return EMPTY_STRING_ARRAY;

		List<String> list = new Lst<>();
		for (Object o : array) {
			String toString = o.toString();
			list.add(toString);
		}
		return list.toArray(EMPTY_STRING_ARRAY);
	}

	/**
	 * <p>Returns an array containing the string representation of each element in the argument
	 * array handling {@code null} elements.</p>
	 *
	 * <p>This method returns {@code null} for a {@code null} input array.</p>
	 *
	 * @param array                the Object[] to be processed, may be null
	 * @param valueForNullElements the value to insert if {@code null} is found
	 * @return a {@code String} array, {@code null} if null array input
	 * @since 3.6
	 */
	public static String[] toStringArray(Object[] array, String valueForNullElements) {
		if (null == array) return null;
		if (array.length == 0) return EMPTY_STRING_ARRAY;

		String[] result = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			Object object = array[i];
			result[i] = (object == null ? valueForNullElements : object.toString());
		}

		return result;
	}

	/**
	 * <p>Inserts elements into an array at the given index (starting from zero).</p>
	 *
	 * <p>When an array is returned, it is always a new array.</p>
	 *
	 * <pre>
	 * ArrayUtils.insert(index, null, null)      = null
	 * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
	 * ArrayUtils.insert(index, null, values)    = null
	 * </pre>
	 *
	 * @param index  the position within {@code array} to insert the new values
	 * @param array  the array to insert the values into, may be {@code null}
	 * @param values the new values to insert, may be {@code null}
	 * @return The new array.
	 * @throws IndexOutOfBoundsException if {@code array} is provided
	 *                                   and either {@code index < 0} or {@code index > array.length}
	 * @since 3.6
	 */
	public static boolean[] insert(int index, boolean[] array, boolean... values) {
		if (array == null) return null;
		if (values == null || values.length == 0) return clone(array);
		if (index < 0 || index > array.length)
			throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);

		boolean[] result = new boolean[array.length + values.length];

		System.arraycopy(values, 0, result, index, values.length);
		if (index > 0) System.arraycopy(array, 0, result, 0, index);
		if (index < array.length) System.arraycopy(array, index, result, index + values.length, array.length - index);
		return result;
	}

	/**
	 * <p>Inserts elements into an array at the given index (starting from zero).</p>
	 *
	 * <p>When an array is returned, it is always a new array.</p>
	 *
	 * <pre>
	 * ArrayUtils.insert(index, null, null)      = null
	 * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
	 * ArrayUtils.insert(index, null, values)    = null
	 * </pre>
	 *
	 * @param index  the position within {@code array} to insert the new values
	 * @param array  the array to insert the values into, may be {@code null}
	 * @param values the new values to insert, may be {@code null}
	 * @return The new array.
	 * @throws IndexOutOfBoundsException if {@code array} is provided
	 *                                   and either {@code index < 0} or {@code index > array.length}
	 * @since 3.6
	 */
	public static byte[] insert(int index, byte[] array, byte... values) {
		if (array == null) return null;
		if (values == null || values.length == 0) return clone(array);
		if (index < 0 || index > array.length)
			throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);

		byte[] result = new byte[array.length + values.length];

		System.arraycopy(values, 0, result, index, values.length);
		if (index > 0) System.arraycopy(array, 0, result, 0, index);
		if (index < array.length) System.arraycopy(array, index, result, index + values.length, array.length - index);
		return result;
	}

	/**
	 * <p>Inserts elements into an array at the given index (starting from zero).</p>
	 *
	 * <p>When an array is returned, it is always a new array.</p>
	 *
	 * <pre>
	 * ArrayUtils.insert(index, null, null)      = null
	 * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
	 * ArrayUtils.insert(index, null, values)    = null
	 * </pre>
	 *
	 * @param index  the position within {@code array} to insert the new values
	 * @param array  the array to insert the values into, may be {@code null}
	 * @param values the new values to insert, may be {@code null}
	 * @return The new array.
	 * @throws IndexOutOfBoundsException if {@code array} is provided
	 *                                   and either {@code index < 0} or {@code index > array.length}
	 * @since 3.6
	 */
	public static char[] insert(int index, char[] array, char... values) {
		if (array == null) return null;
		if (values == null || values.length == 0) return clone(array);
		if (index < 0 || index > array.length)
			throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);

		char[] result = new char[array.length + values.length];

		System.arraycopy(values, 0, result, index, values.length);
		if (index > 0) System.arraycopy(array, 0, result, 0, index);
		if (index < array.length) System.arraycopy(array, index, result, index + values.length, array.length - index);
		return result;
	}

	/**
	 * <p>Inserts elements into an array at the given index (starting from zero).</p>
	 *
	 * <p>When an array is returned, it is always a new array.</p>
	 *
	 * <pre>
	 * ArrayUtils.insert(index, null, null)      = null
	 * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
	 * ArrayUtils.insert(index, null, values)    = null
	 * </pre>
	 *
	 * @param index  the position within {@code array} to insert the new values
	 * @param array  the array to insert the values into, may be {@code null}
	 * @param values the new values to insert, may be {@code null}
	 * @return The new array.
	 * @throws IndexOutOfBoundsException if {@code array} is provided
	 *                                   and either {@code index < 0} or {@code index > array.length}
	 * @since 3.6
	 */
	public static double[] insert(int index, double[] array, double... values) {
		if (array == null) return null;
		if (values == null || values.length == 0) return clone(array);
		if (index < 0 || index > array.length)
			throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);

		double[] result = new double[array.length + values.length];

		System.arraycopy(values, 0, result, index, values.length);
		if (index > 0) System.arraycopy(array, 0, result, 0, index);
		if (index < array.length) System.arraycopy(array, index, result, index + values.length, array.length - index);
		return result;
	}

	/**
	 * <p>Inserts elements into an array at the given index (starting from zero).</p>
	 *
	 * <p>When an array is returned, it is always a new array.</p>
	 *
	 * <pre>
	 * ArrayUtils.insert(index, null, null)      = null
	 * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
	 * ArrayUtils.insert(index, null, values)    = null
	 * </pre>
	 *
	 * @param index  the position within {@code array} to insert the new values
	 * @param array  the array to insert the values into, may be {@code null}
	 * @param values the new values to insert, may be {@code null}
	 * @return The new array.
	 * @throws IndexOutOfBoundsException if {@code array} is provided
	 *                                   and either {@code index < 0} or {@code index > array.length}
	 * @since 3.6
	 */
	public static float[] insert(int index, float[] array, float... values) {
		if (array == null) return null;
		if (values == null || values.length == 0) return clone(array);
		if (index < 0 || index > array.length)
			throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);

		float[] result = new float[array.length + values.length];

		System.arraycopy(values, 0, result, index, values.length);
		if (index > 0) System.arraycopy(array, 0, result, 0, index);
		if (index < array.length) System.arraycopy(array, index, result, index + values.length, array.length - index);
		return result;
	}

	/**
	 * <p>Inserts elements into an array at the given index (starting from zero).</p>
	 *
	 * <p>When an array is returned, it is always a new array.</p>
	 *
	 * <pre>
	 * ArrayUtils.insert(index, null, null)      = null
	 * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
	 * ArrayUtils.insert(index, null, values)    = null
	 * </pre>
	 *
	 * @param index  the position within {@code array} to insert the new values
	 * @param array  the array to insert the values into, may be {@code null}
	 * @param values the new values to insert, may be {@code null}
	 * @return The new array.
	 * @throws IndexOutOfBoundsException if {@code array} is provided
	 *                                   and either {@code index < 0} or {@code index > array.length}
	 * @since 3.6
	 */
	public static int[] insert(int index, int[] array, int... values) {
		if (array == null) return null;
		if (values == null || values.length == 0) return clone(array);
		if (index < 0 || index > array.length)
			throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);

		int[] result = new int[array.length + values.length];

		System.arraycopy(values, 0, result, index, values.length);
		if (index > 0) System.arraycopy(array, 0, result, 0, index);
		if (index < array.length) System.arraycopy(array, index, result, index + values.length, array.length - index);
		return result;
	}

	/**
	 * <p>Inserts elements into an array at the given index (starting from zero).</p>
	 *
	 * <p>When an array is returned, it is always a new array.</p>
	 *
	 * <pre>
	 * ArrayUtils.insert(index, null, null)      = null
	 * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
	 * ArrayUtils.insert(index, null, values)    = null
	 * </pre>
	 *
	 * @param index  the position within {@code array} to insert the new values
	 * @param array  the array to insert the values into, may be {@code null}
	 * @param values the new values to insert, may be {@code null}
	 * @return The new array.
	 * @throws IndexOutOfBoundsException if {@code array} is provided
	 *                                   and either {@code index < 0} or {@code index > array.length}
	 * @since 3.6
	 */
	public static long[] insert(int index, long[] array, long... values) {
		if (array == null) return null;
		if (values == null || values.length == 0) return clone(array);
		if (index < 0 || index > array.length)
			throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);

		long[] result = new long[array.length + values.length];

		System.arraycopy(values, 0, result, index, values.length);
		if (index > 0) System.arraycopy(array, 0, result, 0, index);
		if (index < array.length) System.arraycopy(array, index, result, index + values.length, array.length - index);
		return result;
	}

	/**
	 * <p>Inserts elements into an array at the given index (starting from zero).</p>
	 *
	 * <p>When an array is returned, it is always a new array.</p>
	 *
	 * <pre>
	 * ArrayUtils.insert(index, null, null)      = null
	 * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
	 * ArrayUtils.insert(index, null, values)    = null
	 * </pre>
	 *
	 * @param index  the position within {@code array} to insert the new values
	 * @param array  the array to insert the values into, may be {@code null}
	 * @param values the new values to insert, may be {@code null}
	 * @return The new array.
	 * @throws IndexOutOfBoundsException if {@code array} is provided
	 *                                   and either {@code index < 0} or {@code index > array.length}
	 * @since 3.6
	 */
	public static short[] insert(int index, short[] array, short... values) {
		if (array == null) return null;
		if (values == null || values.length == 0) return clone(array);
		if (index < 0 || index > array.length)
			throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);

		short[] result = new short[array.length + values.length];

		System.arraycopy(values, 0, result, index, values.length);
		if (index > 0) System.arraycopy(array, 0, result, 0, index);
		if (index < array.length) System.arraycopy(array, index, result, index + values.length, array.length - index);
		return result;
	}

	/**
	 * <p>Inserts elements into an array at the given index (starting from zero).</p>
	 *
	 * <p>When an array is returned, it is always a new array.</p>
	 *
	 * <pre>
	 * ArrayUtils.insert(index, null, null)      = null
	 * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
	 * ArrayUtils.insert(index, null, values)    = null
	 * </pre>
	 *
	 * @param <T>    The type of elements in {@code array} and {@code values}
	 * @param index  the position within {@code array} to insert the new values
	 * @param array  the array to insert the values into, may be {@code null}
	 * @param values the new values to insert, may be {@code null}
	 * @return The new array.
	 * @throws IndexOutOfBoundsException if {@code array} is provided
	 *                                   and either {@code index < 0} or {@code index > array.length}
	 * @since 3.6
	 */
	@SafeVarargs
	public static <T> T[] insert(int index, T[] array, T... values) {
		/*
		 * Note on use of @SafeVarargs:
		 *
		 * By returning null when 'array' is null, we avoid returning the vararg
		 * array to the caller. We also avoid relying on the type of the vararg
		 * array, by inspecting the component type of 'array'.
		 */

		if (array == null)
			return null;

		if (values == null || values.length == 0)
			return clone(array);

		if (index < 0 || index > array.length)
			throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);


		//final Class<?> type = array.getClass().getComponentType();
		@SuppressWarnings("unchecked")
		//T[]) Array.newInstance(type,
			T[] result = Arrays.copyOf(array, array.length + values.length);

		System.arraycopy(values, 0, result, index, values.length);

		if (index > 0)
			System.arraycopy(array, 0, result, 0, index);

		if (index < array.length)
			System.arraycopy(array, index, result, index + values.length, array.length - index);

		return result;
	}

	/**
	 * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
	 *
	 * @param array  the array to shuffle
	 * @param rng the source of randomness used to permute the elements
	 * @since 3.6
	 */
	public static void shuffle(Object[] array, RandomGenerator rng) {
		int n = array.length;
		switch (n) {
			case 0, 1 -> { }
			case 2 -> {
				if (rng.nextBoolean())
					swap(array, 0, 1);
			}
			default -> {
				for (int i = n; i > 1; i--)
					swap(array, i - 1, rng.nextInt(i));
			}
		}
	}

	/**
	 * from,to is an inclusive range which is non-standard wrt the other shuffle methods
	 */
	public static void shuffle(int from, int to, IntIntProcedure swapper, RandomGenerator rng) {
		int range = 1 + (to - from);
		for (int i = to; i > from; i--) {
			int a = i - 1;
			int b = rng.nextInt(range) + from;
			if (a != b)
				swapper.value(a, b);
		}
	}


	/**
	 * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
	 *
	 * @param array  the array to shuffle
	 * @param random the source of randomness used to permute the elements
	 * @see <a href="https:
	 * @since 3.6
	 */
	public static void shuffle(boolean[] array, RandomGenerator random) {
		for (int i = array.length; i > 1; i--)
			swapBool(array, i - 1, random.nextInt(i));
	}

	/**
	 * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
	 *
	 * @param array  the array to shuffle
	 * @param random the source of randomness used to permute the elements
	 * @see <a href="https:
	 * @since 3.6
	 */
	public static void shuffle(byte[] array, RandomGenerator random) {
		for (int i = array.length; i > 1; i--)
			swapByte(array, i - 1, random.nextInt(i));
	}

	/**
	 * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
	 *
	 * @param array  the array to shuffle
	 * @param random the source of randomness used to permute the elements
	 * @see <a href="https:
	 * @since 3.6
	 */
	public static void shuffle(char[] array, RandomGenerator random) {
		for (int i = array.length; i > 1; i--)
			swap(array, i - 1, random.nextInt(i), 1);
	}

	/**
	 * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
	 *
	 * @param array  the array to shuffle
	 * @param random the source of randomness used to permute the elements
	 * @see <a href="https:
	 * @since 3.6
	 */
	public static void shuffle(short[] array, RandomGenerator random) {
		shuffle(array, array.length, random);
	}

	public static void shuffle(short[] array, int to, RandomGenerator random) {
		for (int i = to; i > 1; i--)
			swapShort(array, i - 1, random.nextInt(i));
	}

	/**
	 * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
	 *
	 * @param array  the array to shuffle
	 * @param random the source of randomness used to permute the elements
	 * @see <a href="https:
	 * @since 3.6
	 */
	public static void shuffle(long[] array, RandomGenerator random) {
		for (int i = array.length; i > 1; i--)
			swapLong(array, i - 1, random.nextInt(i));
	}

	/**
	 * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
	 *
	 * @param array  the array to shuffle
	 * @param random the source of randomness used to permute the elements
	 * @see <a href="https:
	 * @since 3.6
	 */
	public static void shuffle(float[] array, RandomGenerator random) {
		for (int i = array.length; i > 1; i--)
			swapFloat(array, i - 1, random.nextInt(i));
	}

	/**
	 * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
	 *
	 * @param array  the array to shuffle
	 * @param random the source of randomness used to permute the elements
	 * @see <a href="https:
	 * @since 3.6
	 */
	public static void shuffle(double[] array, RandomGenerator random) {
		for (int i = array.length; i > 1; i--)
			swapDouble(array, i - 1, random.nextInt(i));
	}

//	/**
//	 * Ensures that a range given by its first (inclusive) and last (exclusive) elements fits an array of given length.
//	 * <p>
//	 * <P>This method may be used whenever an array range check is needed.
//	 *
//	 * @param arrayLength an array length.
//	 * @param from        a start index (inclusive).
//	 * @param to          an end index (inclusive).
//	 * @throws IllegalArgumentException       if <code>from</code> is greater than <code>to</code>.
//	 * @throws ArrayIndexOutOfBoundsException if <code>from</code> or <code>to</code> are greater than <code>arrayLength</code> or negative.
//	 */
//	public static void ensureFromTo(int arrayLength, int from, int to) {
//		if (from < 0) throw new ArrayIndexOutOfBoundsException("Start index (" + from + ") is negative");
//		if (from > to)
//			throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ')');
//		if (to > arrayLength)
//			throw new ArrayIndexOutOfBoundsException("End index (" + to + ") is greater than array length (" + arrayLength + ')');
//	}

//	/**
//	 * Ensures that a range given by an offset and a length fits an array of given length.
//	 * <p>
//	 * <P>This method may be used whenever an array range check is needed.
//	 *
//	 * @param arrayLength an array length.
//	 * @param offset      a start index for the fragment
//	 * @param length      a length (the number of elements in the fragment).
//	 * @throws IllegalArgumentException       if <code>length</code> is negative.
//	 * @throws ArrayIndexOutOfBoundsException if <code>offset</code> is negative or <code>offset</code>+<code>length</code> is greater than <code>arrayLength</code>.
//	 */
//	public static void ensureOffsetLength(int arrayLength, int offset, int length) {
//		if (offset < 0) throw new ArrayIndexOutOfBoundsException("Offset (" + offset + ") is negative");
//		if (length < 0) throw new IllegalArgumentException("Length (" + length + ") is negative");
//		if (offset + length > arrayLength)
//			throw new ArrayIndexOutOfBoundsException("Last index (" + (offset + length) + ") is greater than array length (" + arrayLength + ')');
//	}

	/**
	 * Performs a binary search on an already-sorted range: finds the first position where an
	 * element can be inserted without violating the ordering. Sorting is by a user-supplied
	 * comparison function.
	 *
	 * @param mid      Beginning of the range.
	 * @param to       One past the end of the range.
	 * @param firstCut Element to be searched for.
	 * @param comp     Comparison function.
	 * @return The largest index i such that, for every j in the range <code>[first, i)</code>,
	 * <code>comp.apply(array[j], x)</code> is <code>true</code>.
	 */
	private static int lowerBound(int mid, int to, int firstCut, IntComparator comp) {

		int len = to - mid;
		while (len > 0) {
			int half = len / 2;
			int middle = mid + half;
			if (comp.compare(middle, firstCut) < 0) {
				mid = middle + 1;
				len -= half + 1;
			} else len = half;
		}
		return mid;
	}


	/**
	 * Performs a binary search on an already sorted range: finds the last position where an element
	 * can be inserted without violating the ordering. Sorting is by a user-supplied comparison
	 * function.
	 *
	 * @param from      Beginning of the range.
	 * @param mid       One past the end of the range.
	 * @param secondCut Element to be searched for.
	 * @param comp      Comparison function.
	 * @return The largest index i such that, for every j in the range <code>[first, i)</code>,
	 * <code>comp.apply(x, array[j])</code> is <code>false</code>.
	 */
	private static int upperBound(int from, int mid, int secondCut, IntComparator comp) {

		int len = mid - from;
		while (len > 0) {
			int half = len / 2;
			int middle = from + half;
			if (comp.compare(secondCut, middle) < 0) len = half;
			else {
				from = middle + 1;
				len -= half + 1;
			}
		}
		return from;
	}


	public static void shuffle(int[] array, RandomGenerator random) {
		shuffle(array, array.length, random);
	}

	public static void shuffle(int[] array, int len, RandomGenerator random) {
		for (int i = len; i > 1; i--)
			swapInt(array, i - 1, random.nextInt(i));
	}

	public static void shuffle(byte[] array, int len, RandomGenerator random) {
		for (int i = len; i > 1; i--)
			swapByte(array, i - 1, random.nextInt(i));
	}


	public static int nextIndexOf(byte[] array, int startingAt, int endingAt, byte[] target, int targetFrom, int targetTo) {
		int targetLen = targetTo - targetFrom;
		assert (targetLen > 0);

		if (endingAt - startingAt < targetLen)
			return -1;

		outer:
		for (int i = startingAt; i < endingAt - targetLen + 1; i++) {
			for (int j = 0; j < targetLen; j++) if (array[i + j] != target[targetFrom + j]) continue outer;
			return i;
		}
		return -1;


	}

	/**
	 * creates a never-ending (until empty) cyclic ListIterator for a given List
	 * adapted from Guava's Iterators.cycle(...)
	 */
	public static <T> ListIterator<T> cycle(List<T> l) {

		return new ForwardingListIterator<>() {

			ListIterator<T> i = l.listIterator();

			@Override
			protected final ListIterator<T> delegate() {
				return i;
			}

			@Override
			public boolean hasNext() {
				if (!i.hasNext()) {
					if (l.isEmpty())
						return false;
					i = l.listIterator();
				}

				return true;
			}
		};
	}

	/**
	 * simple byte[] interner for low-count elements
	 */
	public static byte[] intern(byte[] map) {
		switch (map.length) {
			case 0:
				return EMPTY_BYTE_ARRAY;
			case 1: {
				return switch (map[0]) {
					case 0 -> BYTE_ZERO;
					case 1 -> BYTE_ONE;
					case 2 -> BYTE_TWO;
					case 3 -> BYTE_THREE;
					default -> throw new UnsupportedOperationException();
				};
			}
			case 2: {
				switch (map[0]) {
					case 0:
						return switch (map[1]) {
							case 0 -> BYTE_ZERO_ZERO;
							case 1 -> BYTE_ZERO_ONE;
							default -> throw new UnsupportedOperationException();
						};
					case 1:
						return switch (map[1]) {
							case 0 -> BYTE_ONE_ZERO;
							case 1 -> BYTE_ONE_ONE;
							case 2 -> BYTE_ONE_TWO;
							case -1 -> BYTE_ONE_NEGONE;
							case -2 -> BYTE_ONE_NEGTWO;
							default -> throw new UnsupportedOperationException();
						};

					case -1:
						return switch (map[1]) {
							case 1 -> BYTE_NEGONE_ONE;
							case 2 -> BYTE_NEGONE_TWO;
							case -1 -> BYTE_NEGONE_NEGONE;
							case -2 -> BYTE_NEGONE_NEGTWO;
							default -> throw new UnsupportedOperationException();
						};

					case 2:
						switch (map[1]) {
							case 1:
								return BYTE_TWO_ONE;
							case 2:
								return BYTE_TWO_TWO;
							case -1:
								return BYTE_TWO_NEGONE;
							case -2:
								return BYTE_TWO_NEGTWO;
						}
				}
			}
		}

		return map;
	}

	public static boolean equalsIdentity(Object[] x, Object[] y) {
		if (x == y) return true;
		if (x.length != y.length) return false;
		for (int i = 0; i < x.length; i++) {
			if (x[i] != y[i])
				return false;
		}
		return true;
	}
	public static boolean equalsIdentity(Object[] x, Object[] y, int n) {
		if (x == y) return true;
		if (x.length < n || y.length < n) return false;
		for (int i = 0; i < n; i++) {
			if (x[i] != y[i])
				return false;
		}
		return true;
	}

//	public static short[] toShort(int[] x) {
//		if (x.length == 0)
//			return EMPTY_SHORT_ARRAY;
//
//		short[] s = new short[x.length];
//		int i = 0;
//		for (int xx : x) {
//			assert (xx <= Short.MAX_VALUE && xx >= Short.MIN_VALUE);
//			s[i++] = (short) xx;
//		}
//		return s;
//	}
//
//	public static boolean containsIdentity(Object[] xx, Object x) {
//		return indexOfInstance(xx, x) != -1;
//	}
//
//	/**
//	 * doesnt do null tests
//	 */
//	public static boolean equalArraysDirect(Object[] a, Object[] b) {
//		if (a == b)
//			return true;
//
//		int len = a.length;
//		if (b.length != len)
//			return false;
//
//		for (int i = 0; i < len; i++) {
//			if (!a[i].equals(b[i]))
//				return false;
//		}
//
//		return true;
//	}

	public static boolean equals(byte[] a, byte[] b, int n) {
		if (a == b) return true;
		for (int i = 0; i < n; i++) {
			if (a[i] != b[i])
				return false;
		}
		return true;
	}
	public static boolean equals(byte[] ab, int a, int b, byte[] xy, int x, int y) {
		if (a == b || ab == xy) return true;
		int n = b - a;
		if (y - x != n) return false;

		while (a < b) {
			if (ab[a++] != xy[x++])
				return false;
		}
		return true;
	}

	/** if filter is null, tests for null */
	public static <X> int removeIf(X[] xx, int a, int b, @Nullable Predicate<? super X> filter) {
		int target;
		boolean f = filter != null;
		for(int i = target = a; i < b; ++i) {
			X x = xx[i];
			if (f ? !filter.test(x) : (x != null)) {
				if (target != i)
					xx[target] = x;
				target++;
			}
		}

		int removed = b - target;
		if (removed > 0)
			Arrays.fill(xx, b-removed, b, null);
		return removed;
	}

	public static byte[] byteOrdinals(int to) {
		assert(to <= Byte.MAX_VALUE);
		byte[] order = new byte[to];
		for (int j = 0; j < to; j++)
			order[j] = (byte) j;
		return order;
	}

//    static public final Pair[] EmptyPairArray = new Pair[0];

	public static <X> X[] maybeEqualIdentity(X[] next, X[] prev) {
		return equalsIdentity(prev, next) ? prev : next;
	}


	/** shuffle spans of equivalent items. use after sorting */
	 public static void shuffleTiered(IntToDoubleFunction rank, IntIntProcedure swapper, int s, Supplier<RandomGenerator> _rng) {
	 	if (s < 2) return;

		int contig = 0;
		double last = rank.applyAsDouble(0);
		RandomGenerator rng = null;
		for (int i = 1; i <= s; i++) {
            double ei = i < s ? rank.applyAsDouble(i) : Double.NaN;
            if (ei == last) {
                contig++;
            } else {
                if (contig > 0) {
                    if (i == s) i--;
                    if (contig > 1)
                        shuffle(i - contig, i, swapper, (rng == null) ? rng = _rng.get() : rng);
                    contig = 0;
                }
                last = ei;
            }
        }
    }

	public static void assertNoNulls(Object[] x, int start, int end) {
		for (int i = start; i < end; i++) {
			if (x[i] == null)
				throw new NullPointerException();
			//assert (x[i] != null);
		}
	}
	public static void assertNoNonNulls(Object[] x, int start, int end) {
		for (int i = start; i < end; i++) {
			if (x[i] != null)
				throw new NullPointerException();
			//assert (x[i] != null);
		}
	}

    public static boolean equals(long[] a, long[] b) {
        if (a == b) return true;
        int l = a.length;
        if (b.length != l)
            return false;
        for (int i = 0; i < l; i++) {
            if (a[i] != b[i])
                return false;
        }
        return true;
    }

	public static boolean equals(double[] a, double[] b, double epsilon) {
		if (Arrays.equals(a, b)) return true;
		int l = a.length;
		for (int i = 0; i < l; i++) {
			if (!Util.equals(a[i], b[i], epsilon))
				return false;
		}
		return true;
	}

	public static boolean equals(long[] a, long[] b, int firstN) {
		if (a == b) return true;
		for (int i = 0; i < firstN; i++) {
			if (a[i] != b[i])
				return false;
		}
		return true;
	}

	public static boolean equals(short[] a, short[] b) {
		if (a == b) return true;
		int l = a.length;
		if (b.length != l)
			return false;
		for (int i = 0; i < l; i++) {
			if (a[i] != b[i])
				return false;
		}
		return true;
	}

	/** descending */
	public static boolean isSorted(int[] l) {
	 	int size = l.length;
		for (int i = 1; i < size; i++) {
			if (l[i] > l[i-1]) return false;
		}
		return true;
	}
	/** descending */
	public static boolean isSorted(short[] l) {
		int size = l.length;
		for (int i = 1; i < size; i++) {
			if (l[i] > l[i-1])
				return false;
		}
		return true;
	}

    public static IntIntProcedure objSwapper(Object[] x) {
        return (a, b) -> swap(x, a, b);
    }
}