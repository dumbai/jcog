package jcog.tree.rtree.rect;

import jcog.tree.rtree.HyperRegion;
import jcog.tree.rtree.point.Double1D;

/**
 * Created by me on 12/2/16.
 */
public abstract class RectDouble1D implements HyperRegion {


    public abstract double from();

    public abstract double to();

    @Override
    public HyperRegion mbr(HyperRegion r) {

        RectDouble1D s = (RectDouble1D) r;
        double from = from();
        double to = to();
        double f = Math.min(from, s.from());
        double t = Math.max(to, s.to());
        return new DefaultRect1D(f, t);
    }

    @Override
    public int dim() {
        return 1;
    }

    @Override
    public double coord(int dimension, boolean maxOrMin) {
        assert(dimension==0);
        return maxOrMin ? to() : from();
    }

    public Double1D center() {
        return new Double1D(center(0));
    }

    @Override
    public double center(int d) {
        assert (d == 0);
        return (from() + to()) / 2.0;
    }

    @Override
    public double range(int dim) {
        return Math.abs(from() - to());
    }

    @Override
    public boolean contains(HyperRegion r) {
        RectDouble1D inner = (RectDouble1D) r;
        return inner.from() >= from() && inner.to() <= to();
    }

    @Override
    public boolean intersects(HyperRegion r) {
        RectDouble1D rr = (RectDouble1D) r;
        return (Math.max(from(), rr.from()) <= Math.min(to(), rr.to()));
    }

    @Override
    public double cost() {
        return range(0);
    }


    public static class DefaultRect1D extends RectDouble1D {


        private final double from;
        private final double to;

        /**
         * point
         */
        public DefaultRect1D(double x) {
            this(x, x);
        }

        /**
         * line segment
         */
        public DefaultRect1D(double from, double to) {
            if (from > to)
                throw new UnsupportedOperationException("order fault");
            this.from = from;
            this.to = to;
        }


        @Override
        public String toString() {
            return "[" + from + ',' + to + ']';
        }

        @Override
        public double from() {
            return from;
        }

        @Override
        public double to() {
            return to;
        }
    }


}
