package jcog.math;

import jcog.Util;
import org.jetbrains.annotations.Nullable;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * An immutable inclusive longerval a..b implementation of LongInterval
 */
public class ImmLongInterval implements LongInterval, Comparable<LongInterval> {

//    static final Comparator<Longerval> comparator = Comparators
//        .byLongFunction((Longerval l) -> l.s)
//        .thenComparingLong((l) -> l.e);

    private static final LongInterval Eternal = new ImmLongInterval(ETERNAL, ETERNAL);

    /** start */
    public final long s;
    /** end */
    public final long e;

    public ImmLongInterval(long s) {
        this.s = this.e = s;
    }

    public ImmLongInterval(long s, long e) {
        assert (/*start != TIMELESS &&*/ e >= s);
        this.s = s;
        this.e = e;
    }

    public static @Nullable LongInterval intersection(long myA, long myB, long otherA, long otherB) {
        return new ImmLongInterval(myA, myB).intersection(otherA, otherB);
    }


    //    @Nullable
//    public static long[] intersectionArray(long myA, long myB, long otherA, long otherB) {
//        return intersectionArray(myA, myB, otherA, otherB, null);
//    }

    @Override
    public final long start() {
        return s;
    }

    @Override
    public final long end() {
        return e;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        LongInterval other = (LongInterval) o;
        return this.s == other.start() && this.e == other.end();
    }

    @Override
    public int hashCode() {
        return Util.hashCombine(0, s, e);
//        throw new UnsupportedOperationException();
    }


    public LongInterval union(ImmLongInterval other) {
        if (this == other) return this;
        LongInterval u = union(other.s, other.e);
        return u.equals(other) ? other : u; //equality to this is already tested
    }

    public final LongInterval union(long bs, long be) {
        long as = this.s;
        if (as == ETERNAL || bs == ETERNAL)
            return ImmLongInterval.Eternal;
        if (as == TIMELESS || bs == TIMELESS)
            throw new UnsupportedOperationException();
        return new ImmLongInterval(min(as, bs), max(this.e, be));
    }

    public final @Nullable LongInterval intersection(long bs, long be) {
        long as = this.s;
        if (as == ETERNAL || bs == ETERNAL)
            return ImmLongInterval.Eternal;
        if (as == TIMELESS || bs == TIMELESS)
            throw new UnsupportedOperationException();

        long ae = this.e;
        long s = max(as, bs), e = min(ae, be);
        return s > e ? null : ((s == as && e == ae) ? this : new ImmLongInterval(s, e));
    }


//	@Nullable public static Longerval intersect(long x1, long x2, long y1, long y2) {
//
//    private long[] intervalArray() {
//        return intervalArray(null);
//    }

//    private long[] intervalArray(@Nullable long[] target) {
//        if (target == null)
//            target = new long[2];
//        target[0] = start;
//        target[1] = end;
//        return target;
//    }
//
//    public static @Nullable long[] intersectionArray(long myA, long myB, long otherA, long otherB, @Nullable long[] target) {
//        @Nullable Longerval x = Longerval.intersection(myA, myB, otherA, otherB);
//        return x == null ? null : x.intervalArray(target);
//    }

    //    /**
//     * Return the longerval with elements from this not in other;
//     * other must not be totally enclosed (properly contained)
//     * within this, which would result in two disjoint longervals
//     * instead of the single one returned by this method.
//     */
//    public Longerval differenceNotProperlyContained(Longerval other) {
//        Longerval diff = null;
//
//        if (other.startsBeforeNonDisjoint(this)) {
//            diff = new Longerval(max(this.start, other.end + 1),
//                    this.end);
//        } else if (other.startsAfterNonDisjoint(this)) {
//            diff = new Longerval(this.start, other.start - 1);
//        }
//        return diff;
//    }

    @Override
    public String toString() {
        return LongInterval.tStr(s,e);
    }

    @Override
    public final int compareTo(LongInterval x) {
        return this==x ?
                0
                :
                comparator2.compare(this, x);
    }

}