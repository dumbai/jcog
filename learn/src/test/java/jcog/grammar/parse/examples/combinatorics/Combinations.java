package jcog.grammar.parse.examples.combinatorics;

import java.util.Iterator;
import java.util.stream.IntStream;

/*
 * Copyright (c) 2000 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * The Combinations class provides an enumeration of all subsets of a group of n
 * objects taken r at a time. The constructor for Combinations accepts the group
 * as an array of Objects, along with the number to select.
 * <p>
 * For example, to choose 3 boys from a list of 5, begin with an array of names:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * Object[] boys = { &quot;Alfred&quot;, &quot;Ben&quot;, &quot;Carl&quot;, &quot;Drew&quot;, &quot;Edwin&quot; };
 * </pre>
 * 
 * </blockquote> To see all combinations of these 5 names taken 3 at a time,
 * create and use a Combinations enumeration:
 * 
 * <blockquote>
 * 
 * <pre>
 * Combinations c = new Combinations(boys, 3);
 * while (c.hasMoreElements()) {
 * 	Object[] combo = (Object[]) c.nextElement();
 * 	for (int i = 0; i &lt; combo.length; i++) {
 * 		System.out.print((String) combo[i] + &quot; &quot;);
 * 	}
 * 	System.out.println();
 * }
 * </pre>
 * 
 * </blockquote>
 * <p>
 * This will print out a 10 line list: <blockquote>
 * 
 * <pre>
 * 	Alfred Ben Carl 
 * 	Alfred Ben Drew 
 * 	Alfred Ben Edwin 
 * 	Alfred Carl Drew 
 * 	Alfred Carl Edwin 
 * 	Alfred Drew Edwin 
 * 	Ben Carl Drew 
 * 	Ben Carl Edwin 
 * 	Ben Drew Edwin 
 * 	Carl Drew Edwin
 * </pre>
 * 
 * </blockquote>
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */

class Combinations implements Iterator<Object> {
	private Object[] inArray;
	private int n;
    private int m;
	private int[] index;
	private boolean hasMore = true;

	/**
	 * Create a Combination to enumerate through all subsets of the supplied
	 * Object array, selecting 'm' at a time.
	 * 
	 * @param Object
	 *            [] inArray the group to choose from
	 * 
	 * @param m
	 *            int the number to select in each choice
	 * 
	 * @exception combinatorics.CombinatoricException
	 *                if m is greater than the length of inArray, or less than
	 *                0.
	 */
    Combinations(Object[] inArray, int m) throws CombinatoricException {

		this.inArray = inArray;
		this.n = inArray.length;
		this.m = m;

		
		Combinatoric.check(n, m);

		/**
		 * 'index' is an array of ints that keep track of the next combination
		 * to return. For example, an index on 5 things taken 3 at a time might
		 * contain {0 3 4}. This index will be followed by {1 2 3}. Initially,
		 * the index is {0 ... m - 1}.
		 */

		index = new int[m];
		for (int i = 0; i < m; i++) {
			index[i] = i;
		}
	}

	/**
	 * @return true, unless we have already returned the last combination.
	 */
	public boolean hasNext() {
		return hasMore;
	}

	/**
	 * Move the index forward a notch. The algorithm finds the rightmost index
	 * element that can be incremented, increments it, and then changes the
	 * elements to the right to each be 1 plus the element on their left.
	 * <p>
	 * For example, if an index of 5 things taken 3 at a time is at {0 3 4},
	 * only the 0 can be incremented without running out of room. The next index
	 * is {1, 1+1, 1+2) or {1, 2, 3}. This will be followed by {1, 2, 4}, {1, 3,
	 * 4}, and {2, 3, 4}.
	 * <p>
	 * The algorithm is from "Applied Combinatorics", by Alan Tucker.
	 * 
	 */
    private void moveIndex() {
		int i = rightmostIndexBelowMax();
		if (i >= 0) {
			index[i] += 1;
			for (int j = i + 1; j < m; j++) {
				index[j] = index[j - 1] + 1;
			}
		} else {
			hasMore = false;
		}
	}

	/**
	 * @return java.lang.Object, the next combination from the supplied Object
	 *         array.
	 *         <p>
	 *         Actually, an array of Objects is returned. The declaration must
	 *         say just "Object", since the Combinations class implements
	 *         Enumeration, which declares that the "nextElement()" returns a
	 *         plain Object. Users must cast the returned object to (Object[]).
	 */
	public Object next() {
		if (!hasMore) {
			return null;
		}

		int bound = m;
		Object[] out = IntStream.range(0, bound).mapToObj(i -> inArray[index[i]]).toArray();

        moveIndex();
		return out;
	}

	/**
	 * @return int, the index which can be bumped up.
	 */
    private int rightmostIndexBelowMax() {

        return IntStream.iterate(m - 1, i -> i >= 0, i -> i - 1).filter(i -> index[i] < n - m + i).findFirst().orElse(-1);
    }

	public void remove() {
		throw new UnsupportedOperationException();
	}
}