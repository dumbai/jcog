package jcog.rl;

import jcog.Fuzzy;
import jcog.Util;
import jcog.activation.DiffableFunction;
import jcog.activation.LeakyReluActivation;
import jcog.activation.SigLinearActivation;
import jcog.activation.SigmoidActivation;
import jcog.agent.Agent;
import jcog.data.list.Lst;
import jcog.nn.BackpropRecurrentNetwork;
import jcog.nn.MLP;
import jcog.nn.layer.DenseLayer;
import jcog.nn.optimizer.SGDOptimizer;
import jcog.predict.DeltaPredictor;
import jcog.predict.LivePredictor;
import jcog.predict.Predictor;
import jcog.random.RandomBits;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.rl.dqn.DirectPolicy;
import jcog.rl.dqn.QPolicy;
import jcog.rl.dqn.QPolicySimul;
import jcog.rl.misc.PopulationPolicy;
import jcog.rl.replay.Replay;
import jcog.rl.replay.ReplayMemory;
import jcog.rl.replay.SimpleReplay;
import jcog.signal.FloatRange;
import org.eclipse.collections.api.block.function.primitive.IntIntToObjectFunction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static jcog.Util.sumAbs;

/** TODO rename: PolicyAgent */
public class PolicyAgent extends Agent {


    public final Policy policy;
    public final Random rng = new XoRoShiRo128PlusRandom();
    public final RandomBits RNG = new RandomBits(rng);

    /** "epsilon" curiosity/exploration parameter.
     * note: this is in addition to curiosity which is applied in AbstractGoalConcept */
    public final FloatRange explore = new FloatRange(0.0f, 0, 1);


    /**
     * "surprise": last iteration's learning loss
     */
    public double errMean, errMin, errMax;

    /** TODO move to subclass */
    @Deprecated Replay replay;

    private transient double[] iPrev;

    public PolicyAgent(int numInputs, int numActions, IntIntToObjectFunction<? extends Policy> policy) {
        this(numInputs, numActions, policy.value(numInputs, numActions));
    }

    public PolicyAgent(int numInputs, int numActions, Policy policy) {
        super(numInputs, numActions);
        this.policy = policy;
        this.policy.clear(rng);
    }


    public static Agent DQN(int inputs, int actions) {
        int deep =
            0;
            //1;
            //2;
            //3;
        return DQN(inputs, false, actions,
                deep,
               /*2*/ /*Util.PHI_min_1f*/ 12, 64);
    }

    public static Agent DQNbig(int inputs, int actions) {
        return DQN(inputs, false, actions, 0,
                8, 16);
    }

    public static Agent DQNae(int inputs, int actions) {
        return DQN(inputs, true, actions, 0,
                0.75f, 7);
    }


    public static PolicyAgent CMAES(int inputs, int actions) {
        return new PolicyAgent(inputs, actions, new PopulationPolicy(
            new PopulationPolicy.CMAESPopulation())
        );
    }

    public static PolicyAgent DQN(int inputs, boolean inputAE, int actions, int deep, float brainsScale, int replays) {
        float dropOut =
            0;
            //0.1f;
            //0.5f;
            //0.75f;
            //0.9f;

        int brains = (int) Math.ceil(brainsScale *
            Fuzzy.mean((float)inputs, actions)
            //inputs
            //actions
        );

        IntIntToObjectFunction<Predictor> brain = (ii, oo) ->
            mlpBrain(ii, oo, brains, deep, inputAE, dropOut);

        PolicyAgent a = new PolicyAgent(inputs, actions,
            (i, o) ->
                new QPolicySimul( i, o, brain)
                //new QPolicy(brain.value(i,o))
//                new QPolicyBranched(i, o,
//                          (ii, oo) -> mlpBrain(ii, oo, brains, precise, inputAE)
//                )
//                new A2C(i,o, o)
        );

        if (replays > 0)
            a.replay(
                new SimpleReplay(16 * 1024, 1 / 3f, replays)
                //new BagReplay(64*replays, replays)
            );

        return a;
    }

