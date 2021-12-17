package jcog.decision.impurity;


import jcog.decision.data.SimpleValue;
import jcog.decision.label.BooleanLabel;
import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImpurityCalculatorTest {

    @Test
    void testGetEmpiricalProbability50_50() {
        Function value1 = SimpleValue.data(new String[]{"a"}, BooleanLabel.TRUE_LABEL);
        Function value2 = SimpleValue.data(new String[]{"a"}, BooleanLabel.FALSE_LABEL);
        Stream<Function<String,BooleanLabel>> f = Stream.of(value1, value2);
        assertEquals(0.5, ImpurityCalculator.empiricalProb("a", f, BooleanLabel.TRUE_LABEL), 0.001);
    }
    
}
