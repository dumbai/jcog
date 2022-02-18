package jcog.nn;

import jcog.Fuzzy;
import jcog.Util;
import jcog.random.RandomBits;
import jcog.random.XoRoShiRo128PlusRandom;

import java.util.Arrays;
import java.util.Random;
import java.util.function.IntToDoubleFunction;

/**
 * "Freeform" (possibly-)Recurrent Network
 * https://github.com/jeffheaton/encog-java-core/blob/master/src/main/java/org/encog/neural/freeform/training/FreeformPropagationTraining.java
 */
public class BackpropRecurrentNetwork extends RecurrentNetwork {

    /**
     * characterizes the effective "dynamic range" of activation values
     */
    static final float wClamp =
        //1;
        //8;
        //.. * initWeightRange;
        //4;
        //16;
        //64;
        //128;
        //1024;
        16 * 1024;

    /**
     * weight gradient clamp
     */
    private double dwClamp =
            Double.POSITIVE_INFINITY;
            //1;
            //initWeightRange*2;
            //initWeightRange;
            //initWeightRange/2;
            //2;
            //1;
            //wClamp / 8f /* steps */;


    /** -1 for auto */
    final int iterationsBackward =
            -1;
    //0.9f;
    final Random rng = new XoRoShiRo128PlusRandom();
    final RandomBits RNG = new RandomBits(rng);
    //    public SGDLayer.SGDWeightUpdater weightUpdater;
    public float momentum = 0f;

    float mutationRate = 0.0005f;

    /** eraseRate should be ~<= mutationRate which can recreate links. otherwise it is gradually neurodegenerative */
    float eraseRate = mutationRate;

    public final double[][] deltaReverse;
    //transient double[][] deltaFwd = null;
    transient double[] dW, dWprev;
    transient double[] dA;


    //    public static void randomizeHe(RealMatrix W, int inputs, Random r) {
//        double sigma =
//                //1.0 / Fuzzy.meanGeo((float)a,b);
//                //2.0 / Fuzzy.mean((float)a,b);
//                //2.0/(a*b);
//                1.0 / inputs;
//        //2.0/(a*b);
//        //(2.0 / (a + b));
//        //2.0 / a;
//
//        boolean noSelfConnections = true;
//        Gaussian g = new Gaussian(0, sigma);
//        final int rows = W.getRowDimension();
//        final int cols = W.getColumnDimension();
//        for (int from = 0; from < rows; from++) {
//            for (int to = 0; to < cols; to++) {
//
//                if (noSelfConnections && from == to)
//                    continue;
//
//                double u = (r.nextFloat() - 0.5) * 2;
//                boolean neg = (u < 0);
//                if (neg) u = -u;
//                double xy = g.value(u);
//                if (neg) xy = -xy;
//
//                W.setEntry(from, to, xy);
//            }
//        }
//    }
    //@Deprecated private transient MetalBitSet visited = null;
    private double weightDecay =
            //1.0E-2;
            1.0E-1;
            //0;

    public BackpropRecurrentNetwork(int inputs, int outputs, int hiddens, int iterations) {
        super(inputs, outputs, hiddens, iterations);

        int n = n();
        this.deltaReverse = new double[iterationsBackward()][n];
    }

//    public BackpropRecurrentLayer(int inputSize, int outputSize, DiffableFunction activation, boolean bias) {
//        super(inputSize, outputSize, activation);
//    }


    /**
     * adds noise
     */
    public void mutateWeights(float _mutationRate) {
        float mutationStrength = 0.1f;

        float mutationRate = _mutationRate * weightsActive();

        int m = RNG.floor(mutationRate);
        int maxTries = m * 16;
        int changed = 0;

        int n = n();
        for (int i = 0; changed < m && i < maxTries; i++) {

            int f = RNG.nextInt(n);
            if (isBias(f)) continue;

            int t = inputsConstant ? (inputs + 1 + RNG.nextInt(hiddens + outputs)) : RNG.nextInt(n);

            if (!isBias(t) && (selfConnections || f != t)) {
                int F = neuronClass(f), T = neuronClass(t);
                float FT = connect[F][T];
                if (FT > 0 && RNG.nextBoolean(FT)) {
                    weights.weightAdd(f, t, mutationStrength * initWeightRange * Fuzzy.polarize(RNG.nextDouble()));
                    changed++;
                }
            }

        }

    }

