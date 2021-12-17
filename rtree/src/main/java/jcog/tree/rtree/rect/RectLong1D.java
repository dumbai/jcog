package jcog.tree.rtree.rect;

import jcog.Util;
import jcog.tree.rtree.HyperRegion;
import jcog.tree.rtree.point.Long1D;

/**
 * Created by me on 12/2/16.
 */
public class RectLong1D implements HyperRegion {

    public final long min;
    public final long max;

    /**
     * point
     */
    public RectLong1D(long f) {
        this(f, f);
    }

    /**
     * range
     */
    public RectLong1D(long f, long t) {
        this.min = f;
        this.max = t;
    }

    @Override
    public HyperRegion mbr(HyperRegion[] rect) {
        int n = rect.length;
        assert (n > 0);
        if (n == 1)
            return rect[0];
        long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        for (HyperRegion h : rect) {
            if (h == null)
                break;
            RectLong1D r = (RectLong1D) h;
            long rmin = r.min;
            if (rmin < min) min = rmin;
            long rmax = r.max;
            if (rmax > max) max = rmax;
        }
        return new RectLong1D(min, max);
    }

    @Override
    public HyperRegion mbr(HyperRegion r) {

        RectLong1D s = (RectLong1D) r;
        long f = Math.min(min, s.min);
        long t = Math.max(max, s.max);
        return new RectLong1D(f, t);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RectLong1D o)) return false;
        return min == o.min && max == o.max;
    }

    @Override
    public int hashCode() {
        return Util.hashCombine(Long.hashCode(min), Long.hashCode(max));
    }

    @Override
    public int dim() {
        return 1;
    }

    @Override
    public double coord(int dimension, boolean maxOrMin) {
        assert(dimension==0);
        return maxOrMin ? max : min;
    }



    public Long1D center() {
        return new Long1D((min + max) / 2);
    }

    @Override
    public double center(int d) {
        assert (d == 0);
        return (min + max) / 2.0;
    }

    @Override
    public double range(int dim) {
        assert (dim == 0);
        return Math.abs(max - min);
    }

    @Override
    public boolean contains(HyperRegion r) {
        RectLong1D inner = (RectLong1D) r;
        return inner.min >= min && inner.max <= max;
    }

    @Override
    public boolean intersects(HyperRegion r) {
        RectLong1D rr = (RectLong1D) r;
        return (Math.max(min, rr.min) <= Math.min(max, rr.max));
    }

    @Override
    public double cost() {
        return range(0);
    }


}