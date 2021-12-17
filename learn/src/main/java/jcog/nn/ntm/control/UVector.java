package jcog.nn.ntm.control;

import jcog.Util;

import java.util.Arrays;

/**
 * Created by me on 7/18/15.
 */
public class UVector {
    public final double[] value;
    public final double[] grad;


    public UVector(int size) {
        value = new double[size];
        grad = new double[size];
    }

    public void clear() {
        Arrays.fill(value, 0);
        Arrays.fill(grad, 0);
    }

    public int size() {
        return value.length;
    }

    public double sumGradientValueProducts() {
        double[] value = this.value;
        int bound = value.length;
        double sum = 0.0;
        for (int i = 0; i < bound; i++)
            sum += value[i] * grad[i];
        return sum;
    }

    public void valueMultiplySelf(double factor) {
        double[] value = this.value;
        for (int i = 0; i < value.length; i++)
            value[i] *= factor;
    }

    public void gradAddSelf(double[] a) {
        int j = 0;
        for (double aa : a)
            gradAddSelf(j++, aa);
    }

    public double gradAddSelf(int i, double dg) {
        return grad[i] += dg;
    }

    public void clearGrad() {
        Arrays.fill(grad, 0);
    }

    public void setDelta(double[] target) {
        double[] v = this.value;
        double[] g = this.grad;
        for (int j = 0; j < v.length; j++)
            g[j] = v[j] - target[j];
    }

    public void setDeltaDirect(double[] delta) {
        double[] g = this.grad;
        System.arraycopy(delta, 0, g, 0, g.length);
    }

    double sumDotSafe(double[] input) {
        double[] v = this.value;
        int s = size();
        double sum = 0;
        for (int i = 0; i < s; i++) {
            double ii = input[i];
            if (Double.isFinite(ii))
                sum = Util.fma(v[i], ii, sum);
        }
        return sum;
    }
}