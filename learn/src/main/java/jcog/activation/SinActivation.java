package jcog.activation;

public class SinActivation implements DiffableFunction {

    public static final SinActivation the = new SinActivation(1);

    public final double freq;

    public SinActivation(double freq) {
        this.freq = freq * Math.PI/2;
    }

    @Override
    public double valueOf(double x) {
        return Math.sin(freq * x);
    }

    @Override
    public double derivative(double x) {
        return freq * Math.cos(freq * x);
    }

}