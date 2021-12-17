package jcog.lab;

import jcog.table.DataTable;
import org.eclipse.collections.api.block.function.primitive.BooleanFunction;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;

import java.util.function.*;

public abstract class NumberSensor<X> extends Sensor<X,Number> {

    public NumberSensor(String name) {
        super(name);
    }


    @Override
    public void register(DataTable data) {
        data.defineNumeric(id);
    }

    public static <X> NumberSensor<X> ofNumber(String id, Function<X,Number> lambda) {
        return new NumberLambdaSensor<>(id, lambda);
    }

    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public static <X> NumberSensor<X> of(String id, FloatFunction<X> f) {
        return ofNumber(id, f::floatValueOf);
    }
    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public static <X> NumberSensor<X> of(String id, BooleanFunction<X> f) {
        return of(id, (Predicate<X>)(f::booleanValueOf));
    }
    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public static <X> NumberSensor<X> of(String id, ToDoubleFunction<X> f) {
        return ofNumber(id, f::applyAsDouble);
    }
    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public static <X> NumberSensor<X> of(String id, ToLongFunction<X> f) {
        return ofNumber(id, f::applyAsLong);
    }
    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public static <X> NumberSensor<X> of(String id, ToIntFunction<X> f) {
        return ofNumber(id, f::applyAsInt);
    }
    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public static <X> NumberSensor<X> of(String id, Predicate<X> f) {
        return ofNumber(id, (x -> f.test(x) ? 1 : 0));
    }



    /** general-purpose numeric scalar value observation
     *  32-bit float.  may be NaN if unknown or N/A */
    private static class NumberLambdaSensor<X> extends NumberSensor<X> {

        private final Function<X,Number> func;

        NumberLambdaSensor(String name, Function<X,Number> f) {
            super(name);
            this.func = f;
        }

        @Override
        public Number apply(X e) {
            return func.apply(e);
        }
    }
}