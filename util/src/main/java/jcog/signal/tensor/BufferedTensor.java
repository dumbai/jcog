package jcog.signal.tensor;

import jcog.signal.Tensor;

public class BufferedTensor extends BatchArrayTensor {

    protected final Tensor from;

    public BufferedTensor(Tensor from) {
        super(from.shape());
        this.from = from;
    }


    @Override public void update() {
        //from.snapshot();
        from.writeTo(data);
    }

}
