package jcog.nn.optimizer;

import jcog.Util;
import jcog.nn.layer.DenseLayer;

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

    /** TODO FloatRange */
    private double weightDecay =
            1.0E-3;
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
        double lr = this.alpha;
        boolean weightDecaying = weightDecay > 0;
        double wL1 = weightDecaying ? Util.sumAbs(W) : 0;
        double weightDecayRate = this.weightDecay;

        float dwMomentum = this.dwMomentum;

        int n = l.ins() * l.outs();
        for (int io = 0; io < n; io++) {
            double dwNext = lerpSafe(dwMomentum, dW[io], dWPrev[io]);
            dWPrev[io] = dwNext;

            double wPrev = W[io];
            double wDecay = weightDecaying ? 1 - Math.abs(wPrev) / (1.0E-16 + wL1) * weightDecayRate : 1;

            W[io] =
                    lerpSafe(lr, wPrev, wPrev * wDecay + dwNext);
                    //lerpSafe(lr, wPrev, (wPrev + dwNext) * wDecay);
                    //lerpSafe(lr, (wPrev*wDecay), ((wPrev*wDecay) + dwNext));
                    //(wPrev * wDecay) + lr * dwNext;
        }
    }

}