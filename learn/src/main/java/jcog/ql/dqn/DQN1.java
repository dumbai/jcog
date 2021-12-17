//package jcog.ql.dqn;
//
//import jcog.signal.FloatRange;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.ArrayDeque;
//import java.util.Arrays;
//import java.util.Deque;
//import java.util.Random;
//
//import static jcog.Util.fma;
//import static jcog.Util.sqr;
//
///**
// * multilayer perceptron, classic DQN
// */
//public class DQN1 implements Policy {
//
//    public final FloatRange alpha = new FloatRange(0.01f, 0, 0.1f);
//    public final FloatRange gamma = new FloatRange(0.5f, 0, 1f);
//
//    public final Mat W1;
//    public final Mat B1;
//    public final Mat W2;
//    public final Mat B2;
//    public Mat present;
//    public Mat past;
//
//    public DQN1(int inputs, int actions) {
//        this(inputs,
//                /* estimate */
//                //inputs * numActions
//                (inputs + actions) * 4
//                //inputs * numActions * 2
//                //Util.mean(inputs, numActions) * 2
//                //(inputs + numActions) * 2
//                , actions);
//
//    }
//
//    public DQN1(int inputs, int hiddenUnits, int actions) {
//        W1 = new Mat(hiddenUnits, inputs);
//        B1 = new Mat(hiddenUnits, 1);
//        W2 = new Mat(actions, hiddenUnits);
//        B2 = new Mat(actions, 1);
//    }
//
//    public void randomize(float rngRange, Random rng) {
//        W1.randomizeGaussian(rngRange, rng);
//        B1.randomizeGaussian(rngRange, rng);
//        W2.randomizeGaussian(rngRange, rng);
//        B2.randomizeGaussian(rngRange, rng);
//    }
//
//    private Mat q(Mat s) {
//        return q(s, null);
//    }
//
//    private Mat q(Mat s, @Nullable MatrixTransform g) {
//
//        if (g == null) g = MatrixTransform.fwd;
//
//        return g.add(g.mul(W2, g.tanh(g.add(g.mul(W1, s), B1))), B2);
//    }
//
//
//    @Override
//    public double learn(double[] action, double reward) {
//        return reward != reward || past == null ? 0 :
//            learn(past, action, reward, present);
//    }
//
//    @Override
//    public double[] predict(double[] x) {
//        Mat future = new Mat(x.length, 1, x);
//        final double[] xQ = q(future).w;
//
//        past = this.present;
//        present = future;
//
//        return xQ;
//    }
//
//    private double learn(Mat prev, double[] action, double reward, Mat next) {
//
//        MatrixTransform t = new MatrixTransform();
//        Mat qPrev = q(prev, t);
//
//        Mat qNext = q(next);
//
//        double errSum = Policy.td(
//                qPrev.w, action,
//                reward, gamma.doubleValue(),
//                qNext.w, qPrev.dw);
//
//        t.backward();
//
//        double alpha = this.alpha.doubleValue();
//        W1.integrate(alpha);
//        W2.integrate(alpha);
//        B1.integrate(alpha);
//        B2.integrate(alpha);
//
//        return errSum;
//    }
//
//    private static class Backprop {
//        private final Backprop.BackpropMethod f;
//        private final Mat[] x;
//
//        private Backprop(Backprop.BackpropMethod f, Mat... x) {
//            this.f = f;
//            this.x = x;
//        }
//
//        void run() {
//            f.back(x);
//        }
//
//        public enum BackpropMethod {
//            ADD {
//                @Override
//                public void back(Mat... args) {
//                    Mat a = args[0];
//                    Mat b = args[1];
//                    Mat c = args[2];
//                    int n = a.w.length;
//                    double[] ad = a.dw, bd = b.dw, cd = c.dw;
//                    for (int i = 0; i < n; i++) {
//                        double cdi = cd[i];
//                        ad[i] += cdi;
//                        bd[i] += cdi;
//                    }
//                }
//            },
//            MUL {
//                @Override
//                public void back(Mat... args) {
//                    Mat a = args[0];
//                    Mat b = args[1];
//                    Mat c = args[2];
//                    int n = a.n;
//                    int an = a.d;
//                    int bn = b.d;
//                    double[] aw = a.w;
//                    double[] bw = b.w;
//                    double[] ad = a.dw;
//                    double[] bd = b.dw;
//                    double[] cd = c.dw;
//
//                    for (int i = 0; i < n; i++) {
//                        for (int j = 0; j < bn; j++) {
//                            double cdij = cd[bn * i + j];
//                            if (cdij != 0) {
//                                for (int k = 0; k < an; k++) {
//                                    int mm1 = an * i + k;
//                                    int mm2 = bn * k + j;
//                                    ad[mm1] = fma(bw[mm2], cdij, ad[mm1]);
//                                    bd[mm2] = fma(aw[mm1], cdij, bd[mm2]);
//                                }
//                            }
//                        }
//                    }
//                }
//            },
//            TANH {
//                @Override
//                public void back(Mat... args) {
//                    Mat a = args[0];
//                    Mat c = args[1];
//                    int n = a.w.length;
//                    double[] ad = a.dw, cw = c.w, cd = c.dw;
//                    for (int i = 0; i < n; i++)
//                        ad[i] = fma((1 - sqr(cw[i])), cd[i], ad[i]);
//                }
//            };
//
//            abstract public void back(Mat... args);
//        }
//    }
//
//    protected static class Experience {
//
//        final double[] action;
//
//        final double reward;
//
//        final Mat prev, next;
//
//        Experience(Mat prev, double[] prevAction, double reward, Mat next) {
////            this.iteration = iteration;
//            this.prev = prev;
//            this.action = prevAction;
//            this.reward = reward;
//            this.next = next;
//        }
//
//    }
//
//    public static final class MatrixTransform {
//
//        public static final MatrixTransform fwd = new MatrixTransform(false);
//
//        final Deque<Backprop> stack;
//
//        public MatrixTransform() {
//            this(true);
//        }
//
//        private MatrixTransform(boolean needsBackprop) {
//            this.stack = needsBackprop ? new ArrayDeque<Backprop>() : null;
//        }
//
//        void backward() {
//            Backprop next;
//            while (((next = this.stack.pollLast())) != null)
//                next.run();
//        }
//
//        Mat tanh(Mat a) {
//            Mat c = new Mat(a.n, a.d);
//            Arrays.setAll(c.w, i -> Math.tanh(a.w[i]));
//            if (stack != null)
//                this.stack.addLast(new Backprop(Backprop.BackpropMethod.TANH, a, c));
//            return c;
//        }
//
//        Mat mul(Mat a, Mat b) {
//            int an = a.d;
//            assert an == b.n;
//            int bn = b.d;
//            int n = a.n;
//
//            //assert(m1d == m2.n): "matmul dimensions misaligned: " + m1d + " != " + m2.n;
//
//            int d = bn;
//            double[] aw = a.w;
//            double[] bw = b.w;
//
//            Mat c = new Mat(n, d);
//            double[] cw = c.w;
//
//            int ijk = 0;
//            for (int i = 0; i < n; i++) { // loop over rows of m1
//                int m1i = an * i;
//                for (int j = 0; j < bn; j++) { // loop over cols of m2
//                    double x = 0; //dot product
//                    for (int k = 0; k < an; k++) // dot product loop
//                        x = fma(aw[m1i + k], bw[d * k + j], x);
//
//                    cw[ijk++] = x;
//                }
//            }
//
//            if (stack != null)
//                this.stack.addLast(new Backprop(Backprop.BackpropMethod.MUL, a, b, c));
//
//            return c;
//        }
//
//        Mat add(Mat a, Mat b) {
//            double[] wa = a.w;
//            double[] wb = b.w;
//
//            int n = wa.length;
//            assert n == wb.length;
//
//            Mat c = new Mat(a.n, a.d);
//            double[] wc = c.w;
//
//            for (int i = 0; i < n; i++)
//                wc[i] = wa[i] + wb[i];
//
//            if (stack != null)
//                stack.addLast(new Backprop(Backprop.BackpropMethod.ADD, a, b, c));
//
//            return c;
//        }
//
//
//    }
//
//
//    //    @Override
////    public @Nullable AgentBuilder meta() {
////        FloatNormalizer err = new PolarFloatNormalizer(Float.MIN_NORMAL).contraction(0.001f);
////        FloatNormalizer errSlow = new PolarFloatNormalizer(Float.MIN_NORMAL).contraction(0.0001f);
////        FloatNormalizer errFast = new PolarFloatNormalizer(Float.MIN_NORMAL).contraction(0.01f);
////        FloatSupplier rew = () -> {
////            double r = reward;
////            if (r!=r) return 0;
////            return (float) r;
////        };
////        AgentBuilder a = new AgentBuilder(()-> {
////            float e = (err.valueOf((float) this.err));
//////            System.out.println(e);
////            //return 1-e;
////            return Util.mean(Util.unitize(1-(e/2 + 0.5f)) , rew.asFloat());
////        });
////
////        //a.in(err::valueOf); //HACK
////        a.in(() -> errSlow.valueOf((float) this.err));
////        a.in(() -> errFast.valueOf((float) this.err));
////
////        a.in(rew);
////
////        a.in(() -> gamma.asFloat());
////        a.out(2, (i)->{
////            gamma.set(gamma.asFloat() + (i==0 ? -1 : +1) * 0.1);
////        });
////        a.in(() -> alpha.asFloat());
////        a.out(2, (i)->{
////            alpha.set(clampSafe(alpha.asFloat() + (i==0 ? -1 : +1) * 0.003, 0.001f, 0.01f));
////        });
////
////        a.out(1, (i)->{  /* null */ });
////
////        a.history(3);
////        return a;
////    }
//
//}