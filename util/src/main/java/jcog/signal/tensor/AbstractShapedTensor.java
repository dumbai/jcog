package jcog.signal.tensor;

import jcog.signal.Tensor;
import jcog.util.ArrayUtil;

/** constant shape */
public abstract class AbstractShapedTensor extends AbstractTensor {
    public final int[] shape;
    protected final transient int[] stride;

    protected AbstractShapedTensor(int[] shape) {

        this.stride = shape.length > 1 ? Tensor.stride(shape) : ArrayUtil.EMPTY_INT_ARRAY;

        this.shape = shape;
    }


    @Override
    public int[] stride() {
        return stride;
    }

    @Override
    public int[] shape() {
        return shape;
    }

}
