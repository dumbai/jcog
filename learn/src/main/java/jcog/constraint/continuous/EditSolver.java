package jcog.constraint.continuous;

import jcog.constraint.continuous.exceptions.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;

/** is this complete? */
class EditSolver extends ContinuousConstraintSolver {
    protected final Map<DoubleSupplier, EditInfo> edits = new LinkedHashMap<>();

    public void addEditVariable(DoubleSupplier variable, double strength) throws DuplicateEditVariableException, RequiredFailureException {
        if (edits.containsKey(variable)) {
            throw new DuplicateEditVariableException();
        }

        strength = Strength.clip(strength);

        if (strength == Strength.REQUIRED) {
            throw new RequiredFailureException();
        }

        List<DoubleTerm> terms = new ArrayList<>();
        terms.add(new DoubleTerm(variable));
        ContinuousConstraint constraint = new ContinuousConstraint(new Expression(terms), ScalarComparison.Equal, strength);

        try {
            add(constraint);
        } catch (DuplicateConstraintException | UnsatisfiableConstraintException e) {
            e.printStackTrace();
        }


        EditInfo info = new EditInfo(constraint, cns.get(constraint), 0.0);
        edits.put(variable, info);
    }

    public void removeEditVariable(DoubleSupplier variable) throws UnknownEditVariableException {
        EditInfo edit = edits.get(variable);
        if (edit == null) {
            throw new UnknownEditVariableException();
        }

        try {
            remove(edit.constraint);
        } catch (UnknownConstraintException e) {
            e.printStackTrace();
        }

        edits.remove(variable);
    }

    public boolean hasEditVariable(DoubleSupplier variable) {
        return edits.containsKey(variable);
    }

    public void suggestValue(DoubleSupplier variable, double value) throws UnknownEditVariableException {
        EditInfo info = edits.get(variable);
        if (info == null) {
            throw new UnknownEditVariableException();
        }

        double delta = value - info.constant;
        info.constant = value;

        Row row = rows.get(info.tag.marker);
        if (row != null) {
            if (row.addToConstant(-delta) < 0.0) {
                infeasibleRows.add(info.tag.marker);
            }
            dualOptimize();
            return;
        }

        row = rows.get(info.tag.other);
        if (row != null) {
            if (row.addToConstant(delta) < 0.0) {
                infeasibleRows.add(info.tag.other);
            }
            dualOptimize();
            return;
        }

        for (Map.Entry<Symbol, Row> symbolRowEntry : rows.entrySet()) {
            Row currentRow = symbolRowEntry.getValue();
            double coefficient = currentRow.coefficientFor(info.tag.marker);
            Symbol k = symbolRowEntry.getKey();
            if (coefficient != 0.0 && currentRow.addToConstant(delta * coefficient) < 0.0 && k.type != Symbol.Type.EXTERNAL) {
                infeasibleRows.add(k);
            }
        }

        dualOptimize();
    }

    void dualOptimize() throws InternalSolverError {
        while (!infeasibleRows.isEmpty()) {
            Symbol leaving = infeasibleRows.remove(infeasibleRows.size() - 1);
            Row row = rows.remove(leaving);
            if (row != null && row.getConstant() < 0.0) {
                Symbol entering = getDualEnteringSymbol(row);
                if (entering.type == Symbol.Type.INVALID) {
                    throw new InternalSolverError("internal solver error");
                }
                row.solveFor(leaving, entering);
                substitute(entering, row);
                rows.put(entering, row);
            }
        }
    }

    protected Symbol getDualEnteringSymbol(Row row) {
        Symbol entering = null;
        double ratio = Double.MAX_VALUE;
        
        for (Symbol s : row.cells.keySet()) {
            if (s.type != Symbol.Type.DUMMY) {
                double currentCell = row.cells.get(s);
                if (currentCell > 0.0) {
                    double coefficient = objective.coefficientFor(s);
                    double r = coefficient / currentCell;
                    if (r < ratio) {
                        ratio = r;
                        entering = s;
                    }
                }
            }
        }

        return entering != null ? entering : new Symbol(Symbol.Type.INVALID);
    }

}