    private static MLP mlpBrain(int i, int o, int brains, int depth, boolean inputAE, float dropOut) {

        List<MLP.LayerBuilder> layers = new Lst<>(4);

        if (inputAE) {
//                layers.add(new MLP.AutoEncoderLayerBuilder(
//                                //Fuzzy.mean(i,o*4)
//                                i
//                                //Math.round(Util.lerp(0.33f, i, o))
//                        )
//                );
            layers.add(new MLP.AutoEncoderLayerBuilder(
                    //(int) Math.ceil(Util.sqrt(i))
                    //i*2
                    //i/3
                    i / 2
                    //Fuzzy.mean(i,o*4)
                    //i
                    //Math.round(Util.lerp(0.33f, i, o))
                )
            );
        }

        var hiddenActivation =
            //SigmoidActivation.the
            //ReluActivation.the
            LeakyReluActivation.the
            //SigLinearActivation.the
            //new LeakyReluActivation(0.1f)
            //EluActivation.the
            //new SigLinearActivation(4, -1, +1)
            //TanhActivation.the
        ;

        layers.add(new MLP.Dense(brains, hiddenActivation));

        if (depth > 0) {
            for (int p = 0; p < depth; p++) {
                int hidden = Util.lerpInt((p + 1) / (depth + 1f), brains, o);
                layers.add(new MLP.Dense(hidden, hiddenActivation));
            }

        }

        //action output
        layers.add(new MLP.Dense(o, dqnOutputActivation));

        //layers.add(new NormalizeLayer(o));

        MLP p = new MLP(i, layers).optimizer(
            new SGDOptimizer(0)
            //new SGDOptimizer(0).minibatches(3)
            //new SGDOptimizer(0.9f)
            //new SGDOptimizer(0.9f).minibatches(3)
            //new SGDOptimizer(0.99f).minibatches(128)
            //new SGDOptimizer(0.9f).minibatches(32)
            //new AdamOptimizer()
            //new AdamOptimizer().minibatches(3)
            //new AdamOptimizer().momentum(0.99, 0.99)
            //new RMSPropOptimizer()
        );

        if (dropOut > 0) {
            for (int l = 0; l < p.layers.length; l++)
                if (p.layers[l] instanceof DenseLayer D) D.dropout = dropOut;
        }

        p.clear();
        return p;
    }

    public static Agent DQrecurrent(int inputs, int actions, float brainsScale, int computeIters, int replayIters) {
        int brains = (int) Math.ceil(Fuzzy.mean(inputs, actions) * brainsScale);
        return new PolicyAgent(inputs, actions,
            (i, o) -> new QPolicySimul(i, o,
                (ii, oo) -> recurrentBrain(ii, oo, brains, computeIters)
            )
        ).replay(
            new SimpleReplay(16 * 1024, 1/3f, replayIters)
        );
    }

    private static final DiffableFunction dqnOutputActivation =
        SigmoidActivation.the;
        //SigLinearActivation.the;
        //LinearActivation.the;
        //new SigLinearActivation(2, -2, +2);
        //TanhActivation.the;

    private static BackpropRecurrentNetwork recurrentBrain(int inputs, int actions, int hidden, int iterations) {
        BackpropRecurrentNetwork b = new BackpropRecurrentNetwork(
                inputs, actions, hidden, iterations);
        //b.momentum = 0.9f;
        b.activationFn(
                //LeakyReluActivation.the,
                SigLinearActivation.the,
                dqnOutputActivation

                //new SigLinearActivation()
                //new SigLinearActivation(0, +10, 0, +1)
                //LinearActivation.the
                //new LeakyReluActivation(0.1f),
                //SinActivation.the,
                //ReluActivation.the
                //new SigLinearActivation()

        );
        return b;
    }


    public static Agent DQN_NTM(int inputs, int actions) {
        //return new DigitizedPredictAgent(2, inputs, actions,
        return new PolicyAgent(inputs, actions,
                (ii, oo) ->
                {
                    final LivePredictor.NTMPredictor p = new LivePredictor.NTMPredictor(ii, oo, 2, 2);
                    p.clear(ThreadLocalRandom.current());
                    return new QPolicy(p);
                }
        ).replay(new SimpleReplay(8 * 1024, 1 / 3f, 3));
    }

    public static Agent Random(int inputs, int actions) {
        return new Agent(inputs, actions) {

            final Random rng = new XoRoShiRo128PlusRandom();

            @Override
            protected void apply(double[] actionPrev, float reward, double[] input, double[] qNext) {
                for (int a = 0; a < qNext.length; a++)
                    qNext[a] = rng.nextFloat();
            }
        };
    }

//    public static Agent DQN_LSTM(int inputs, int actions) {
//        return new ValuePredictAgent(inputs, actions, (i, a) ->
//                new PredictorPolicy(new LSTM(i, a, 1)));
//    }


    public Agent replay(Replay r) {
        this.replay = r;
        return this;
    }

