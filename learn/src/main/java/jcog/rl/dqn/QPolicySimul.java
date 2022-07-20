package jcog.rl.dqn;

import jcog.Util;
import jcog.WTF;
import jcog.data.DistanceFunction;
import jcog.decide.Decide;
import jcog.decide.DecideSoftmax;
import jcog.predict.Predictor;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.rl.Policy;
import org.eclipse.collections.api.block.function.primitive.IntIntToObjectFunction;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static jcog.Util.sqr;

public class QPolicySimul implements Policy {

    final int inputs, actions;
    public final QPolicy q;

    /** TODO returned by ActionEncoder method  */
    @Deprecated private final int actionDiscretization = 2;

    /** TODO returned by ActionEncoder method  */
    @Deprecated private final int actionsInternal;

    /** maps action vector to a distribution of virtual d^n basis vectors */
    interface ActionEncoder {
        double[] actionEncode(double[] x, int actionsInternal);
        double[] actionDecode(double[] z, int actions);
    }

    private ActionEncoder c =
        new DistanceActionEncoder();
        //new FuzzyDistanceActionEncoder();
        //new SoftDistanceActionEncoder();
        //new BinaryActionEncoder();

    public QPolicySimul(int inputs, int actions, IntIntToObjectFunction<Predictor> p) {
        this.inputs = inputs;
        this.actions = actions;
        this.actionsInternal = (int)Math.pow(actionDiscretization, actions);
        this.q = new QPolicy(p.value(inputs, actionsInternal));
    }

    @Override
    public void clear(Random rng) {
        q.clear(rng);
    }

    @Override
    public double[] learn(double[] xPrev, double[] actionPrev, double reward, double[] x, float pri) {
        return c.actionDecode(q.learn(xPrev,
                c.actionEncode(actionPrev, actionsInternal),
                reward, x, pri), actions);
    }


    /** HACK 2-ary thresholding */
    public static class BinaryActionEncoder implements ActionEncoder {

        private final Decide decide =
            new DecideSoftmax(0.1f, new XoRoShiRo128PlusRandom());
            //new DecideRoulette(new XoRoShiRo128PlusRandom());


        @Override public double[] actionEncode(double[] x, int actionsInternal) {
            //assert (actionDiscretization == 2);
            double[] z = new double[actionsInternal];
            int actions = x.length;
            int Z = 0;
            for (int i = 0; i < actions; i++) {
                double a = x[i];
                if (a >= 0.5)
                    Z |= 1 << i;
            }
            z[Z] = 1;
            return z;
        }


        @Override public double[] actionDecode(double[] z, int actions) {
            //System.out.println(n2(z));
            //HACK 2-ary thresholding
            //assert (actionDiscretization == 2);
            return actionDecodeDecide(z, actions, decide);
        }


    }
    private static double[] actionDecodeDecide(double[] z, int actions, Decide decide) {
        double[] y = new double[actions];
        int Z = decide.applyAsInt(z);
        for (int i = 0; i < actions; i++) {
            boolean a = (Z & (1 << i)) != 0;
            if (a)
                y[i] = 1;
        }
        return y;
    }

    /** HACK 2-ary thresholding */
    public static class DistanceActionEncoder implements ActionEncoder {

        //private final float decodeSpecificity = 1;

        private final float temperature =
            0.5f;
            //1;
            //0.25f;
            //2;
            //0.1f;

        private boolean normalizeManhattanOrCartesian = true;

        /** TODO still not perfect */
        public double[] actionEncode2(double[] x, int actionsInternal) {
            int actions = x.length;
            double[] z = new double[actionsInternal];
            double[] xDelta = x.clone();
            double[] xSum = new double[x.length];

            var rng = ThreadLocalRandom.current();

            int jMax = actionsInternal;
            for (int j = 0; j < jMax; j++) {
                int best = -1; double bestDist = Double.POSITIVE_INFINITY;
                int iOffset = rng.nextInt(actionsInternal); //for fairness
                for (int _i = 0; _i < actionsInternal; _i++) {
                    int i = (iOffset + _i)%actionsInternal;
                    if (z[i] > 0) continue; //already added

                    double[] yi = idealDecode(i, actions);
                    double dist = DistanceFunction.distanceCartesian(xSum, yi);
                    if (dist < bestDist) {
                        bestDist = dist; best = i;
                    }
                }
                if (best < 0) break; //HACK

                double yScale;
                double[] yi = idealDecode(best, actions);
                if (j == 0) {
                    yScale = 1;
                } else {
                    yScale =
                            //Util.max(xx);
                            vectorProject(xDelta, yi);
                }
                if (yScale!=yScale)
                    break; //HACK
                z[best] = yScale;
                Util.mul(yScale, yi);

                for (int i = 0; i < actions; i++) {
                    xDelta[i] -= yi[i];
                    xSum[i] += yi[i];
                }
                if (len(xDelta) < Float.MIN_NORMAL)
                    break;
            }

            //Util.normalizeCartesian(z, z.length, Double.MIN_NORMAL);
            double zSum = Util.sumAbs(z); Util.mul(1/zSum, z); //Normalize Manhattan

            double diff = DistanceFunction.distanceManhattan(x, actionDecode(z, actions));
            if (diff!=diff)
                throw new WTF();
            System.out.println("diff: " + diff);

            //System.out.println(n4(x) + "\t->\t" + n4(z) + "\t=\t" + n4(actionDecode(z, actions)));

            return z;
        }

