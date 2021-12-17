package jcog.signal.tensor;

import jcog.signal.Tensor;

/**
 * chains N equally-shaped tensors along the existing 0th dimension
 * TODO make the composed super-dimension arbitrary.
 * it can either extend an existing shape dimension or add a new dimension
 */
public class TensorChain extends TensorSerial {


    private TensorChain(int[] shape, Tensor... sub) {
        super(sub);
    }

    public static Tensor get(Tensor... t) {
        assert (t.length > 0);

        if (t.length == 1)
            return t[0];

        int[] shape = t[0].shape().clone();
        shape[0] = 0;
        for (Tensor x : t) {
            int[] xs = x.shape();
            assert (xs.length == shape.length);
            for (int d = 1; d < xs.length; d++) {
                assert (xs[d] == shape[d]);
            }
            shape[0] += xs[0];
        }
        TensorChain tt = new TensorChain(shape, t);
        tt.update();
        return tt;
    }


    //wtf?
//    @Override
//    public void forEach(IntFloatProcedure sequential, int start, int end) {
//        assert (start == 0);
//        assert (end == volume());
//        final int[] p = {0};
//        for (Tensor x : sub) {
//            x.forEach((i, v) -> {
//                sequential.value(p[0]++, v);
//            });
//        }
//    }

}