    /**
     * TODO parameter to choose individual, or batch
     */
    @Override
    public synchronized void apply(double[] actionPrev /* TODO */, float reward, double[] x, double[] actionNext) {

        double[] iPrev = this.iPrev;
        if (iPrev == null)
            iPrev = this.iPrev = x.clone();

        var e = new ReplayMemory(replay != null ? replay.t : 0, iPrev, actionPrev, reward, x);
        run(e, actionNext, 1);

        if (replay != null)
            replay.run(this, actionPrev, reward, x, iPrev, actionNext);

        System.arraycopy(x, 0, iPrev, 0, x.length);
    }



    /** @return dq[] */
    public synchronized double[] run(ReplayMemory e, @Nullable double[] action, float alpha) {

        @Nullable DeltaPredictor p = (DeltaPredictor) ((policy instanceof DirectPolicy) ? ((DirectPolicy) policy).p :
                (policy instanceof QPolicy ? ((QPolicy) policy).p : null));

        double errBefore = p!=null ? p.deltaSum : Double.NaN;

        double[] actionNext = e.learn(policy, alpha);

        if (action == null) return null;

        System.arraycopy(actionNext, 0, action, 0, actionNext.length);
        return runDQ(action, p, errBefore, actionNext);
    }

    /** computes delta and err for some impl's */
    @Deprecated @Nullable private double[] runDQ(@Nullable double[] action, @Nullable DeltaPredictor p, double errBefore, double[] actionNext) {
        double[] dq = null;

        if (policy instanceof QPolicy) {
            dq = ((QPolicy) policy).dq;
            err(dq);
        } else if (policy instanceof QPolicySimul Q) {
            dq = Q.q.dq;
            err(dq);
        } else if (p !=null && policy instanceof DirectPolicy) {
            double errAfter = p.deltaSum;
            double err = errAfter - errBefore;
            errMean = err;
            dq = null;
        }
        return dq;
    }

    @Override
    protected void actionFilter(double[] actionNext) {
        float e = explore.floatValue();
        if (e <= Float.MIN_NORMAL) return;

        for (int i = 0; i < actionNext.length; i++) {
            if (RNG.nextBoolean(e))
                actionNext[i] = RNG.nextFloat();

        }
    }

    protected void err(double[] qd) {
        double errTotal = sumAbs(qd);
        errMean = errTotal / qd.length;
        double errMin = Double.POSITIVE_INFINITY, errMax = Double.NEGATIVE_INFINITY;
        for (int i = 0, qdLength = qd.length; i < qdLength; i++) {
            double x = qd[i];
            double xAbs = Math.abs(x);
            errMin = Math.min(errMin, xAbs);
            errMax = Math.max(errMax, xAbs);
        }
        this.errMin = errMin;
        this.errMax = errMax;
    }

