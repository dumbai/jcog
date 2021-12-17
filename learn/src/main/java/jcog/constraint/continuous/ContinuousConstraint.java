package jcog.constraint.continuous;

import jcog.data.list.Lst;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;

/**
 * Created by alex on 30/01/15.
 */
public class ContinuousConstraint {

    public final Expression expression;
    public final ScalarComparison op;
    private double strength;

    public ContinuousConstraint(Expression expr, ScalarComparison op) {
        this(expr, op, Strength.REQUIRED);
    }

    public ContinuousConstraint(Expression expr, ScalarComparison op, double strength) {
        this.expression = reduce(expr);
        this.op = op;
        this.strength = Strength.clip(strength);
    }

    public ContinuousConstraint(ContinuousConstraint other, double strength) {
        this(other.expression, other.op, strength);
    }

    private static Expression reduce(Expression expr){

        Map<DoubleSupplier, Double> vars = new LinkedHashMap<>(expr.terms.size());
        for(DoubleTerm term: expr.terms)
            vars.merge(term.var, term.coefficient, Double::sum);

        List<DoubleTerm> reducedTerms = new Lst<>(vars.size());
        for(Map.Entry<DoubleSupplier, Double> variableDoubleEntry : vars.entrySet())
            reducedTerms.add(new DoubleTerm(variableDoubleEntry.getKey(), variableDoubleEntry.getValue()));

        return new Expression(reducedTerms, expr.getConstant());
    }

    public double strength() {
        return strength;
    }

    public ContinuousConstraint setStrength(double strength) {
        this.strength = strength;
        return this;
    }

    @Override
    public String toString() {
        return "expression: (" + expression + ") strength: " + strength + " operator: " + op;
    }

}
