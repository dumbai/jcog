package jcog.data;

import jcog.Util;

import static java.lang.Math.sqrt;

@FunctionalInterface
public interface DistanceFunction {

    //TODO fail fast version:
    // double distance(double[] a, double[] b, double distMin, double distMax);

    double distance(double[] a, double[] b);

    static double distanceCartesianSq(double[] x, double[] y) {
        int l = y.length;
        double sum = 0;
        for (int i = 0; i < l; i++)
            sum += Util.sqr(y[i] - x[i]);
        return sum;
    }

    static double distanceCartesian(double[] x, double[] y) {
        return sqrt(distanceCartesianSq(x,y));
    }

    static double distanceCartesianSq(float[] x, float[] y) {
        int l = y.length;
        double sum = 0.0;
        for (int i = 0; i < l; i++)
            sum += Util.sqr(y[i] - x[i]);
        return sum;
    }

    static double distanceManhattan(double[] x, double[] y) {
        int l = y.length;
        double sum = 0;
        for (int i = 0; i < l; i++)
            sum += Math.abs(y[i] - x[i]);
        return sum;
    }

}