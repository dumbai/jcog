package jcog.decision.impurity;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Impurity calculation method of decision tree. It is used during training while trying to find best split. For example
 * one split results in 5 positive and 5 negative labels and another in 9 positive and 1 negative. Impurity calculator
 * should return high value in the first case and low value in the second. That means that during training we will
 * prefer second split as that will provide most information. Most popular methods are entropy and gini index.
 * <p>
 * Annotated as functional interface to allow lambda usage.
 *
 * @author Ignas
 */
@FunctionalInterface
public interface ImpurityCalculator {



    /**
     * Calculates impurity value. High impurity implies low information gain and more random labels of data which in
     * turn means that split is not very good.
     *
     *
     * @param value
     * @param splitData Data subset on which impurity is calculated.
     * @return Impurity.
     */
    <K, V> double impurity(K value, Supplier<Stream<Function<K, V>>> splitData);


    /**
     * Calculate and return empirical probability of positive class. p+ = n+ / (n+ + n-).
     *
     * @param splitData Data on which positive label probability is calculated.
     * @return Empirical probability.
     */
    static <K, V> double empiricalProb(K value, Stream<Function<K, V>> splitData, V positive) {
        int[] ratio = new int[2];
        splitData.map(d -> d.apply(value)).forEach(v -> {
            boolean eqP = v.equals(positive);
            ratio[eqP ? 1 : 0]++;
        });
        return ratio[1] / ((double)(ratio[0] + ratio[1]));
    }
}
