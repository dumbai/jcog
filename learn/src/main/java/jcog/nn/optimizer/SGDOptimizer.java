package jcog.nn.optimizer;

import jcog.Util;
import jcog.nn.layer.DenseLayer;
import jcog.signal.FloatRange;

import static jcog.Util.lerpSafe;

/**
 * 'vanilla' stochastic gradient descent (SGD), with optional:
 *      momentum
 *      L1-regularization weight decay
 */
public class SGDOptimizer extends BatchWeightUpdater {

    /**
     * gradient momentum.  The coefficient for how much of the previous delta is applied to each weight.
     * In theory, prevents local minima stall.
     *
     * TODO FloatRange
     */
    public float dwMomentum;

    private transient float alpha;

    public static final float WEIGHT_DECAY_DEFAULT =
        //1.0E-3f;
        1.0E-4f;
        //1.0E-5f;

    public final FloatRange weightDecay = FloatRange.unit(WEIGHT_DECAY_DEFAULT);

    public static final double wEpsilon =
        1.0E-24;
        //0;
        //1;
        //0.5;
        //1.0E-1;
        //2.0E-2;
        //8.0E-3;

//    /** whether weight decay is scaled by L1(delta) */
//    private static final boolean weightDecayDX = false;


    public SGDOptimizer(float dwMomentum) {
        this.dwMomentum = dwMomentum;
    }

    @Override
    public void reset(int weights, float alpha) {
        super.reset(weights, alpha);
        this.alpha = alpha;
    }

    @Override protected void updateWeights(DenseLayer l, double[] dW, double[] dWPrev, double[] W) {
        double pri = this.alpha;
        final float _weightDecayRate = this.weightDecay.floatValue();
        boolean weightDecaying = _weightDecayRate > 0;
        double wL1 = weightDecaying ? Util.sumAbs(W) : 0;
        double weightDecayRate =
                //_weightDecayRate / (wEpsilon + wL1);
                _weightDecayRate / (1 + wL1);

        float dwMomentum = this.dwMomentum;

        int n = l.ins() * l.outs();
        for (int io = 0; io < n; io++) {
            double dwNext = lerpSafe(dwMomentum, dW[io], dWPrev[io]);
            dWPrev[io] = dwNext;

            double wPrev = W[io];
//            double wDecay = weightDecaying ? 1 - Math.abs(wPrev) / (1.0E-16 + wL1) * weightDecayRate : 1;
//            double wNext = wPrev * wDecay + dwNext;
//            W[io] = lerpSafe(lr, wPrev, wNext);

            if (weightDecaying) {
//                double decayFactor = 1 -
//                        weightDecayRate * pri *
//                                Math.abs(wPrev) / (wEpsilon + wL1);
//                wPrev *= decayFactor;

                double decayed = wPrev * weightDecayRate;
                dwNext -= decayed;
//                wPrev -= decayed;
            }

            W[io] =
                Util.fma(dwNext, pri, wPrev);
                //wPrev + dwNext * pri;
        }
    }

}