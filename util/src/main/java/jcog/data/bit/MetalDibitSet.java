package jcog.data.bit;

import java.util.function.Predicate;

/** 2-bit pairs impl by one contiguous internal BitSet */
public class MetalDibitSet extends LongArrayBitSet {

	public MetalDibitSet(int capacity) {
		super(Math.max(64,capacity * 2));
	}

//	public void set(int i, byte value) {
//		assert(value >= 0 && value <= 3);
//		throw new TODO();
//	}

	/** returns upper */
	public final boolean set(int i, boolean lower, boolean upper) {
		this._set2(i * 2, lower, upper);
		return upper;
	}

	/** gets lower and upper, returned in bits 0 and 1 */
	public byte get(int i) {
		return (byte) ((lower(i) ? 1 : 0) | ((upper(i) ? 1 : 0) << 1));
	}

	public boolean lower(int i) {
		return this.test(i*2);
	}
	public boolean upper(int i) {
		return this.test(i*2+1);
	}

	private int isKnown(int slot) {
		return this.dibitIfFirstTrue(slot*2);
	}

	private static boolean known(int k) { return (k & 1)!=0; }
	private static boolean itIs(int k)    { return (k & 2)!=0; }
	private static boolean itIsnt(int k)  { return (k & 2)==0; }

	public boolean memoizeVector(int[] s, Object x, Predicate[] tests, MetalBitSet polarity) {
		int n = s.length;

		//test knowns
		int pending = 0; //bitset
		for (int i = 0; i < n; i++) {
			int k = isKnown(s[i]);
			if (known(k)) {
				if (!is(i, itIs(k), polarity))
					return false;  //CUT
			} else {
				pending |= (1 << i);
			}
		}

		if (pending != 0) {
			for (int i = 0; i < n; i++) {
				if (0 != (pending & (1 << i))) {
					if (!is(i, set(s[i], true, tests[i].test(x)), polarity))
						return false;
				}
			}
		}

		return true;
	}

	private static boolean is(int i, boolean k, MetalBitSet polarity) {
		return k == polarity.test(i);
	}

	public <X> boolean memoize(int slot, X x, Predicate<X> test) {
		int k = isKnown(slot);
		return known(k) ? itIs(k) : set(slot, true, test.test(x));
	}
}