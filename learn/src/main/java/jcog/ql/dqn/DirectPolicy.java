package jcog.ql.dqn;

import jcog.Util;
import jcog.predict.DeltaPredictor;
import jcog.predict.Predictor;
import org.jetbrains.annotations.Nullable;

/** trains a model:
 *    in: state -> actions x reward
 */
public class DirectPolicy extends PredictorPolicy {


    public DirectPolicy(Predictor p) {
        super(p);
    }

    @Override
    public double[] learn(@Nullable double[] xPrev, double[] actionPrev, double reward, double[] x, float pri) {


        return learnDirect(xPrev, actionPrev, reward, x, pri);
        //return learnIncremental(xPrev, actionPrev, reward, x, pri);
        //return learnContrastive(xPrev, actionPrev, reward, x, pri);


    }

    private double[] learnDirect(@Nullable double[] xPrev, double[] actionPrev, double reward, double[] x, float pri) {
        float alpha = pri * learn.asFloat();
        double[] a = actionPrev.clone();
        Util.mul(reward, a);
        //Util.mul(Fuzzy.polarize(reward), act0);

        p.put(xPrev, a, alpha);

        return predict(x);
    }

    /** incremental (similar to DQN) */
    private double[] learnIncremental(@Nullable double[] xPrev, double[] actionPrev, double reward, double[] x, float pri) {
        double[] da = predict(xPrev);
        for (int i = 0; i < da.length; i++)
            da[i] = pri * (reward * actionPrev[i] - da[i]);

        ((DeltaPredictor)p).putDelta(da, learn.asFloat());
        ((DeltaPredictor)p).deltaSum += Util.sumAbs(da); //HACK

        return predict(x);
    }

    private double[] learnContrastive(@Nullable double[] xPrev, double[] actionPrev, double reward, double[] x, float pri) {
        {
            double[] ay = actionPrev.clone(); Util.mul(reward, ay);
            double[] yes =
                    p.put(x, ay, pri);

            double[] an = actionPrev.clone();
            for (int i = 0; i < an.length; i++) an[i] = 1 - an[i];
            Util.mul(1  - reward, an);
            double[] xn = x.clone(); Util.mul(-1, xn); //HACK store negated

            double[] no = p.put(xn, an, pri);

            //return yes;

            double[] yesAndNotNo = yes.clone();
            for (int i = 0; i < yesAndNotNo.length; i++)
                //yesAndNotNo[i] = 0.5f + yes[i]/2 - no[i]/2;
                yesAndNotNo[i] = (yes[i])/(1.0E-8 + yes[i] + no[i]);
            return yesAndNotNo;
        }

    }
}