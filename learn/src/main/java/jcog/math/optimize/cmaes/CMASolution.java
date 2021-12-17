package jcog.math.optimize.cmaes;

/**
 * solution point in search space. Rather plain implementation of the interface ISolutionPoint.
 *
 * @see CMASolution
 */
public class CMASolution implements java.io.Serializable {
    /**
     * objective function value of x
     */
    private double functionValue = Double.NaN;
    /**
     * argument to objective function to be optimized
     */
    private double[] x;

//	/* * as I do not know how to inherit clone in a decent way
//	 * and clone even might produce shallow copies
//	 */
//	public CMASolution deepCopy() {
//		return new CMASolution(x, functionValue, evaluation);
//	}
    /**
     * count when the solution was evaluated
     */
    private long evaluation;

    public CMASolution() {
    }

    public CMASolution(double[] x, double fitnessValue, long evaluation) {
        // super(); // cave: default values for fields overwrite super()
        this.functionValue = fitnessValue;
        this.x = x.clone(); // deep copy, see http://java.sun.com/docs/books/jls/third_edition/html/arrays.html 10.7
        this.evaluation = evaluation;
    }

    public CMASolution(double[] x) {
        this.x = x;
    }

    // getter functions
    public double getFitness() {
        return functionValue;
    }

    // setter functions
    public void setFitness(double f) {
        functionValue = f;
    }

    public long getEvaluationNumber() {
        return evaluation;
    }

    public void setEvaluationNumber(long e) {
        evaluation = e;
    }

    public double[] getX() {
        return x.clone();
    }

    public void setX(double[] x_in) {
        x = new double[x_in.length];
        System.arraycopy(x_in, 0, x, 0, x.length);
    }
}