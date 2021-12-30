package jcog.normalize;

import jcog.Util;
import org.eclipse.collections.api.block.function.primitive.FloatToFloatFunction;

import static java.lang.Math.abs;
import static jcog.Util.lerp;

public class FloatNormalizer implements FloatToFloatFunction {

    public double min, max;
    double minMin = Double.NEGATIVE_INFINITY, minMax = Double.POSITIVE_INFINITY;
    double maxMin = Double.NEGATIVE_INFINITY, maxMax = Double.POSITIVE_INFINITY;

    /**
     * contraction rate: how quickly the normalizatoin limits shrink to zero
     * TODO determine according to # of iterations period count
     */
    private double contracting;

    /**
     * expansion rate: how quickly the normalization limits can grow
     * TODO determine according to # of iterations period count
     */
    private double expanding;

    /** whether to reflect the min/max across zero */
    private boolean polar = false;

    public FloatNormalizer(int expandIterations, int contractIterations) {
        period(expandIterations, contractIterations);
        clear();
    }

    public final FloatNormalizer minLimit(double minLimit, double maxLimit) {
        this.minMin = minLimit; this.minMax = maxLimit;
        return this;
    }
    public final FloatNormalizer maxLimit(double minLimit, double maxLimit) {
        this.maxMin = minLimit; this.maxMax = maxLimit;
        return this;
    }

    public final FloatNormalizer period(int expandIterations, int contractIterations) {
        expanding = Util.halflifeRate(expandIterations);
        contracting = Util.halflifeRate(contractIterations);
        if (Util.equals(contracting,1))
            throw new UnsupportedOperationException("instantaneous contraction");

        return this;
    }

    public final FloatNormalizer polar() {
        this.polar = true;
        return this;
    }

    protected double normalize(float x) {
        if (x != x)
            return Float.NaN; //uninitialized

        double r = Math.max(0, max - min);
        return r <= Float.MIN_NORMAL ? 0.5f : ((x - min) / r);
    }

    @Override
    public String toString() {
        return "FloatNormalizer{" + min + ".." + max + '}';
    }

    public void clear() {
        this.min = max = Float.NaN;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public float valueOf(float raw) {
        if (raw != raw)
            return Float.NaN;

        updateRange(raw);

        return (float) normalize(raw);
    }

    FloatNormalizer updateRange(double x) {
        double min = this.min, max = this.max;
        if (min != min) {
            min = max = x; //initialize
        } else {
            //double mid = (min + max) / 2;

            //contract bottom towards mid
            if (x > min) min = _min(lerp(contracting, min, x /*mid*/));

            //contract top towards mid
            if (x < max) max = _max(lerp(contracting, max, x /*mid*/));

            if (x < min) min = _min(lerp(expanding, min, x));

            if (x > max) max = _max(lerp(expanding, max, x));


        }
        range(min, max);
        return this;
    }

    public final FloatNormalizer range(double min, double max) {
        if (polar) {
            double amp = Math.max(abs(min), abs(max));
            _range(-amp, +amp);
        } else {
            _range(Math.min(max, min), Math.max(min, max));
        }
        return this;
    }

    private void _range(double min, double max) {
        min = _min(min);
        max = _max(max);
        if (min > max) {
            min = max = (min+max)/2; //HACK mean if out of order
        }
        this.min = min; this.max = max;
    }

    private double _max(double max) {
        return Util.clamp(max, maxMin, maxMax);
    }

    private double _min(double min) {
        return Util.clamp(min, minMin, minMax);
    }

    //    public static class FloatBiasedNormalizer extends FloatNormalizer {
//        public final FloatRange bias;
//
//        public FloatBiasedNormalizer(FloatRange bias) {
//            this.bias = bias;
//        }
//
//        @Override
//        protected float normalize(float x, float min, float max) {
//            float y = super.normalize(x, min, max);
//            if (y == y) {
//                float balance = Util.unitize(bias.floatValue());
//                if (y >= 0.5f) {
//                    return Util.lerp(2f * (y - 0.5f), balance, 1f);
//                } else {
//                    return Util.lerp(2f * (0.5f - y), balance, 0f);
//                }
//            } else
//                return Float.NaN;
//
//            //return Util.unitize(y + (b - 0.5f));
//        }
//    }
}