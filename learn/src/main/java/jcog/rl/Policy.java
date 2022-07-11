package jcog.rl;

import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * RL value prediction policy
 */
public interface Policy  {

    void clear(Random rng);

    /** @return action vector */
    double[] learn(@Nullable double[] xPrev, double[] actionPrev, double reward, double[] x, float pri);

}

//dq[a] = Util.fma(alpha * action[a], ((1-gamma)*reward  + gq - qPrev[a]), dq[a]);
//dq[a] = Util.fma(alpha, (action[a] * (reward  + gq) - qPrev[a] ), dq[a]);
//dq[a] = Util.fma(alpha, (action[a] * (reward  ) + gq- qPrev[a] ), dq[a]);
//dq[a] = Util.fma(alpha, (action[a] * (reward+ gq - qPrev[a]) ), dq[a]);
//dq[a] = Util.fma(alpha, (action[a] * (reward ) + gq - qPrev[a]), dq[a]);



//double denom = (action[a] * reward) + ((1-action[a])*(1-reward));
//            if (denom < Float.MIN_NORMAL)
//                continue;

//    dq[a] = Util.fma(alpha, (action[a] * reward + gq - qPrev[a]), dq[a]);

//            dq[a] += alpha * (action[a] * (reward + gq) - qPrev[a])
//            dq[a] += alpha * action[a] * (reward + gq - qPrev[a])
//            dq[a] += alpha * (action[a] * (reward + gq - qPrev[a])
//                             +
//                             ((1-action[a]) * ((1-reward) + gq - qPrev[a]) )
//            )

//alpha * (action[a]/actionSum * reward + gq - qPrev[a])
//                    alpha * ((action[a] * reward) + gq - qPrev[a])
//                    alpha * ((action[a] * reward)/denom + gq - qPrev[a])
//                    alpha * action[a]*(reward/denom + gq - qPrev[a])
//                    alpha * ((action[a] * reward)/denom + action[a]*(gq - qPrev[a]))
//                    alpha * ((action[a] * reward ) ) - qNext[a]
////                    alpha * ((action[a] * (reward + gq)) - qPrev[a])
//                    //alpha * (action[a] * reward + (1-action[a]) * (1-reward) + gq - qPrev[a])
////                    alpha * action[a] * (reward + gq)
;
//experimental
//            Q[a] += alpha * action[a] * ((Fuzzy.polarize(reward) * (1-gamma)) + (qNext[a]-qPrev[a]) * gamma);