package jcog.math;


import static com.google.common.math.IntMath.factorial;

/**
 * from: http://stackoverflow.com/a/5578494
 */
public final class Combinations {

	private final int[] a;
	private final int n;
	private final int r;
	private final int total;
	private int remain;


	public Combinations(int n, int r) {
		if (n < 1)
			throw new IllegalArgumentException("Set must have at least one element");

		if (r > n)
			throw new IllegalArgumentException("Subset length can not be greater than set length");

		this.n = n;
		this.r = r;
		int nFact = factorial(n);
		int rFact = factorial(r);
		int nminusrFact = factorial(n - r);
		total = nFact / (rFact * nminusrFact);
		a = new int[r];
		reset();
	}


	public void reset() {
		int[] a = this.a;
		int alen = a.length;
		for (int i = 0; i < alen; i++)
			a[i] = i;
		remain = total;
	}


	public int remaining() {
		return remain;
	}

	public boolean hasNext() {
		return remain > 0;
	}

	public int[] prev() {
		return a;
	}
	public int[] next() {

        int[] a = this.a;
		if (remain == total) {
			remain--;
			return a;
		}

		int i = r - 1;
		while (a[i] == n - r + i)
			i--;

		a[i]++;
		for (int j = i + 1; j < r; j++)
			a[j] = a[i] + j - i;

		remain--;
		return a;
	}
}
