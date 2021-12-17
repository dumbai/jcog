package jcog.activation;

public final class LinearActivation implements DiffableFunction {

    public static DiffableFunction the = new LinearActivation();

    private LinearActivation() { }

    @Override
    public double derivative(double x) {
        return 1;
    }

    @Override
    public double valueOf(double v) {
        return v;
    }
}