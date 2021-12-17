package jcog.normalize;

import jcog.math.FloatSupplier;

public class FloatNormalized implements FloatSupplier {

    private final FloatSupplier in;

    public final FloatNormalizer normalizer;

    @Deprecated public FloatNormalized(FloatSupplier in) {
        this(in, new FloatNormalizer(
                2, 2000));
    }
    public FloatNormalized(FloatSupplier in, FloatNormalizer norm) {
        normalizer = norm;
        this.in = in;
    }

    public final FloatNormalized polar() {
        normalizer.polar();
        return this;
    }

    @Override
    public float asFloat() {
        float raw = in.asFloat();
        return raw == raw ? normalizer.valueOf(raw) : Float.NaN;
    }

    public FloatNormalized period(int expansionIters, int contractionIters) {
        normalizer.period(expansionIters, contractionIters);
        return this;
    }

    @Deprecated public FloatNormalized updateRange(float x) {
        normalizer.updateRange(x);
        return this;
    }
    @Override
    public String toString() {
        return getClass().getSimpleName() +
                in + "," +
                "min=" + normalizer.min() +
                ", max=" + normalizer.max() +
                '}';
    }


    public final FloatNormalized range(double min, double max) {
        normalizer.range(min, max);
        return this;
    }

    public FloatNormalized minLimit(double min, double max) {
        normalizer.minLimit(min, max);
        return this;
    }
    public FloatNormalized maxLimit(double min, double max) {
        normalizer.maxLimit(min, max);
        return this;
    }

}