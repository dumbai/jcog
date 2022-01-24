package jcog.nn;

import jcog.Util;
import jcog.activation.DiffableFunction;
import jcog.activation.LinearActivation;
import jcog.nn.layer.AbstractLayer;
import jcog.nn.layer.AutoEncoderLayer;
import jcog.nn.layer.DenseLayer;
import jcog.nn.optimizer.AdamOptimizer;
import jcog.nn.optimizer.WeightUpdater;
import jcog.predict.DeltaPredictor;
import jcog.random.RandomBits;
import jcog.random.XoRoShiRo128PlusRandom;
import org.eclipse.collections.api.block.function.primitive.IntToObjectFunction;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

/**
 * a sequence of layers
 *
 * http:
 *
 * Notes ( http:
 * Surprisingly, the same robustness is observed for the choice of the neural
 network size and structure. In our experience, a multilayer perceptron with 2
 hidden layers and 20 neurons per layer works well over a wide range of applications.
 We use the tanh activation function for the hidden neurons and the
 standard sigmoid function at the output neuron. The latter restricts the output
 range of estimated path costs between 0 and 1 and the choice of the immediate
 costs and terminal costs have to be done accordingly. This means, in a typical
 setting, terminal goal costs are 0, terminal failure costs are 1 and immediate
 costs are usually set to a small value, e.g. c = 0.01. The latter is done with the
 consideration, that the expected maximum episode length times the transition
 costs should be well below 1 to distinguish successful trajectories from failures.
 As a general impression, the success of learning depends much more on the
 proper setting of other parameters of the learning framework. The neural network
 and its training procedure work very robustly over a wide range of choices.

 https://pabloinsente.github.io/the-multilayer-perceptron
 https://github.com/elixir-nx/axon
 */
public class MLP extends DeltaPredictor {

    private final int weightCount;
    private WeightUpdater updater =
        new AdamOptimizer();
        //new SGDOptimizer(0.5f);
        //new SGDLayer.RMSProp();
        //new SGDLayer.RMSPropGraves();

    public final AbstractLayer[] layers;


    public final MLP optimizer(WeightUpdater o) {
        this.updater = o;
        return this;
    }


    public interface LayerBuilder extends IntToObjectFunction<AbstractLayer> {
        int size();
    }

    /** no bias neuron */
    public static class Output implements LayerBuilder {
        final int size;

        public Output(int size) {
            this.size = size;
        }

        @Override
        public DenseLayer valueOf(int i) {
            return new DenseLayer(i, size, LinearActivation.the, false);
        }

        @Override
        public int size() {
            return size;
        }
    }

    /** fully-connected layer def */
    public static class Dense implements LayerBuilder {
        final int size;
        final DiffableFunction activation;
        boolean bias = true;

        public Dense(int size) {
            this(size, null);
        }

        public Dense(int size, DiffableFunction activation) {
            this.size = size;
            this.activation = activation;
        }

        public Dense bias(boolean b) {
            this.bias = b;
            return this;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public DenseLayer valueOf(int i) {
            return new DenseLayer(i, size, activation, bias);
        }
    }
    public static class AutoEncoderLayerBuilder implements LayerBuilder {

        private final int out;

        public AutoEncoderLayerBuilder(int out) {
            this.out = out;
        }

        @Override
        public int size() {
            return out;
        }

        @Override
        public AbstractLayer valueOf(int i) {
            return new AutoEncoderLayer(i, out);
        }
    }

    public MLP(int inputs, LayerBuilder... layer) {
        this(inputs, List.of(layer));
    }

    public MLP(int inputs, List<LayerBuilder> layer) {
        int L = layer.size();
        assert(L > 0);

        layers = Util.arrayOf(i ->
            layer.get(i).valueOf(i > 0 ? layer.get(i-1).size() : inputs),
            new AbstractLayer[L]);

        weightCount = Stream.of(layers).mapToInt(i -> i.ins()*i.outs()).sum();
    }

    public void clear() {
        clear(ThreadLocalRandom.current());
    }

    @Override public void clear(Random r) {
        for (AbstractLayer m : layers)
            m.randomize(r);
    }

//    public MLP randomize(float amp, Random r) {
//        for (MLPLayer m : layers)
//            m.randomize(amp, r);
//        return this;
//    }

    final RandomBits rng = new RandomBits(new XoRoShiRo128PlusRandom());

    @Override public double[] get(double[] x) {
        for (AbstractLayer layer : layers)
            x = layer.forward(x, rng);
        return x;
    }

    @Override public void putDelta(double[] dx, float pri) {
        AbstractLayer[] l = this.layers;
        for (AbstractLayer x : l)
            x.startNext();

        updater.reset(weightCount(), pri);
        for (int i = l.length - 1; i >= 0; i--)
            dx = l[i].delta(updater, dx);
    }

    /** TODO cache */
    private int weightCount() {
        return weightCount;
    }

}