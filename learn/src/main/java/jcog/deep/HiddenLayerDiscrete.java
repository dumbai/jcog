package jcog.deep;

import jcog.Util;

import java.util.Random;
import java.util.stream.IntStream;

import static jcog.deep.utils.binomial;
import static jcog.deep.utils.uniform;

public class HiddenLayerDiscrete extends HiddenLayer {
    public int N;
    public int n_in;
    public int n_out;
    public double[][] W;
    public double[] b;
    public Random rng;


    public HiddenLayerDiscrete(int n_in, int n_out, double[][] W, double[] b, Random rng) {
        super(n_in, n_out, W, b, rng, null);

        this.n_in = n_in;
        this.n_out = n_out;

		this.rng = rng == null ? new Random(1234) : rng;

        if(W == null) {
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
    }

    public double output(int[] input, double[] w, double b) {
        int bound = n_in;
        double linear_output = IntStream.range(0, bound).mapToDouble(j -> w[j] * input[j]).sum();
        linear_output += b;
        return Util.sigmoid(linear_output);
    }

    public void sample_h_given_v(double[] input, double[] sample) {
        for(int i=0; i<n_out; i++) {
            sample[i] = binomial(1, output(input, W[i], b[i]), rng);
        }
    }
}
