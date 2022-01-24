package jcog.ql.dqn;

import jcog.Util;
import jcog.data.list.Lst;
import jcog.predict.Predictor;
import org.eclipse.collections.api.block.function.primitive.IntIntToObjectFunction;

import java.util.List;
import java.util.Random;

import static jcog.Util.unitizeSafe;

public class QPolicyBranched implements Policy {

    final int inputs, actions;
    public final List<PredictorPolicy> predictors;

    final int actionDiscretization = 2;

    protected QPolicyBranched(int inputs, int actions, IntIntToObjectFunction<Predictor> p) {
        this.inputs = inputs;
        this.actions = actions;
        this.predictors = new Lst<>( Util.arrayOf(z -> new QPolicy(p.value(inputs, actionDiscretization)), new PredictorPolicy[actions]));
    }

    @Override
    public void clear(Random rng) {
        for (PredictorPolicy p : this.predictors)
            p.clear(rng);
    }

    @Override
    public double[] learn(double[] xPrev, double[] actionPrev, double reward, double[] x, float pri) {
        double[] z = new double[actions];
        for (int a = 0; a < actions; a++)
            z[a] = dac(predictors.get(a).learn(xPrev, adc(actionPrev[a]), reward, x, pri));

        return z;
    }

    @Override
    public double[] predict(double[] input) {
        double[] z = new double[actions];
        for (int a = 0; a < actions; a++)
            z[a] = dac(predictors.get(a).predict(input));

        return z;
    }

    private static double dac(double[] y) {
        assert(y.length==2);
        double y01 = unitizeSafe(y[0]) + unitizeSafe(y[1]);
        return y01 > Float.MIN_NORMAL ? y[1]/y01 : 0.5f;
    }

    /** TODO refine */
    private static double[] adc(double x) {
        if (x < 0.5f) return new double[] { 1, 0 }; else return new double[] { 0, 1 };

//        double[] y = new double[2];
//        y[1] = x;
//        y[0] = 1-x;
//        return y;
    }

}