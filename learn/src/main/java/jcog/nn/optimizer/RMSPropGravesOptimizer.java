package jcog.nn.optimizer;

import jcog.activation.DiffableFunction;
import jcog.data.bit.MetalBitSet;
import jcog.nn.layer.DenseLayer;
import jcog.nn.ntm.learn.RMSPropWeightUpdater;

import static jcog.Util.fma;

/**
 * https://medium.com/konvergen/accelerating-the-adaptive-methods-rmsprop-momentum-and-adam-aa5249ef1e78
 */
public class RMSPropGravesOptimizer implements WeightUpdater {

    RMSPropWeightUpdater u;

    @Override
    public void reset(int weights, float alpha) {
        if (u == null)
            u = new RMSPropWeightUpdater(weights, alpha);
        u.reset();
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
        for (int io = 0, o = 0; o < oo; o++) {

            double dxo = deltaIn[o] * act.derivative(out[o]);

            final double[] delta = l.delta;
            for (int i = 0; i < ii; i++, io++) {
                delta[i] = fma(W[io], dxo, delta[i]);
                dW[io] = set.test(io) ? in[i] * dxo : 0;
            }

        }
        u.update(W, dW);
    }
}