    public void eraseWeights(float _eraseRate) {
        float eraseRate = _eraseRate * weightsActive();
        int e = RNG.floor(eraseRate);
        int maxTries = e * 8;
        int changed = 0;

        int n = n();
        for (int i = 0; changed < e && i < maxTries; i++) {

            int f = RNG.nextInt(n); if (isBias(f)) continue;
            int t = RNG.nextInt(n); if (isBias(t)) continue;
            if (selfConnections || f != t) {
                double w = weights.weight(f, t);
                if (w != 0) {
                    //prune weaker weights
                    if (RNG.nextBoolean((float) (1 / (1 + 3 * Math.abs(w))))) {
                        weights.weightSet(f, t, 0);
                        changed++;
                    }
                }
            }

        }
    }


    /**
     * TODO subtract corner if outputsTerminal
     */
    private int weightsActive() {
        //return Util.sqr(state.n());
        int n = n();
        int a = inputsConstant ? n * (n - inputs) : n * n;

        if (!selfConnections)
            a -= n;

        return a;
    }

    private double dwClamp(double dw) {
        return Util.clampSafe(dw, -dwClamp, +dwClamp);
    }

    private void updateNeuronGradient(int f, double[] delta, double[] deltaNext) {
        double s = weights.dotColumn(f, delta);

        deltaNext[f] = s != 0 ?
                s * deriv(f)
                :
                0;
    }


    private double deriv(int neuron) {
        return activationFn(neuron).derivative(value(neuron));
    }

    @Override
    public void putDelta(double[] dx, float pri) {
        int n = n();

        int iterationsBackward = iterationsBackward();
        //int iterationsPredictive = iterationsForward;

        if (dA == null) {
            this.dA = new double[n];
//            this.deltaReverse = new double[iterationsBackward][n];
            //this.deltaFwd = new double[iterationsPredictive][n];

            int numWeights = weights.weights();
            this.dW = new double[numWeights];
            this.dWprev = new double[numWeights];

        }


        boolean outputTerminal = outputsTerminal;


        //iterative backpropagation through time, or something similar
        double[] delta = outputDelta(dx, this.dA);

        updateNeuronGradientsSeparate(iterationsBackward, delta);
        //updateNeuronGradientsAccumulate(iterationsBackward, delta);

        //System.out.println(n4(delta) + "\n" + n4(deltaReverse[0]) + "\n" + n4(deltaReverse[1]) + "\n");


        for (int t = ts(); t < n; t++) {
            if (!isBias(t))
                updateIncomingWeightGradient(t, delta, dW);
        }

        double deltaL1 = Util.sumAbs(delta)/delta.length;
        float priDelta = (float) (pri * deltaL1);

        updateWeights(/*priDelta*/ pri, momentum,

                pri * BackpropRecurrentNetwork.this.weightDecay * deltaL1
//                BackpropRecurrentNetwork.this.weightDecay * pri
                );

        mutateWeights(mutationRate * priDelta);
        eraseWeights(eraseRate * priDelta);
    }

    private int iterationsBackward() {
        return BackpropRecurrentNetwork.this.iterationsBackward < 0 ? iterationsForward : BackpropRecurrentNetwork.this.iterationsBackward;
    }

    private int ts() {
        return inputsConstant ? inputs + 1 /* non-input, non-biases */ : 0;
    }

    private int fe() {
        return n() - (outputsTerminal ? outputs : 0);
    }

    private void updateNeuronGradientsSeparate(int iterationsBackward, double[] delta) {
        int fe = fe(), ts = ts();
        int n = n();
        for (int i = 0; i < iterationsBackward; i++) {

            double[] dn = deltaReverse[i];
            Arrays.fill(dn, 0);

            double[] dp = i == 0 ? delta : deltaReverse[i - 1];

            for (int f = 0; f < fe; f++)
                updateNeuronGradient(f, dp, dn);

        }

        //mergeDeltaMax(iterationsBackward, delta);
        mergeDeltaPlus(iterationsBackward, delta,
                //i -> 1 - i/((float)(iterationsBackward))
                i -> Math.pow(Util.PHI_min_1, iterationsBackward)
                //i -> Math.pow(1 - i/((float)(iterationsBackward)), 1.5)
                //i -> 1
                //i -> i/((float)(iterationsBackward)) //BAD
                //i -> Math.pow(Util.PHI_min_1, i)
        );
    }

//    private void updateNeuronGradientsAccumulate(int iterationsBackward, double[] delta) {
//        int fe = fe();
//
//        int n = delta.length;
//
//        double[] deltaNext = new double[n];
//
//        for (int i = 0; i < iterationsBackward; i++) {
//
//            for (int f = 0; f < fe; f++)
//                updateNeuronGradient(f, delta, deltaNext);
//
//            for (int f = 0; f < fe; f++)
//                delta[f] += deltaNext[f];
//        }
//
//        //Util.mul(1f / iterationsBackward, delta);
//
//    }


