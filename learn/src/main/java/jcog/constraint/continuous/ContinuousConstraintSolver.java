package jcog.constraint.continuous;


import jcog.constraint.continuous.exceptions.DuplicateConstraintException;
import jcog.constraint.continuous.exceptions.InternalSolverError;
import jcog.constraint.continuous.exceptions.UnknownConstraintException;
import jcog.constraint.continuous.exceptions.UnsatisfiableConstraintException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 30/01/15.
 */
public class ContinuousConstraintSolver {

    private static final double EPS = 1.0e-8;

    public static boolean nearZero(double value ) {
        return (value < 0.0 ? -value : value) < EPS;
    }

    protected static class Tag {
        Symbol marker;
        Symbol other;

        public Tag() {
            marker = new Symbol(Symbol.Type.INVALID);
            other = new Symbol(Symbol.Type.INVALID);
        }
    }

    protected static class EditInfo {
        final Tag tag;
        final ContinuousConstraint constraint;
        double constant;

        public EditInfo(ContinuousConstraint constraint, Tag tag, double constant) {
            this.constraint = constraint;
            this.tag = tag;
            this.constant = constant;
        }
    }

    protected final Map<ContinuousConstraint, Tag> cns = new LinkedHashMap<>();
    protected final Map<Symbol, Row> rows = new LinkedHashMap<>();
    public final Map<DoubleVar, Symbol> vars = new LinkedHashMap<>();
    protected final List<Symbol> infeasibleRows = new ArrayList<>();
    protected final Row objective = new Row();
    private Row artificial;


    /**
     * Add a constraint to the solver.
     *
     * @param constraint
     * @throws DuplicateConstraintException     The given constraint has already been added to the solver.
     * @throws UnsatisfiableConstraintException The given constraint is required and cannot be satisfied.
     */
    public void add(ContinuousConstraint constraint) throws DuplicateConstraintException, UnsatisfiableConstraintException {

        if (cns.containsKey(constraint)) {
            throw new DuplicateConstraintException(constraint);
        }

        var tag = new Tag();
        var row = createRow(constraint, tag);
        var subject = chooseSubject(row, tag);

        if (subject.type == Symbol.Type.INVALID && allDummies(row)) {
            if (!nearZero(row.getConstant())) {
                throw new UnsatisfiableConstraintException(constraint);
            } else {
                subject = tag.marker;
            }
        }

        if (subject.type == Symbol.Type.INVALID) {
            if (!addWithArtificialVariable(row))
                throw new UnsatisfiableConstraintException(constraint);
        } else {
            row.solveFor(subject);
            substitute(subject, row);
            rows.put(subject, row);
        }

        cns.put(constraint, tag);

        optimize(objective);
    }

    public void remove(ContinuousConstraint constraint) throws UnknownConstraintException, InternalSolverError {
        var tag = cns.get(constraint);
        if (tag == null)
            throw new UnknownConstraintException(constraint);

        cns.remove(constraint);
        removeConstraintEffects(constraint, tag);

        var row = rows.remove(tag.marker);
        if (row == null) {
            row = getMarkerLeavingRow(tag.marker);
            if (row == null)
                throw new InternalSolverError("internal solver error");

            Symbol leaving = null;
            for (var s : rows.entrySet()) {
                if (s.getValue() == row) {
                    leaving = s.getKey();
                    break;
                }
            }
            if (leaving == null)
                throw new InternalSolverError("internal solver error");

            rows.remove(leaving);
            row.solveFor(leaving, tag.marker);
            substitute(tag.marker, row);
        }
        optimize(objective);
    }

    void removeConstraintEffects(ContinuousConstraint constraint, Tag tag) {
        if (tag.marker.type == Symbol.Type.ERROR) {
            removeMarkerEffects(tag.marker, constraint.strength());
        } else if (tag.other.type == Symbol.Type.ERROR) {
            removeMarkerEffects(tag.other, constraint.strength());
        }
    }

    void removeMarkerEffects(Symbol marker, double strength) {
        var row = rows.get(marker);
        if (row != null) {
            objective.insert(row, -strength);
        } else {
            objective.insert(marker, -strength);
        }
    }

    Row getMarkerLeavingRow(Symbol marker) {
        var dmax = Double.MAX_VALUE;
        var r1 = dmax;
        var r2 = dmax;

        Row first = null;
        Row second = null;
        Row third = null;

        for (var symbolRowEntry : rows.entrySet()) {
            var candidateRow = symbolRowEntry.getValue();
            var c = candidateRow.coefficientFor(marker);
            if (c == 0.0) {
                continue;
            }
            if ((symbolRowEntry.getKey()).type == Symbol.Type.EXTERNAL) {
                third = candidateRow;
            } else if (c < 0.0) {
                var r = -candidateRow.getConstant() / c;
                if (r < r1) {
                    r1 = r;
                    first = candidateRow;
                }
            } else {
                var r = candidateRow.getConstant() / c;
                if (r < r2) {
                    r2 = r;
                    second = candidateRow;
                }
            }
        }

        if (first != null)
            return first;
        else if (second != null)
            return second;
        else
            return third;
    }

