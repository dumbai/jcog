package jcog.constraint.continuous;

import java.util.function.DoubleSupplier;

/**
 * Created by alex on 30/01/15.
 */
public class DoubleTerm implements DoubleSupplier {

    public final DoubleSupplier var;
    double coefficient;

    public DoubleTerm(DoubleSupplier var, double coefficient) {
        this.var = var;
        this.coefficient = coefficient;
    }

    public DoubleTerm(DoubleSupplier var) {
        this(var, 1.0);
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }

    @Override
    public String toString() {
        return "variable: (" + var + ") coefficient: "  + coefficient;
    }

    @Override
    public double getAsDouble() {
         return coefficient * var.getAsDouble();
    }

    public boolean mutable() {
        return var instanceof DoubleVar;
    }
}
