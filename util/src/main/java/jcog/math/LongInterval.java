package jcog.math;

import jcog.Fuzzy;
import jcog.Research;
import jcog.Util;
import jcog.WTF;
import org.eclipse.collections.impl.block.factory.Comparators;

import java.util.Comparator;

import static java.lang.Math.*;
import static jcog.Fuzzy.mean;

/**
 * pair of 64-bit signed long integers representing an interval.
 * a special 'ETERNAL' value represents (-infinity,+infinity)
 * <p>
 * TODO allow (-infinity, ..x) and (x, +infinity)
 */
public interface LongInterval extends LongIntervalArray {

    Comparator<LongInterval> comparator2 = Comparators
            .byLongFunction(LongInterval::start)
            .thenComparingLong(LongInterval::end);
    long ETERNAL = Long.MIN_VALUE;
    long TIMELESS = Long.MAX_VALUE;
    int MIN = -1;
    int MEAN = 0;
    int MAX = 1;
    int SUM = 2;

    static int compare(long as, long ae, long bs, long be) {
        //return Long.compare(as + ae, bs + be);
        long d = as - bs + ae - be;
        if (d == 0) return 0;
        else if (d > 0) return +1;
        else return -1;
    }

    //    public static String tStr(long t) {
    //        if (t == ETERNAL)
    //            return "ETE";
    //        else if (t == TIMELESS)
    //            return "TIMELESS";
    //        else
    //            return String.valueOf(t);
    //    }
    static String tStr(long s, long e) {
        if (s == ETERNAL)
            return "ETE";
        else if (s == TIMELESS)
            return "TIMELESS";
        else
            return s + ".." + e;
    }

//	static double intersectLength(double as, double ae, double bs, double be) {
//		double a = max(as, bs);
//		double b = min(ae, be);
//		return Math.max(0, b-a);
//	}

    /**
     * returns -1 if no intersection; 0 = adjacent, > 0 = non-zero interval in common
     */
    static long intersectLength(long as, long ae, long bs, long be) {
        if (as == TIMELESS || bs == TIMELESS)
            throw new UnsupportedOperationException();
        if (as == ETERNAL) {
            if (bs == ETERNAL) throw new UnsupportedOperationException();
            return be - bs;
        } else if (bs == ETERNAL)
            return ae - as;
        else {
            return intersectLengthRaw(as, ae, bs, be);
        }
    }

    private static long intersectLengthRaw(long as, long ae, long bs, long be) {
        long a = max(as, bs), b = min(ae, be);
        return a <= b ? b - a : -1;
    }

    private static double intersectLengthRaw(double as, double ae, double bs, double be) {
        double a = max(as, bs), b = min(ae, be);
        return a <= b ? b - a : 0;
    }

    private static boolean intersectsRaw(double as, double ae, double bs, double be) {
        return max(as, bs) <= min(ae, be);
    }

    /**
     * cs,ce = container;   xs,xe = possibly contained
     */
    static boolean containsRaw(long cs, long ce, long xs, long xe) {
        return cs <= xs && xe <= ce;
    }

    static boolean containsRaw(double cs, double ce, double xs, double xe) {
        return cs <= xs && xe <= ce;
    }

    static long minTimeTo(long fs, long fe, long ts, long te) {
        return intersectsRaw(fs, fe, ts, te) ? 0 :
            minTimeToRaw(fs, fe, ts, te);
    }

    static double minTimeTo(double fs, double fe, double ts, double te) {
        return intersectsRaw(fs, fe, ts, te) ? 0 :
            minTimeToRaw(fs, fe, ts, te);
    }

    static double diffTotal(double fs, double fe, double ts, double te) {
        return abs(fs - ts) + abs(fe - te);
    }

