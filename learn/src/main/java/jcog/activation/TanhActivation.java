package jcog.activation;

import jcog.Util;

public class TanhActivation implements DiffableFunction
{
    public static final TanhActivation the = new TanhActivation();

    @Override
    public double valueOf(double x) {
        return Math.tanh(x);
    }

    @Override
    public double derivative(double x) {
        return 1 - Util.sqr(Math.tanh(x));
    }

}