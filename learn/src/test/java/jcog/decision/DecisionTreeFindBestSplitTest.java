package jcog.decision;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static jcog.decision.data.SimpleValue.data;
import static jcog.decision.feature.PredicateFeature.feature;
import static jcog.decision.label.BooleanLabel.FALSE_LABEL;
import static jcog.decision.label.BooleanLabel.TRUE_LABEL;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DecisionTreeFindBestSplitTest {
    
    @Test
    void testBooleanSplit() {
        DecisionTree<String, Object> tree = new DecisionTree();
        String labelColumnName = "answer";
        
        String[] headers = {labelColumnName, "x1", "x2"};
        List<Function<String,Object>> dataSet = Lists.newArrayList();
        dataSet.add(data(headers, TRUE_LABEL, true, true));
        dataSet.add(data(headers, FALSE_LABEL, true, false));
        dataSet.add(data(headers, FALSE_LABEL, false, true));
        dataSet.add(data(headers, FALSE_LABEL, false, false));
        
        List<Function<Function<String,Object>,Object>> features = List.of(
            feature("x1", true),
            feature("x2", true),
            feature("x1", false),
            feature("x2", false)
        );
        
        
        Function<Function<String,Object>,Object> bestSplit = tree.bestSplit(labelColumnName, dataSet::stream, features.stream());
        assertEquals("x1 = true", bestSplit.toString());

        Map<Object, List<Function<String,Object>>> split = DecisionTree.split(bestSplit, dataSet::stream);
        
        
        assertEquals(TRUE_LABEL, split.get(FALSE).get(0).apply(labelColumnName));
        assertEquals(FALSE_LABEL, split.get(FALSE).get(1).apply(labelColumnName));
        assertEquals(FALSE_LABEL, split.get(TRUE).get(0).apply(labelColumnName));
        assertEquals(FALSE_LABEL, split.get(TRUE).get(1).apply(labelColumnName));


        Function<Function<String, Object>, Object> newBestSplit = tree.bestSplit(labelColumnName, () -> split.get(FALSE).stream(), features.stream());
        assertEquals("x2 = true", newBestSplit.toString());

        Map<Object, List<Function<String, Object>>> newSplit = DecisionTree.split(newBestSplit, split.get(FALSE)::stream);
        assertEquals(TRUE_LABEL, newSplit.get(FALSE).get(0).apply(labelColumnName));
        assertEquals(FALSE_LABEL, newSplit.get(TRUE).get(0).apply(labelColumnName));
    }

}
