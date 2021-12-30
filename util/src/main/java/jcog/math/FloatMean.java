package jcog.math;

import jcog.signal.FloatRange;
import org.eclipse.collections.api.block.function.primitive.FloatToFloatFunction;

/**
 * exponential moving average of a Float source.
 * can operate in either low-pass (exponential moving average of a signal) or
 * high-pass modes (signal minus its exponential moving average).
 * <p>
 * https://dsp.stackexchange.com/a/20336
 * https://en.wikipedia.org/wiki/Exponential_smoothing
 * <p>
 * warning this can converge/stall.  best to use FloatAveragedWindow instead
 */
public class FloatMean implements FloatToFloatFunction {
    private float value;
    protected final FloatRange alpha;
    private final boolean lowOrHighPass;

    public FloatMean(float alpha) {
        this(alpha, true);
    }

    public FloatMean(float alpha, boolean lowOrHighPass) {
        this(FloatRange.unit(alpha), lowOrHighPass);
    }

    public FloatMean(FloatRange alpha, boolean lowOrHighPass) {
        this.alpha = alpha;
        this.lowOrHighPass = lowOrHighPass;
    }

    @Override
    public float valueOf(float x) {


//        synchronized (this) {
            if (x != x)
                return this.value;

            float p = value, next;
            if (p == p) {
                double alpha = alpha(x, value);
                next = (float) (alpha * x + (1 - alpha) * p);
            } else {
                next = x;
            }
            this.value = next;
            return lowOrHighPass ? next : x - next;
//        }
    }

    protected float alpha(float next, float prev) {
        return this.alpha.asFloat();
    }

    /**
     * previous value computed by valueOf
     */
    public float floatValue() {
        return value;
    }

}