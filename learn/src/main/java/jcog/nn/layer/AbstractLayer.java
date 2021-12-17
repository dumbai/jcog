package jcog.nn.layer;

import jcog.nn.optimizer.WeightUpdater;
import jcog.random.RandomBits;

import java.util.Random;

public abstract class AbstractLayer {
    public double[] out;
    public double[] in;


    protected AbstractLayer(int inputSize, int outputSize) {
        out = new double[outputSize];
        in = new double[inputSize];
    }

    protected AbstractLayer() {

    }

    /** https://intoli.com/blog/neural-network-initialization/ */
    public abstract void randomize(Random r);

    public abstract double[] forward(double[] x, RandomBits rng);

    //public abstract double[] reverse(double[] dx, float learningRate);

    public int ins() {
        return in.length;
    }

    public int outs() {
        return out.length;
    }

    public void startNext() {

    }

    abstract public double[] delta(WeightUpdater updater, double[] dx);

}