package jcog.activation;

import jcog.Util;

public class SigmoidActivation implements DiffableFunction {
    public static final SigmoidActivation the = new SigmoidActivation();

    private SigmoidActivation() {

    }

//    public static double expFast(double val) {
//        long tmp = (long) (1512775 * val + (1072693248 - 60801));
//        return Double.longBitsToDouble(tmp << 32);
//    }

    @Override
    public double valueOf(double x) {
        return Util.sigmoid(x);
    }

    @Override
    public double derivative(double x) {
        return x * (1 - x);
    }


}