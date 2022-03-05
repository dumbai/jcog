package jcog.activation;

/**
 * Exponential Linear Unit
 * https://ml-cheatsheet.readthedocs.io/en/latest/activation_functions.html#elu */
public class EluActivation implements DiffableFunction {

    public static final EluActivation the = new EluActivation();

    private final double alpha;

    public EluActivation() {
        this(1);
    }

    public EluActivation(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public double valueOf(double z) {
        return z >= 0 ? z : alpha *
                Math.expm1(z)
                //(Math.exp(z) - 1)
        ;
    }

    @Override
    public double derivative(double x) {
        return x > 0 ? 1 : alpha * Math.exp(x);
    }

}