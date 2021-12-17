package jcog.signal.tensor;

import jcog.signal.Tensor;
import jcog.util.PriReturn;
import org.eclipse.collections.api.block.function.primitive.FloatFloatToFloatFunction;
import org.eclipse.collections.api.block.function.primitive.FloatToFloatFunction;
import org.jetbrains.annotations.Nullable;

public interface WritableTensor extends Tensor {

    void setAt(int linearCell, float newValue);

    default void set(float newValue, int... cell) {
        setAt(index(cell), newValue);
    }

    /** returns the new value after adding */
    default float addAt(int linearCell, float x) {
        float next = getAt(linearCell);
        if (next!=next) next = 0; //reset to zero HACK TODO optional
        next += x;
        setAt(linearCell, next);
        return next;
    }

    /** updates each cell individually */
    default void update(FloatToFloatFunction f) {
        int v = volume();
        for (int i = 0; i < v; i++) {
            float x = getAt(i);
            float y = f.valueOf(x);
            if (x!=y) //TODO NaN sensitive
                setAt(i, y);
        }
    }

    default float mulAt(int linearCell, float x) {
        float next = getAt(linearCell) * x;
        setAt(linearCell, next);
        return next;
    }

    default void setAt(int linearCellStart, float[] values) {
        int i = linearCellStart;
        for (float v: values)
            setAt(i++, v);
    }

//    default void readFrom(Tensor from, int[] fromStart, int[] fromEnd, int[] myStart, int[] myEnd) {
//        throw new TODO();
//    }

    default /* final */ float merge(int linearCell, float x, FloatFloatToFloatFunction f) {
        return merge(linearCell, x, f, PriReturn.Post);
    }

    default float merge(int linearCell, float x, FloatFloatToFloatFunction f, @Nullable PriReturn y) {
        float prev = getAt(linearCell);

        float next = f.valueOf(prev, x);

        if (prev!=next)
            setAt(linearCell, next);

        return y!=null ? y.apply(x, prev, next) : Float.NaN;
    }

    default void fill(float x) {
        fill(0, volume(), x);
    }

    default void fill(int a, int b, float x) {
        for (int i = a; i < b; i++)
            setAt(i, x);
    }

    default void setAll(float[] values) {
        int v = volume();
        if(v!=values.length) throw new ArrayIndexOutOfBoundsException();

        for (int i = 0; i < v; i++) setAt(i, values[i]);
    }

    default void setAll(double[] values) {
        int v = volume();
        if(v!=values.length) throw new ArrayIndexOutOfBoundsException();

        for (int i = 0; i < v; i++) setAt(i, (float)values[i]);
    }

    default void set(Tensor x) {
        x.forEach(this::setAt);
    }
}