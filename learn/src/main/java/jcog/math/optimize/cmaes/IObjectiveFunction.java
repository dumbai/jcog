package jcog.math.optimize.cmaes;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * Minimalistic interface of a single-objective function (fitness function) to be minimized.
 * the Predicate<double[]> is test for feasibility.
 */
public interface IObjectiveFunction extends ToDoubleFunction<double[]>, Predicate<double[]> {

    @Override
    default boolean test(double[] x) {
        return true;
    }
}