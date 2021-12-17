package jcog.constraint.continuous.exceptions;

import jcog.constraint.continuous.ContinuousConstraint;

/**
 * Created by alex on 30/01/15.
 */
public class UnsatisfiableConstraintException extends KiwiException {

    public UnsatisfiableConstraintException(ContinuousConstraint constraint) {
        super(constraint.toString());
    }
}
