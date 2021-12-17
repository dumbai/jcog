package jcog.signal.named;

import jcog.signal.FloatRange;
import jcog.signal.tensor.ArrayTensor;

/**
 * labeled FloatRange controls underlying ArrayTensor.  useful for UI purposes that rely on reflection for constructing widgets
 */
public class RGB extends ArrayTensor {
    public final FloatRange red;
    public final FloatRange green;
    public final FloatRange blue;

    public RGB() {
        this(0, 1);
    }

    public RGB(float min, float max) {
        super(new float[3]);
        red = new FloatRange(1f, min, max) {
            @Override
            public void set(float newValue) {
                super.set(data[0] = newValue);
            }
        };
        green = new FloatRange(1f, min, max) {
            @Override
            public void set(float newValue) {
                super.set(data[1] = newValue);
            }
        };
        blue = new FloatRange(1f, min, max) {
            @Override
            public void set(float newValue) {
                super.set(data[2] = newValue);
            }
        };
    }
}