package jcog.activation;

public class LeakyReluActivation implements DiffableFunction {

    public static DiffableFunction the = new LeakyReluActivation(0.01);

    final double a;

    public LeakyReluActivation(double a) {
        this.a = a;
    }

    @Override
    public double valueOf(double x) {
        return x < 0 ? x * a : x;
    }

    @Override
    public double derivative(double x) {
        return x <= 0 ? a : +1;

//        if (x < -ReluActivation.thresh) return -a;
//        else if (x > ReluActivation.thresh) return 1;
//        else
//            return 0;
    }

}