    private void mergeDeltaMax(int iterationsBackward, double[] delta) {
        int n = n();
        for (int i = 0; i < iterationsBackward; i++) {
            double[] di = deltaReverse[i];
            for (int f = 0; f < n; f++) {
                double diF = di[f];
                double diAbs = Math.abs(diF);
                double dfAbs = Math.abs(delta[f]);
                if (diAbs > dfAbs)
                    delta[f] = diF;
            }
        }
    }
    /** pri defines an envelope that determines how the
     *  network will 'stretch' itself in computation time */
    private void mergeDeltaPlus(int iterationsBackward, double[] delta, IntToDoubleFunction pri) {
        int n = n();
        double pSum = 0;
        for (int i = 0; i < iterationsBackward; i++) {
            double[] di = deltaReverse[i];
            double p = pri.applyAsDouble(i);
            for (int f = 0; f < n; f++)
                delta[f] += di[f] * p; //TODO fma

            pSum += p;
        }
        for (int i = 0; i < n; i++)
            delta[i] /= pSum;
    }

    private double[] outputDelta(double[] dx, double[] delta) {

        Arrays.fill(delta, 0);

        for (int i = 0; i < outputs; i++) {
            int neuron = outputNeuron(i);
            delta[neuron] = deriv(neuron) * dx[i];
        }

        return delta;
    }

    //        /** TODO untested, not working right (derivatives computed twice, so something is redundant) */
//        private void updateWeights(float pri) {
//            //HACK copy from state
//            final int n = state.weights.getRowDimension();
//            int k = 0;
//            for (int from = 0; from < n; from++)
//                for (int to = 0; to < n; to++)
//                    this.W[k++] = state.weight(from, to);
//
//            weightUpdater.reset(n*n, pri);
//            weightUpdater.update(this, delta);
//
//            k = 0;
//            for (int from = 0; from < n; from++)
//                for (int to = 0; to < n; to++)
//                    state.weightSet(from, to, this.W[k++]);
//
//        }

    private void updateIncomingWeightGradient(int toNeuron, double[] delta, double[] dW) {
        int n = n();
        double dx = delta[toNeuron];
        if (dx != 0) {
            for (int fromNeuron = 0; fromNeuron < n; fromNeuron++) {
                int fromConnection = fromNeuron + toNeuron * n;
                dW[fromConnection] = dx * value(fromNeuron); //TODO compute from state at the given iteration.  not this which would be the final state
            }
        }
    }


    /**
     * SGD original impl
     */
    private void updateWeights(float pri, double momentum, double weightDecayRate) {


        int n = n();

        double wL1 = weights.weightL1();

        double[] dW = this.dW;
        double[] dWprev = this.dWprev;


        boolean decaying = weightDecayRate!=0;
        boolean dwClamping = dwClamp!=Double.POSITIVE_INFINITY;

        int ft = 0;
        for (int t = 0; t < n; t++) {
            for (int f = 0; f < n; f++, ft++) {

                double dw = Util.lerpSafe(momentum, dW[ft], dWprev[ft]);

                if (dwClamping) dw = dwClamp(dw);

                dWprev[ft] = dw;

                final double wPrev = weights.weight(f, t);

                double wPrevDecayed;
                if (wPrev!=0 && decaying) {
                    double decay = (1 - weightDecayRate * Math.abs(wPrev) / (1.0E-8 + wL1));
                    wPrevDecayed = wPrev * decay;
                } else
                    wPrevDecayed = wPrev;

                double wNext =
                        // Util.lerpSafe(pri, wPrev, dw + wPrevDecayed)
                        wPrevDecayed + pri * dw;
                        //Util.fma(pri, dw, wPrev)
                            //* (1 - Math.abs(wPrev) / (1.0E-8 + wL1) * weightDecayRate)
                            //- (wPrev / (1.0E-8 + wL1) * weightDecayRate)
                        ;

                if (wPrev != wNext)
                    weights.weightSet(f, t, wNext);
            }
        }
    }

}