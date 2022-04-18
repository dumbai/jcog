package jcog.rl.dqn;

import jcog.Fuzzy;
import jcog.Is;
import jcog.TODO;
import jcog.Util;
import jcog.predict.Predictor;
import jcog.rl.PredictorPolicy;
import jcog.signal.FloatRange;

import java.util.concurrent.atomic.AtomicBoolean;

import static jcog.Util.clampSafe;


@Is({"Q-learning", "State-action-reward-state-action"})
public class QPolicy extends PredictorPolicy {


    /** "gamma" discount factor: importance of future rewards
     *  https://en.wikipedia.org/wiki/Q-learning#Discount_factor */
    public final FloatRange plan = new FloatRange(0.1f, 0, 1);


    /** NaN to disable */
    private static final float tdErrClamp =
        //Float.NaN;
        1;
        //10;

    /** TODO move into separate impls of the update function */
    public final AtomicBoolean sarsaOrQ = new AtomicBoolean(false);

    /**
     * https://medium.com/analytics-vidhya/munchausen-reinforcement-learning-9876efc829de
     * https://github.com/BY571/Munchausen-RL/blob/master/M-DQN.ipynb
     * experimental
     */
    public final AtomicBoolean munchausen = new AtomicBoolean(false);
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


    boolean rewardDelta = false;

    /** TODO may not matter */
    boolean rewardPolarize = false;

    private transient double rewardPrev = Double.NaN;

    /**
     * Q update function
     * @see https://towardsdatascience.com/reinforcement-learning-temporal-difference-sarsa-q-learning-expected-sarsa-on-python-9fecfda7467e
     */
    @Override public synchronized double[] learn(double[] xPrev, double[] action, double reward, double[] x, float pri) {
        if (dq == null || dq.length!=action.length) dq = new double[action.length];

        double[] qPrev = predict(xPrev).clone(); //TODO is clone() necessary?
        double[] qNext = predict(x).clone(); //TODO is clone() necessary?

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

        double rewardPrev = this.rewardPrev;
        this.rewardPrev = reward;
        if (rewardDelta) {
            reward = rewardPrev==rewardPrev ? reward - rewardPrev : 0;
        } else {
            if (rewardPolarize) reward = Fuzzy.polarize(reward);
        }

        for (int a = 0; a < n; a++) {
            double qPrevA = qPrev[a], qNextA = qNext[a];

            /* estimate of optimal future value */
            double gq = m ?
                gqMunch(gamma, qNextMax, logsumNext, qPrevMax, logsumPrev, qPrevA, qNextA) :
                (sarsaOrQ ? gamma * qNextA : gammaQNextMax);


            double aa = action[a];
            dq[a] = aa * (reward + gq - qPrevA);
            //dq[a] = aa * (reward + gq) - qPrevA;
            //dq[a] = aa * (reward) + (gq - qPrevA);
            //dq[a] = aa * ((reward*action[a]) + gq - qPrevA); //fair proportion of reward, assuming sum(action)=1
        }

        if (p instanceof jcog.predict.DeltaPredictor D) {
            if (tdErrClamp == tdErrClamp) {
                clampSafe(dq, -tdErrClamp, +tdErrClamp);
                //Util.normalizePolar(dq, tdErrClamp); //TODO this may only work if tdErrClamp=1
            }
            float p = pri * learn.floatValue();
            D.putDelta(dq, p);
        } else
            throw new TODO("d.put(plus(q,dq), learnRate) ?");

        return qNext;
    }

    private double gqMunch(double gamma, double qNextMax, double logsumNext, double qPrevMax, double logsumPrev, double qPrevA, double qNextA) {
        double gq;
        // Get predicted Q values (for next states) from target model to calculate entropy term with logsum
        double mNext = qNextA - qNextMax - logsumNext;

        double mPrev = qPrevA - qPrevMax - logsumPrev;

        boolean terminal = false; //TODO final episode iteration

        gq = m_alpha * clampSafe(mPrev, lo, 0) +
                (terminal ? 0 : (gamma * qNextA * (qNextA - mNext)));
        return gq;
    }


    private static double qMax(double[] q) {
        int qMaxIndex = Util.argmax(q);
//        if (qMaxIndex == -1)
//            return q[0];
//        else
            return q[qMaxIndex];
    }

}