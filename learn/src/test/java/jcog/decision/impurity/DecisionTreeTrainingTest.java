package jcog.decision.impurity;

import jcog.decision.DecisionTree;
import jcog.decision.TableDecisionTree;
import jcog.decision.data.SimpleValue;
import jcog.decision.feature.P;
import jcog.table.DataTable;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.FloatColumn;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static jcog.decision.data.SimpleValue.classification;
import static jcog.decision.data.SimpleValue.data;
import static jcog.decision.feature.PredicateFeature.feature;
import static jcog.decision.label.BooleanLabel.FALSE_LABEL;
import static jcog.decision.label.BooleanLabel.TRUE_LABEL;
import static org.junit.jupiter.api.Assertions.*;

class DecisionTreeTrainingTest {


//    static final Function<JsonNode, @Nullable Function<String, Object>> jsonValue = (j) -> j::get;


	/**
	 * Test if decision tree correctly learns simple AND function.
	 * Should learn tree like this:
	 * x1 = true
	 * /       \
	 * yes       No
	 * /           \
	 * x2 = true      LABEL_FALSE
	 * /    \
	 * yes     No
	 * /         \
	 * LABEL_TRUE    LABEL_FALSE
	 */
	@Test
	void testTrainingAndFunction() {
		DecisionTree<String, Object> tree = new DecisionTree();
		String[] header = {"x1", "x2", "answer"};

		SimpleValue data1 = data(header, TRUE, TRUE, TRUE_LABEL);
		SimpleValue data2 = data(header, TRUE, FALSE, FALSE_LABEL);
		SimpleValue data3 = data(header, FALSE, TRUE, FALSE_LABEL);
		SimpleValue data4 = data(header, FALSE, FALSE, FALSE_LABEL);

		Function<Function<String, Boolean>, Object>
			feature1 = feature("x1", TRUE),
			feature2 = feature("x1", FALSE),
			feature3 = feature("x2", TRUE),
			feature4 = feature("x2", FALSE);

		tree.put("answer", List.of(data1, data2, data3, data4), (Collection) List.of(feature1, feature2, feature3, feature4));

		DecisionTree.DecisionNode<?> root = tree.root();

		assertEquals("x1 = true", root.toString());
        assertNull(root.label);

		assertEquals("x2 = true", root.get(FALSE).toString());
        assertNull(root.get(FALSE).label);
		assertTrue(root.get(FALSE).get(FALSE).isLeaf());
		assertEquals(TRUE_LABEL, root.get(FALSE).get(FALSE).label);
		assertTrue(root.get(FALSE).get(TRUE).isLeaf());
		assertEquals(FALSE_LABEL, root.get(FALSE).get(TRUE).label);

		assertTrue(root.get(TRUE).isLeaf());
		assertEquals(FALSE_LABEL, root.get(TRUE).label);

	}


	/**
	 * Test if decision tree correctly learns simple OR function.
	 * Should learn tree like this:
	 * x1 = true
	 * /       \
	 * yes       No
	 * /           \
	 * LABEL_TRUE     x2 = true
	 * /    \
	 * yes     No
	 * /         \
	 * LABEL_TRUE    LABEL_FALSE
	 */
	@Test
	void testTrainingORFunction() {
		DecisionTree<String, Object> tree = new DecisionTree();
		String[] header = {"x1", "x2", "answer"};

		SimpleValue data1 = data(header, TRUE, TRUE, TRUE_LABEL);
		SimpleValue data2 = data(header, TRUE, FALSE, TRUE_LABEL);
		SimpleValue data3 = data(header, FALSE, TRUE, TRUE_LABEL);
		SimpleValue data4 = data(header, FALSE, FALSE, FALSE_LABEL);

		Function<Function<String, Boolean>, Object>
			feature1 = feature("x1", TRUE),
			feature2 = feature("x1", FALSE),
			feature3 = feature("x2", TRUE),
			feature4 = feature("x2", FALSE);

		tree.put("answer", List.of(data1, data2, data3, data4), (Collection)List.of(feature1, feature2, feature3, feature4));

		DecisionTree.DecisionNode<?> root = tree.root();
		assertEquals("x1 = true", root.toString());
        assertNull(root.label);

		assertTrue(root.get(FALSE).isLeaf());
		assertEquals(TRUE_LABEL, root.get(FALSE).label);

		assertEquals("x2 = true", root.get(TRUE).toString());
        assertNull(root.get(TRUE).label);
		assertTrue(root.get(TRUE).get(FALSE).isLeaf());
		assertEquals(TRUE_LABEL, root.get(TRUE).get(FALSE).label);
		assertTrue(root.get(TRUE).get(TRUE).isLeaf());
		assertEquals(FALSE_LABEL, root.get(TRUE).get(TRUE).label);

	}


