package jcog.deep;

import jcog.Util;

import java.util.Random;

public class dA {
    public final int n_visible;
    public final int n_hidden;
    public final double[][] W;
    public final double[] hbias;
    public final double[] vbias;
    public final Random rng;

    public dA(int n_visible, int n_hidden, Random rng) {
        this(n_visible, n_hidden, null, null, null, rng);
    }

    public dA(int n_visible, int n_hidden,
              double[][] W, double[] hbias, double[] vbias, Random rng) {
        this.n_visible = n_visible;
        this.n_hidden = n_hidden;

		this.rng = rng == null ? new Random(1234) : rng;

        if (W == null) {
            this.W = new double[this.n_hidden][this.n_visible];
            double a = 1.0 / this.n_visible;

            for (int i = 0; i < this.n_hidden; i++) {
                for (int j = 0; j < this.n_visible; j++) {
                    this.W[i][j] = utils.uniform(-a, a, rng);
                }
            }
        } else {
            this.W = W;
        }

        if (hbias == null) {
            this.hbias = new double[this.n_hidden];
            for (int i = 0; i < this.n_hidden; i++) this.hbias[i] = 0;
        } else {
            this.hbias = hbias;
        }

        if (vbias == null) {
            this.vbias = new double[this.n_visible];
            for (int i = 0; i < this.n_visible; i++) this.vbias[i] = 0;
        } else {
            this.vbias = vbias;
        }
    }

    public void get_corrupted_input(double[] x, double[] tilde_x, double p) {
        for (int i = 0; i < n_visible; i++) {
			tilde_x[i] = x[i] == 0 ? 0 : utils.binomial(1, p, rng);
        }
    }

    
    public double[] encode(double[] x, double[] y) {
        for (int i = 0; i < n_hidden; i++) {
            y[i] = 0;
            for (int j = 0; j < n_visible; j++) {
                y[i] += W[i][j] * x[j];
            }
            y[i] += hbias[i];
            y[i] = Util.sigmoid(y[i]);
        }
        return y;
    }

    
    public void decode(double[] y, double[] z) {
        for (int i = 0; i < n_visible; i++) {
            z[i] = 0;
            for (int j = 0; j < n_hidden; j++) {
                z[i] += W[j][i] * y[j];
            }
            z[i] += vbias[i];
            z[i] = Util.sigmoid(z[i]);
        }
    }

    public double[] train(double[] x, double lr, double corruption_level, double[] z) {
        double[] y = new double[n_hidden];
        assert(z.length == n_visible);

        double p = 1 - corruption_level;

        double[] tilde_x = new double[n_visible];
        get_corrupted_input(x, tilde_x, p);
        encode(tilde_x, y);


        double[] L_vbias = new double[n_visible];
        double[] L_hbias = new double[n_hidden];


        decode(y, z);

        
        for (int i = 0; i < n_visible; i++) {
            L_vbias[i] = x[i] - z[i];
            vbias[i] += lr * L_vbias[i];
        }

        
        for (int i = 0; i < n_hidden; i++) {
            L_hbias[i] = 0;
            for (int j = 0; j < n_visible; j++) {
                L_hbias[i] += W[i][j] * L_vbias[j];
            }
            L_hbias[i] *= y[i] * (1 - y[i]);
            hbias[i] += lr * L_hbias[i];
        }

        
        for (int i = 0; i < n_hidden; i++) {
            for (int j = 0; j < n_visible; j++) {
                W[i][j] += lr * (L_hbias[i] * tilde_x[j] + L_vbias[j] * y[i]);
            }
        }
        return y;
    }

    public void reconstruct(double[] x, double[] z) {
        double[] y = new double[n_hidden];

        encode(x, y);
        decode(y, z);
    }

    private static void test_dA() {
        Random rng = new Random(123);

        double learning_rate = 0.1;
        double corruption_level = 0.3;
        int training_epochs = 100;

        int train_N = 10;
        int n_visible = 20;
        int n_hidden = 5;

        double[][] train_X = {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0}
        };

        dA da = new dA(n_visible, n_hidden, null, null, null, rng);


        @Deprecated double[] z = new double[train_X[0].length];
        for (int epoch = 0; epoch < training_epochs; epoch++) {
            for (int i = 0; i < train_N; i++) {
                da.train(train_X[i], learning_rate, corruption_level, z);
            }
        }

        
        double[][] test_X = {
                {1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0}
        };

        int test_N = 2;
        double[][] reconstructed_X = new double[test_N][n_visible];

        
        for (int i = 0; i < test_N; i++) {
            da.reconstruct(test_X[i], reconstructed_X[i]);
            for (int j = 0; j < n_visible; j++) {
                System.out.printf("%.5f ", reconstructed_X[i][j]);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        test_dA();
    }

    public void randomize() {
        for (int i = 0; i < n_hidden; i++) {
            for (int j = 0; j < n_visible; j++) {
                W[i][j] = ((rng.nextFloat()-0.5f)*2f)/n_visible;
            }
        }
    }
}