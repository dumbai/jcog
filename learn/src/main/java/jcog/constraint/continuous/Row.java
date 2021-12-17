package jcog.constraint.continuous;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by alex on 30/01/15.
 */
class Row {

    private double constant;

    public final Map<Symbol, Double> cells;

    Row() {
        this(0);
    }

    Row(double constant) {
        this.cells = new LinkedHashMap<>();
        this.constant = constant;
    }

    Row(Row other) {
        this.cells = new LinkedHashMap<>(other.cells);
        this.constant = other.constant;
    }

    public double getConstant() {
        return constant;
    }

    public void setConstant(double constant) {
        this.constant = constant;
    }

    /**
     * Add a constant value to the row constant.
     *
     * @return The new value of the constant
     */
    double addToConstant(double value) {
        return this.constant += value;
    }

    /**
     * Insert a symbol into the row with a given coefficient.
     * <p/>
     * If the symbol already exists in the row, the coefficient will be
     * added to the existing coefficient. If the resulting coefficient
     * is zero, the symbol will be removed from the row
     */
    void insert(Symbol symbol, double _coefficient) {
        cells.merge(symbol, _coefficient, (existingCoefficient, coefficient) -> {
            double coefficient1 = coefficient;
            if (existingCoefficient != null)
                coefficient1 += existingCoefficient;

            return ContinuousConstraintSolver.nearZero(coefficient1) ? null : coefficient1;
        });
    }

    /**
     * Insert a symbol into the row with a given coefficient.
     * <p/>
     * If the symbol already exists in the row, the coefficient will be
     * added to the existing coefficient. If the resulting coefficient
     * is zero, the symbol will be removed from the row
     */
    void insert(Symbol symbol) {
        insert(symbol, 1.0);
    }

    /**
     * Insert a row into this row with a given coefficient.
     * The constant and the cells of the other row will be multiplied by
     * the coefficient and added to this row. Any cell with a resulting
     * coefficient of zero will be removed from the row.
     *
     * @param other
     * @param coefficient
     */
    void insert(Row other, double coefficient) {
        this.constant += other.constant * coefficient;



        for (Map.Entry<Symbol, Double> e : other.cells.entrySet()) {

            

            


            this.cells.merge(e.getKey(), e.getValue() * coefficient,
                    (existing, cc)->{
                        Double existing1 = existing;
                        if (existing1 == null)
                            existing1 = 0.0;

                        existing1 += cc;

                        return ContinuousConstraintSolver.nearZero(existing1) ? null : existing1;
                    });
        }
    }

    /**
     * Insert a row into this row with a given coefficient.
     * The constant and the cells of the other row will be multiplied by
     * the coefficient and added to this row. Any cell with a resulting
     * coefficient of zero will be removed from the row.
     *
     * @param other
     */
    void insert(Row other) {
        insert(other, 1.0);
    }

    /**
     * Remove the given symbol from the row.
     */
    void remove(Symbol symbol) {

        cells.remove(symbol);
        
        /*CellMap::iterator it = m_cells.find( symbol );
        if( it != m_cells.end() )
            m_cells.erase( it );*/
    }

    /**
     * Reverse the sign of the constant and all cells in the row.
     */
    void reverseSign() {
        this.constant = -this.constant;
        cells.replaceAll((k,v)->-v);
    }

    /**
     * Solve the row for the given symbol.
     * <p/>
     * This method assumes the row is of the form a * x + b * y + c = 0
     * and (assuming solve for x) will modify the row to represent the
     * right hand side of x = -b/a * y - c / a. The target symbol will
     * be removed from the row, and the constant and other cells will
     * be multiplied by the negative inverse of the target coefficient.
     * The given symbol *must* exist in the row.
     *
     * @param symbol
     */
    void solveFor(Symbol symbol) {
        double coeff = -1.0 / cells.remove(symbol);
        this.constant *= coeff;

        cells.replaceAll((k,v)->v*coeff);
    }

    /**
     * Solve the row for the given symbols.
     * <p/>
     * This method assumes the row is of the form x = b * y + c and will
     * solve the row such that y = x / b - c / b. The rhs symbol will be
     * removed from the row, the lhs added, and the result divided by the
     * negative inverse of the rhs coefficient.
     * The lhs symbol *must not* exist in the row, and the rhs symbol
     * must* exist in the row.
     *
     * @param lhs
     * @param rhs
     */
    void solveFor(Symbol lhs, Symbol rhs) {
        insert(lhs, -1.0);
        solveFor(rhs);
    }

    /**
     * Get the coefficient for the given symbol.
     * <p/>
     * If the symbol does not exist in the row, zero will be returned.
     *
     * @return
     */
    double coefficientFor(Symbol symbol) {
        return cells.getOrDefault(symbol, 0.0);
    }

    /**
     * Substitute a symbol with the data from another row.
     * <p/>
     * Given a row of the form a * x + b and a substitution of the
     * form x = 3 * y + c the row will be updated to reflect the
     * expression 3 * a * y + a * c + b.
     * If the symbol does not exist in the row, this is a no-op.
     */
    void substitute(Symbol symbol, Row row) {
        Double coefficient = cells.remove(symbol);
        if (coefficient!=null)
            insert(row, coefficient);
    }

}