        public static double dotProduct(double[] x, double[] y) {
            assert(x.length == y.length);
            double s = 0;
            for (int i = 0; i < x.length; i++)
                s += (x[i]*y[i]);
            return s;
        }

        public static double len(double[] x) {
            double s = 0;
            for (int i = 0; i < x.length; i++)
                s += sqr(x[i]);
            return Math.sqrt(s);
        }

        private double vectorProject(double[] a, double[] b) {
            return dotProduct(a, b) / len(b);
        }

        //TODO not great
        @Override public double[] actionEncode(double[] x, int actionsInternal) {
            //assert (actionDiscretization == 2);
            double[] z = new double[actionsInternal];
            int actions = x.length;
            double zSum = 0;
            double distMax = 1; //z.length;
            //TODO refine
            for (int i = 0; i < actionsInternal; i++) {
                double d = dist(x, idealDecode(i, actions));
//                double weight =
//                    actionsInternal == 2 ? Math.max(0, 1-d) /* clean */ : 1 / sqr(1 + d * actionsInternal); /*blur intense*/
//                    //(distMax - d)/distMax;
//                    //Math.max(0, 1-d); //clean
//                    //1 / (1 + d * actionsInternal); //blur
//                    //1 / sqr(1 + d * actionsInternal); //blur intense
//                    //1 / (1 + d * actions);
//                    //Math.max(0, 1-d/actions);
//                    //sqr(Math.max(0, 1-d/actions));
//                    //Math.max(0, 1-sqr(d/actions));
//                    //Math.max(0, 1-d*2);
//                    //1 / (1 + d);
//                    //1 / sqr(1 + d);
//                    //1 / sqr(1 + d * actions);
////                z[i] = weight;
//                zSum += weight;
                z[i] = d;
            }
//            double zMax = Util.max(z), zMin = Util.min(z);
            for (int i = 0; i < z.length; i++) {
//                z[i] = Util.normalize(z[i], zMin, zMax);
                z[i] = 1.0 / (1.0 + Math.pow(z[i]*actionsInternal, 3));
                zSum += z[i];
            }


            if (zSum > Float.MIN_NORMAL) {
                if (normalizeManhattanOrCartesian)
                    Util.mul(1 / zSum, z);
                else {
                    Util.normalizeCartesian(z, z.length, Double.MIN_NORMAL);
                }
            }

//            double diff = DistanceFunction.distanceManhattan(x, actionDecode(z, actions));
//            System.out.println("diff: " + diff);

            //System.out.println(n4(x) + "\t->\t" + n4(z) + "\t=\t" + n4(actionDecode(z, actions)));
            return z;
        }

        /** experimental */
        private double[] actionEncode0(double[] x, int actionsInternal) {
            //assert (actionDiscretization == 2);
            int actionsExternal = x.length;
            double[] z = new double[actionsInternal];
            int actions = x.length;
//            double zSum = 0;
            for (int i = 0; i < actionsInternal; i++) {
                double d = dist(x, idealDecode(i, actions));
                z[i] = d;
//                double weight =
//                        //Math.max(0, 1 - d); //clean
//                        //1 / (1 + d * actionsInternal); //blur
//                        1 / sqr(1 + d * actionsInternal); //blur intense
//                        //1 / (1 + d * actions);
//                        //Math.max(0, 1-d/actions);
//                        //sqr(Math.max(0, 1-d/actions));
//                        //Math.max(0, 1-sqr(d/actions));
//                        //Math.max(0, 1-d*2);
//                        //1 / (1 + d);
//                        //1 / sqr(1 + d);
//                        //1 / sqr(1 + d * actions);
//
//                z[i] = weight;
//                zSum += weight;
            }

            Util.normalizeUnit(z);
            for (int i = 0; i < z.length; i++)
                z[i] = 1 - z[i]; //dist -> weight
            Util.normalizeHamming(z, Double.MIN_NORMAL);

            //double zMin = Util.min(z), zMax = Util.max(z);

//            if (zSum > Float.MIN_NORMAL) {
//                Util.mul(1 / zSum, z);
//
////                //0..1 -> -1..+1
////                for (int i = 0; i < z.length; i++)
////                    z[i] = Fuzzy.polarize(z[i]);
//            } else {
//                Arrays.fill(z, 1.0 / actionsInternal);
//            }

            return z;
        }

