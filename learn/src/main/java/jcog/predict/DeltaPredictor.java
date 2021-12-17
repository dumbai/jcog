package jcog.predict;

import jcog.Util;
import jcog.util.ArrayUtil;

/** predictor which can be trained by gradients (deltas) */
public abstract class DeltaPredictor implements Predictor {

    /** current error vector */
    @Deprecated private transient double[] d = ArrayUtil.EMPTY_DOUBLE_ARRAY;

    /** running total of deltas */
    public double deltaSum = 0;

    private static double[] _delta(double[] yPredict, double[] y, double[] d) {

        int n = yPredict.length;
        assert(y.length == n);

        if (d.length!=n)
            d = new double[n];

        for (int i = 0; i < n; i++)
            d[i] = y[i] - yPredict[i];

        return d;
    }

    /** returns the estimate for the input, prior to training.
     *  uses local error vector so must be synchronized.
     */
    @Override public /*synchronized*/ double[] put(double[] x, double[] y, float pri) {
        double[] yPredict = get(x);

        this.d = _delta(yPredict, y, this.d);

        deltaSum += Util.sumAbs(d);

        putDelta(d, pri);

        return yPredict;
    }


    /** TODO this may need to accumulate deltas, not weight deltas */
    public abstract void putDelta(double[] d, float pri);


//    /** https://datascience.stackexchange.com/questions/5863/where-does-the-sum-of-squared-errors-function-in-neural-networks-come-from */
//    public final double errorSquared() {
//        synchronized(this) {
//            double err = 0;
//            for (double e : d)
//                err += e * e;
//            return err;
//        }
//    }

//    @Deprecated public final double errorAbs() {
//        synchronized(this) {
//            double err = 0;
//            for (double dd : this.d)
//                err += Math.abs(dd);
//            return err;
//        }
//    }

//    public /* final */ Predictor adapt(DoubleToDoubleFunction i, DoubleToDoubleFunction o) {
//        return new Predictor() {
//            private double[] I(double[] x) { return Util.arrayOf(n -> i.applyAsDouble(x[n]), x); }
//            private double[] O(double[] y) { return Util.arrayOf(n -> o.applyAsDouble(y[n]), y); }
//
//            @Override
//            public void clearDeltas() {
//                Predictor.this.clearDeltas();
//            }
//
//            @Override
//            public void putDelta(double[] d, float pri) {
//                Predictor.this.putDelta(d, pri);
//            }
//
//            @Override
//            public double[] get(double[] x) {
//                return O(Predictor.this.get(I(x)));
//            }
//
//            @Override
//            public Predictor randomize(Random rng) {
//                return Predictor.this.randomize(rng);
//            }
//        };
//    }

}