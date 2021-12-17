package jcog.signal.tensor;

import jcog.signal.Tensor;
import org.eclipse.collections.api.block.function.primitive.FloatToFloatFunction;
import org.eclipse.collections.api.block.procedure.primitive.IntFloatProcedure;

/** view applying a specified function to each element */
public class TensorFunc implements Tensor {

    public final FloatToFloatFunction func;
    private final Tensor from;

    public TensorFunc(Tensor from, FloatToFloatFunction func) {
        this.from = from;
        this.func = func;
    }

    @Override
    public int volume() {
        return from.volume();
    }

    @Override
    public float get(int... cell) {
        return func.valueOf(from.get(cell));
    }

    @Override
    public float getAt(int linearCell) {
        return func.valueOf(from.getAt(linearCell));
    }

    @Override
    public int index(int... cell) {
        return from.index(cell);
    }

    @Override
    public float[] snapshot() {
        float[] x = from.snapshot();
        for (int i = 0; i < x.length; i++)
            x[i] = func.valueOf(x[i]);
        return x;
    }

    public ArrayTensor get() {
        return new ArrayTensor(snapshot(), shape());
    }

    @Override
    public int[] shape() {
        return from.shape();
    }

    @Override
    public void forEach(IntFloatProcedure each, int start, int end) {
        from.forEach((i,v) -> each.value(i, func.valueOf(v)), start, end);
    }

}