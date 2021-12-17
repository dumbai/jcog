package jcog.constraint.continuous;

import jcog.constraint.continuous.exceptions.DuplicateConstraintException;
import jcog.constraint.continuous.exceptions.NonlinearExpressionException;
import jcog.constraint.continuous.exceptions.UnsatisfiableConstraintException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by alex on 20/11/2014.
 */
class RealWorldTests {

    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String TOP = "top";
    private static final String BOTTOM = "bottom";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String CENTERX = "centerX";
    private static final String CENTERY = "centerY";

    private static final String[] CONSTRAINTS = {

            "container.columnWidth == container.width * 0.4",
            "container.thumbHeight == container.columnWidth / 2",
            "container.padding == container.width * (0.2 / 3)",
            "container.leftPadding == container.padding",
            "container.rightPadding == container.width - container.padding",
            "container.paddingUnderThumb == 5",
            "container.rowPadding == 15",
            "container.buttonPadding == 20",

            "thumb0.left == container.leftPadding",
            "thumb0.top == container.padding",
            "thumb0.height == container.thumbHeight",
            "thumb0.width == container.columnWidth",

            "title0.left == container.leftPadding",
            "title0.top == thumb0.bottom + container.paddingUnderThumb",
            "title0.height == title0.intrinsicHeight",
            "title0.width == container.columnWidth",

            "thumb1.right == container.rightPadding",
            "thumb1.top == container.padding",
            "thumb1.height == container.thumbHeight",
            "thumb1.width == container.columnWidth",

            "title1.right == container.rightPadding",
            "title1.top == thumb0.bottom + container.paddingUnderThumb",
            "title1.height == title1.intrinsicHeight",
            "title1.width == container.columnWidth",

            "thumb2.left == container.leftPadding",
            "thumb2.top >= title0.bottom + container.rowPadding",
            "thumb2.top == title0.bottom + container.rowPadding !weak",
            "thumb2.top >= title1.bottom + container.rowPadding",
            "thumb2.top == title1.bottom + container.rowPadding !weak",
            "thumb2.height == container.thumbHeight",
            "thumb2.width == container.columnWidth",

            "title2.left == container.leftPadding",
            "title2.top == thumb2.bottom + container.paddingUnderThumb",
            "title2.height == title2.intrinsicHeight",
            "title2.width == container.columnWidth",

            "thumb3.right == container.rightPadding",
            "thumb3.top == thumb2.top",

            "thumb3.height == container.thumbHeight",
            "thumb3.width == container.columnWidth",

            "title3.right == container.rightPadding",
            "title3.top == thumb3.bottom + container.paddingUnderThumb",
            "title3.height == title3.intrinsicHeight",
            "title3.width == container.columnWidth",

            "thumb4.left == container.leftPadding",
            "thumb4.top >= title2.bottom + container.rowPadding",
            "thumb4.top >= title3.bottom + container.rowPadding",
            "thumb4.top == title2.bottom + container.rowPadding !weak",
            "thumb4.top == title3.bottom + container.rowPadding !weak",
            "thumb4.height == container.thumbHeight",
            "thumb4.width == container.columnWidth",

            "title4.left == container.leftPadding",
            "title4.top == thumb4.bottom + container.paddingUnderThumb",
            "title4.height == title4.intrinsicHeight",
            "title4.width == container.columnWidth",

            "thumb5.right == container.rightPadding",
            "thumb5.top == thumb4.top",
            "thumb5.height == container.thumbHeight",
            "thumb5.width == container.columnWidth",

            "title5.right == container.rightPadding",
            "title5.top == thumb5.bottom + container.paddingUnderThumb",
            "title5.height == title5.intrinsicHeight",
            "title5.width == container.columnWidth",

            "line.height == 1",
            "line.width == container.width",
            "line.top >= title4.bottom + container.rowPadding",
            "line.top >= title5.bottom + container.rowPadding",

            "more.top == line.bottom + container.buttonPadding",
            "more.height == more.intrinsicHeight",
            "more.left == container.leftPadding",
            "more.right == container.rightPadding",

            "container.height == more.bottom + container.buttonPadding"
    };