        /** TODO abstract for distance function parameter */
        private double dist(double[] x, double[] y) {
            return DistanceFunction.distanceManhattan(x,y);
            //return DistanceFunction.distanceCartesian(x,y); //may be too far to reach max(0, 1-x) for all-in-between points
        }


        @Override public double[] actionDecode(double[] z, int actions) {
            //z = z.clone();Util.normalize(z);
            //System.out.println(n2(z));

            //double zMin = Util.min(z), zMax = Util.max(z);
            //double zRange = zMax - zMin;

            //z = Util.normalize(z);

            double[] y = new double[actions];
            double s = 0;
            for (int i = 0; i < z.length; i++) {
                double zi =
                        Util.softmax(z[i], temperature);
                        //Util.unitizeSafe(z[i]);
                double[] ideal = idealDecode(i, actions);
                for (int a = 0; a <actions; a++)
                    y[a] += zi * ideal[a];
                s += zi;
            }
            if (s > Double.MIN_NORMAL)
                Util.mul(1/s, y);
            else {
                //TODO randomize?
            }

//            boolean noise = true;
//            if (noise) {
//                var rng = ThreadLocalRandom.current();
//                System.out.println(s);
//                for (int a = 0; a < actions; a++) {
//                    y[a] = Util.unitizeSafe(y[a] +
//                        //rng.nextGaussian(0, s/10)
//                        (Util.max(s-1, 0)/100)*rng.nextDouble(-1, +1)
//                    );
//                }
//            }

            return y;
        }

        private static double[] idealDecode(int Z, int actions) {
            double[] z = new double[actions];
            for (int i = 0; i < actions; i++) {
                if ((Z & (1 << i)) != 0)
                    z[i] = 1;
            }
            return z;
        }
    }

    public static class FuzzyDistanceActionEncoder extends DistanceActionEncoder {
        @Override
        public double[] actionDecode(double[] z, int actions) {

            //TODO refine

            int zArgMax = Util.argmax(z);
            double zMax = z[zArgMax];
            double zOtherSum = 0;
            for (int i = 0; i < z.length; i++) {
                if (i!=zArgMax) {
                    double zI = z[i];
                    zOtherSum += zI / zMax;
                }
            }
            double uncertainty = zOtherSum / (z.length - 1);
            //double uncertainty = 1 - (Util.max(z) - Util.min(z));
            //System.out.println(uncertainty + " " + n2(z));
            Random rng = new XoRoShiRo128PlusRandom();
//            for (int i = 0; i < z.length; i++)
//                z[i] = Util.unitizeSafe( z[i] + uncertainty * ((rng.nextFloat()-0.5f)*2f) /* TODO gaussian */ );

            double[] y = super.actionDecode(z, actions);

            for (int i = 0; i < y.length; i++)
                y[i] = Util.unitizeSafe( y[i] + uncertainty * ((rng.nextFloat()-0.5f)*2f) /* TODO gaussian */ );


            return y;
        }
    }
    public static class SoftDistanceActionEncoder extends DistanceActionEncoder {
        private final Decide decide =
                new DecideSoftmax(0.1f, new XoRoShiRo128PlusRandom());

        @Override
        public double[] actionDecode(double[] z, int actions) {
            //return actionDecodeDecide(z, actions, decide);

            //multisampled softmax:
            int iterations = 8;
            double[] y = new double[actions];
            for (int i = 0; i < iterations; i++) {
                double[] yi = actionDecodeDecide(z, actions, decide);
                for (int j = 0; j < actions; j++)
                    y[j] += yi[j];
            }
            Util.mul(1f/iterations, y);
            return y;
        }
    }

}