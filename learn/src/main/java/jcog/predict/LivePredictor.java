package jcog.predict;

import jcog.TODO;
import jcog.Util;
import jcog.math.FloatSupplier;
import jcog.math.optimize.MyCMAESOptimizer;
import jcog.nn.ntm.NTM;
import jcog.nn.ntm.learn.RMSPropWeightUpdater;
import org.eclipse.collections.api.block.function.primitive.IntIntToObjectFunction;
import org.eclipse.collections.api.block.function.primitive.LongToFloatFunction;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Random;

/**
 * NOT TESTED YET
 * http:
 * https:
 */
public class LivePredictor {

    public float learningRate = 0.02f;

    /** TODO use or rewrite with Tensor api */
    @Deprecated public interface Framer {

        /** computes training vector from current observations */
        double[] input(long now);

        /** projections, hypothetical */
        double[] input(long when, double[] hypotheticalInput);

        double[] outputs();

        /** clones / copies the state to a new or existing Framer instance */
        Framer copyTo(@Nullable Framer projecting);

        Predictor build(IntIntToObjectFunction<Predictor> model);

    }

    /*public static class Autoregression implements Framer {

    }*/

    public static class DenseFramer implements Framer {
        /** time -> value function, (ex: one per concept) */
        private final LongToFloatFunction[] ins;
        private final LongToFloatFunction[] outs;

        private final int history;
        private final FloatSupplier dur;

        /**
         * temporary buffers, re-used
         */
        private double[] past;
        private double[] present;

        public DenseFramer(LongToFloatFunction[] ins, int history, FloatSupplier dur, LongToFloatFunction[] outs) {
            this.ins = ins;
            this.outs = outs;
            this.history = history;
            this.dur = dur;
            past = new double[history * ins.length];
            present = new double[outs.length];
        }

        @Override
        public Predictor build(IntIntToObjectFunction<Predictor> model) {
            return model.value(past.length, present.length);
        }

        @Override
        public Framer copyTo(@Nullable Framer x) {
            if (x == null) {
                x = new DenseFramer(ins, history, null /*-1*/, outs);
                ((DenseFramer)x).past = past.clone();
                ((DenseFramer)x).present = present.clone();
            } else {
                System.arraycopy(past, 0, ((DenseFramer) x).past, 0, past.length);
                System.arraycopy(present, 0, ((DenseFramer) x).present, 0, present.length);
            }
            return x;
        }

        @Override
        public double[] input(long now) {

            loadPast(now);

            loadPresent(now);

            return past;
        }

        public void loadPresent(long now) {
            int k = 0;
            for (LongToFloatFunction o : outs)
                present[k++] = o.valueOf(now);
        }

        private void loadPast(long now) {
            int p = 0;
            int dur = Math.round(this.dur.asFloat());
            for (int t = 1; t <= history; t++) {
                now -= dur; //go further back in time

                for (LongToFloatFunction i : ins)
                    past[p++] = i.valueOf(now);
            }
        }

        @Override public double[] input(long when, double[] projectedInput) {
            assert(projectedInput.length == present.length);
            loadPast(when);
            System.arraycopy(projectedInput, 0, present, 0, projectedInput.length);
            return past;
        }