	/**
	 * Test if decision tree correctly learns simple XOR function.
	 * Should learn tree like this:
	 * x1 = true
	 * /       \
	 * yes       No
	 * /           \
	 * x2 = true        x2 = true
	 * /    \              /    \
	 * yes     No          yes     No
	 * /         \          /         \
	 * LABEL_FALSE LABEL_TRUE  LABEL_TRUE LABEL_FALSE
	 */
	@Test
	void testTrainingXORFunction() {
		DecisionTree<String, Object> tree = new DecisionTree();
		String[] header = {"x1", "x2", "answer"};

		SimpleValue data1 = data(header, TRUE, TRUE, FALSE_LABEL);
		SimpleValue data2 = data(header, TRUE, FALSE, TRUE_LABEL);
		SimpleValue data3 = data(header, FALSE, TRUE, TRUE_LABEL);
		SimpleValue data4 = data(header, FALSE, FALSE, FALSE_LABEL);


		Function<Function<String, Boolean>, Object>
			feature1 = feature("x1", TRUE),
			feature2 = feature("x1", FALSE),
			feature3 = feature("x2", TRUE),
			feature4 = feature("x2", FALSE);

		tree.put("answer",
				List.of(data1, data2, data3, data4),
			(Collection) List.of(feature1, feature2, feature3, feature4));
		tree.print();

		DecisionTree.DecisionNode root = tree.root();
		assertEquals("x1 = true", root.toString());
		assertNull(root.label);

		assertEquals("x2 = true", root.get(FALSE).toString());
		assertEquals("x2 = true", root.get(TRUE).toString());
	}

	@Test
	void testLearnSimpleMoreLessFeature() {
		DecisionTree<String, Integer> tree = new DecisionTree<>();
		String[] header = {"x1", "answer"};

		tree.put(
			"answer",
			(Collection) List.of(
				data(header, 1, FALSE_LABEL),
				data(header, 2, FALSE_LABEL),
				data(header, 3, TRUE_LABEL),
				data(header, 4, TRUE_LABEL)),
				List.of(
					feature("x1", P.moreThan(0), "> 0"),
					feature("x1", P.moreThan(1), "> 1"),
					feature("x1", P.moreThan(2), "> 2"))
		);

		tree.print();

		DecisionTree.DecisionNode<?> root = tree.root();
		assertEquals("x1 > 2", root.toString());
        assertNull(root.label);

		assertTrue(root.get(FALSE).isLeaf());
		assertEquals(TRUE_LABEL, root.get(FALSE).label);
		assertTrue(root.get(TRUE).isLeaf());
		assertEquals(FALSE_LABEL, root.get(TRUE).label);


	}

	/**
	 * Test classify function which finds path in decision tree to leaf node.
	 *
	 * @author Ignas
	 */
	@Test
	void testClassify() {


		DecisionTree tree = new DecisionTree();
		String[] header = {"x1", "x2", "answer"};

		SimpleValue data1 = data(header, TRUE, TRUE, TRUE_LABEL);
		SimpleValue data2 = data(header, TRUE, FALSE, FALSE_LABEL);
		SimpleValue data3 = data(header, FALSE, TRUE, FALSE_LABEL);
		SimpleValue data4 = data(header, FALSE, FALSE, FALSE_LABEL);


		Function<Function<String, Boolean>, Object>
			feature1 = feature("x1", TRUE),
			feature2 = feature("x1", FALSE),
			feature3 = feature("x2", TRUE),
			feature4 = feature("x2", FALSE);

		tree.put("answer",
			List.of(data1, data2, data3, data4),
			List.of(feature1, feature2, feature3, feature4));


		String[] classificationHeader = {"x1", "x2"};
		assertEquals(TRUE_LABEL, tree.get(classification(classificationHeader, TRUE, TRUE)));
		assertEquals(FALSE_LABEL, tree.get(classification(classificationHeader, TRUE, FALSE)));
		assertEquals(FALSE_LABEL, tree.get(classification(classificationHeader, FALSE, TRUE)));
		assertEquals(FALSE_LABEL, tree.get(classification(classificationHeader, FALSE, FALSE)));
	}


	@Test
	void FloatDecisionTable1() {

		DataTable t = new DataTable();
		t.addColumns(FloatColumn.create("a"), FloatColumn.create("b"), FloatColumn.create("x"));
		t.add(0, 1, 0);
		t.add(1, 1, 1);
		t.add(2, 1, 0);
		t.add(5, 0, 1);
		t.add(5, 0, 0);
		t.add(4, 2, 1);
		TableDecisionTree tr = new TableDecisionTree(t, 2, 4, 2);

		tr.print();
		tr.printExplanations();

		List<DecisionTree.DecisionNode.LeafNode> leavesList = tr.leaves().collect(toList());

//		assertEquals(
//			"[1.0, 0.0, 0.0, 0.0, 0.0]"
//			//"[1.0, 1.0, 1.0, 1.0, 0.0]"
//			, leavesList.toString());

	}


}