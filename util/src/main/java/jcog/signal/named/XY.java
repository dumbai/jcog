package jcog.signal.named;


import jcog.signal.FloatRange;
import jcog.signal.tensor.ArrayTensor;

public abstract class XY extends ArrayTensor {
    public final FloatRange x;
    public final FloatRange y;

    public XY(float min, float max) {
        this(min, max, new float[2]);
    }

    protected XY(float min, float max, float[] array) {
        super(array);

        x = new FloatRange(1f, min, max) {
            @Override
            public void set(float newValue) {
                super.set(data[0] = newValue);
            }
        };
        y = new FloatRange(1f, min, max) {
            @Override
            public void set(float newValue) {
                super.set(data[1] = newValue);
            }
        };
    }
}