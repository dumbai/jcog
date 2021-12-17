package jcog.signal.tensor;

import jcog.signal.Tensor;
import org.eclipse.collections.api.block.function.primitive.FloatFloatToFloatFunction;
import org.eclipse.collections.api.block.procedure.primitive.IntFloatProcedure;

/** tensor computed by applying a function to its previous value */
public class TensorMerge extends BatchArrayTensor {

    private final FloatFloatToFloatFunction func;
    private final Tensor from;

    protected TensorMerge(Tensor from) {
        super(from.shape());
        this.from = from;
        //noinspection CastToIncompatibleInterface
        this.func = (FloatFloatToFloatFunction)this;
    }

    public TensorMerge(Tensor from, FloatFloatToFloatFunction func) {
        super(from.shape());
        this.from = from;
        this.func = func;
    }

    /** updates any local state variables prior to a batch operation */
    protected void commit() {

    }

    @Override
    public float[] snapshot() {
        commit();
        return super.snapshot();
    }

    @Override
    public void forEach(IntFloatProcedure each, int start, int end) {
        commit();
        super.forEach(each, start, end);
    }

    @Override
    public void writeTo(FloatFloatToFloatFunction perElement, float[] target, int offset) {
        commit();
        super.writeTo(perElement, target);
    }


    @Override
    public void update() {
        commit();
        from.writeTo(func, data);
    }


}