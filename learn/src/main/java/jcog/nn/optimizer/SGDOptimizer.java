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
        1.0E-3f;
        //1.0E-4f;
        //1.0E-5f;
        //0; //disabled

    public final FloatRange weightDecay = FloatRange.unit(WEIGHT_DECAY_DEFAULT);

    @Deprecated public static final double wEpsilon =
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
        float _weightDecayRate = this.weightDecay.floatValue();
        boolean weightDecaying = _weightDecayRate > 0;
        double wL1 = weightDecaying ? Util.sumAbs(W) : 0;
        double weightDecayRate =
                //_weightDecayRate / (wEpsilon + wL1);
                _weightDecayRate / (1 + wL1);

        float dwMomentum = this.dwMomentum;
        boolean momentum = dwMomentum > 0;

        int n = l.ins() * l.outs();
        for (int io = 0; io < n; io++) {
            double dwP = dWPrev[io];
            double dwN = dW[io];

            double dw = momentum ? lerpSafe(dwMomentum, dwN, dwP) : dwN;

            dWPrev[io] = dw;

            double wP = W[io];

            if (weightDecaying) {
                double decay = wP * weightDecayRate;
                dw -= decay;
            }

            double wN =
                Util.fma(dw, pri, wP);
                //wPrev + dwNext * pri;

            W[io] = wN;
        }
    }

}