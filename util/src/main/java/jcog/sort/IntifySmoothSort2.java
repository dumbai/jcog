package jcog.sort;

import org.eclipse.collections.api.block.function.primitive.ShortToIntFunction;

/**
 * variation that uses IntFunction<X> instead of comparators, for faster cached comparisons
 */
public record IntifySmoothSort2(short[] m, ShortToIntFunction rank) {

	public void sort(int lo, int hi) {
		hi--;

		assert (hi < SmoothSort.LP[32]) : "Maximum length exceeded for smoothsort implementation";

		int head = lo, p = 1, pshift = 1;

		while (head < hi) {
			if ((p & 3) == 3) {
				sift(pshift, head);
				p >>>= 2;
				pshift += 2;
			} else {
				if (SmoothSort.LP[pshift - 1] >= hi - head)
					trinkle(p, pshift, head, false);
				else
					sift(pshift, head);

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

		trinkle(p, pshift, head, false);

		while (pshift != 1 || p != 1) {
			if (pshift <= 1) {
				int trail = Integer.numberOfTrailingZeros(p & ~1);
				p >>>= trail;
				pshift += trail;
			} else {
				p <<= 2;
				p ^= 7;
				pshift -= 2;

				trinkle(p >>> 1, pshift + 1, head - SmoothSort.LP[pshift] - 1, true);
				trinkle(p, pshift, head - 1, true);
			}

			head--;
		}
	}

	private void trinkle(int p, int pshift, int head, boolean trusty) {
		short val = m[head];
		int vhead = p == 1 ? -1 /* elide */ : rank.valueOf(val);

		while (p != 1) {
			int stepson = head - SmoothSort.LP[pshift];
			short mstepson = m[stepson];

			int vstepson = rank.valueOf(mstepson);
			if (vstepson >= vhead)
				break;

			if (!trusty && pshift > 1) {
				int rt = head - 1;
				if (rank.valueOf(m[rt]) <= vstepson)
					break;
                int lf = rt - SmoothSort.LP[pshift - 2];
				if (rank.valueOf(m[lf]) <= vstepson)
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
			sift(pshift, head);
		}
	}

	private void sift(int pshift, int head) {

		short mhead = m[head];
		int vhead = pshift > 1 ? rank.valueOf(mhead)
				:
				-1; //elide

		while (pshift > 1) {
			int rt = head - 1;
			int lf = rt - SmoothSort.LP[pshift - 2];

			short mlf = m[lf];
			short mrt = m[rt];
			int vlf = rank.valueOf(mlf), vrt = rank.valueOf(mrt);
			if (vhead <= vlf && vhead <= vrt)
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
		m[head] = mhead;
	}


}