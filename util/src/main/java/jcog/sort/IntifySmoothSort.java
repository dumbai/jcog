package jcog.sort;

import java.util.function.ToIntFunction;

/**
 * variation that uses IntFunction<X> instead of comparators, for faster cached comparisons
 */
public enum IntifySmoothSort {
	;


	public static <X> void smoothSort(X[] m, int lo, int hi, ToIntFunction<X> rank) {

        assert(hi < SmoothSort.LP[32]): "Maximum length exceeded for smoothsort implementation";

		int head = lo, p = 1, pshift = 1;

		while (head < hi) {
			if ((p & 3) == 3) {
				sift(m, pshift, head, rank);
				p >>>= 2;
				pshift += 2;
			} else {
				if (SmoothSort.LP[pshift - 1] >= hi - head)
					trinkle(m, p, pshift, head, false, rank);
				else
					sift(m, pshift, head, rank);

				if (pshift == 1) {
					p <<= 1;
					pshift--;
				} else {
					p <<= (pshift - 1);
					pshift = 1;
				}
			}
			p |= 1;
			head++;

		}

		trinkle(m, p, pshift, head, false, rank);

		while (pshift != 1 || p != 1) {
			if (pshift <= 1) {
				int trail = Integer.numberOfTrailingZeros(p & ~1);
				p >>>= trail;
				pshift += trail;
			} else {
				p <<= 2;
				p ^= 7;
				pshift -= 2;

				trinkle(m, p >>> 1, pshift + 1, head - SmoothSort.LP[pshift] - 1, true, rank);
				trinkle(m, p, pshift, head - 1, true, rank);
			}

			head--;
		}
	}

	private static <X> void trinkle(X[] m, int p, int pshift, int head, boolean trusty, ToIntFunction<X> rank) {
		X val = m[head];
		int vval = p!=1 ? rank.applyAsInt(val)
				:
				-1; //elide

		while (p != 1) {
			int stepson = head - SmoothSort.LP[pshift];
            X mstepson = m[stepson];

			int vstepson = rank.applyAsInt(mstepson);
			if (vstepson >= vval)
				break;

			if (!trusty && pshift > 1) {
				int rt = head - 1;
				if (rank.applyAsInt(m[rt]) <= vstepson)
					break;
                int lf = rt - SmoothSort.LP[pshift - 2];
				if (rank.applyAsInt(m[lf]) <= vstepson)
					break;
			}

			m[head] = mstepson;

			head = stepson;
			int trail = Integer.numberOfTrailingZeros(p & ~1);
			p >>>= trail;
			pshift += trail;
			trusty = false;
		}

		if (!trusty) {
			m[head] = val;
			sift(m, pshift, head, rank);
		}
	}

	private static <X> void sift(X[] m, int pshift, int head, ToIntFunction<X> rank) {
		X val = m[head];
		int vval = pshift > 1 ? rank.applyAsInt(val)
				:
				-1; //elide

		while (pshift > 1) {
			int rt = head - 1;
			int lf = rt - SmoothSort.LP[pshift - 2];

			X mlf = m[lf], mrt = m[rt];
			int vlf = rank.applyAsInt(mlf), vrt = rank.applyAsInt(mrt);
			if (vval <= vlf && vval <= vrt)
			    break;

			if (vlf <= vrt) {
				m[head] = mlf;
				head = lf;
				pshift--;
			} else {
				m[head] = mrt;
				head = rt;
				pshift -= 2;
			}
		}
		m[head] = val;
	}


}