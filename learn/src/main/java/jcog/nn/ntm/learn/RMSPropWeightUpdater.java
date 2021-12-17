package jcog.nn.ntm.learn;

import jcog.Util;
import jcog.nn.ntm.control.UVector;
import jcog.nn.ntm.control.Unit;


/**
 *  Support class for RMSProp (Graves variation)
 *  https://medium.com/konvergen/accelerating-the-adaptive-methods-rmsprop-momentum-and-adam-aa5249ef1e78
 *  https://arxiv.org/pdf/1308.0850v5.pdf
 *  https://github.com/chainer/chainer/blob/master/chainer/optimizers/rmsprop.py#L72
 * */
@Deprecated public class RMSPropWeightUpdater implements IWeightUpdater {

    private final double __GradientMomentum;
    private final double __DeltaMomentum;
    public double alpha;
    private final double __ChangeAddConstant;
    private final double[] n;
    private final double[] g;
    private final double[] deltaPrev;
    /**
     * time index
     */
    private int i;
    private boolean neg;


    public RMSPropWeightUpdater(int weights, double alpha) {
        this(weights, alpha,
                0.99, 0.9, 1.0E-3//??
//                0.5, 0.5, 0.001 //??
                //0.95f, 0.9f, 0.0001 //values in: Generating Sequences WithRecurrent Neural Networks
                //0.95, 0.9f,1E-4 //??
//                 0.05, 0.05, 0.001 //??
        );
    }

    private RMSPropWeightUpdater(int weights, double alpha, double gradientMomentum, double deltaMomentum, double changeAddConstant) {
        n = new double[weights];
        g = new double[weights];
        deltaPrev = new double[weights];

        __GradientMomentum = gradientMomentum;
        __DeltaMomentum = deltaMomentum;
        this.alpha = alpha;
        __ChangeAddConstant = changeAddConstant;
    }

    @Override
    public void reset() {
        i = 0;
    }

    @Deprecated
    @Override
    public void update(Unit x) {

        double e = x.grad;

        double a = __GradientMomentum;

        double ni = n[i] =
            a * n[i] + (1 - a) * e * e;
        double gi = g[i] =
            a * g[i] + (1 - a) * e;

        double[] delta = this.deltaPrev;

        x.value += (delta[i] =
                delta(delta[i], e, ni, gi, __DeltaMomentum, _alpha(), __ChangeAddConstant));
        i++;
    }

    @Override
    public void update(UVector unit) {
        update(unit.value, unit.grad);
    }


    public void update(double[] value, double[] delta) {
        double changeConst = __ChangeAddConstant;
        double deltaMomentum = __DeltaMomentum;

        double alpha = _alpha();

        double m = __GradientMomentum; //"Aleph"

        double[] deltaPrev = this.deltaPrev;

        double[] n = this.n;
        double[] g = this.g;
        int i = this.i;
        for (int x = 0; x < value.length; x++, i++) {

            double e = delta[x];
//            double ugradGM = (1 - gm) * e;
//            double ni = n[t] = gm * n[t] + ugradGM * e;
//            double gi = g[t] = gm * g[t] + ugradGM;

            double ni = n[i] =
                    m * n[i] + (1 - m) * e * e;
            double gi = g[i] =
                    m * g[i] + (1 - m) * e;

            value[x] += (deltaPrev[i] =
                delta(deltaPrev[i], e, ni, gi, deltaMomentum, alpha, changeConst));
        }
        this.i = i;
    }

    private double _alpha() {
        return this.alpha * (neg ? -1 : +1);
    }

    private static double delta(double deltaPrev, double deltaNext, double ni, double gi, double deltaMomentum, double alpha, double changeConst) {
        double factor = 1 / Math.sqrt(ni - Util.sqr(gi) + changeConst);
        if (!Double.isFinite(factor))
            factor = 0; //HACK

        return
            deltaPrev * deltaMomentum
            +
            deltaNext * alpha * factor;
    }

    public RMSPropWeightUpdater negate() {
        this.neg = true;
        return this;
    }
}