    static long minTimeToRaw(long fs, long fe, long ts, long te) {
        return min(min(abs(fe - ts), abs(fs - ts)), min(abs(fe - te), abs(fs - te)));
    }

//	static double separation(double fs, double fe, double ts, double te) {
//		return intersectsRaw(fs, fe, ts, te) ? 0 : minTimeToRaw(fs, fe, ts, te);
//	}

//	static double minTimeTo(double fs, double fe, double ts, double te) {
//		//if (containsRaw(fs, fe, ts, te)) return 0;
//		return min(min(abs(fe - ts), abs(fs - ts)),min(abs(fe - te), abs(fs - te)));
//	}
//	static double maxTimeTo(double fs, double fe, double ts, double te) {
//		return max(max(abs(fe - ts), abs(fs - ts)), max(abs(fe - te), abs(fs - te)));
//	}

//    /** min time to shift 'from' to 'to' */
//    static long minTimeShiftTo(long fs, long fe, long ts, long te) {
//		if (containsRaw(fs, fe, ts, te) /*|| containsRaw(ts, te, fs, fe)*/)
//			return 0;
//		else {
//			//TODO better
//			return abs( (fs + fe)/2 - (ts + te)/2 ); //midpoint delta
//			//return min(Math.abs(fs - ts), Math.abs(fe - te));
//		}
//    }

    static double minTimeToRaw(double fs, double fe, double ts, double te) {
        return Util.min(
                Util.min(abs(fe - ts), abs(fs - ts)),
                Util.min(abs(fe - te), abs(fs - te)));
    }

    static long[] range(long center, float diameter) {
        long rad = (long) ceil(diameter / 2.0);
        return new long[]{
                center - rad,
                center + rad
        };
    }

    static long[] unionArray(long xs, long xe, long ys, long ye) {
        return (ys == ETERNAL ? new long[]{xs, xe} :
                (xs == ETERNAL ? new long[]{ys, ye} :
                        new long[]{min(xs, ys), max(xe, ye)})
        );
    }

    /**
     * choosing optimal subrange of task/belief by
     * shrinking occurrence range to wrap the shorter of the two
     */
    static void trimToward(long[] x, long os, long oe) {
        long xs = x[0], xe = x[1];

        if (containsRaw(xs, xe, os, oe)) {
            x[0] = os;
            x[1] = oe;
        } else {
            long xr = xe - xs, or = oe - os;

            if (or >= xr) return;

            if (xe <= oe) {
                xs = xe - or; //after: align right
            } else {
                assert (oe < xs + or);
                xe = xs + or; //before: align left
            }
            x[0] = xs;
            x[1] = xe;

        }


    }

    /**
     * true if [as..ae] intersects [bs..be]
     */
    static boolean intersects(long as, long ae, long bs, long be) {
        assert (as != TIMELESS && bs != TIMELESS);
        return intersectsSafe(as, ae, bs, be);
    }

    static boolean intersectsSafe(long as, long ae, long bs, long be) {
        return (as == ETERNAL) || (bs == ETERNAL) || intersectsRaw(as, ae, bs, be);
    }

    static boolean intersectsRaw(long as, long ae, long bs, long be) {
        return max(as, bs) <= min(ae, be);
    }

    static boolean intersectsRaw(long a, long bs, long be) {
        return max(a, bs) <= min(a, be);
    }


    //		return internew Longerval(x1, x2).intersection(y1, y2);
//	}


//	static long unionLength(long x1, long x2, long y1, long y2) {
//		return max(x2, y2) - min(x1, y1);
//	}

//	/**
//	 * returns -1 if no intersection; 0 = adjacent, > 0 = non-zero interval in common
//	 */
//	static int intersectLength(int x1, int x2, int y1, int y2) {
//		int a = max(x1, x2);
//		int b = min(y1, y2);
//		return a <= b ? b - a : -1;
//	}

    /**
     * does not test for eternal, timeless
     */
    static long minTimeToRaw(long x, long s, long e) {
        return containsRaw(x, s, e) ? 0 :
                min(abs(x - s), abs(x - e));
    }

    /**
     * warning: does not test for eternal, timeless
     */
    static long maxTimeToRaw(long x, long s, long e) {
        return max(abs(x - s), abs(x - e));
    }