    public static Agent DQrecurrent(int i, int o) {
        return DQrecurrent(i, o,
                //8, 4
                4, 4, 16
                //0.1f, 4
                //0.25f, 6
                //0.25f, 8
        );
    }


//    @Deprecated public static class DigitizedPredictAgent extends ValuePredictAgent {
//
//        float actionContrast =
//            1;
//            //2;
//
//        private final int actionDigitization;
//
//        public DigitizedPredictAgent(int actionDigitization, int numInputs, int numActions, IntIntToObjectFunction<Predictor> p) {
//            super(numInputs, numActions, (int i, int o)->
//                    new PredictorPolicy(p.value(i,o*actionDigitization)));
//            assert (actionDigitization > 1);
//            this.actionDigitization = actionDigitization;
//        }
//
//        @Override
//        public synchronized void apply(double[] action, float reward, double[] input, double[] qNext) {
////            System.out.println(Str.n2(action));
//            final double[] aa = split(action);
////            System.out.println(Str.n2(aa));
//            double[] qNextTmp = new double[qNext.length * actionDigitization];
//            super.apply(aa, reward, input, qNextTmp);
//
//            join(qNextTmp, qNext);
////            joinSoftmax(actionNextTmp, qNext);
//
////            System.out.println(Str.n2(actionNextTmp));
////            System.out.println(Str.n2(qNext));
////            System.out.println();
//        }
//
//        public double[] split(double[] x) {
//            double[] y = new double[x.length * actionDigitization];
//            for (int i = 0, j = 0; i < x.length; i++) {
//                double I = x[i];
//
//                assertUnitized(I);
//
//                Digitize digitizer =
//                    Digitize.FuzzyNeedle;
//                    //Digitize.BinaryNeedle;
//                float ii = (float) I;
//                for (int d = 0; d < actionDigitization; d++)
//                    y[j + d] = digitizer.digit(ii, d, actionDigitization);
//
//                //contrast exponent curve
//                if (actionContrast!=1) {
//                    for (int d = 0; d < actionDigitization; d++)
//                        y[j + d] = Math.pow(y[j + d], actionContrast);
//                }
//
//                //make each action's components sum to 1
//                Util.normalize(y, j, j + actionDigitization, 0, Util.max(j, j+actionDigitization, y));
//
//                j += actionDigitization;
//            }
//            return y;
//        }
//
//        //final static double thresh = Math.pow(Float.MIN_NORMAL, 4);
//
//        public double[] join(double[] y, double[] tgt) {
//            for (int i = 0, k = 0; k < tgt.length; ) {
//                tgt[k++] = undigitizeWeightedMean(y, i);
//                i += actionDigitization;
//            }
//            return tgt;
//        }
//
//        /**
//         * digital -> analog
//         */
//        protected double undigitizeWeightedMean(double[] y, int i) {
//            double x = 0, sum = 0;
//            for (int d = 0; d < actionDigitization; d++) {
//                double D = y[i + d];
//                //D = Fuzzy.unpolarize(D);
//                //D = Util.unitize(D);
//                if (D > 1)
//                    Util.nop();
//
//                D = Math.max(0, D);
//
//                //D = Math.max(0, D)/max;
//                //D = Util.normalize(D, min, max);
//                float value = ((float) d) / (actionDigitization - 1);
//                x += value * D;
//                sum += D;
//            }
//            if (sum > Float.MIN_NORMAL)
//                x /= sum;
//            else
//                x = 0.5f;
//            return x;
//        }
//
//        public double[] joinSoftmax(double[] y, double[] tgt) {
//            final DecideSoftmax decide = new DecideSoftmax(0.1f, rng);
//            for (int i = 0, k = 0; k < tgt.length; ) {
//                int index = decide.applyAsInt(Util.toFloat(y, i, i + actionDigitization));
//                tgt[k++] = ((float) index) / (actionDigitization - 1);
//                i += actionDigitization;
//            }
//            return tgt;
//        }
//
//    }


//    private static class NormalizeLayer implements MLP.LayerBuilder {
//        private final int o;
//
//        public NormalizeLayer(int i) {
//            this.o = i;
//        }
//        @Override
//        public int size() {
//            return o;
//        }
//        @Override
//        public AbstractLayer valueOf(int i) {
//            return new StatelessLayer(i, o) {
//
//                @Override
//                public double[] delta(SGDLayer.SGDWeightUpdater updater, double[] dx, float pri) {
//                    return dx;
//                }
//
//                @Override
//                public double[] forward(double[] x, RandomBits rng) {
//                    //return Util.normalize(x.clone());
//                    return Util.normalize(x.clone(), 0, Util.max(x));
//                }
//            };
//        }
//
//    }
//    private static class SplitPolarLayer implements MLP.LayerBuilder {
//        private final int o;
//
//        public SplitPolarLayer(int i) {
//            this.o = i*2;
//        }
//        @Override
//        public int size() {
//            return o;
//        }
//        @Override
//        public AbstractLayer valueOf(int i) {
//            return new StatelessLayer(i, o) {
//
//                @Override
//                public double[] delta(SGDLayer.SGDWeightUpdater updater, double[] dx, float pri) {
//                    //TODO
//                    return null;
//
////                    int n = dx.length;
////                    assert(n==o*2);
////                    double[] dy = new double[n/2];
////                    for (int i = 0, o = 0; i < n; i++) {
////                        double PN = dx[i];
////                        double P, N;
////                        if (PN >= 0) {
////                            P = +PN; N = 0;
////                        } else{
////                            N = -PN; P = 0;
////                        }
////                        dy[o++] = P; dy[o++] = N;
////                    }
////                    return dy;
//                }
//
//                @Override
//                public double[] forward(double[] x, RandomBits rng) {
//                    return PolarValuePredictAgent.split(x);
//                }
//            };
//        }
//
//    }
//
//    /** includes a prediction normalization step. which is not entirely necessary. not yet sure if helpful. probably unstable */
//    @Deprecated private static class PolarPredictorPolicy extends PredictorPolicy {
//        public PolarPredictorPolicy(Predictor p) {
//            super(p);
//        }
//
//        @Override
//        public double[] predict(double[] x) {
//            double[] y = super.predict(x);
//            for (int i = 0; i < y.length; i+=2) {
//                double p = y[i];
//                double n = y[i+1];
//                double s = p + n;
//                if (s > Float.MIN_NORMAL) {
//                    y[i] = p / s;
//                    y[i + 1] = n / s;
//                }
//            }
//            return y;
//        }
//    }
}