package jcog.data;


import jcog.Util;
import jcog.data.atomic.AtomicCycle;
import org.hipparchus.linear.ArrayRealVector;

import java.util.Arrays;

import static jcog.Str.n4;
import static jcog.Util.assertFinite;

/**
 * TODO implement Tensor
 */
public class Centroid extends ArrayRealVector {

    /** serial unique ID */
    private static final AtomicCycle.AtomicCycleNonNegative ID = new AtomicCycle.AtomicCycleNonNegative();
    public int id;
    public final int _id;

    private transient double tmpErr, tmpDistance;

    public Centroid(int internalID, int dimensions) {
        super(dimensions);
        this._id = internalID;
        clear();
    }

    public void clear() {
        clearID();
        Arrays.fill(getDataRef(), Double.NaN);
        tmpErr = 0;
        tmpDistance = Float.NaN;
    }

    private void clearID() {
        id = ID.getAndIncrement();
    }

    @Override
    public boolean equals(Object other) {
        return this == other;
        //return (this == other) || ((Centroid) other)._id == _id;
    }

    @Override
    public int hashCode() {
        return (id + 1) * 37;
    }

    /**
     * create a node from two existing nodes
     */
    public void mix(Centroid A, Centroid B) {
        clearID();

        assert (A != B);

        double[] a = A.getDataRef();
        double[] b = B.getDataRef();
        double[] ab = getDataRef();
        int n = ab.length;
        double aToB = B.tmpErr / (A.tmpErr + B.tmpErr);

        //assertFinite(aToB);
        if (!Double.isFinite(aToB))
            aToB = 0.5;

        for (int i = 0; i < n; i++)
            ab[i] = Util.lerp(aToB, a[i], b[i]);

        setTmpErr(
            //Util.lerp(aToB, A.tmpErr, B.tmpErr)
            0
        );
    }

    public Centroid randomizeUniform(int dim, double min, double max) {
        setEntry(dim, Math.random() * (max - min) + min);
        return this;
    }

    public Centroid randomizeUniform(double min, double max) {
        int dim = getDimension();
        for (int i = 0; i < dim; i++) {
            setEntry(i, Math.random() * (max - min) + min);
        }
        return this;
    }

    public double localError() {
        return tmpErr;
    }

    private Centroid setTmpErr(double localError) {
        assertFinite(localError);
        this.tmpErr = localError;
        return this;
    }

    public void mulLocalError(double alpha) {
        setTmpErr(this.tmpErr * alpha);
    }


//    public static double distanceCartesianSq(double[] x, double[] y) {
//        int l = y.length;
//        return IntStream.range(0, l).mapToDouble(i -> y[i] - x[i]).map(d -> d * d).sum();
//    }


    /**
     * 0 < rate < 1.0
     * TODO move to Util
     */
    public void lerp(double[] x, double rate) {
        double[] d = getDataRef();
        double antirate = 1 - rate;
        int n = d.length;
        for (int i = 0; i < n; i++)
            d[i] = (antirate * d[i]) + (rate * x[i]);
    }

    public void add(double[] x) {
        double[] d = getDataRef();
        int n = d.length;
        for (int i = 0; i < n; i++)
            d[i] += x[i];
    }

    @Override
    public String toString() {
        return id + ": <" + n4(getDataRef()) + "> lErr=" + n4(tmpErr) + " dist=" + n4(tmpDistance());
    }

    /** tests the first dimension value if not NaN */
    public boolean active() {
        double v = getEntry(0);
        return v == v;
    }

    public double learn(double[] x, DistanceFunction dist) {
        double[] d = getDataRef();
        if (d[0]!=d[0]) {
            //inactive, assign the value as-is
            System.arraycopy(x, 0, d, 0, x.length);
            return this.tmpDistance = 0;
        } else {
            return (this.tmpDistance = dist.distance(d, x));
        }
    }


    public double tmpDistance() {
        return tmpDistance;
    }

    /*** move the centroid towards the point being learned, at the given rate */
    public void updateLocalError(double[] x, double winnerUpdateRate) {
        double e = localError();
        setTmpErr((e ==Float.POSITIVE_INFINITY ? 0 : e) + tmpDistance());
        lerp(x, winnerUpdateRate);
    }

}