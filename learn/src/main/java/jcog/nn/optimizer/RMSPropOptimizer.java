package jcog.nn.optimizer;

import jcog.Util;
import jcog.activation.DiffableFunction;
import jcog.data.bit.MetalBitSet;
import jcog.nn.layer.DenseLayer;
import jcog.util.ArrayUtil;

import static jcog.Util.fma;
import static jcog.Util.lerpSafe;

public class RMSPropOptimizer implements WeightUpdater {


    static private final boolean eps_inside_sqrt = true;

    /**
     * alpha, learning rate
     */
    public double alpha;

    /**
     * aka "Aleph"
     */
    private final double momentum =
            .99;
    //0.9;

    private final double eps =
            1.0e-8;
    //1.0e-2;

    /**
     * "mean-square" running average
     */
    private double[] ms = ArrayUtil.EMPTY_DOUBLE_ARRAY;
    private int pAbs;

    @Override
    public void reset(int weights, float alpha) {
        if (ms.length != weights) ms = new double[weights];

        this.alpha = alpha;
        pAbs = 0;
    }

    @Override
    public void update(DenseLayer l, double[] deltaIn) {
        int ii = l.ins(), oo = l.outs();

        double[] W = l.W;
        double[] out = l.out;
        DiffableFunction act = l.activation;
        double[] dW = l.dW;
        MetalBitSet set = l.enabled;
        double[] in = l.in;
        double alpha = this.alpha;
        double eps = this.eps;


        double momentum = this.momentum;
        int pAbs = this.pAbs, pRel = 0;
        double[] ms = this.ms;

        for (int o = 0; o < oo; o++) {

            double OO = deltaIn[o];
            if (!Double.isFinite(OO))
                continue;

            double dxo = OO * act.derivative(out[o]);

            double[] delta = l.delta;

            for (int i = 0; i < ii; i++, pAbs++, pRel++) {
                //TODO reconcile dropout behavior with Momentum impl
//                    delta[i] = fma(W[io], dxo, delta[i]);
//                    dW[io] = set.test(io) ? in[i] * dxo : 0;

//                    if (set.test(io))
//                        delta[i] = fma(W[io], dxo, delta[i]);
//                    dW[io] = in[i] * dxo;

                //ignore dropout state:
                double II = in[i];
//                    if (Double.isFinite(II)) {
                //dW[io] = lr * II * dxo;

                double dw = dW[pRel] = /*gradClamp*/(II * dxo);
                double m = lerpSafe(momentum, Util.sqr(dw), ms[pAbs]);
                ms[pAbs] = m;

                double denom =
                        eps_inside_sqrt ? Math.sqrt(m + eps) : Math.sqrt(m) + eps;

                W[pRel] += alpha * dw / denom;
//                    }

                delta[i] = fma(W[pRel], dxo, delta[i]);
            }

        }

        this.pAbs = pAbs;
    }

}