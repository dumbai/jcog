package jcog.data.array;

/*		 
 * Copyright (C) 2010 Sebastiano Vigna 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *
 * For the sorting code:
 *
 * Copyright (C) 1999 CERN - European Organization for Nuclear Research.
 *
 *   Permission to use, copy, modify, distribute and sell this software and
 *   its documentation for any purpose is hereby granted without fee,
 *   provided that the above copyright notice appear in all copies and that
 *   both that copyright notice and this permission notice appear in
 *   supporting documentation. CERN makes no representations about the
 *   suitability of this software for any purpose. It is provided "as is"
 *   without expressed or implied warranty. 
 */


/** A class providing static methods and objects that do useful things with big arrays.
 * 
 * <h2>Introducing big arrays</h2>
 * 
 * <p>A <em>big array</em> is an array-of-arrays representation of an array. The length of a big array
 * is bounded by {@link Long#MAX_VALUE} rather than {@link Integer#MAX_VALUE}. The type of a big array
 * is that of an array-of-arrays, so a big array of integers is of type <code>int[][]</code>.
 * 
 * <p>If <code>a</code> is a big array, <code>a[0]</code>, <code>a[1]</code>, &hellip; are called
 * the <em>segments</em> of the big array. All segments, except possibly for the last one, are of length
 * {@link #SEGMENT_SIZE}. Given an index <code>i</code> into a big array, there is an associated
 * <em>{@linkplain #segment(long) segment}</em> and an associated <em>{@linkplain #displacement(long) displacement}</em>
 * into that segment. Access to single members happens by means of accessors defined in the type-specific
 * versions (see, e.g., {@link IntBigArrays#get(int[][], long)} and {@link IntBigArrays#set(int[][], long, int)}), 
 * but you can also use the methods {@link #segment(long)}/{@link #displacement(long)} to access entries manually.
 * 
 * <h2>Scanning big arrays</h2>
 * 
 * <p>You can scan a big array using the following idiomatic form:
 * <pre>
 *   for( int i = 0; i &lt; a.length; i++ ) {
 *      final int[] t = a[ i ];
 *      final int l = t.length;
 *      for( int d = 0; d &lt; l; d++ ) { do something with t[ d ] }
 *   }
 * </pre>
 * or using the (simpler and usually faster) reversed version:
 * <pre>
 *   for( int i = a.length; i-- != 0; ) {
 *      final int[] t = a[ i ];  
 *      for( int d = s.length; d-- != 0; ) { do something with t[ d ] }
 *   }
 * </pre>
 * <p>Inside the inner loop, the original index in <code>a</code> can be retrieved using {@link #index(int, int) index(segment, displacement)}.
 * Note that caching is essential in making these loops essentially as fast as those scanning standard arrays (as iterations
 * of the outer loop happen very rarely). Using loops of this kind is extremely faster than using a standard
 * loop and accessors.
 * 
 * <p>In some situations, you might want to iterate over a part of a big array having an offset and a length. In this case, the
 * idiomatic loops are as follows:
 * <pre>
 *   for( int i = segment( offset ); i &lt; segment( offset + length + SEGMENT_MASK ); i++ ) {
 *      final int[] t = a[ i ];
 *      final int l = Math.min( t.length, offset + length - start( i ) );
 *      for( int d = Math.max( 0, offset - start( i ) ); d &lt; l; d++ ) { do something with t[ d ] }
 *   }
 * </pre>
 * or, in a reversed form,
 * <pre>
 *   for( int i = segment( offset + length + SEGMENT_MASK ); i-- != segment( offset ); ) {
 *      final int[] t = a[ i ];
 *      final int s = Math.max( 0, offset - start( i ) );
 *      for( int d = Math.min( t.length, offset + length - start( i ) ); d-- != s ; ) { do something with t[ d ] }
 *   }
 * </pre>
 * 
 * <h2>Literal big arrays</h2>
 * 
 * <p>A literal big array can be easily created by using the suitable type-specific <code>wrap()</code> method
 * (e.g., {@link IntBigArrays#wrap(int[])}) around a literal standard array. Alternatively, for very small
 * arrays you can just declare a literal array-of-array (e.g., <code>new int[][] { { 1, 2 } }</code>). Be warned,
 * however, that this can lead to creating illegal big arrays if for some reason (e.g., stress testing) {@link #SEGMENT_SIZE}
 * is set to a value smaller than the inner array length.
 * 
 * <h2>Big alternatives</h2>
 * 
 * <p>If you find the kind of &ldquo;bare hands&rdquo; approach to big arrays not enough object-oriented, please use
 * big lists based on big arrays (.e.g, {@link IntBigArrayBigList}). Big arrays follow the Java tradition of 
 * considering arrays as a &ldquo;legal alien&rdquo;&mdash;something in-between an object and a primitive type. This
 * approach lacks the consistency of a full object-oriented approach, but provides some significant performance gains.
 *
 * <h2>Additional methods</h2>
 * 
 * <p>In addition to commodity methods, this class contains {@link BigSwapper}-based implementations
 * of {@linkplain #quickSort(long, long, LongComparator, BigSwapper) quicksort} and of
 * a stable, in-place {@linkplain #mergeSort(long, long, LongComparator, BigSwapper) mergesort}. These
 * generic sorting methods can be used to sort any kind of list, but they find their natural
 * usage, for instance, in sorting big arrays in parallel.
 *
 * @see it.unimi.dsi.fastutil.Arrays
 */

