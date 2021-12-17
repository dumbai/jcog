package jcog.math;

import org.eclipse.collections.api.block.function.primitive.FloatToFloatFunction;

public enum UnitCurve implements FloatToFloatFunction {
    Linear {
        @Override
        public float valueOf(float x) {
            return x;
        }
    },
    Quadratic {
        @Override
        public float valueOf(float x) {
            return x*x;
        }
    },
    Sqrt {
        @Override
        public float valueOf(float x) {
            return (float) Math.sqrt(x);
        }
    }
}
