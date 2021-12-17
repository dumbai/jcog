package jcog.nn.ntm.learn;

import jcog.nn.ntm.control.UVector;
import jcog.nn.ntm.control.Unit;
import org.hipparchus.analysis.function.Gaussian;

import java.util.Random;

public class RandomWeightInitializer implements IWeightUpdater {
    private final Random rng;

    public RandomWeightInitializer(Random rand) {
        rng = rand;
    }

    @Override
    public void reset() {
    }

    @Override
    public void update(Unit data) {
        data.value = update(new double[] { data.value })[0];
    }

    /** see: MLPLayer */
    @Deprecated @Override public void update(UVector data) {
        double[] W = data.value;
        update(W);
    }

    @Deprecated private double[] update(double[] W) {
        double a = Math.sqrt(W.length)*2; //HACK
        double sigma = Math.sqrt(1.0 / a); //https://intoli.com/blog/neural-network-initialization/

        Gaussian g = new Gaussian(0, sigma);
        for (int i = 0; i < W.length; i++) {
            double u = (rng.nextFloat() - 0.5) * 2;
            boolean neg = (u < 0);
            if (neg) u = -u;
            double y = g.value(u);
            if (neg) y = -y;

            W[i] = y;
        }
        return W;
    }

}