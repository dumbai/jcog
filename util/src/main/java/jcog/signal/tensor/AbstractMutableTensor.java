package jcog.signal.tensor;

public abstract class AbstractMutableTensor extends AbstractShapedTensor implements WritableTensor {

    protected AbstractMutableTensor(int[] shape) {
        super(shape);
    }

}
