package jcog.activation;

import org.eclipse.collections.api.block.function.primitive.DoubleToDoubleFunction;

public interface DiffableFunction extends DoubleToDoubleFunction  {


    double derivative(double x);

//    /** clamps to 0..1 */
//    DiffableFunction DirectUnitize = new DiffableFunction() {
//        @Override
//        public double derivative(double x) {
//            return 1;
//        }
//
//        @Override
//        public double valueOf(double v) {
//            return Util.unitize(v);
//        }
//
//    };

    default void applyTo(float[] y, int from, int to) {
        for (int o = from; o < to; o++)
            y[o] = (float)valueOf(y[o]);
    }
    default void applyTo(double[] y, int from, int to) {
        for (int o = from; o < to; o++)
            y[o] = valueOf(y[o]);
    }
}