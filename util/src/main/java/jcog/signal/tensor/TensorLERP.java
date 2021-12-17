package jcog.signal.tensor;

import jcog.Util;
import jcog.math.FloatSupplier;
import jcog.signal.Tensor;
import org.eclipse.collections.api.block.function.primitive.FloatFloatToFloatFunction;


/** rate between 0 and 1 */
public class TensorLERP extends TensorMerge implements FloatFloatToFloatFunction {

    private final FloatSupplier rate;
    private float currentRate;

    public TensorLERP(Tensor from, float rate) {
        this(from, ()->rate);
    }

    public TensorLERP(Tensor from, FloatSupplier rate) {
        super(from);
        this.rate = rate;
        commit();
    }



    @Override protected void commit() {
        currentRate = rate.asFloat();
    }

    @Override
    public float valueOf(float current, float next) {
        return Util.lerp(currentRate, current, next);
    }

}