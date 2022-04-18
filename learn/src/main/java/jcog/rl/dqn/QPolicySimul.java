package jcog.rl.dqn;

import jcog.TODO;
import jcog.Util;
import jcog.data.DistanceFunction;
import jcog.decide.Decide;
import jcog.decide.DecideSoftmax;
import jcog.predict.Predictor;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.rl.Policy;
import org.eclipse.collections.api.block.function.primitive.IntIntToObjectFunction;

import java.util.Random;

import static jcog.Str.n2;
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

    @Override
    public double[] predict(double[] input) {
        throw new TODO();
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

        private final float decodeSpecificity = 1;

        @Override public double[] actionEncode(double[] x, int actionsInternal) {
            //assert (actionDiscretization == 2);
            double[] z = new double[actionsInternal];
            int actions = x.length;
            double zSum = 0;
            for (int i = 0; i < actionsInternal; i++) {
                double d = dist(x, idealDecode(i, actions));
                double weight =
                        actionsInternal == 2 ? Math.max(0, 1-d) /* clean */ : 1 / sqr(1 + d * actionsInternal); /*blur intense*/
                        //1 / (1 + d * actionsInternal); //blur
                        //1 / sqr(1 + d * actionsInternal); //blur intense
                        //Math.max(0, 1-d); //clean
                        //1 / (1 + d * actions);
                        //Math.max(0, 1-d/actions);
                        //sqr(Math.max(0, 1-d/actions));
                        //Math.max(0, 1-sqr(d/actions));
                        //Math.max(0, 1-d*2);
                        //1 / (1 + d);
                        //1 / sqr(1 + d);
                        //1 / sqr(1 + d * actions);

                z[i] = weight;
                zSum += weight;
            }

            if (zSum > Float.MIN_NORMAL) {
                Util.mul(1 / zSum, z);

//                //0..1 -> -1..+1
//                for (int i = 0; i < z.length; i++)
//                    z[i] = Fuzzy.polarize(z[i]);
            }

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
                double zi = Math.pow(
                        z[i]
                        //(z[i] - zMin)/zRange //NORMALIZATION to 0..1
                        //z[i] - zMin
                        //(1 - zMax) + z[i]
                        , decodeSpecificity);
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