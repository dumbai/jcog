package jcog.activation;

import static java.lang.Math.exp;
import static java.lang.Math.tanh;
import static jcog.Util.sqr;

/** https://arxiv.org/abs/1908.08681 */
public final class MishActivationFunction implements DiffableFunction {

    public static DiffableFunction the = new MishActivationFunction();

    private MishActivationFunction() {

    }

    @Override
    public double valueOf(double x) {
        return x * tanh( SoftPlusActivation.the.valueOf(x));
    }
    @Override
    public double derivative(double x) {
        double w = 4*(x+1) + 4*exp(2*x) + exp(3*x) + exp(x)*(4*x+6);
        double l = 2*exp(x) + exp(2*x) + 2;
        return exp(x) * w / sqr(l);
    }


}