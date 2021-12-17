package jcog.deep;

import jcog.Util;

import java.util.Random;
import java.util.stream.IntStream;

public class RBM {
    private final double[] ph_mean;
    private final double[] nh_samples;
    private final double[] ph_sample;
    private final double[] nv_means;
    private final double[] nv_samples;
    private final double[] nh_means;
    private final double[] h;
    public final int n_visible;
    public final int n_hidden;
    public final double[][] W;
    public final double[] hbias;
    public final double[] vbias;
    public final Random rng;


    public RBM(int n_visible, int n_hidden,
               double[][] W, double[] hbias, double[] vbias, Random rng) {
        this.n_visible = n_visible;
        this.n_hidden = n_hidden;

        ph_mean = new double[n_hidden];
        ph_sample = new double[n_hidden];
        nv_means = new double[n_visible];
        nv_samples = new double[n_visible];
        nh_means = new double[n_hidden];
        nh_samples = new double[n_hidden];

        h = new double[n_hidden];

		this.rng = rng == null ? new Random(1234) : rng;

        if (W == null) {
            this.W = new double[this.n_hidden][this.n_visible];
            double a = 1.0; 

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


    public void contrastive_divergence(double[] input, double lr, int k) {


        /* CD-k */
        sample_h_given_v(input, ph_mean, ph_sample);

        for (int step = 0; step < k; step++) {
            gibbs_hvh(step == 0 ? ph_sample : nh_samples, nv_means, nv_samples, nh_means, nh_samples);
        }

        for (int i = 0; i < n_hidden; i++) {
            for (int j = 0; j < n_visible; j++) {
                
                W[i][j] += lr * (ph_mean[i] * input[j] - nh_means[i] * nv_samples[j]) /* / N */;
            }
            hbias[i] += lr * (ph_sample[i] - nh_means[i]) /* / N*/;
        }


        for (int i = 0; i < n_visible; i++) {
            vbias[i] += lr * (input[i] - nv_samples[i]) /* / N*/;
        }

    }


    public void sample_h_given_v(double[] v0_sample, double[] mean, double[] sample) {
        for (int i = 0; i < n_hidden; i++) {
            mean[i] = propup(v0_sample, W[i], hbias[i]);
            sample[i] = (int) utils.binomial(1, mean[i], rng);
        }
    }

    public void sample_v_given_h(double[] h0_sample, double[] mean, double[] sample) {
        for (int i = 0; i < n_visible; i++) {
            mean[i] = propdown(h0_sample, i, vbias[i]);
            sample[i] = (int) utils.binomial(1, mean[i], rng);
        }
    }

    public double propup(double[] v, double[] w, double b) {
        int bound = n_visible;
        double pre_sigmoid_activation = IntStream.range(0, bound).mapToDouble(j -> w[j] * v[j]).sum();
        pre_sigmoid_activation += b;
        return activate(pre_sigmoid_activation);
    }

    public double activate(double a) {
        return Util.sigmoid(a);
    }

    public double propdown(double[] h, int i, double b) {
        int bound = n_hidden;
        double pre_sigmoid_activation = IntStream.range(0, bound).mapToDouble(j -> W[j][i] * h[j]).sum();
        pre_sigmoid_activation += b;
        return activate(pre_sigmoid_activation);
    }

    void gibbs_hvh(double[] h0_sample, double[] nv_means, double[] nv_samples, double[] nh_means, double[] nh_samples) {
        sample_v_given_h(h0_sample, nv_means, nv_samples);
        sample_h_given_v(nv_samples, nh_means, nh_samples);
    }




    public void reconstruct(double[] v, double[] reconstructed_v) {

        for (int i = 0; i < n_hidden; i++) {
            h[i] = propup(v, W[i], hbias[i]);
        }

        for (int i = 0; i < n_visible; i++) {
            double a = 0.0;
            for (int j = 0; j < n_hidden; j++) {
                a += W[j][i] * h[j];
            }
            a += vbias[i];

            reconstructed_v[i] = activate(a);
        }
    }


}
