package jcog.rl.dqn;

import jcog.TODO;
import jcog.Util;
import jcog.data.DistanceFunction;
import jcog.decide.Decide;
import jcog.decide.DecideRoulette;
import jcog.decide.DecideSoftmax;
import jcog.predict.Predictor;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.rl.Policy;
import org.eclipse.collections.api.block.function.primitive.IntIntToObjectFunction;

import java.util.Random;

import static java.lang.Math.sqrt;
import static jcog.Util.sqr;
import static jcog.Util.unitizeSafe;

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
        double[] actionDecode(double[] x, int actions);
    }

    private ActionEncoder c =
        //new BinaryActionEncoder();
        new DistanceActionEncoder();


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
            new DecideSoftmax(0.05f, new XoRoShiRo128PlusRandom());
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


        @Override public double[] actionDecode(double[] x, int actions) {
            //HACK 2-ary thresholding
            //assert (actionDiscretization == 2);
            double[] z = new double[actions];
            //x = Util.normalize(x);
            int Z = decide.applyAsInt(x);
            for (int i = 0; i < actions; i++) {
                boolean a = (Z & (1 << i)) != 0;
                if (a)
                    z[i] = 1;
            }
            return z;
        }
    }

    /** HACK 2-ary thresholding */
    public static class DistanceActionEncoder implements ActionEncoder {

        @Override public double[] actionEncode(double[] x, int actionsInternal) {
            //assert (actionDiscretization == 2);
            double[] z = new double[actionsInternal];
            int actions = x.length;
            double zSum = 0;
            for (int i = 0; i < actionsInternal; i++) {
                double d = dist(x, idealDecode(i, actions));
                double weight =
                        //1 / (1 + d * actions);
                        //Math.max(0, 1-d/actions);
                        //sqr(Math.max(0, 1-d/actions));
                        //Math.max(0, 1-sqr(d/actions));
                        //Math.max(0, 1-d);
                        //Math.max(0, 1-d*2);
                        //1 / (1 + d);
                        //1 / sqr(1 + d);
                        //1 / sqr(1 + d * actions);
                        1 / sqr(1 + d * actionsInternal);
                z[i] = weight;
                zSum += weight;
            }

            if (zSum > Float.MIN_NORMAL)
                Util.mul(1/zSum, z);

            return z;
        }

        /** TODO abstract for distance function parameter */
        private double dist(double[] x, double[] y) {
            return DistanceFunction.distanceCartesian(x,y);
            //return DistanceFunction.distanceManhattan(x,y);
        }


        @Override public double[] actionDecode(double[] z, int actions) {
            //HACK 2-ary thresholding
            //assert (actionDiscretization == 2);
            double[] y = new double[actions];
            double s = 0;
            for (int i = 0; i < z.length; i++) {
                double zi = z[i];
                double[] ideal = idealDecode(i, actions);
                for (int a = 0; a <actions; a++)
                    y[a] += zi * ideal[a];
                s += zi;
            }
            if (s > Float.MIN_NORMAL)
                Util.mul(1/s, y);
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


}