    /**
     * Update the values of the external solver variables.
     */
    public void update() {

        for (Map.Entry<DoubleVar, Symbol> entry : vars.entrySet()) {
            DoubleVar key = entry.getKey();
            Symbol value = entry.getValue();
            var row = rows.get(value);
            if (row != null)
                key.value(row.getConstant());
        }
    }


    /**
     * Create a new Row object for the given constraint.
     * <p/>
     * The terms in the constraint will be converted to cells in the row.
     * Any term in the constraint with a coefficient of zero is ignored.
     * This method uses the `getVarSymbol` method to get the symbol for
     * the variables added to the row. If the symbol for a given cell
     * variable is basic, the cell variable will be substituted with the
     * basic row.
     * <p/>
     * The necessary slack and error variables will be added to the row.
     * If the constant for the row is negative, the sign for the row
     * will be inverted so the constant becomes positive.
     * <p/>
     * The tag will be updated with the marker and error symbols to use
     * for tracking the movement of the constraint in the tableau.
     */
    Row createRow(ContinuousConstraint constraint, Tag tag) {
        var expression = constraint.expression;
        var row = new Row(expression.getConstant());

        var terms = expression.terms;
        for (DoubleTerm term : terms) {
            var coefficient = term.coefficient;
            if (!nearZero(coefficient)) {

                if (term.var instanceof DoubleVar) {
                    var symbol = getVarSymbol(((DoubleVar) term.var));

                    var otherRow = rows.get(symbol);

                    if (otherRow == null) {
                        row.insert(symbol, coefficient);
                    } else {
                        row.insert(otherRow, coefficient);
                    }
                }
            }
        }

        var str = constraint.strength();

        switch (constraint.op) {
            case LessThanOrEqual:
            case GreaterThanOrEqual:
                var coeff = constraint.op == ScalarComparison.LessThanOrEqual ? 1.0 : -1.0;
                var slack = new Symbol(Symbol.Type.SLACK);
                tag.marker = slack;
                row.insert(slack, coeff);
                if (str < Strength.REQUIRED) {
                    var error = new Symbol(Symbol.Type.ERROR);
                    tag.other = error;
                    row.insert(error, -coeff);
                    objective.insert(error, str);
                }
                break;
            case Equal:
                if (str < Strength.REQUIRED) {
                    var errplus = new Symbol(Symbol.Type.ERROR);
                    var errminus = new Symbol(Symbol.Type.ERROR);
                    tag.marker = errplus;
                    tag.other = errminus;
                    row.insert(errplus, -1.0); 
                    row.insert(errminus, 1.0);
                    objective.insert(errplus, str);
                    objective.insert(errminus, str);
                } else {
                    var dummy = new Symbol(Symbol.Type.DUMMY);
                    tag.marker = dummy;
                    row.insert(dummy);
                }
                break;
        }

        
        if (row.getConstant() < 0.0) {
            row.reverseSign();
        }

        return row;
    }

    /**
     * Choose the subject for solving for the row
     * <p/>
     * This method will choose the best subject for using as the solve
     * target for the row. An invalid symbol will be returned if there
     * is no valid target.
     * The symbols are chosen according to the following precedence:
     * 1) The first symbol representing an external variable.
     * 2) A negative slack or error tag variable.
     * If a subject cannot be found, an invalid symbol will be returned.
     */
    private static Symbol chooseSubject(Row row, Tag tag) {

        for (var cell : row.cells.entrySet()) {
            if (cell.getKey().type == Symbol.Type.EXTERNAL) {
                return cell.getKey();
            }
        }
        if (tag.marker.type == Symbol.Type.SLACK || tag.marker.type == Symbol.Type.ERROR) {
            if (row.coefficientFor(tag.marker) < 0.0)
                return tag.marker;
        }
        if (tag.other != null && (tag.other.type == Symbol.Type.SLACK || tag.other.type == Symbol.Type.ERROR)) {
            if (row.coefficientFor(tag.other) < 0.0)
                return tag.other;
        }
        return new Symbol(Symbol.Type.INVALID);
    }