    static double meanTimeToRaw(long when, long s, long e) {
        return (abs(s - when) + abs(e - when)) / 2.0;
    }

    private static boolean containsRaw(long w, long s, long e) {
        return w >= s && w <= e;
    }


//	default boolean isDuringAny(long... when) {
//		if (when.length == 2 && when[0] == when[1]) return isDuring(when[0]);
//		return Arrays.stream(when).anyMatch(this::isDuring);
//	}
//
//	default boolean isDuringAll(long... when) {
//		if (when.length == 2 && when[0] == when[1]) return isDuring(when[0]);
//		return Arrays.stream(when).allMatch(this::isDuring);
//	}
//
//	default boolean isDuring(long when) {
//		if (when == ETERNAL)
//			return true;
//		long start = start();
//		return (start == ETERNAL) || (start == when) || ((when >= start) && (when <= end()));
//	}

    private static boolean _during(long x, long start, long end) {
        return start <= x && end >= x;
    }

    static double dSepNorm(double[] a, double[] b) {
        return dSepNorm(a[0], a[1], b[0], b[1]);
    }
    static double dSepFraction(double[] a, double[] b) {
        return dSepFraction(a[0], a[1], b[0], b[1]);
    }

    static double dSepNorm(double a0, double a1, double b0, double b1) {
        return (abs(a0 - b0) + abs(a1 - b1))
                / (1 + Util.mean(a1 - a0, b1 - b0));
    }

    /** ratio of absolute extrema distance to intersection range */
    @Research static double dSepFraction(double a0, double a1, double b0, double b1) {
//        return (abs(a0 - b0) + abs(a1 - b1))
//                / (1 + intersectLengthRaw(a0, a1, b0, b1));
        return ((abs(a0 - b0) + abs(a1 - b1))
                / (1 + intersectLengthRaw(a0, a1, b0, b1))
                / (1 + Fuzzy.mean(a1-a0, b1-b0))) //normalize to mean range
                ;
    }

    long start();

//	/** does not test for eternal, timeless */
//	static double minTimeToRaw(double x, double s, double e) {
//		return x >= s && x <= e ? 0 :
//				min(abs(x - s), abs(x - e));
//	}

    long end();

    @Override
    default long[] startEndArray() {
        return new long[]{start(), end()};
    }

//	default long minTimeTo(long a) {
//		return minTimeTo(a, a);
//	}

    default long mid() {
        long s = start();
        return (s == ETERNAL || s == TIMELESS) ? s : mean(s, end());
    }


//	default double timeDiffMean(long s, long e) {
//		return (abs(start()-s) + abs(end()-e))/2.0;
//	}

    /**
     * return number of elements between a and b inclusively. x..x is length 1.
     * if b &lt; a, then length is 0.  9..10 has length 2.
     */
    default long range() {
        long s = start();
        if (s == ETERNAL || s == TIMELESS)
            throw new ArithmeticException("ETERNAL range calculated");
        return 1 + (end() - s);
    }

    default long rangeElse(long ifEternal) {
        long s = start();
        return s == ETERNAL ? ifEternal : 1 + (end() - s);
    }

//	default long timeDiffSum(long w) {
//		//assert(!ETERNAL());
//		return abs(start()-w) + abs(end()-w);
//	}

    /**
     * finds the nearest point within the provided interval relative to some point in this interval
     */
    @Deprecated
    default long nearestPointExternal(long a, long b) {
        if (a == b || a == ETERNAL)
            return a;

        long s = start();
        if (s == ETERNAL)
            return (a + b) / 2L;

        long e = end();

        long mid = (s + e) / 2;
        if (s >= a && e <= b)
            return mid;

        return abs(mid - a) <= abs(mid - b) ? a : b;
    }

