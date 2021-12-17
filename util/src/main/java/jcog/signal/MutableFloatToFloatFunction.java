package jcog.signal;

import jcog.Util;
import org.eclipse.collections.api.block.function.primitive.FloatToFloatFunction;

public class MutableFloatToFloatFunction implements FloatToFloatFunction {
    FloatToFloatFunction f;

    public MutableFloatToFloatFunction(FloatToFloatFunction f) {
        this.f = f;
    }

    @Override
    public float valueOf(float x) {
        return f.valueOf(x);
    }

    public MutableFloatToFloatFunction linear(float min, float max) {
        this.f = x -> Util.lerp(x, min, max);
        return this;
    }

    public MutableFloatToFloatFunction poly(float initialDegree, float min, float max) {
        this.f = new PolyFloatToFloatFunction(initialDegree, min, max);
        return this;
    }

    public static class LinearFloatToFloatFunction implements FloatToFloatFunction {

        /** TODO mutable? */
        private final float min;
        private final float max;

        public LinearFloatToFloatFunction(float min, float max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public float valueOf(float x) {
            return Util.lerp(x, min, max);
        }
    }
    public static class PolyFloatToFloatFunction extends LinearFloatToFloatFunction {

        public final FloatRange degree;

        public PolyFloatToFloatFunction(float initialDegree, float min, float max) {
            super(min, max);
            degree = new FloatRange(initialDegree, 0, 16);
        }

        @Override
        public float valueOf(float x) {
            return super.valueOf((float) Math.pow(x, degree.asFloat()));
        }
    }
}