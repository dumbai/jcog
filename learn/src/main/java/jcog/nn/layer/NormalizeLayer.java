package jcog.nn.layer;

import jcog.Util;
import jcog.nn.MLP;
import jcog.nn.optimizer.WeightUpdater;

import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * normalizes a layer per instance,
 * recording the normalization factor for backprop
 *
 * TODO normalization strategy parameter
 * TODO extend affine / linear layer:
 *         f(g(x)) = a * (g(x) - b)
 *    d/dx f(g(x)) = a * g'(x)
 */
public class NormalizeLayer implements MLP.LayerBuilder {
    private final int n;
    public NormalizeLayer(int n) {
        this.n = n;
    }

    @Override
    public int size() {
        return n;
    }

    @Override
    public AbstractLayer valueOf(int i) {
        return new AbstractLayer(i, i) {

            transient private double a = 1, b = 0;

            @Override
            public void randomize(Random r) {
            }

            @Override
            public double[] forward(double[] x, RandomGenerator rng) {
                double min = Util.min(x),
                        max = Util.max(x);
                        //sum = Util.sum(x);

                int n = x.length;
                double range = max - min;
                double a, b;
                if (range > Double.MIN_NORMAL) {
                    a = this.a = 1 / range;
                    //a = this.a = 1 / Util.sumAbs(x);
//                    double sum = 0;
//                    for (int i = 0; i < n; i++)
//                        sum += x[i] - min;
//                    a = this.a = 1 / sum;
                    b = this.b = -min;
                } else {
                    a = this.a = 1;
                    b = this.b = 0;
                }

                double[] y = x.clone();
                for (int i = 0; i < n; i++)
                    y[i] = a * (y[i] + b); //TODO fma?

                return y;
            }

            double[] delta;

            @Override
            public double[] delta(WeightUpdater updater, double[] dx) {
                int n = dx.length;
                if (delta == null || delta.length != n)
                    delta = new double[n];

                double[] delta = this.delta;

                double a = this.a;
                for (int i = 0; i < n; i++)
                    delta[i] = a * dx[i];

                return delta;
            }
        };
    }
}