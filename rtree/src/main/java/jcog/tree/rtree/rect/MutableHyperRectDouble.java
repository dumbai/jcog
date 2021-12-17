package jcog.tree.rtree.rect;

import jcog.Util;
import jcog.tree.rtree.point.DoubleND;

import java.util.Arrays;

public class MutableHyperRectDouble extends HyperRectDouble {

    public MutableHyperRectDouble(HyperRectDouble toClone) {
        super(new DoubleND(toClone.min), new DoubleND(toClone.max));
    }
    public MutableHyperRectDouble(DoubleND toClone) {
        super(new DoubleND(toClone), new DoubleND(toClone));
    }

    public MutableHyperRectDouble(int dim) {
        super(new DoubleND(dim), new DoubleND(dim));
    }

    public HyperRectDouble zero() {
        Arrays.fill(min.coord, 0);
        Arrays.fill(max.coord, 0);
        return this;
    }

    public HyperRectDouble mbrSelf(double[] b) {
        double[] minA = min.coord;
        int dim = minA.length;
        if (b.length!=dim)
            throw new ArrayIndexOutOfBoundsException();

        double[] maxA = max.coord;
        for (int i = 0; i < dim; i++) {
            double bb = b[i];
            minA[i] = Math.min(minA[i], bb);
            maxA[i] = Math.max(maxA[i], bb);
        }
        return this;
    }

    public HyperRectDouble mbrSelf(HyperRectDouble x) {


        double[] minA = min.coord;
        double[] minB = x.min.coord;
        int dim = minA.length;
        if (minB.length!=dim)
            throw new ArrayIndexOutOfBoundsException();

        double[] maxA = max.coord;
        double[] maxB = x.max.coord;
        for (int i = 0; i < dim; i++) {
            minA[i] = Math.min(minA[i], minB[i]);
            maxA[i] = Math.max(maxA[i], maxB[i]);
        }
        return this;
    }

    /** extends the boundary uniformly across all dimensions by a constant amount (adds a margin) */
    public void grow(double distance) {
        int dim = dim();
        double[] minA = min.coord, maxA = max.coord;
        for (int i = 0; i < dim; i++) {
            minA[i] -= distance;
            maxA[i] += distance;
        }
    }
    public void grow(int dim, double distance) {
        double[] minA = min.coord, maxA = max.coord;
        minA[dim] -= distance;
        maxA[dim] += distance;
    }

    public void growPct(double pct) {
        int dim = dim();
        double[] minA = min.coord, maxA = max.coord;
        for (int i = 0; i < dim; i++) {
            double r = pct * range(i);
            minA[i] -= r;
            maxA[i] += r;
        }

    }

    public MutableHyperRectDouble lerp(HyperRectDouble h, float rate) {
        int dim = dim();

        for (int i = 0; i < dim; i++) {
            min.coord[i] = Util.lerp(rate, min.coord[i], h.min.coord[i]);
            max.coord[i] = Util.lerp(rate, max.coord[i], h.max.coord[i]);
        }

        return this;
    }
}
