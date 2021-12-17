package jcog.math;

import org.hipparchus.util.CombinatoricsUtils;

import java.util.NoSuchElementException;

import static jcog.util.ArrayUtil.swapInt;

/** from http://stackoverflow.com/questions/2920315/permutation-of-array */
public class Permutations {


	/** total possible */
	int num;

	/** current iteration */
	int count;

	int size;

	protected int[] ind;

	public Permutations() {
	}

	public Permutations restart(int size) {

		this.size = size;

		int[] ind = this.ind;
		if (ind == null || ind.length < size)
			this.ind = ind = new int[size];

		for (int i = 0; i < size; i++)
			ind[i] = i;

		count = -1;
		num = (int) CombinatoricsUtils.factorial(size);

		return this;
	}

	public final boolean hasNext() {
		return count < num - 1;
	}

	/**
	 * Computes next permutations. Same array instance is returned every time!
	 *
	 */
	public final int[] next() {
		int[] ind = this.ind;

		int count = (++this.count);
		if (count == 0)
			return ind;
		if (count == num)
			throw new NoSuchElementException();

		int size = this.size;
		for (int tail = size - 1; tail > 0; tail--) {
			int itm = ind[tail - 1];
			if (itm < ind[tail]) {
				int s = size - 1;
				while (itm >= ind[s])
					s--;

				swapInt(ind, tail - 1, s);

				for (int i = tail, j = size - 1; i < j; i++, j--)
					swapInt(ind, i, j);

				break;
			}
		}

		return ind;
	}

	public int permute(int index) {
		return ind[index];
	}

	public int total() {
		return num;
	}
}
