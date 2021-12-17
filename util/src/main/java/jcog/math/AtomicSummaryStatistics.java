//package jcog.math;
//
//import org.eclipse.collections.api.block.procedure.primitive.DoubleProcedure;
//import org.eclipse.collections.api.block.procedure.primitive.FloatProcedure;
//
//import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.concurrent.atomic.DoubleAccumulator;
//import java.util.function.Consumer;
//
///** see also: https://github.com/apache/spark/blob/master/core/src/main/scala/org/apache/spark/util/StatCounter.scala */
//public class AtomicSummaryStatistics implements FloatProcedure, DoubleProcedure, StatisticalSummary {
//
//    protected long count;
//    protected double min;
//    protected double max;
//    protected double mean;
//    protected double sum;
//
//    CopyOnWriteArrayList<Consumer<AtomicSummaryStatistics>> onCommit;
//
//    /**
//     * NaN triggers reset
//     */
//    final DoubleAccumulator update = new DoubleAccumulator((ss, v) -> {
//        if (v == v) {
//
//
//            sum += v;
//            if (min > v) min = v;
//            if (max < v) max = v;
//
//
//
//            double tmpMean = mean;
//
//            double delta = v - tmpMean;
//            mean += delta / ++count;
//            return ss + delta * (v - mean);
//        } else {
//            if (onCommit!=null) {
//                for (Consumer<AtomicSummaryStatistics> c : onCommit)
//                    c.accept(this);
//            }
//            count = 0;
//            mean = 0;
//            sum = 0;
//            min = Double.POSITIVE_INFINITY;
//            max = Double.NEGATIVE_INFINITY;
//            return 0;
//        }
//    }, 0);
//
//    /**
//     * Construct an empty instance with zero count, zero sum,
//     * {@code float.POSITIVE_INFINITY} min, {@code float.NEGATIVE_INFINITY}
//     * max and zero average.
//     */
//    public AtomicSummaryStatistics() {
//        clear();
//    }
//
//    @Override
//    public final void value(float each) {
//        accept(each);
//    }
//
//    @Override
//    public void value(double each) {
//        accept(each);
//    }
//
//    public final void clear() {
//        update.accumulate(Double.NaN);
//    }
//
//
//
//
//
//    @Override
//    public final void accept(double value) {
//        if (value != value)
//            return;
//        update.accumulate(value);
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//    /**
//     * Returns the sum of values recorded, or zero if no values have been
//     * recorded.
//     * <p>
//     * If any recorded value is a NaN or the sum is at any point a NaN
//     * then the sum will be NaN.
//     * <p>
//     * <p> The value of a floating-point sum is a function both of the
//     * input values as well as the order of addition operations. The
//     * order of addition operations of this method is intentionally
//     * not defined to allow for implementation flexibility to improve
//     * the speed and accuracy of the computed result.
//     * <p>
//     * In particular, this method may be implemented using compensated
//     * summation or other technique to reduce the error bound in the
//     * numerical sum compared to a simple summation of {@code float}
//     * values.
//     *
//     * @return the sum of values, or zero if none
//     * @apiNote Values sorted by increasing absolute magnitude tend to yield
//     * more accurate results.
//     */
//    @Override
//    public final double getSum() {
//        return sum;
//
//
//
//
//
//
//
//
//
//
//
//    }
//
//    /**
//     * Returns the minimum recorded value, {@code float.NaN} if any recorded
//     * value was NaN or {@code float.POSITIVE_INFINITY} if no values were
//     * recorded. Unlike the numerical comparison operators, this method
//     * considers negative zero to be strictly smaller than positive zero.
//     *
//     * @return the minimum recorded value, {@code float.NaN} if any recorded
//     * value was NaN or {@code float.POSITIVE_INFINITY} if no values were
//     * recorded
//     */
//    @Override
//    public final double getMin() {
//        return min;
//    }
//
//    @Override
//    public long getN() {
//        return count;
//    }
//
//    /**
//     * Returns the maximum recorded value, {@code float.NaN} if any recorded
//     * value was NaN or {@code float.NEGATIVE_INFINITY} if no values were
//     * recorded. Unlike the numerical comparison operators, this method
//     * considers negative zero to be strictly smaller than positive zero.
//     *
//     * @return the maximum recorded value, {@code float.NaN} if any recorded
//     * value was NaN or {@code float.NEGATIVE_INFINITY} if no values were
//     * recorded
//     */
//    @Override
//    public final double getMax() {
//        return max;
//    }
//
//    /**
//     * {@inheritDoc}
//     * <p>
//     * Returns a non-empty string representation of this object suitable for
//     * debugging. The exact presentation format is unspecified and may vary
//     * between implementations and versions.
//     */
//    @Override
//    public String toString() {
//        return String.format(
//                "%s{n=%d, sum=%f, min=%f, avg=%f, max=%f}",
//                getClass().getSimpleName(),
//                count,
//                sum,
//                min,
//                mean,
//                max);
//    }
//
//
//    /**
//     * Returns the standard deviation of the values that have been added.
//     * <p>
//     * Double.NaN is returned if no values have been added.
//     * </p>
//     * The Standard Deviation is a measure of how spread out numbers are.
//     *
//     * @return the standard deviation
//     */
//    @Override
//    public double getStandardDeviation() {
//        double v = getVariance();
//		return v == v ? Math.sqrt(v) : Double.NaN;
//    }
//
//    @Override
//    public double getMean() {
//        return mean;
//    }
//
//    @Override
//    public double getVariance() {
//        long c = count;
//        if (c == 0) return Double.NaN;
//        return update.doubleValue() / (c);
//    }
//
//    public void on(Consumer<AtomicSummaryStatistics> o) {
//        if (onCommit==null)
//            onCommit = new CopyOnWriteArrayList<>();
//        onCommit.add(o);
//    }
//
//    /** asynchronous sum integrator */
//    public AtomicSummaryStatistics sumIntegrator() {
//        AtomicSummaryStatistics i = new AtomicSummaryStatistics();
//        on((x) -> i.accept(x.sum));
//        return i;
//    }
//
//
//
//
//
//
//
//
//    public float sumThenClear() {
//        float f = (float) sum;
//        clear();
//        return f;
//    }
//    public float meanThenClear() {
//        float f = (float) mean;
//        clear();
//        return f;
//    }
//
//}
