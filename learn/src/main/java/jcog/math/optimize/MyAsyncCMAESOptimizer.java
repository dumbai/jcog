package jcog.math.optimize;

import jcog.Util;
import org.hipparchus.linear.RealMatrix;
import org.jetbrains.annotations.Nullable;

abstract public class MyAsyncCMAESOptimizer extends MyCMAESOptimizer {
    @Nullable
    private transient FitEval e;
    @Nullable
    private transient RealMatrix arx, arz;

    public MyAsyncCMAESOptimizer(int maxIter, double stopFitness, int popSize, double[] sigma) {
        super(maxIter, stopFitness, popSize, sigma);
    }

    transient double[][] X = null;
    transient double[] penalty = null;

    public double[] best() {
        int best = Util.argmax(e.fitness);
        return X[best];
    }

    @Override
    protected boolean iterateEval(RealMatrix arx, RealMatrix arz, FitEval e) {
        if (X == null)
            X = new double[lambda][arx.getColumnDimension()];

        if (e.isRepairMode && penalty == null)
            penalty = new double[lambda];

        for (int k = 0; k < lambda; k++) {
            var point = arx.getColumn(k);

            double penalty;
            if (e.isRepairMode) {
                double[] repaired = e.repair(point);
                penalty = e.penalty(point, repaired);
                point = repaired;
            } else
                penalty = 0;

            this.X[k] = point;
            this.penalty[k] = penalty;
        }

        this.e = e;
        this.arx = arx;
        this.arz = arz;

        if (apply(X)) {
            return true;
        } else {
            this.e = null; this.arx = this.arz = null; //clear refs
            return false;
        }
    }

    /**
     * afterwards, it asynchronously call commit(y) with the results later. returns whether to continue, and if false it wont expect any following asynch commit call
     */
    abstract protected boolean apply(double[][] X);

    /**
     * for callee to provide batch answers asynchronously
     */
    public final synchronized void commit(double[] y) {

        for (int k = 0; k < lambda; k++)
            e.value[k] = valuePenalty(y[k], penalty!=null ? penalty[k] : null);

        e.iterateAfter(arx, arz);
    }

    @Override
    public double computeObjectiveValue(double[] params) {
        throw new UnsupportedOperationException();
    }
}
