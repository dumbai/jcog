package jcog.nn.layer;

import jcog.Fuzzy;
import jcog.activation.DiffableFunction;
import jcog.data.bit.MetalBitSet;
import jcog.nn.optimizer.WeightUpdater;
import org.hipparchus.analysis.function.Gaussian;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

import static jcog.Util.fma;

public class DenseLayer extends AbstractLayer {


    private static final float DROPOUT_DEFAULT =
            0;
            //0.01f;
            //0.05f;
            //0.1f;
            //0.5f;
            //0.2f;
            //0.1f;
            //0;

    public final double[] delta;
    public final double[] W;

    /**
     * each optimizer impl should manage this
     */
    @Deprecated public final double[] dW;
    @Deprecated public final double[] dWPrev;


    /**
     * per weight, holds enabled / disabled state for dropout
     */
    public final MetalBitSet enabled;
    public final DiffableFunction activation;
    private final boolean bias;
    /**
     * https://jmlr.org/papers/volume15/srivastava14a/srivastava14a.pdf
     */
    public float dropout;

    public boolean enabledAll;


    public DenseLayer(int inputSize, int outputSize, DiffableFunction activation, boolean bias) {
        super(inputSize + (bias ? 1 : 0), outputSize);
        if (inputSize < 1)
            throw new UnsupportedOperationException();
        if (outputSize < 1)
            throw new UnsupportedOperationException();
        this.bias = bias;
        delta = new double[in.length];
        W = new double[in.length * outputSize];
        dW = new double[W.length];
        dWPrev = new double[W.length];
        enabled = MetalBitSet.bits(W.length);
        dropout = DROPOUT_DEFAULT > 0 &&
                inputSize > (1 / (DROPOUT_DEFAULT)) ?
                DROPOUT_DEFAULT : 0;
        this.activation = activation;
        startNext();
    }

    //    /**
//     * gradient post-processing
//     * https://neptune.ai/blog/understanding-gradient-clipping-and-how-it-can-fix-exploding-gradients-problem
//     */
//    public static double gradClamp(double x) {
//        return clampSafe(x, -1, +1);
//        //return x;
//    }

    /**
     * https://machinelearningmastery.com/weight-initialization-for-deep-learning-neural-networks/
     * https://intoli.com/blog/neural-network-initialization/
     */
    @Override
    public void randomize(Random r) {
        //randomizeXavier(r);
        randomizeHe(r);
    }

    private void randomizeXavier(Random r) {
        int n = ins() * outs();
        double factor = Math.sqrt(n);
        for (int i = 0; i < W.length; i++) {
            W[i] = ((r.nextFloat() - 0.5f) * 2) / factor;
        }
    }

    private void randomizeHe(Random r) {
        float a =
            //in.length*2;
            in.length;
            //in.length/2f;
            //Util.sqrt(in.length);
            //in.length + out.length;
        randomizeHe(this.W, a, r);
    }

    public static void randomizeHe(double[] W, float n, Random r) {
        double sigma =
                //1.0 / Fuzzy.meanGeo((float)a,b);
                //2.0 / Fuzzy.mean((float)a,b);
                //2.0/(a*b);
                1.0 / n;
        //2.0/(a*b);
        //(2.0 / (a + b));
        //2.0 / a;
        Gaussian g = new Gaussian(0, sigma);
        for (int i = 0; i < W.length; i++) {
            double u = Fuzzy.polarize(r.nextDouble());
            boolean neg = (u < 0);
            if (neg) u = -u;
            double y = g.value(u);
            if (neg) y = -y;

            W[i] = y;
        }
    }

    /**
     * forward prop
     */
    @Override
    public double[] forward(double[] x, RandomGenerator rng) {
        double[] in = this.in, W = this.W, out = this.out;
        System.arraycopy(x, 0, in, 0, x.length);
        int I = in.length;

        if (bias) in[I - 1] = 1;


        int O = out.length;
        int io = 0;
//        int biasInput = bias ? I - 1 : -1;

        float dropIn = 1 - dropout;

        int n = W.length;
        DiffableFunction a = this.activation;

        for (int o = 0; o < O; o++) {
            double y = 0;
            for (int i = 0; i < I; i++, io++) {

                double ii = in[i];
                if (
                        ii == ii //not NaN
                    //Double.isFinite(ii)
                )
                    y = fma(ii * dropIn, W[io], y);

            }

            out[o] = a!=null ? a.valueOf(y) : y /* linear */;
        }
        return out;
    }

    @Override
    public double[] delta(WeightUpdater updater, double[] dx) {
        updater.update(this, dx);
        return delta;
    }

    public void startNext() {
        updateDropout();
        Arrays.fill(delta, 0);
    }

    private void updateDropout() {
        if (!enabledAll)
            enabled.setAll(true);

        boolean enabledAll = true;
        float dropout = this.dropout;
        if (dropout <= Float.MIN_NORMAL) return;

        float dropIn = 1 - dropout;

        int n = W.length;
        Random rng = ThreadLocalRandom.current(); //HACK
        final int maxSkip = Math.max(1, Math.round(n * dropout));
        int nextDropOut = rng.nextInt(maxSkip*2);

        for (int io = nextDropOut; io < n; ) {
            enabled.set(io, false);
            enabledAll = false;
            io += rng.nextInt(maxSkip*2);
        }
        this.enabledAll = enabledAll;

    }

}