public enum BigArrays {
	;
	/** The shift used to compute the segment associated with an index (equivalently, the logarithm of the segment size). */
	public static final int SEGMENT_SHIFT = 27;
	/** The current size of a segment (2<sup>27</sup>) is the largest size that makes
	 * the physical memory allocation for a single segment strictly smaller
	 * than 2<sup>31</sup> bytes. */
	public static final int SEGMENT_SIZE = 1 << SEGMENT_SHIFT;
	/** The mask used to compute the displacement associated to an index. */
	public static final int SEGMENT_MASK = SEGMENT_SIZE - 1;

	/** Computes the segment associated with a given index.
	 * 
	 * @param index an index into a big array.
	 * @return the associated segment.
	 */
	public static int segment( long index ) {
		return (int)( index >>> SEGMENT_SHIFT );
	}
	
	/** Computes the displacement associated with a given index.
	 * 
	 * @param index an index into a big array.
	 * @return the associated displacement (in the associated {@linkplain #segment(long) segment}).
	 */
	public static int displacement( long index ) {
		return (int)( index & SEGMENT_MASK );
	}
	
	/** Computes the starting index of a given segment.
	 * 
	 * @param segment the segment of a big array.
	 * @return the starting index of the segment.
	 */
	public static long start( int segment ) {
		return (long)segment << SEGMENT_SHIFT;
	}
	
	/** Computes the index associated with given segment and displacement.
	 * 
	 * @param segment the segment of a big array.
	 * @param displacement the displacement into the segment.
	 * @return the associated index: that is, {@link #segment(long) segment(index(segment, displacement)) == segment} and
	 * {@link #displacement(long) displacement(index(segment, displacement)) == displacement}.
	 */
	public static long index( int segment, int displacement ) {
		return start( segment ) + displacement;
	}
	
	/** Ensures that a range given by its first (inclusive) and last (exclusive) elements fits a big array of given length.
	 *
	 * <P>This method may be used whenever a big array range check is needed.
	 *
	 * @param bigArrayLength a big-array length.
	 * @param from a start index (inclusive).
	 * @param to an end index (inclusive).
	 * @throws IllegalArgumentException if <code>from</code> is greater than <code>to</code>.
	 * @throws ArrayIndexOutOfBoundsException if <code>from</code> or <code>to</code> are greater than <code>bigArrayLength</code> or negative.
	 */
	public static void ensureFromTo( long bigArrayLength, long from, long to ) {
		if ( from < 0 ) throw new ArrayIndexOutOfBoundsException( "Start index (" + from + ") is negative" );
		if ( from > to ) throw new IllegalArgumentException( "Start index (" + from + ") is greater than end index (" + to + ')');
		if ( to > bigArrayLength ) throw new ArrayIndexOutOfBoundsException( "End index (" + to + ") is greater than big-array length (" + bigArrayLength + ')');
	}

