package jcog.activation;

/** https://kawahara.ca/what-is-the-derivative-of-relu/ */
public class ReluActivation implements DiffableFunction {

    public static DiffableFunction the = new ReluActivation();

    private ReluActivation() { }

    static final double thresh = Math.pow(Float.MIN_NORMAL, -4);

    @Override
    public double derivative(double x) {
        //HARD knee
        return x <= 0 ? 0 : 1;

//        //SOFT knee
//        if (x < -thresh) return 0;
//        else if (x > thresh) return 1;
//        else return 0.5;
    }

    /** https://kawahara.ca/what-is-the-derivative-of-relu/ */
    @Override public double valueOf(double x) {
        return x <= 0 ? 0 : x;
        //return Math.max(0, x);
    }
}