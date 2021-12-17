package jcog.nn.optimizer;

import jcog.nn.layer.DenseLayer;

public interface WeightUpdater {
    void reset(int weights, float alpha);

    /** @param deltaIn delta coming from next layer, backwards */
    void update(DenseLayer l, double[] deltaIn);
}