    /**
     * finds the nearest point inside this interval to the provided range, which may be
     * inside, intersecting, or disjoint from this interval.
     */
    @Deprecated
    default long nearestPointInternal(long a, long b) {

        assert (b >= a && (a != ETERNAL || a == b));

        if (a == ETERNAL)
            return mid();

        long s = this.start();
        if (s == ETERNAL)
            return ETERNAL;

        long e = this.end();
        if (s == e)
            return s;

        if ((a >= s) && (b <= e)) {
            return (a + b) / 2L;
        } else if (a < s && b > e) {
            return (s + e) / 2L;
        } else {
            long se = (s + e) / 2L;
            long ab = (a + b) / 2L;
            return se <= ab ? e : s;
        }
    }

    /**
     * if the task intersects (ex: occurrs during) the specified interval,
     * returned time distance is zero, regardless of how far it may extend before or after it
     */
    default long minTimeTo(long a, long b) {

        if (a == TIMELESS)
            throw new WTF(); //return TIMELESS;

        if (a == ETERNAL)
            return 0;

        long s = start(); //assert(s!=TIMELESS);
        if (s == ETERNAL)
            return 0;

        long sa = abs(s - a);
        if (sa == 0)
            return 0; //internal

        long e = end();
        if (a == b) {
            return s == e ? sa :
                    (max(a, s) <= min(a, e)) ? 0 : min(sa, abs(e - b));
        } else {
            if (/*e != s && */intersectsRaw(a, b, s, e)) //TODO maybe needs contains
                return 0; //internal
            long sab = min(sa, abs(s - b));
            return s == e ? sab : min(sab, min(abs(e - a), abs(e - b)));
        }
    }

    default long diff(long w) {
        long s = start();
        if (s == ETERNAL) return 0;
        long e = end();
        if (containsRaw(w, s, e)) return 0;
        else return min(abs(s - w), abs(e - w));
    }

    default long timeMeanTo(long x) {
        return timeTo(x, false, MEAN);
    }

    default long timeMeanDuringOrTo(long x) {
        return timeTo(x, true, MEAN);
    }

    default long timeTo(long x, boolean zeroIfDuring, int mode) {
        if (x == ETERNAL) return 0;
        long s = start();
        if (s == ETERNAL) return 0;

        long e = end();

        if (zeroIfDuring && _during(x, s, e))
            return 0; //contained

        long ds = abs(s - x);
        if (s == e) return ds;
        else {
            long de = abs(e - x);
            return switch (mode) {
                case MIN -> min(ds, de);
                case MEAN -> mean(ds, de);
                case MAX -> max(ds, de);
                case SUM -> ds + de;
                default -> throw new UnsupportedOperationException();
            };
        }
    }

    default boolean intersects(LongInterval i) {
        return this == i || intersects(i.start(), i.end());
    }

    default boolean intersectsRaw(LongInterval i) {
        return this == i || intersectsRaw(i.start(), i.end());
    }

    default boolean intersects(long s, long e) {
        assert (s != TIMELESS);
        if (s == ETERNAL)
            return true;
        long start = start();
        return (start == ETERNAL) || intersectsRaw(s, e);
    }

    default boolean intersectsRaw(long s, long e) {
        return (e >= start() && s <= end());
    }

    default boolean contains(long s, long e) {
        assert (s != TIMELESS);
        return containsSafe(s, e);
    }

    default boolean containsSafe(long s, long e) {
        long start = start();
        return start == ETERNAL || (s != ETERNAL && (start <= s && e <= end()));
    }

    default boolean containsRaw(LongInterval b) {
        return this == b || (b.start() >= start() && b.end() <= end());
    }


    default boolean containedBySafe(long cs, long ce) {
        if (cs == ETERNAL)
            return true;

        long start = start();
        return start != ETERNAL && cs <= start && end() <= ce;
    }

    /**
     * eternal contains itself
     */
    default boolean contains(LongInterval b) {
        if (this == b) return true;
        long as = start();
        if (as == ETERNAL)
            return true;
        else {
            long bs = b.start();
            return /*bs != ETERNAL &&*/ bs >= as && b.end() <= end();
        }
    }

    default long diffRaw(long a, long b) {
        return abs(a - start()) + abs(b - end());
    }


//    default double timeDiffMean(long when) {
//		return timeDiffSum(when)/2.0;
//	}
}