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
public class FloatAveraged implements FloatToFloatFunction {
    private float prev;
    private final FloatRange alpha;
    private final boolean lowOrHighPass;

    public FloatAveraged(float alpha) {
        this(alpha, true);
    }

    public FloatAveraged(float alpha, boolean lowOrHighPass) {
        this(new FloatRange(alpha, 0, 1f), lowOrHighPass);
    }

    public FloatAveraged(FloatRange alpha, boolean lowOrHighPass) {
        this.alpha = alpha;
        this.lowOrHighPass = lowOrHighPass;
    }

    @Override
    public float valueOf(float x) {


//        synchronized (this) {
            if (x != x)
                return this.prev;

            float p = prev, next;
            if (p == p) {
                double alpha = this.alpha.asFloat();
                next = (float) (alpha * x + (1 - alpha) * p);
            } else {
                next = x;
            }
            this.prev = next;
            return lowOrHighPass ? next : x - next;
//        }
    }

    /**
     * previous value computed by valueOf
     */
    public float floatValue() {
        return prev;
    }


}