package jcog.signal.tensor;

import jcog.Util;
import jcog.signal.Tensor;

import java.util.Collection;

/** similar to TensorChain, this provides a dynamic
 *  serialized/linearized/flattened view of
 *  a fixed array of tensors of arbitrary shape.
 */

public class TensorSerial extends BatchArrayTensor {

    private final Tensor[] sub;

    public TensorSerial(Collection<Tensor> sub) {
        this(sub.toArray(Tensor.EmptyArray));
    }

    public TensorSerial(Tensor... sub) {
        super(new int[] { Util.sum(Tensor::volume, sub) });
        this.sub = sub;
    }


    @Override
    public float getAt(int linearCell) {
        throw new UnsupportedOperationException("TODO similar to the other get");
    }

    @Override
    public void update() {
        int c = 0;
        for (Tensor x : sub) {
            int xv = x.volume();
            x.writeTo(data, c);
            c += xv;
        }
    }

    @Deprecated public void writeTo(double[] target) {
        int n = target.length;
        for (int i = 0; i < n; i++)
            target[i] = data[i];
    }
}