        @Override
        public double[] outputs() {
           return present;
        }

//        private double[] shift() {
//            if (past == null || past.length!=(history * ins.length)) {

//            } else {
//                    System.arraycopy(present, 0, present, outs.length, outs.length);
////                int stride = ins.length;
////                int all = past.length;
////                System.arraycopy(past, stride, past, 0, all - stride);
////                System.arraycopy(present, 0, past, stride, present.length);
//            }
//
//            return past;
//        }
    }

//    public static class LSTMPredictor extends Predictor {
//        private final int memoryScale;
//        float learningRate;
//        public LiveSTM lstm;
//        private double[] in;
//
//
//        public LSTMPredictor(float learningRate, int memoryScale) {
//            this.learningRate = learningRate;
//            this.memoryScale = memoryScale;
//        }
//
//        @Override
//        public void randomize(float rngRange, Random r) {
//            lstm.agent.randomize(rngRange, r);
//        }
//
//        @Override
//        public String toString() {
//            return super.toString() + '[' + lstm + ']';
//        }
//
//        @Override
//        public double[] put(double[] x, double[] y, float pri) {
//            synchronized (this) {
//                size(x.length, y.length);
//
//                double[] yPredicted = get(x);
//                lstm.agent.put(x, y, pri * learningRate);
//                return yPredicted;
//            }
//        }
//
//        @Override
//        public double putDelta(double[] d, float pri) {
//            return lstm.agent.putDelta(in, d, pri * learningRate);
//        }
//
//        public LSTMPredictor size(int xLen, int yLen) {
//            if (lstm == null || lstm.inputs != xLen || lstm.outputs != yLen) {
//                lstm = new LiveSTM(xLen, yLen, memoryScale) {
//                    @Deprecated
//                    @Override
//                    protected ExpectedVsActual observe() {
//                        throw new UnsupportedOperationException();
//                    }
//                };
//            }
//            return this;
//        }
//
//        @Override
//        public double[] get(double[] x) {
//            size(x.length, lstm.outputs);
//            return lstm.agent.get(this.in = x);
//        }
//
//    }

    /** TODO */
    public static class CMAESPredictor implements Predictor {
        final MyCMAESOptimizer m;

        public CMAESPredictor(int i, int o) {
            int parameters = i*o; //TODO

            double[] sigma = new double[parameters];
            Arrays.fill(sigma, 0.5f);
            m = new MyCMAESOptimizer(1, Double.NaN, i*o, sigma);
        }

        @Override
        public double[] put(double[] x, double[] y, float pri) {
            throw new TODO();
        }

        @Override
        public double[] get(double[] x) {
            return new double[0];
        }

        @Override
        public void clear(Random rng) {

        }
    }

    public static class NTMPredictor extends DeltaPredictor {
        public final NTM n;
        private final RMSPropWeightUpdater learn;

        private final int cycles;

        public double alpha = 0.005;

        private double[] input;

        public NTMPredictor(int ins, int outs, int cycles) {
            this(ins, outs, cycles, 1);
        }

        public NTMPredictor(int ins, int outs, int cycles, int heads) {
            this.cycles = cycles;

            int controllerSize =
                //outs;
                Util.sqrtInt((1 + ins) * (1 + outs));
                //Fuzzy.mean(ins, outs);
            this.n = new NTM(ins, outs, Math.max(2, controllerSize), heads, cycles);
            this.learn = new RMSPropWeightUpdater(n.weightCount(), 1)
                    .negate(); //HACK
        }

        @Override
        @Deprecated public double[] put(double[] x, double[] y, float pri) {
            learn.alpha = pri;
                //this.alpha * pri;
            NTM[] Z = n.put(x, y, cycles, learn);
            return Z[Z.length-1].output();
        }

        @Override
        public void putDelta(double[] d, float pri) {
            learn.alpha = /*this.alpha * */pri;
            n.clear();
            n.put(new double[][] { input }, new double[][] { d } , true, cycles, learn);
//            var y = //DistanceFunction.distanceCartesianManhattan(n.input, d);
//                    Util.sum(d.length, (int i) -> Math.abs(d[i]));
//            return y;
        }

        @Override
        public double[] get(double[] x) {
            input = x;
            return n.get(new double[][] { x }, cycles);
        }

        @Override
        public void clear(Random rng) {
        }
    }


    public final Predictor p;
    public final Framer framer;

    public LivePredictor(Framer framer, IntIntToObjectFunction<Predictor> model) {
        this.framer = framer;
        this.p = framer.build(model);
    }


    public double[] next(long when) {
        synchronized (p) {
            return p.put(framer.input(when), framer.outputs(), learningRate);
        }
    }

    /** applies the vector as new hypothetical present inputs,
     * after shifting the existing data (destructively)
     * down one time slot.
     * then prediction can proceed again
     *
     * typically this will need done in a cloned copy so as not to modify the learning model
     */
    public double[] project(long when, double[] hypotheticalInput) {
        synchronized (p) {
            return p.get(framer.input(when, hypotheticalInput));
        }
    }


}