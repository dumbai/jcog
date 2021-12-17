package jcog.deep;

import jcog.Util;

import java.util.Arrays;
import java.util.Random;
import java.util.function.DoubleFunction;

import static jcog.deep.utils.binomial;
import static jcog.deep.utils.uniform;

public class HiddenLayer {
    public final int n_in;
    public final int n_out;
    public final double[][] W;
    public final double[] b;
    public final Random rng;
    public DoubleFunction<Double> activation;
    public DoubleFunction<Double> dactivation;

    public HiddenLayer(int n_in, int n_out, double[][] W, double[] b, Random rng, String activation) {
        this.n_in = n_in;
        this.n_out = n_out;

		this.rng = rng == null ? new Random(1234) : rng;

        if (W == null) {
            this.W = new double[n_out][n_in];
            double a = 1.0 / this.n_in;

            for(int i=0; i<n_out; i++) {
                for(int j=0; j<n_in; j++) {
                    this.W[i][j] = uniform(-a, a, rng);
                }
            }
        } else {
            this.W = W;
        }

		this.b = b == null ? new double[n_out] : b;

        if (activation == null || "sigmoid".equals(activation)) {
            this.activation = Util::sigmoid;
            this.dactivation = utils::dsigmoid;
        } else if ("tanh".equals(activation)) {
            this.activation = Math::tanh;
            this.dactivation = utils::dtanh;
        } else if ("ReLU".equals(activation)) {
            this.activation = utils::ReLU;
            this.dactivation = utils::dReLU;
        } else {
            throw new IllegalArgumentException("activation function not supported");
        }

    }

    public double output(double[] input, double[] w, double b) {
        int bound = n_in;

        double linear_output = 0;
        for (int j = 0; j < bound; j++)
            linear_output += w[j] * input[j];

        linear_output += b;

        return activation.apply(linear_output);
    }


    public void forward(double[] input, double[] output) {
        for(int i=0; i<n_out; i++) {
            output[i] = this.output(input, W[i], b[i]);
        }
    }

    public void backward(double[] input, double[] dy, double[] prev_layer_input, double[] prev_layer_dy, double[][] prev_layer_W, double lr) {
        if(dy == null) dy = new double[n_out];

        int prev_n_in = n_out;
        int prev_n_out = prev_layer_dy.length;

        for(int i=0; i<prev_n_in; i++) {
            dy[i] = 0;
            for(int j=0; j<prev_n_out; j++) {
                dy[i] += prev_layer_dy[j] * prev_layer_W[j][i];
            }

            dy[i] *= dactivation.apply(prev_layer_input[i]);
        }

        for(int i=0; i<prev_n_in; i++) {
            for(int j=0; j<n_in; j++) {
                W[i][j] += lr * dy[i] * input[j];
            }
            b[i] += lr * dy[i];
        }
    }

    static double[] dropout(int size, double p, Random rng) {
        double[] mask = new double[10];
        int count = 0;
        for (int i = 0; i < size; i++) {
            double binomial = binomial(1, p, rng);
            if (mask.length == count) mask = Arrays.copyOf(mask, count * 2);
            mask[count++] = binomial;
        }
        mask = Arrays.copyOfRange(mask, 0, count);

        return mask;
    }
}