    private static ConstraintParser.CassowaryVariableResolver createVariableResolver(ContinuousConstraintSolver solver, Map<String, HashMap<String, DoubleVar>> nodeHashMap) {
        ConstraintParser.CassowaryVariableResolver variableResolver = new ConstraintParser.CassowaryVariableResolver() {

            private DoubleVar getVariableFromNode(HashMap<String, DoubleVar> node, String variableName) {

                try {
                    if (node.containsKey(variableName)) {
                        return node.get(variableName);
                    } else {
                        DoubleVar variable = new DoubleVar(variableName);
                        node.put(variableName, variable);
                        switch (variableName) {
                            case RIGHT:
                                solver.add(C.equals(variable, C.add(getVariableFromNode(node, LEFT), getVariableFromNode(node, WIDTH))));
                                break;
                            case BOTTOM:
                                solver.add(C.equals(variable, C.add(getVariableFromNode(node, TOP), getVariableFromNode(node, HEIGHT))));
                                break;
                            case CENTERX:
                            case CENTERY:

                                break;
                        }
                        return variable;
                    }
                } catch(DuplicateConstraintException | UnsatisfiableConstraintException e) {
                    e.printStackTrace();
                }

                return null;

            }

            private HashMap<String, DoubleVar> getNode(String nodeName) {
                HashMap<String, DoubleVar> node;
                if (nodeHashMap.containsKey(nodeName)) {
                    node = nodeHashMap.get(nodeName);
                } else {
                    node = new HashMap<>();
                    nodeHashMap.put(nodeName, node);
                }
                return node;
            }

            @Override
            public DoubleVar resolveVariable(String variableName) {

                String[] stringArray = variableName.split("\\.");
                if (stringArray.length == 2) {
                    String nodeName = stringArray[0];
                    String propertyName = stringArray[1];

                    HashMap<String, DoubleVar> node = getNode(nodeName);

                    return getVariableFromNode(node, propertyName);

                } else {
                    throw new RuntimeException("can't resolve variable");
                }
            }

            @Override
            public Expression resolveConstant(String name) {
                try {
                    return new Expression(Double.parseDouble(name));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        };
        return variableResolver;
    }

    @Test
    void testSimple() {
        int a = 20;
        assertEquals(20, a);
    }

    @Test
    void testGridLayout() throws DuplicateConstraintException, UnsatisfiableConstraintException, NonlinearExpressionException {

        ContinuousConstraintSolver solver = new ContinuousConstraintSolver();
        Map<String, HashMap<String, DoubleVar>> nodes = new HashMap<>();

        ConstraintParser.CassowaryVariableResolver r = createVariableResolver(solver, nodes);

        for (String constraint : CONSTRAINTS)
            solver.add(ConstraintParser.parse(constraint, r));

        solver.add(ConstraintParser.parse("container.width == 300", r));
        solver.add(ConstraintParser.parse("title0.intrinsicHeight == 100", r));
        solver.add(ConstraintParser.parse("title1.intrinsicHeight == 110", r));
        solver.add(ConstraintParser.parse("title2.intrinsicHeight == 120", r));
        solver.add(ConstraintParser.parse("title3.intrinsicHeight == 130", r));
        solver.add(ConstraintParser.parse("title4.intrinsicHeight == 140", r));
        solver.add(ConstraintParser.parse("title5.intrinsicHeight == 150", r));
        solver.add(ConstraintParser.parse("more.intrinsicHeight == 160", r));

        solver.update();

        double EPSILON = 1.0e-2;
        assertEquals(20, nodes.get("thumb0").get("top").getAsDouble(), EPSILON);
        assertEquals(20, nodes.get("thumb1").get("top").getAsDouble(), EPSILON);

        assertEquals(85, nodes.get("title0").get("top").getAsDouble(), EPSILON);
        assertEquals(85, nodes.get("title1").get("top").getAsDouble(), EPSILON);

        assertEquals(210, nodes.get("thumb2").get("top").getAsDouble(), EPSILON);
        assertEquals(210, nodes.get("thumb3").get("top").getAsDouble(), EPSILON);

        assertEquals(275, nodes.get("title2").get("top").getAsDouble(), EPSILON);
        assertEquals(275, nodes.get("title3").get("top").getAsDouble(), EPSILON);

        assertEquals(420, nodes.get("thumb4").get("top").getAsDouble(), EPSILON);
        assertEquals(420, nodes.get("thumb5").get("top").getAsDouble(), EPSILON);

        assertEquals(485, nodes.get("title4").get("top").getAsDouble(), EPSILON);
        assertEquals(485, nodes.get("title5").get("top").getAsDouble(), EPSILON);
    }

  /*  @Test
    public void testGridLayoutUsingEditVariables() throws CassowaryError {

        final SimplexSolver solver = new SimplexSolver();
        solver.setAutosolve(true);

        final HashMap<String, HashMap<String, Variable>> nodeHashMap = new HashMap<String, HashMap<String, Variable>>();

        ConstraintParser.CassowaryVariableResolver variableResolver = createVariableResolver(solver, nodeHashMap);

        for (String constraint : CONSTRAINTS) {
            solver.addConstraint(ConstraintParser.parseConstraint(constraint, variableResolver));
        }

        Variable containerWidth = nodeHashMap.get("container").get("width");
        Variable title0IntrinsicHeight = nodeHashMap.get("title0").get("intrinsicHeight");
        Variable title1IntrinsicHeight = nodeHashMap.get("title1").get("intrinsicHeight");
        Variable title2IntrinsicHeight = nodeHashMap.get("title2").get("intrinsicHeight");
        Variable title3IntrinsicHeight = nodeHashMap.get("title3").get("intrinsicHeight");
        Variable title4IntrinsicHeight = nodeHashMap.get("title4").get("intrinsicHeight");
        Variable title5IntrinsicHeight = nodeHashMap.get("title5").get("intrinsicHeight");
        Variable moreIntrinsicHeight = nodeHashMap.get("more").get("intrinsicHeight");

        solver.addStay(containerWidth);
        solver.addStay(title0IntrinsicHeight);
        solver.addStay(title1IntrinsicHeight);
        solver.addStay(title2IntrinsicHeight);
        solver.addStay(title3IntrinsicHeight);
        solver.addStay(title4IntrinsicHeight);
        solver.addStay(title5IntrinsicHeight);
        solver.addStay(moreIntrinsicHeight);

        solver.addEditVar(containerWidth);
        solver.addEditVar(title0IntrinsicHeight);
        solver.addEditVar(title1IntrinsicHeight);
        solver.addEditVar(title2IntrinsicHeight);
        solver.addEditVar(title3IntrinsicHeight);
        solver.addEditVar(title4IntrinsicHeight);
        solver.addEditVar(title5IntrinsicHeight);
        solver.addEditVar(moreIntrinsicHeight);
        solver.beginEdit();

        solver.suggestValue(containerWidth, 300);
        solver.suggestValue(title0IntrinsicHeight, 100);
        solver.suggestValue(title1IntrinsicHeight, 110);
        solver.suggestValue(title2IntrinsicHeight, 120);
        solver.suggestValue(title3IntrinsicHeight, 130);
        solver.suggestValue(title4IntrinsicHeight, 140);
        solver.suggestValue(title5IntrinsicHeight, 150);
        solver.suggestValue(moreIntrinsicHeight, 160);

        solver.resolve();

        solver.solve();

        assertEquals(20, nodeHashMap.get("thumb0").get("top").value(), EPSILON);
        assertEquals(20, nodeHashMap.get("thumb1").get("top").value(), EPSILON);

        assertEquals(85, nodeHashMap.get("title0").get("top").value(), EPSILON);
        assertEquals(85, nodeHashMap.get("title1").get("top").value(), EPSILON);

        assertEquals(210, nodeHashMap.get("thumb2").get("top").value(), EPSILON);
        assertEquals(210, nodeHashMap.get("thumb3").get("top").value(), EPSILON);

        assertEquals(275, nodeHashMap.get("title2").get("top").value(), EPSILON);
        assertEquals(275, nodeHashMap.get("title3").get("top").value(), EPSILON);

        assertEquals(420, nodeHashMap.get("thumb4").get("top").value(), EPSILON);
        assertEquals(420, nodeHashMap.get("thumb5").get("top").value(), EPSILON);

        assertEquals(485, nodeHashMap.get("title4").get("top").value(), EPSILON);
        assertEquals(485, nodeHashMap.get("title5").get("top").value(), EPSILON);

    }
*/
  
    @Test
    void testGridX1000() throws DuplicateConstraintException, UnsatisfiableConstraintException, NonlinearExpressionException {

        long nanoTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            testGridLayout();
        }
        System.out.println("testGridX1000 took " + (System.nanoTime() - nanoTime) / 1000000);
    }

    /*
    @Test
    public void testGridWithEditsX1000() throws CassowaryError {

        long nanoTime = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            testGridLayoutUsingEditVariables();
        }
        System.out.println("testGridWithEditsX1000 took " + (System.nanoTime() - nanoTime) / 1000000 + " ms");

    }*/

    static void printNodes(HashMap<String, HashMap<String, DoubleSupplier>> variableHashMap) {
        for (Map.Entry<String, HashMap<String, DoubleSupplier>> pairs : variableHashMap.entrySet()) {
            System.out.println("node " + pairs.getKey());
            printVariables(pairs.getValue());
        }
    }

    private static void printVariables(HashMap<String, DoubleSupplier> variableHashMap) {
        for (Map.Entry<String, DoubleSupplier> pairs : variableHashMap.entrySet()) {
            System.out.println(' ' + pairs.getKey() + " = " + pairs.getValue().getAsDouble() + " (address:" + pairs.getValue().hashCode() + ')');
        }
    }

}
