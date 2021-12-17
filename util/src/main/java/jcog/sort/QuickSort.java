package jcog.sort;

import jcog.data.array.IntComparator;
import jcog.util.ArrayUtil;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;
import org.eclipse.collections.api.block.function.primitive.IntToDoubleFunction;
import org.eclipse.collections.api.block.function.primitive.IntToFloatFunction;
import org.eclipse.collections.api.block.procedure.primitive.IntIntProcedure;

/**
 * https://algs4.cs.princeton.edu/23quicksort/
 * https://en.wikipedia.org/wiki/Sorting_algorithm#Comparison_sorts */
public enum QuickSort { ;

	/**
	 * threshold below which reverts to bubblesort
	 * TODO tune
	 *
	 * bubble sort mean = n * n
	 * quick sort mean  = 2 * n * log(n)
	 *
	 */
	public static final int SMALL = 6;

	/**  threshold for median-of-3
	 * TODO tune
	 *
	 * */
	private static final int MEDIUM = SMALL * 10;

	/**
	 * Sorts the specified range of elements using the specified swapper and according to the order induced by the specified
	 * comparator using quicksort.
	 * <p>
	 * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
	 * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
	 * 1249&minus;1265, 1993.
	 *
	 * @param from    the index of the first element (inclusive) to be sorted.
	 * @param to      the index of the last element (exclusive) to be sorted.
	 * @param cmp    the comparator to determine the order of the generic data.
	 * @param swapper an object that knows how to swap the elements at any two positions.
	 */
	public static void quickSort(int from, int to, IntComparator cmp, IntIntProcedure swapper) {
		int len;
		while ((len = to - from) > 1) {

			if (len <= SMALL) {
				bubbleSort(from, to, cmp, swapper);
				return;
			}

			int m = mid(from, to, cmp, len);

			int a = from;
			int b = a;
			int c = to - 1;
			int d = c;
			while (true) {
				int comparison;
				while (b <= c && ((comparison = cmp(b, m, cmp)) <= 0)) {
					if (comparison == 0) {
						if (a == m) m = b;
						else if (b == m) m = a;
						swapper.value(a++, b);
					}
					b++;
				}
				while (c >= b && ((comparison = cmp(c, m, cmp)) >= 0)) {
					if (comparison == 0) {
						if (c == m) m = d;
						else if (d == m) m = c;
						swapper.value(c, d--);
					}
					c--;
				}
				if (b > c) break;
				if (b == m) m = d;
				swapper.value(b++, c--);
			}


			vecSwapUntil(swapper, from, b, Math.min(a - from, b - a));

			vecSwapUntil(swapper, b, to, Math.min(d - c, to - d - 1));

			{
				int s = b - a;
				if (s > 1)
					//TODO push
					quickSort(from, from + s, cmp, swapper); //TODO non-recursive
					//TODO pop
			}

			{
				int s = d - c;
				if (s > 1)
					//quickSort(to - s, to, comp, swapper);
					from = to - s;
				else
					break; //done //TODO pop , else done
			}

		}
	}

	private static int cmp(int x, int y, IntComparator cmp) {
		return x == y ? 0 : cmp.compare(x, y);
	}

	private static int mid(int from, int to, IntComparator cmp, int len) {
		int m = from + len / 2;
		if (len > SMALL) {
			int l = from;
			int n = to - 1;
			if (len > MEDIUM) {
				int s = len / 8;
				l = med3(l, l + s, l + 2 * s, cmp);
				m = med3(m - s, m, m + s, cmp);
				n = med3(n - 2 * s, n - s, n, cmp);
			}
			m = med3(l, m, n, cmp);
		}
		return m;
	}

	private static void bubbleSort(int from, int to, IntComparator cmp, IntIntProcedure swapper) {
		//bubble sort
		for (int i = from; i < to; i++)
			for (int j = i; j > from && cmp.compare(j - 1, j) > 0; j--)
				swapper.value(j - 1, j);
	}

	/**
	 * Returns the index of the median of the three indexed chars.
	 */
	private static int med3(int a, int b, int c, IntComparator cmp) {
		int ab = cmp.compare(a, b);
		int ac = cmp.compare(a, c);
		int bc = cmp.compare(b, c);
		return (ab < 0 ?
			(bc < 0 ? b : ac < 0 ? c : a) :
			(bc > 0 ? b : ac > 0 ? c : a));
	}

	private static void vecSwapUntil(IntIntProcedure swapper, int from, int to, int s) {
		int t = to-s;
		for (; s > 0; s--, from++, t++)
			swapper.value(from, t);
	}

	/**
	 * sorts descending
	 */
	public static void sort(int[] a, IntToFloatFunction v) {
		sort(a, 0, a.length, v);
	}
	/**
	 * sorts descending
	 */
	public static void sort(byte[] a, IntToFloatFunction v) {
		sort(a, 0, a.length, v);
	}
	private static void sort(int[] x, int left, int right /* inclusive */, IntToFloatFunction v) {
		quickSort(left, right, (a, b)->a==b ? 0 : Float.compare(v.valueOf(a), v.valueOf(b)),
			(a, b) -> ArrayUtil.swapInt(x, a, b));
	}
	private static void sort(byte[] x, int left, int right /* inclusive */, IntToFloatFunction v) {
		quickSort(left, right, (a, b)->a==b ? 0 : Float.compare(v.valueOf(a), v.valueOf(b)),
				(a, b) -> ArrayUtil.swapByte(x, a, b));
	}

	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public static <X> void sort(X[] x, int left, int right /* inclusive */, IntToDoubleFunction v) {
		quickSort(left, right, (a, b)->a==b ? 0 :
				Double.compare(v.valueOf(a), v.valueOf(b)), ArrayUtil.objSwapper(x));
	}

	/**
	 * sorts descending, left and right BOTH inclusive
	 */
	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public static <X> void sort(X[] x, int left, int right /* inclusive */, FloatFunction<X> v) {
		quickSort(left, right, (a, b) -> a == b ? 0 :
			Float.compare(v.floatValueOf(x[a]), v.floatValueOf(x[b])),
			ArrayUtil.objSwapper(x));
	}

	/** modifies order of input array */
	public static <X> X[] sort(X[] x,  FloatFunction<X> v) {
		sort(x, 0, x.length, v);
		return x;
	}
}