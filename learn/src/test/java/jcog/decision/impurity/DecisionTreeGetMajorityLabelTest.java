package jcog.decision.impurity;

import com.google.common.collect.Lists;
import jcog.decision.DecisionTree;
import jcog.decision.TestValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static jcog.decision.label.BooleanLabel.FALSE_LABEL;
import static jcog.decision.label.BooleanLabel.TRUE_LABEL;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DecisionTreeGetMajorityLabelTest {


    @Test
    void testGetMajorityLabel() {
        List<UnaryOperator<Object>> data = Lists.newArrayList();
        data.add(new TestValue(TRUE_LABEL));
        data.add(new TestValue(FALSE_LABEL));
        data.add(new TestValue(TRUE_LABEL));
        data.add(new TestValue(FALSE_LABEL));
        data.add(new TestValue(FALSE_LABEL));
        assertEquals("false", DecisionTree.majority(null, (Stream) data.stream()).toString());
    }

    @Test
    void testGetMajorityLabelWhenEqualCounts() {
        List<UnaryOperator<Object>> data = Lists.newArrayList();
        data.add(new TestValue(TRUE_LABEL));
        data.add(new TestValue(FALSE_LABEL));
        data.add(new TestValue(TRUE_LABEL));
        data.add(new TestValue(FALSE_LABEL));
        assertEquals(String.valueOf(Boolean.FALSE), DecisionTree.majority(null, (Stream) data.stream()).toString());
    }
}