	/** Ensures that a range given by an offset and a length fits a big array of given length.
	 *
	 * <P>This method may be used whenever a big array range check is needed.
	 *
	 * @param bigArrayLength a big-array length.
	 * @param offset a start index for the fragment
	 * @param length a length (the number of elements in the fragment).
	 * @throws IllegalArgumentException if <code>length</code> is negative.
	 * @throws ArrayIndexOutOfBoundsException if <code>offset</code> is negative or <code>offset</code>+<code>length</code> is greater than <code>bigArrayLength</code>.
	 */
	public static void ensureOffsetLength( long bigArrayLength, long offset, long length ) {
		if ( offset < 0 ) throw new ArrayIndexOutOfBoundsException( "Offset (" + offset + ") is negative" );
		if ( length < 0 ) throw new IllegalArgumentException( "Length (" + length + ") is negative" );
		if ( offset + length > bigArrayLength ) throw new ArrayIndexOutOfBoundsException( "Last index (" + ( offset + length ) + ") is greater than big-array length (" + bigArrayLength + ')');
	}

	
	private static final int SMALL = 7;
	private static final int MEDIUM = 40;

	/**
	 * Transforms two consecutive sorted ranges into a single sorted range. The initial ranges are
	 * <code>[first, middle)</code> and <code>[middle, last)</code>, and the resulting range is
	 * <code>[first, last)</code>. Elements in the first input range will precede equal elements in
	 * the second.
	 */
	private static void inPlaceMerge( long from, long mid, long to, LongComparator comp, BigSwapper swapper ) {
		if ( from >= mid || mid >= to ) return;
		if ( to - from == 2 ) {
			if ( comp.compare( mid, from ) < 0 ) {
				swapper.swap( from, mid );
			}
			return;
		}
		long firstCut;
		long secondCut;
		if ( mid - from > to - mid ) {
			firstCut = from + ( mid - from ) / 2;
			secondCut = lowerBound( mid, to, firstCut, comp );
		}
		else {
			secondCut = mid + ( to - mid ) / 2;
			firstCut = upperBound( from, mid, secondCut, comp );
		}

		long first2 = firstCut;
		long middle2 = mid;
		long last2 = secondCut;
		if ( middle2 != first2 && middle2 != last2 ) {
			long first1 = first2;
			long last1 = middle2;
			while ( first1 < --last1 )
				swapper.swap( first1++, last1 );
			first1 = middle2;
			last1 = last2;
			while ( first1 < --last1 )
				swapper.swap( first1++, last1 );
			first1 = first2;
			last1 = last2;
			while ( first1 < --last1 )
				swapper.swap( first1++, last1 );
		}

		mid = firstCut + ( secondCut - mid );
		inPlaceMerge( from, firstCut, mid, comp, swapper );
		inPlaceMerge( mid, secondCut, to, comp, swapper );
	}

	/**
	 * Performs a binary search on an already sorted range: finds the first position where an
	 * element can be inserted without violating the ordering. Sorting is by a user-supplied
	 * comparison function.
	 * 
	 * @param mid Beginning of the range.
	 * @param to One past the end of the range.
	 * @param firstCut Element to be searched for.
	 * @param comp Comparison function.
	 * @return The largest index i such that, for every j in the range <code>[first, i)</code>,
	 * <code>comp.apply(array[j], x)</code> is <code>true</code>.
	 */
	private static long lowerBound( long mid, long to, long firstCut, LongComparator comp ) {
		long len = to - mid;
		while ( len > 0 ) {
			long half = len / 2;
			long middle = mid + half;
			if ( comp.compare( middle, firstCut ) < 0 ) {
				mid = middle + 1;
				len -= half + 1;
			}
			else {
				len = half;
			}
		}
		return mid;
	}

	/** Returns the index of the median of three elements. */
	private static long med3( long a, long b, long c, LongComparator comp ) {
		int ab = comp.compare( a, b );
		int ac = comp.compare( a, c );
		int bc = comp.compare( b, c );
		return ( ab < 0 ?
				( bc < 0 ? b : ac < 0 ? c : a ) :
				( bc > 0 ? b : ac > 0 ? c : a ) );
	}

	/** Sorts the specified range of elements using the specified big swapper and according to the order induced by the specified
	 * comparator using mergesort.
	 * 
	 * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result
	 * of the sort. The sorting algorithm is an in-place mergesort that is significantly slower than a 
	 * standard mergesort, but does not allocate additional memory.
	 * 
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 * @param comp the comparator to determine the order of the generic data (arguments are positions).
	 * @param swapper an object that knows how to swap the elements at any two positions.
	 */
	public static void mergeSort( long from, long to, LongComparator comp, BigSwapper swapper ) {
		long length = to - from;

		
		if ( length < SMALL ) {
			for ( long i = from; i < to; i++ ) {
				for ( long j = i; j > from && ( comp.compare( j - 1, j ) > 0 ); j-- ) {
					swapper.swap( j, j - 1 );
				}
			}
			return;
		}

		
		long mid = ( from + to ) >>> 1;
		mergeSort( from, mid, comp, swapper );
		mergeSort( mid, to, comp, swapper );

		
		
		if ( comp.compare( mid - 1, mid ) <= 0 ) return;

		
		inPlaceMerge( from, mid, to, comp, swapper );
	}

