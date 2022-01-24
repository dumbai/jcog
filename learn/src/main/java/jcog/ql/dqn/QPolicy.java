package jcog.ql.dqn;

import jcog.Is;
import jcog.TODO;
import jcog.Util;
import jcog.predict.Predictor;
import jcog.signal.FloatRange;

import java.util.concurrent.atomic.AtomicBoolean;

import static jcog.Util.clampSafe;


@Is({"Q-learning", "State-action-reward-state-action"})
public class QPolicy extends PredictorPolicy {


    /** "gamma" discount factor: importance of future rewards
     *  https://en.wikipedia.org/wiki/Q-learning#Discount_factor */
    public final FloatRange plan = new FloatRange(0.1f, 0, 1);


    /** TODO move into separate impls of the update function */
    public final AtomicBoolean sarsaOrQ = new AtomicBoolean(true);

    static final float tdErrClamp =
        Float.NaN;
        //10;
        //1;

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

    transient double[] dq;
    private double[] qNext;

    protected QPolicy(Predictor p) {
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

        double[] qPrev = predict(xPrev).clone();
        double[] qNext = predict(i).clone();

        double gamma = plan.doubleValue();
        int n = action.length;

        boolean m = munchausen.getOpaque(); if (m) sarsaOrQ.set(false); //HACK

        boolean sarsaOrQ = this.sarsaOrQ.getOpaque();
        double qNextMax = sarsaOrQ ?
                Double.NaN /* computed below */ : qMax(qNext);
        double gammaQNextMax = gamma * qNextMax;

        float PRI =
            pri;
            //pri / n;

        double logsumNext = m ? Util.logsumexp(qNext, -qNextMax, 1/entropy_tau)*entropy_tau : Double.NaN;

        double qPrevMax = m ? Util.max(qPrev) : Double.NaN;
        double logsumPrev = m ? Util.logsumexp(qPrev, -qPrevMax, 1/entropy_tau)*entropy_tau : Double.NaN;


        //int a = new DecideSoftmax(0.1f, ThreadLocalRandom.current()).applyAsInt(action);
        for (int a = 0; a < n; a++) {
        //int a = Decide.Greedy.applyAsInt(action); {
            //Arrays.fill(dq, 0);
            /*for (int a = 0; a < n; a++)*/

            double qPrevA = qPrev[a], qNextA = qNext[a];

            if (m) {
                // Get predicted Q values (for next states) from target model to calculate entropy term with logsum
                double mNext = qNextA - qNextMax - logsumNext;

                double mPrev = qPrevA - qPrevMax - logsumPrev;

                boolean terminal = false; //TODO final episode iteration

//                dq[a] = PRI * action[a] * (
//                            reward +
//                            m_alpha * clampSafe(mPrev, lo, 0) +
//                            (terminal ? 0 : (gamma * qNextA * (qNextA - mNext)))
//                            //(gamma * qNextA * (qNextA - mNext))//*(1 - dones)
//                ) - qPrevA;

                dq[a] = PRI * (
                        action[a] * reward +
                                m_alpha * clampSafe(mPrev, lo, 0) +
                                (terminal ? 0 : (gamma * qNextA * (qNextA - mNext)))
                        //(gamma * qNextA * (qNextA - mNext))//*(1 - dones)
                ) - qPrevA;

//                dq[a] = PRI * action[a] *(
//                     reward +
//                    m_alpha * clampSafe(mPrev, lo, 0) +
//                    (terminal ? 0 : (gamma * qNextA * (qNextA - mNext)))
//                    //(gamma * qNextA * (qNextA - mNext))//*(1 - dones)
//                ) - qPrevA;

            } else {
                /* estimate of optimal future value */
                double gq = sarsaOrQ ? gamma * qNextA : gammaQNextMax;

                //dq[a] = PRI * action[a] * reward + gq - qPrevA;
                dq[a] = PRI * (action[a] * (reward + gq - qPrevA));
//

                //dq[a] = PRI * action[a] * (reward + gq) - qPrevA;

//                double ar =
//                    (reward >= 0.5f ? (action[a] * polarize(reward)) : ((1-action[a]) * -polarize(reward)));
//                    //(reward >= 0.5f ? (action[a] * reward) : ((1-action[a]) * (1-reward)));
//                dq[a] = PRI * ar + gq - qPrevA;

//                dq[a] = PRI * (
//                              action[a]     *    (reward + gq)
//                              -
//                              (1-action[a]) * ((1-reward) - (1-gq))
//                        ) - qPrevA;

//                dq[a] = PRI * (
//                            action[a]     *    (reward + gq)
//                            - (1-action[a]) * ((1-reward) - (1-gq))
//                        ) - qPrevA;


//                dq[a] = PRI * (
//                        action[a]     *    (reward)
//                        -  (1-action[a]) * ((1-reward))
//                        + gq
//                ) - qPrevA;

                //dq[a] = action[a] * reward + gq - qPrev[a]);

                //dq[a] = action[a] * (reward + gq - qPrev[a]);

            }

            //LOSS
            //https://github.com/jlow2499/LinearRegressionByStochasitcGradientDescent/blob/master/SGD.R
            /* nothing, as-is */ //MSE
            //Util.mul(2, dq); //L2
            //Util.mul(1.0/Util.sumAbs(dq), dq); //L1


        }

        if (p instanceof jcog.predict.DeltaPredictor) {
            if (tdErrClamp == tdErrClamp) {
                clampSafe(dq, -tdErrClamp, +tdErrClamp);
                //Util.normalizePolar(dq, tdErrClamp); //TODO this may only work if tdErrClamp=1
            }
            ((jcog.predict.DeltaPredictor) p).putDelta(dq, learn.floatValue());
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