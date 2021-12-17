package jcog.constraint.continuous;

import jcog.constraint.continuous.exceptions.DuplicateConstraintException;
import jcog.constraint.continuous.exceptions.NonlinearExpressionException;
import jcog.constraint.continuous.exceptions.UnsatisfiableConstraintException;

import java.util.HashMap;

/**
 * Created by alex on 27/11/2014.
 */
public class Benchmarks {

    private static void testAddingLotsOfConstraints() throws DuplicateConstraintException, UnsatisfiableConstraintException, NonlinearExpressionException {
        ContinuousConstraintSolver solver = new ContinuousConstraintSolver();

        HashMap<String, DoubleVar> variables = new HashMap<>();

        ConstraintParser.CassowaryVariableResolver variableResolver = new ConstraintParser.CassowaryVariableResolver() {

            @Override
            public DoubleVar resolveVariable(String variableName) {
                DoubleVar variable = null;
                if (variables.containsKey(variableName)) {
                    variable =  variables.get(variableName);
                } else {
                    variable = new DoubleVar(variableName);
                    variables.put(variableName, variable);
                }
                return variable;
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

        solver.add(ConstraintParser.parse("variable0 == 100", variableResolver));

        for (int i = 1; i < 3000; i++) {
            String constraintString  = getVariableName(i) + " == 100 + " + getVariableName(i - 1);

            ContinuousConstraint constraint = ConstraintParser.parse(constraintString, variableResolver);

            System.gc();
            long timeBefore = System.nanoTime();

            solver.add(constraint);

            System.out.println(i + "," + ((System.nanoTime() - timeBefore) / 1000) );
        }


    }

    private static String getVariableName(int number) {
        return "getVariable" + number;
    }

    public static void main(String [ ] args) {
        try {
            testAddingLotsOfConstraints();
        } catch (DuplicateConstraintException | NonlinearExpressionException | UnsatisfiableConstraintException e) {
            e.printStackTrace();
        }
    }

}
