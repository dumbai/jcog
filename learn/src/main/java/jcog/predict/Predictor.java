package jcog.predict;

import jcog.Is;

import java.util.Random;
import java.util.function.UnaryOperator;

/** Map-like interface for learning associations of numeric vectors */
@Is("Autoassociative_memory")
public interface Predictor extends UnaryOperator<double[]>  {

    /*synchronized*/ double[] put(double[] x, double[] y, float pri);

    /** predict: analogous to Map.get */
    double[] get(double[] x);

    void clear(Random rng);

    @Override
    default /* final */ double[] apply(double[] x) { return get(x); }

//    public final double[] put(Tensor x, double[] y, float pri) {
//        return put(x.doubleArrayShared(), y, pri);
//    }

}