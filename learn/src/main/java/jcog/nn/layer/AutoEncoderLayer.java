package jcog.nn.layer;

import jcog.TODO;
import jcog.Util;
import jcog.deep.AbstractAutoencoder;
import jcog.deep.Autoencoder;
import jcog.nn.optimizer.WeightUpdater;
import jcog.random.XoRoShiRo128PlusRandom;

import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * TODO weightUpdater?
 */
public class AutoEncoderLayer extends AbstractLayer {


    //float noise = 0;
    public final AbstractAutoencoder ae;

    final boolean normalize = false;

    float learningRate = 0.005f;

    public AutoEncoderLayer(int inputSize, int outputSize) {
        super(inputSize, outputSize);
        this.ae = new Autoencoder(inputSize, outputSize, new XoRoShiRo128PlusRandom());
        //ae.noise.set(noise);
        ae.normalize.set(normalize);
//                ae.activation = TanhActivation.the;
    }

    @Override
    public void randomize(Random r) {
        ae.clear(r);
    }

    @Override
    public double[] forward(double[] x, RandomGenerator rng) {
        System.arraycopy(x, 0, in, 0, x.length);
        if (ae instanceof Autoencoder) {
            ae.put(((Autoencoder) ae).x = Util.toFloat(this.in, ((Autoencoder) ae).x), learningRate); //HACK
            return out = Util.toDouble(((Autoencoder) ae).y, out);
        } else {
            ae.put(this.in, learningRate);
            throw new TODO();
        }
    }

    @Override
    public double[] delta(WeightUpdater updater, double[] dx) {
        return null; //new double[0];
    }
}