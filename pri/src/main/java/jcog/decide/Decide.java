package jcog.decide;

import jcog.Util;
import jcog.random.XoRoShiRo128PlusRandom;

import java.util.function.ToIntFunction;

/**
 * returns an integer in the range of 0..n for the input vector of length n
 */
@FunctionalInterface
public interface Decide extends ToIntFunction<float[]> {

    /** argmax (greedy), with shuffling in case of a tie */
    Decide Greedy = new DecideEpsilonGreedy(0, new XoRoShiRo128PlusRandom(1));

    default int applyAsInt(double... x) {
        return applyAsInt(Util.toFloat(x));
    }

}