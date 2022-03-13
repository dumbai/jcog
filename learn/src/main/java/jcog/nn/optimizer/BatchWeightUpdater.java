package jcog.nn.optimizer;

import jcog.activation.DiffableFunction;
import jcog.data.bit.MetalBitSet;
import jcog.nn.layer.DenseLayer;

import java.util.Arrays;

import static jcog.Util.fma;

public abstract class BatchWeightUpdater implements WeightUpdater {
    int iteration = -1;
    protected int minibatches = 1;

    @Override
    public void reset(int weights, float alpha) {
        iteration++;
    }

    @Override public final void update(DenseLayer l, double[] dOut) {

        updateGrad(l, dOut);

        if (iteration % minibatches == 0)
            commitGrad(l);
    }

    private void commitGrad(DenseLayer l) {
        double[] dW = l.dW;
        updateWeights(l, dW, l.dWPrev, l.W);
        Arrays.fill(dW, 0);
    }

    private void updateGrad(DenseLayer l, double[] dOut) {
        int ii = l.ins(), oo = l.outs();

        double[] dW = l.dW;
        double[] W = l.W;
        double[] out = l.out;
        double[] dIn = l.delta;

        double[] in = l.in;
        DiffableFunction act = l.activation;

        boolean dropping = l.dropping;
        MetalBitSet e = dropping ? l.enabled : null;


        //update gradients
        for (int o = 0, io = 0; o < oo; o++) {

            double dOutO = dOut[o] * (act != null ? act/*TODO act[o]*/.derivative(out[o]) : 1);
            if (!Double.isFinite(dOutO))
                continue; //skip

            for (int i = 0; i < ii; i++, io++) {

                double inI;
                if (dOutO == 0 || (dropping && !e.test(io)) || !Double.isFinite(inI=in[i]))
                    continue; //skip

                dIn[i] = fma(W[io], dOutO, dIn[i]);
                dW[io] = fma(inI, dOutO, dW[io]);
            }
        }
    }

    protected abstract void updateWeights(DenseLayer l, double[] dW, double[] dWPrev, double[] w);


    public WeightUpdater minibatches(int i) {
        minibatches = i;
        return this;
    }
}