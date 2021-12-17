package jcog.deep;

import jcog.Util;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.util.ArrayUtil;

import java.util.Random;

import static jcog.Util.toDouble;
import static jcog.Util.toFloat;

/**
 * from: deeplearning.net 'sda.java'
 */
public class StackedAutoencoder extends AbstractAutoencoder {
    public int outs;
    @Deprecated
    transient double[] z;
    private int inputs;
    private int[] hidden_layer_sizes;
    private int layers;
    private HiddenLayer[] sigmoid_layers;
//    private LogisticRegression output;
    private dA[] dA_layers;

    public StackedAutoencoder(int inputs, int[] hidden_layer_sizes, int outputs, Random rng) {

        this.inputs = inputs;
        this.hidden_layer_sizes = hidden_layer_sizes;
        this.outs = outputs;
        this.layers = hidden_layer_sizes.length;

        this.sigmoid_layers = new HiddenLayer[layers];
        this.dA_layers = new dA[layers];

        for (int i = 0; i < this.layers; i++) {
            int ins = i == 0 ?
                    this.inputs : this.hidden_layer_sizes[i - 1];

            this.sigmoid_layers[i] = new HiddenLayer(ins, this.hidden_layer_sizes[i], null, null, rng, null);

            this.dA_layers[i] = new dA(ins, this.hidden_layer_sizes[i], this.sigmoid_layers[i].W, this.sigmoid_layers[i].b, null, rng);
        }

//        this.output = new LogisticRegression(this.hidden_layer_sizes[this.n_layers - 1], this.outs);
        clear(rng);

    }


    @Override
    public void clear(Random rng) {
        if (rng == null)
            rng = new XoRoShiRo128PlusRandom();
        this.rng = rng;

        for (var s : sigmoid_layers) {
            clear(s.W, 1f / s.W.length);
        }
        for (var h : dA_layers) {
            clear(h.W, 1f / h.W.length);
        }
    }

    private void update(double[] x, double lr, double corruption_level) {
        for (int i = 0; i < x.length; i++)
            x[i] = pre((float) x[i]);
        double[] layer_input = ArrayUtil.EMPTY_DOUBLE_ARRAY;
        double[][] inputs = new double[layers][];

        boolean train = lr > 0;
//        for (int i = 0; i < n_layers; i++) {
        for (int l = 0; l < layers; l++) {

            if (l == 0) {
                layer_input = x.clone();
            } else {
                int prev_layer_input_size = l == 1 ? this.inputs : hidden_layer_sizes[l - 2];

                double[] prev_layer_input = layer_input.clone();

                layer_input = new double[hidden_layer_sizes[l - 1]];

                sigmoid_layers[l - 1].forward(prev_layer_input, layer_input);

            }

            if (l == layers - 1) {
                double[] y = layer_input;
                if (normalize.getOpaque()) {
                    Util.normalize(y);
                }

                //post-process final layer
                for (int j = 0; j < y.length; j++)
                    y[j] = encodePost((float) y[j]);

            }

            inputs[l] = layer_input;
        }

        if (train) {
            for (int l = 0; l < layers; l++) {
                double[] li = inputs[l];
                double[] zi = new double[li.length];
                dA_layers[l].train(li, lr / x.length, corruption_level, zi);
                if (l == 0)
                    z = zi;
            }
        }
    }

    @Deprecated
    public void put(double[][] train_X, double[][] train_Y, double pri) {
        int N = train_X.length;
        for (int n = 0; n < N; n++)
            put(train_X[n], train_Y[n], pri / N);
    }

    public void put(double[] x, double[] y, double pri) {
        double[] layer_input = ArrayUtil.EMPTY_DOUBLE_ARRAY;

        double[] prev_layer_input;

        for (int i = 0; i < layers; i++) {
            if (i == 0) {
                prev_layer_input = new double[inputs];
                System.arraycopy(x, 0, prev_layer_input, 0, inputs);
            } else {
                prev_layer_input = new double[hidden_layer_sizes[i - 1]];
                System.arraycopy(layer_input, 0, prev_layer_input, 0, hidden_layer_sizes[i - 1]);
            }

            layer_input = new double[hidden_layer_sizes[i]];
            sigmoid_layers[i].forward(prev_layer_input, layer_input);

        }

//        output.put(layer_input, y, pri);
    }

    public void predict(double[] x, double[] y) {

        double[] prev_layer_input = new double[inputs];
        System.arraycopy(x, 0, prev_layer_input, 0, inputs);


        double[] layer_input = new double[0];
        for (int i = 0; i < layers; i++) {
            layer_input = new double[sigmoid_layers[i].n_out];

            for (int k = 0; k < sigmoid_layers[i].n_out; k++) {
                double linear_output = 0;

                for (int j = 0; j < sigmoid_layers[i].n_in; j++) {
                    linear_output += sigmoid_layers[i].W[k][j] * prev_layer_input[j];
                }
                linear_output += sigmoid_layers[i].b[k];
                layer_input[k] = Util.sigmoid(linear_output);
            }

            if (i < layers - 1) {
                prev_layer_input = new double[sigmoid_layers[i].n_out];
                System.arraycopy(layer_input, 0, prev_layer_input, 0, sigmoid_layers[i].n_out);
            } else {
                for (int j = 0; j < y.length; j++) {
                    y[j] = layer_input[j];
                }
            }
        }

//
//        for (int i = 0; i < output.n_out; i++) {
//            y[i] = 0;
//            for (int j = 0; j < output.n_in; j++) {
//                y[i] += output.W[i][j] * layer_input[j];
//            }
//            y[i] += output.b[i];
//        }
//
//        output.softmax(y);
    }

    /**
     * encode
     */
    @Override
    public void put(double[] x, float learnRate) {
        double[] y = new double[outs];

        update(x, learnRate, 0);

        dA_layers[layers - 1].encode(x, y);

        latent(toFloat(x), toFloat(y), toFloat(z)); //HACK
    }

    public float[] get(float[] x) {
        return toFloat(get(toDouble(x))); //HACK
    }

    public double[] get(double[] x) {
        double[] y = new double[outs];
        update(x, 0, 0);
        return dA_layers[layers - 1].encode(x, y);
    }

}