    /**
     * Add the row to the tableau using an artificial variable.
     * <p/>
     * This will return false if the constraint cannot be satisfied.
     */
    private boolean  addWithArtificialVariable(Row row) {


        var art = new Symbol(Symbol.Type.SLACK);
        rows.put(art, new Row(row));

        boolean success;
        {
            artificial = new Row(row);


            optimize(artificial);
            success = nearZero(artificial.getConstant());
            artificial = null;
        }


        var rowptr = rows.get(art);

        boolean completed = false;
        boolean res = false;
        if (rowptr != null) {
            var deleteQueue = new ArrayList<>();
            for (Map.Entry<Symbol, Row> entry : rows.entrySet()) {
                Symbol key = entry.getKey();
                Row value = entry.getValue();
                if (value == rowptr) deleteQueue.add(key);
            }
            for (Object symbol : deleteQueue) {
                rows.remove(symbol);
            }

            if (rowptr.cells.isEmpty()) {
                res = success;
                completed = true;
            } else {
                deleteQueue.clear();
                var entering = anyPivotableSymbol(rowptr);
                if (entering.type == Symbol.Type.INVALID) {
                    completed = true;
                } else {
                    rowptr.solveFor(art, entering);
                    substitute(entering, rowptr);
                    rows.put(entering, rowptr);
                }
            }

        }
        if (!completed) {
            for (Row r : rows.values()) {
                r.remove(art);
            }

            objective.remove(art);

            res = success;
        }


        return res;
    }

    /**
     * Substitute the parametric symbol with the given row.
     * <p/>
     * This method will substitute all instances of the parametric symbol
     * in the tableau and the objective function with the given row.
     */
    void substitute(Symbol symbol, Row row) {
        for (var rowEntry : rows.entrySet()) {
            var v = rowEntry.getValue();
            v.substitute(symbol, row);
            var k = rowEntry.getKey();
            if (k.type != Symbol.Type.EXTERNAL && v.getConstant() < 0.0) {
                infeasibleRows.add(k);
            }
        }

        objective.substitute(symbol, row);

        if (artificial != null) {
            artificial.substitute(symbol, row);
        }
    }

    /**
     * Optimize the system for the given objective function.
     * <p/>
     * This method performs iterations of Phase 2 of the simplex method
     * until the objective function reaches a minimum.
     *
     * @throws InternalSolverError The value of the objective function is unbounded.
     */
    void optimize(Row objective) {
        while (true) {
            var entering = getEnteringSymbol(objective);
            if (entering.type == Symbol.Type.INVALID) {
                return;
            }

            var entry = getLeavingRow(entering);
            if (entry == null) {
                throw new InternalSolverError("The objective is unbounded.");
            }

            Symbol leaving = null;
            for (var key : rows.entrySet()) {
                if (key.getValue() == entry) {
                    leaving = key.getKey();
                }
            }

            rows.remove(leaving);
            entry.solveFor(leaving, entering);
            substitute(entering, entry);
            rows.put(entering, entry);
        }
    }


    /**
     * Compute the entering variable for a pivot operation.
     * <p/>
     * This method will return first symbol in the objective function which
     * is non-dummy and has a coefficient less than zero. If no symbol meets
     * the criteria, it means the objective function is at a minimum, and an
     * invalid symbol is returned.
     */
    private static Symbol getEnteringSymbol(Row objective) {

        for (var cell : objective.cells.entrySet()) {

            var k = cell.getKey();
            if (k.type != Symbol.Type.DUMMY && cell.getValue() < 0.0) {
                return k;
            }
        }
        return new Symbol(Symbol.Type.INVALID);

    }


    /**
     * Get the first Slack or Error symbol in the row.
     * <p/>
     * If no such symbol is present, and Invalid symbol will be returned.
     */
    private static Symbol anyPivotableSymbol(Row row) {
        Symbol found = null;
        for (Symbol k : row.cells.keySet()) {
            if (k.type == Symbol.Type.SLACK || k.type == Symbol.Type.ERROR) {
                found = k;
                break;
            }
        }
        return found!=null ? found : new Symbol(Symbol.Type.INVALID);
    }

    /**
     * Compute the row which holds the exit symbol for a pivot.
     * <p/>
     * This documentation is copied from the C++ version and is outdated
     * <p/>
     * <p/>
     * This method will return an iterator to the row in the row map
     * which holds the exit symbol. If no appropriate exit symbol is
     * found, the end() iterator will be returned. This indicates that
     * the objective function is unbounded.
     */
    private Row getLeavingRow(Symbol entering) {
        var ratio = Double.MAX_VALUE;
        Row row = null;

        for (var symbolRowEntry : rows.entrySet()) {
            if ((symbolRowEntry.getKey()).type != Symbol.Type.EXTERNAL) {
                var candidateRow = symbolRowEntry.getValue();
                var temp = candidateRow.coefficientFor(entering);
                if (temp < 0) {
                    var temp_ratio = (-candidateRow.getConstant() / temp);
                    if (temp_ratio < ratio) {
                        ratio = temp_ratio;
                        row = candidateRow;
                    }
                }
            }
        }
        return row;
    }

    /**
     * Get the symbol for the given variable.
     * <p/>
     * If a symbol does not exist for the variable, one will be created.
     */
    private Symbol getVarSymbol(DoubleVar variable) {
        return vars.computeIfAbsent(variable, (v) -> new Symbol(Symbol.Type.EXTERNAL));
    }

    /**
     * Test whether a row is composed of all dummy variables.
     */
    private static boolean allDummies(Row row) {
        return row.cells.keySet().stream().noneMatch(x -> x.type != Symbol.Type.DUMMY);
    }

}