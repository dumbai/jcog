package jcog.activation;

/** https://en.wikipedia.org/wiki/Rectifier_(neural_networks)#Softplus */
public class SoftPlusActivation implements DiffableFunction {

    public static final SoftPlusActivation the = new SoftPlusActivation();

    private SoftPlusActivation() { }

    @Override
    public double valueOf(double x) {
        return Math.log(1+Math.exp(x));
    }

    @Override
    public double derivative(double x) {
        return 1/(1+Math.exp(-x));
    }

}