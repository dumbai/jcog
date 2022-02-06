package jcog.rl.dqn;

import jcog.Is;
import jcog.TODO;
import jcog.Util;
import jcog.decide.Decide;
import jcog.predict.Predictor;
import jcog.rl.PredictorPolicy;
import jcog.signal.FloatRange;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static jcog.Util.clampSafe;


@Is({"Q-learning", "State-action-reward-state-action"})
public class QPolicy extends PredictorPolicy {


    /** "gamma" discount factor: importance of future rewards
     *  https://en.wikipedia.org/wiki/Q-learning#Discount_factor */
    public final FloatRange plan = new FloatRange(0.9f, 0, 1);


    /** TODO move into separate impls of the update function */
    public final AtomicBoolean sarsaOrQ = new AtomicBoolean(false);

    /** NaN to disable */
    private static final float tdErrClamp =
        Float.NaN;
        //10;
        //1;

    /**
     * https://medium.com/analytics-vidhya/munchausen-reinforcement-learning-9876efc829de
     * https://github.com/BY571/Munchausen-RL/blob/master/M-DQN.ipynb
     * experimental
     */
    public final AtomicBoolean munchausen = new AtomicBoolean(true);
    private static final float m_alpha = 0.9f;
    private static final float entropy_tau =
            //1;
            0.03f;
    private static final float lo = -1;

    public transient double[] dq;
    private double[] qNext;

    public QPolicy(Predictor p) {
        super(p);
    }


    /**
     * TD update policy
     * @see https://towardsdatascience.com/reinforcement-learning-temporal-difference-sarsa-q-learning-expected-sarsa-on-python-9fecfda7467e
     *
     * @param dq - (output) this will be modified
     */
    @Override public double[] learn(double[] xPrev, double[] action, double reward, double[] i, float pri) {
        if (dq == null || dq.length!=action.length) dq = new double[action.length];

        double[] qPrev = predict(xPrev).clone(); //TODO is clone() necessary?
        double[] qNext = predict(i).clone(); //TODO is clone() necessary?

        double gamma = plan.doubleValue();
        int n = action.length;

        boolean m = munchausen.getOpaque(); if (m) sarsaOrQ.set(false); //HACK

        boolean sarsaOrQ = this.sarsaOrQ.getOpaque();
        double qNextMax = sarsaOrQ ?
                Double.NaN /* computed below */ : qMax(qNext);
        double gammaQNextMax = gamma * qNextMax;

        double logsumNext = m ? Util.logsumexp(qNext, -qNextMax, 1/entropy_tau)*entropy_tau : Double.NaN;

        double qPrevMax = m ? Util.max(qPrev) : Double.NaN;
        double logsumPrev = m ? Util.logsumexp(qPrev, -qPrevMax, 1/entropy_tau)*entropy_tau : Double.NaN;


        for (int a = 0; a < n; a++) {
        //Arrays.fill(dq, 0); int a = Decide.Greedy.applyAsInt(action); {

            double qPrevA = qPrev[a], qNextA = qNext[a];

            double gq;

            if (m) {
                // Get predicted Q values (for next states) from target model to calculate entropy term with logsum
                double mNext = qNextA - qNextMax - logsumNext;

                double mPrev = qPrevA - qPrevMax - logsumPrev;

                boolean terminal = false; //TODO final episode iteration

                gq = m_alpha * clampSafe(mPrev, lo, 0) +
                        (terminal ? 0 : (gamma * qNextA * (qNextA - mNext)));


            } else {
                /* estimate of optimal future value */
                gq = sarsaOrQ ? gamma * qNextA : gammaQNextMax;


            }

            dq[a] = action[a] * (reward + gq - qPrevA);
            //dq[a] = action[a] * reward + (gq - qPrevA);
            //dq[a] = action[a] * (reward + gq) - qPrevA;

        }

        if (p instanceof jcog.predict.DeltaPredictor) {
            if (tdErrClamp == tdErrClamp) {
                clampSafe(dq, -tdErrClamp, +tdErrClamp);
                //Util.normalizePolar(dq, tdErrClamp); //TODO this may only work if tdErrClamp=1
            }
            ((jcog.predict.DeltaPredictor) p).putDelta(dq, pri * learn.floatValue());
        } else
            throw new TODO("d.put(plus(q,dq), learnRate) ?");

        return qNext;
    }



    private static double qMax(double[] q) {
        int qMaxIndex = Util.argmax(q);
//        if (qMaxIndex == -1)
//            return q[0];
//        else
            return q[qMaxIndex];
    }

}