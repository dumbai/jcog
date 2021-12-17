package jcog.constraint.continuous;

import jcog.constraint.continuous.exceptions.DuplicateConstraintException;
import jcog.constraint.continuous.exceptions.UnknownConstraintException;
import jcog.constraint.continuous.exceptions.UnsatisfiableConstraintException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class Tests {

    private static double EPSILON = 1.0e-8;

    @Test
    void simpleNew() throws UnsatisfiableConstraintException, DuplicateConstraintException {
        ContinuousConstraintSolver solver = new ContinuousConstraintSolver();
        DoubleVar x = new DoubleVar("x");


        solver.add(C.equals(C.add(x, 2), 20));

        solver.update();

        assertEquals(x.getAsDouble(), 18, EPSILON);
    }

    @Test
    void simple0() throws UnsatisfiableConstraintException, DuplicateConstraintException {
        ContinuousConstraintSolver solver = new ContinuousConstraintSolver();
        DoubleVar x = new DoubleVar("x");
        DoubleVar y = new DoubleVar("y");

        solver.add(C.equals(x, 20));

        solver.add(C.equals(C.add(x, 2), C.add(y, 10)));

        solver.update();

        System.out.println("x " + x.getAsDouble() + " y " + y.getAsDouble());

        assertEquals(y.getAsDouble(), 12, EPSILON);
        assertEquals(x.getAsDouble(), 20, EPSILON);
    }

    @Test
    void simple1() throws DuplicateConstraintException, UnsatisfiableConstraintException {
        DoubleVar x = new DoubleVar("x");
        DoubleVar y = new DoubleVar("y");
        ContinuousConstraintSolver solver = new ContinuousConstraintSolver();
        solver.add(C.equals(x, y));
        solver.update();
        assertEquals(x.getAsDouble(), y.getAsDouble(), EPSILON);
    }

    @Test
    void casso1() throws DuplicateConstraintException, UnsatisfiableConstraintException {
        DoubleVar x = new DoubleVar("x");
        DoubleVar y = new DoubleVar("y");
        ContinuousConstraintSolver solver = new ContinuousConstraintSolver();

        solver.add(C.lessThanOrEqualTo(x, y));
        solver.add(C.equals(y, C.add(x, 3.0)));
        solver.add(C.equals(x, 10.0).setStrength(Strength.WEAK));
        solver.add(C.equals(y, 10.0).setStrength(Strength.WEAK));

        solver.update();

        if (Math.abs(x.getAsDouble() - 10.0) < EPSILON) {
            assertEquals(10, x.getAsDouble(), EPSILON);
            assertEquals(13, y.getAsDouble(), EPSILON);
        } else {
            assertEquals(7, x.getAsDouble(), EPSILON);
            assertEquals(10, y.getAsDouble(), EPSILON);
        }
    }

    @Test
    void addDelete1() throws DuplicateConstraintException, UnsatisfiableConstraintException, UnknownConstraintException {
        DoubleVar x = new DoubleVar("x");
        ContinuousConstraintSolver solver = new ContinuousConstraintSolver();

        solver.add(C.lessThanOrEqualTo(x, 100).setStrength(Strength.WEAK));

        solver.update();
        assertEquals(100, x.getAsDouble(), EPSILON);

        ContinuousConstraint c10 = C.lessThanOrEqualTo(x, 10.0);
        ContinuousConstraint c20 = C.lessThanOrEqualTo(x, 20.0);

        solver.add(c10);
        solver.add(c20);

        solver.update();

        assertEquals(10, x.getAsDouble(), EPSILON);

        solver.remove(c10);

        solver.update();

        assertEquals(20, x.getAsDouble(), EPSILON);

        solver.remove(c20);
        solver.update();

        assertEquals(100, x.getAsDouble(), EPSILON);

        ContinuousConstraint c10again = C.lessThanOrEqualTo(x, 10.0);

        solver.add(c10again);
        solver.add(c10);
        solver.update();

        assertEquals(10, x.getAsDouble(), EPSILON);

        solver.remove(c10);
        solver.update();
        assertEquals(10, x.getAsDouble(), EPSILON);

        solver.remove(c10again);
        solver.update();
        assertEquals(100, x.getAsDouble(), EPSILON);
    }

    @Test
    void addDelete2() throws DuplicateConstraintException, UnsatisfiableConstraintException, UnknownConstraintException {
        DoubleVar x = new DoubleVar("x");
        DoubleVar y = new DoubleVar("y");
        ContinuousConstraintSolver solver = new ContinuousConstraintSolver();

        solver.add(C.equals(x, 100).setStrength(Strength.WEAK));
        solver.add(C.equals(y, 120).setStrength(Strength.STRONG));

        ContinuousConstraint c10 = C.lessThanOrEqualTo(x, 10.0);
        ContinuousConstraint c20 = C.lessThanOrEqualTo(x, 20.0);

        solver.add(c10);
        solver.add(c20);
        solver.update();

        assertEquals(10, x.getAsDouble(), EPSILON);
        assertEquals(120, y.getAsDouble(), EPSILON);

        solver.remove(c10);
        solver.update();

        assertEquals(20, x.getAsDouble(), EPSILON);
        assertEquals(120, y.getAsDouble(), EPSILON);

        ContinuousConstraint cxy = C.equals(C.multiply(x, 2.0), y);
        solver.add(cxy);
        solver.update();

        assertEquals(20, x.getAsDouble(), EPSILON);
        assertEquals(40, y.getAsDouble(), EPSILON);

        solver.remove(c20);
        solver.update();

        assertEquals(60, x.getAsDouble(), EPSILON);
        assertEquals(120, y.getAsDouble(), EPSILON);

        solver.remove(cxy);
        solver.update();

        assertEquals(100, x.getAsDouble(), EPSILON);
        assertEquals(120, y.getAsDouble(), EPSILON);
    }

    @Test
    void inconsistent1() throws InternalError, DuplicateConstraintException, UnsatisfiableConstraintException {
        assertThrows(UnsatisfiableConstraintException.class, () -> {
            DoubleVar x = new DoubleVar("x");
            ContinuousConstraintSolver solver = new ContinuousConstraintSolver();

            solver.add(C.equals(x, 10.0));
            solver.add(C.equals(x, 5.0));

            solver.update();
        });
    }

    @Test
    void inconsistent2() throws DuplicateConstraintException, UnsatisfiableConstraintException {
        assertThrows(UnsatisfiableConstraintException.class, () -> {
            DoubleVar x = new DoubleVar("x");
            ContinuousConstraintSolver solver = new ContinuousConstraintSolver();

            solver.add(C.greaterThanOrEqualTo(x, 10.0));
            solver.add(C.lessThanOrEqualTo(x, 5.0));
            solver.update();
        });
    }

    @Test
    void inconsistent3() throws DuplicateConstraintException, UnsatisfiableConstraintException {
        assertThrows(UnsatisfiableConstraintException.class, () -> {
            DoubleVar w = new DoubleVar("w");
            DoubleVar x = new DoubleVar("x");
            DoubleVar y = new DoubleVar("y");
            DoubleVar z = new DoubleVar("z");
            ContinuousConstraintSolver solver = new ContinuousConstraintSolver();

            solver.add(C.greaterThanOrEqualTo(w, 10.0));
            solver.add(C.greaterThanOrEqualTo(x, w));
            solver.add(C.greaterThanOrEqualTo(y, x));
            solver.add(C.greaterThanOrEqualTo(z, y));
            solver.add(C.greaterThanOrEqualTo(z, 8.0));
            solver.add(C.lessThanOrEqualTo(z, 4.0));
            solver.update();
        });
    }

}
