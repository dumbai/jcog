package jcog.math;

import jcog.Util;
import org.eclipse.collections.api.block.procedure.primitive.FloatProcedure;
//import org.hipparchus.stat.descriptive.StatisticalSummary;

/**
 * floatSummaryStatistics in java.util can't be cleared
 * wtf
 * anyway we'll add stdev calcluation to this and it will
 * serve as a lighter weight replacement for Apache
 * Commons Math SummaryStatistics which also is undesirable
 *
 */
public class RecycledSummaryStatistics implements FloatProcedure/*, StatisticalSummary*/ {

    protected long count;

    protected double sSum;
    

    protected double min;
    protected double max;
    protected double mean;

    /**
     * Construct an empty instance with zero count, zero sum,
     * {@code float.POSITIVE_INFINITY} min, {@code float.NEGATIVE_INFINITY}
     * max and zero average.
     */
    public RecycledSummaryStatistics() {
        clear();
    }

    @Override
    public final void value(float each) {
        accept(each);
    }

    public final void clear() {
        count = 0;
        sSum = 0;
        mean = 0;
        min = Float.POSITIVE_INFINITY;
        max = Float.NEGATIVE_INFINITY;
    }
    /**
     * Records another value into the summary information.
     *
     * @param value the input value
     *
     * NOT THREAD SAFE
     */
    public final void accept(double value) {

        
        
        double tmpMean = mean;

        double delta = value - tmpMean;
        mean += delta / ++count;
        sSum += delta * (value - mean);

        
        if (min > value) min = value;
        if (max < value) max = value;
    }












    /**
     * Returns the sum of values recorded, or zero if no values have been
     * recorded.
     *
     * If any recorded value is a NaN or the sum is at any point a NaN
     * then the sum will be NaN.
     *
     * <p> The value of a floating-point sum is a function both of the
     * input values as well as the order of addition operations. The
     * order of addition operations of this method is intentionally
     * not defined to allow for implementation flexibility to improve
     * the speed and accuracy of the computed result.
     *
     * In particular, this method may be implemented using compensated
     * summation or other technique to reduce the error bound in the
     * numerical sum compared to a simple summation of {@code float}
     * values.
     *
     * @apiNote Values sorted by increasing absolute magnitude tend to yield
     * more accurate results.
     *
     * @return the sum of values, or zero if none
     */
    //@Override
    public final double getSum() {
        return (float) getMean() * count;











    }

    /**
     * Returns the minimum recorded value, {@code float.NaN} if any recorded
     * value was NaN or {@code float.POSITIVE_INFINITY} if no values were
     * recorded. Unlike the numerical comparison operators, this method
     * considers negative zero to be strictly smaller than positive zero.
     *
     * @return the minimum recorded value, {@code float.NaN} if any recorded
     * value was NaN or {@code float.POSITIVE_INFINITY} if no values were
     * recorded
     */
    //@Override
    public final double getMin() {
        return min;
    }

    //@Override
    public long getN() {
        return count;
    }

    /**
     * Returns the maximum recorded value, {@code float.NaN} if any recorded
     * value was NaN or {@code float.NEGATIVE_INFINITY} if no values were
     * recorded. Unlike the numerical comparison operators, this method
     * considers negative zero to be strictly smaller than positive zero.
     *
     * @return the maximum recorded value, {@code float.NaN} if any recorded
     * value was NaN or {@code float.NEGATIVE_INFINITY} if no values were
     * recorded
     */
    //@Override
    public final double getMax() {
        return max;
    }

    /**
     * {@inheritDoc}
     *
     * Returns a non-empty string representation of this object suitable for
     * debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.
     */
    @Override
    public String toString() {
        return String.format(
                "%s{n=%d, sum=%f, min=%f, avg=%f, max=%f}",
                getClass().getSimpleName(),
                getN(),
                getSum(),
                getMin(),
                (float) getMean(),
                getMax());
    }

    /** to 0..1.0 range */
    public final float normalize(float n) {
        double min = getMin();
        double max = getMax();
        double range = max - min;
		/* estimate of an FP epsilon */
		return range < Float.MIN_NORMAL * 64f ? 0.5f : (float) ((n - min) / (range));
    }

    /**
     * Returns the standard deviation of the values that have been added.
     * <p>
     * Double.NaN is returned if no values have been added.
     * </p>
     * The Standard Deviation is a measure of how spread out numbers are.
     * @return the standard deviation
     */
    //@Override
    public double getStandardDeviation() {
        double v = getVariance();
		return v == v ? (float) Math.sqrt(v) : Float.NaN;
    }

    //@Override
    public double getMean() {
        return mean;
    }

    //@Override
    public double getVariance() {
        long c = count;
        if (c == 0) return Float.NaN;
        return sSum / c;
    }

    /** returns the proportion that is lies between min and max. if min==max, then returns 0.  clips to 0..1.0 */
    public float norm(float x) {
        double min = this.min;
        double max = this.max;
        return norm(x, min, max);
    }

    public float normPolar(float x) {
        return Util.unitize( x / magnitude() );
    }

    public float magnitude() {
        return (float)Math.max(Math.abs(max), Math.abs(min));
    }

    public static float norm(float x, double min, double max) {
        double r = max - min;
        if (r < Double.MIN_NORMAL) return 0;
        return Util.unitize( (float)((x - min) / r) );
    }
















    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double v) {
        this.max = v;
    }







}