	/** Sorts the specified range of elements using the specified big swapper and according to the order induced by the specified
	 * comparator using quicksort. 
	 * 
	 * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
	 * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
	 * 1249&minus;1265, 1993.
	 * 
	 * @param from the index of the first element (inclusive) to be sorted.
	 * @param to the index of the last element (exclusive) to be sorted.
	 * @param comp the comparator to determine the order of the generic data.
	 * @param swapper an object that knows how to swap the elements at any two positions.
	 * 
	 */
	public static void quickSort( long from, long to, LongComparator comp, BigSwapper swapper ) {
		long len = to - from;
		
		if ( len < SMALL ) {
			for ( long i = from; i < to; i++ )
				for ( long j = i; j > from && ( comp.compare( j - 1, j ) > 0 ); j-- ) {
					swapper.swap( j, j - 1 );
				}
			return;
		}

		
		long m = from + len / 2; 
		if ( len > SMALL ) {
			long l = from, n = to - 1;
			if ( len > MEDIUM ) { 
				long s = len / 8;
				l = med3( l, l + s, l + 2 * s, comp );
				m = med3( m - s, m, m + s, comp );
				n = med3( n - 2 * s, n - s, n, comp );
			}
			m = med3( l, m, n, comp ); 
		}


        long c = to - 1;
        long a = from;
        long b = a, d = c;
		
		while ( true ) {
			int comparison;
			while ( b <= c && ( ( comparison = comp.compare( b, m ) ) <= 0 ) ) {
				if ( comparison == 0 ) {
					if ( a == m ) m = b; 
					else if ( b == m ) m = a; 
					swapper.swap( a++, b );
				}
				b++;
			}
			while ( c >= b && ( ( comparison = comp.compare( c, m ) ) >= 0 ) ) {
				if ( comparison == 0 ) {
					if ( c == m ) m = d; 
					else if ( d == m ) m = c; 
					swapper.swap( c, d-- );
				}
				c--;
			}
			if ( b > c ) break;
			if ( b == m ) m = d; 
			else if ( c == m ) m = c; 
			swapper.swap( b++, c-- );
		}


        long s = Math.min(a - from, b - a);
		vecSwap( swapper, from, b - s, s );
        long n = from + len;
        s = Math.min( d - c, n - d - 1 );
		vecSwap( swapper, b, n - s, s );

		
		if ( ( s = b - a ) > 1 ) quickSort( from, from + s, comp, swapper );
		if ( ( s = d - c ) > 1 ) quickSort( n - s, n, comp, swapper );
	}

	/**
	 * Performs a binary search on an already-sorted range: finds the last position where an element
	 * can be inserted without violating the ordering. Sorting is by a user-supplied comparison
	 * function.
	 * 
	 * @param from Beginning of the range.
	 * @param mid One past the end of the range.
	 * @param secondCut Element to be searched for.
	 * @param comp Comparison function.
	 * @return The largest index i such that, for every j in the range <code>[first, i)</code>,
	 * <code>comp.apply(x, array[j])</code> is <code>false</code>.
	 */
	private static long upperBound( long from, long mid, long secondCut, LongComparator comp ) {
		long len = mid - from;
		while ( len > 0 ) {
			long half = len / 2;
			long middle = from + half;
			if ( comp.compare( secondCut, middle ) < 0 ) {
				len = half;
			}
			else {
				from = middle + 1;
				len -= half + 1;
			}
		}
		return from;
	}

	/**
	 * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
	 */
	private static void vecSwap( BigSwapper swapper, long from, long l, long s ) {
		for ( int i = 0; i < s; i++, from++, l++ ) swapper.swap( from, l );
	}

    /**
     * Created by me on 6/8/15.
     */
    @FunctionalInterface
    public interface BigSwapper {
        /**
         * Swaps the data at the given positions.
         *
         * @param a
         *            the first position to swap.
         * @param b
         *            the second position to swap.
         */
        void swap(long a, long b);
    }
}