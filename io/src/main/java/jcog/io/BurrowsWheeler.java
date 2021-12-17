package jcog.io;

import jcog.sort.QuickSort;
import jcog.util.ArrayUtil;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.ByteObjectHashMap;

import java.util.Arrays;

/** modified from https://github.com/fujiawu/burrows-wheeler-compression/blob/master/BurrowsWheeler.java */
public enum BurrowsWheeler {
	;

	private static class CircularSuffixArray {

		private final byte[] input;
		private final int[] index;

		CircularSuffixArray(byte[] input)  {
			this.input = input;

			int n = input.length;

			index = new int[n];
			for (int i = 0; i < n; i++)
				index[i] = i;

			QuickSort.quickSort(0, n, this::compare, this::swap);
		}

		private void swap(int a, int b) {
			ArrayUtil.swapInt(index, a, b);
		}

		private int compare(int a, int b) {
			if (a==b)
				return 0;

			int[] index = this.index;
			int s1 = index[a], s2 = index[b];
			int t1 = s1, t2 = s2;
			byte[] input = this.input;
			int n = input.length;
			for (int i = 0; i < n; i++) {
				byte c1 = input[t1], c2 = input[t2];
				if (c1 < c2)
					return -1;
				else if (c1 > c2)
					return 1;
				else {
					if (++t1 == n) t1 = 0;
					if (++t2 == n) t2 = 0;
				}
			}

			int d = s2 - s1;
			return Integer.compare(d, 0);
		}

	}

	public static int encode(byte[] input, byte[] output) {

		CircularSuffixArray suffixes = new CircularSuffixArray(input);

		int n = suffixes.input.length;
		int key = -1;

		for (int i = 0; i < n; i++) {
			int si = suffixes.index[i];
			if (si == 0) key = i;

			int p = (si + n - 1) % n;

			output[i] = input[p < 0 ? p + n : p];
		}

		return key;
	}

	public static byte[] decode(byte[] in, int key, byte[] out) {

		// map list of positions for each characters 
		int n = in.length;
		ByteObjectHashMap<IntArrayList> positions = new ByteObjectHashMap<>(Math.min(255,n));
		for (int i = 0; i < n; i++ )
			positions.getIfAbsentPut(in[i], IntArrayList::new).add(i);

		Arrays.sort(in); // sort last word

		int[] next = new int[n];
		for (int i = 0; i < n; i++)
			next[i] = positions.get(in[i]).removeAtIndex(0);

		int cur = key;
		for (int i = 0; i < n; i++) {
			out[i] = in[cur];
			cur = next[cur];
		}

		return out;
	}



}