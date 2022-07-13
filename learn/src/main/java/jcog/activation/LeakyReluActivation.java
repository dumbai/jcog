package jcog.activation;

/** https://paperswithcode.com/method/leaky-relu
 *  https://www.quora.com/What-are-the-advantages-of-using-Leaky-Rectified-Linear-Units-Leaky-ReLU-over-normal-ReLU-in-deep-learning?share=1
 * */
public class LeakyReluActivation implements DiffableFunction {

    public static DiffableFunction the = new LeakyReluActivation(0.01);

    /** slope */
    final double a;

    public LeakyReluActivation(double slope) {
        this.a = slope;
    }

    @Override
    public double valueOf(double x) {
        return x <= 0 ? x * a : x;
    }

    @Override
    public double derivative(double x) { return x <= 0 ? a : +1; }

}