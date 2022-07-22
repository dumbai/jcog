package jcog.rl;

import jcog.predict.Predictor;
import jcog.signal.FloatRange;

import java.util.Random;

public abstract class PredictorPolicy implements Policy {
    public final Predictor p;

    /** "alpha" learning rate */
    public final FloatRange learn = new FloatRange(
        0.01f
        //1.0E-3f
        //1.0E-4f
        //0.005f
    , 0, 1);

    protected PredictorPolicy(Predictor p) {
        this.p = p;
    }

    @Override
    public void clear(Random rng) {
        p.clear(rng);

//        double[] x = new double[((MLP)p).ins()];
//        double[] y = new double[((MLP)p).outs()];
//        Arrays.fill(y, 0.5f);
//
//        int wipe = 10000;
//        for (int i = 0; i < wipe; i++) {
//            for (int j = 0; j < x.length; j++)
//                x[j] = rng.nextFloat();
//
//            p.put(x, y, learn.floatValue());
//        }
    }

    public final double[] predict(double[] input) {
        return p.get(input);
    }
}