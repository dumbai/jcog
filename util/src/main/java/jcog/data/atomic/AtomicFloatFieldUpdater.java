//package jcog.data.atomic;
//
//import jcog.math.FloatSupplier;
//
//import org.eclipse.collections.api.block.function.primitive.FloatToFloatFunction;
//import org.eclipse.collections.api.block.function.primitive.FloatToIntFunction;
//import org.eclipse.collections.api.block.procedure.primitive.FloatProcedure;
//
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.function.IntBinaryOperator;
//import java.util.function.IntUnaryOperator;
//
//import static java.lang.Float.floatToIntBits;
//import static java.lang.Float.intBitsToFloat;
//
//
//@Deprecated public final class AtomicFloatFieldUpdater<X>  {
//
//    public static final int iNaN = floatToIntBits(Float.NaN);
//    public static final int iZero = floatToIntBits(0f);
//    /** @noinspection AtomicFieldUpdaterNotStaticFinal*/
//    public final MetalAtomicIntegerFieldUpdater<X> INT;
//
//
////    /** for whatever reason, the field updater needs constructed from within the target class
////     * so it must be passed as a parameter here.
////     * ex: AtomicIntegerFieldUpdater.newUpdater(AtomicFloat.class, "f")
////     */
////    @Deprecated public AtomicFloatFieldUpdater(AtomicIntegerFieldUpdater<X> u) {
////        this.updater = u;
////    }
//
//    public AtomicFloatFieldUpdater(Class<X> cl, String f) {
//        this.INT = new MetalAtomicIntegerFieldUpdater<>(cl, f);
//    }
//
//    public void setNaN(X x) {
//        INT.set(x, iNaN);
//    }
//
//    public void set(X x, float value) {
//        //INT.set(x, floatToIntBits(value));
//        INT.set(x, value);
//    }
//
//
//
//    public void add(X x, float add) {
//
//        //INT.updateAndGet(x, v -> floatToIntBits(intBitsToFloat(v) + addAt));
//
//        int prev, next;
//        do {
//            prev = INT.get(x);
//            next = floatToIntBits( intBitsToFloat(prev) + add);
//        } while(prev!=next && !INT.compareAndSet(x, prev, next));
//
//
//        //adapted from AtomicDouble:
////        int i;
////        do {
////            i = updater.get(x);
////        } while (!updater.compareAndSet(x, i, Float.floatToIntBits(intBitsToFloat(i) +addAt)));
//    }
//
//    private float updateGet(X x, IntUnaryOperator y) {
//        return intBitsToFloat(INT.updateAndGet(x, y));
//    }
//    private float getUpdate(X x, IntUnaryOperator y) {
//        return intBitsToFloat(INT.getAndUpdate(x, y));
//    }
//    private float updateGet(X x, IntBinaryOperator yMustFloatizeBothInts, float arg) {
//        return intBitsToFloat(INT.accumulateAndGet(x, Float.floatToIntBits(arg), yMustFloatizeBothInts));
//    }
//
//
//
//    private void update(X x, IntUnaryOperator y) {
//        INT.updateAndGet(x, y);
//    }
//
//
//    public float updateIntAndGet(X x, FloatToIntFunction f) {
//        return updateGet(x, v -> f.valueOf(intBitsToFloat(v)));
//    }
//
//    public float getAndUpdate(X x, FloatSupplier f) {
//        return getUpdate(x, v -> floatToIntBits(f.asFloat()));
//    }
//    public float updateAndGet(X x, FloatSupplier f) {
//        return updateGet(x, v -> floatToIntBits(f.asFloat()));
//    }
//
////    public float updateAndGet(X x, FloatToFloatFunction f) {
////        return updateGet(x, v -> floatToIntBits(f.valueOf(intBitsToFloat(v))));
////    }
////
////    public float getAndUpdate(X x, FloatFloatToFloatFunction f, float y) {
////        return getUpdate(x, v -> floatToIntBits(f.apply(intBitsToFloat(v), y)));
////    }
//
//    /** @see AtomicInteger.accumulateAndGet */
//    public float updateAndGet(X x, FloatFloatToFloatFunction f, float y) {
//        //return updateGet(x, v -> floatToIntBits(f.apply(intBitsToFloat(v), y)));
//
//        int prev, next;
//        float nextFloat;
//        do {
//            prev = INT.get(x);
//            nextFloat = f.apply(intBitsToFloat(prev), y);
//            next = floatToIntBits(nextFloat);
//        } while(prev!=next && !INT.compareAndSet(x, prev, next));
//        return nextFloat;
//    }
//
//    public void update(X x, FloatFloatToFloatFunction f, float y) {
//        int prev, next;
//        do {
//            prev = INT.get(x);
//            next = floatToIntBits(f.apply(intBitsToFloat(prev), y));
//        } while(prev!=next && !INT.compareAndSet(x, prev, next));
//    }
//
//    public float getAndSet(X x, float value) {
//        return intBitsToFloat(INT.getAndSet(x, floatToIntBits(value)));
//    }
//
//    public float setAndGet(X x, float value) {
//        //INT.set(x, floatToIntBits(value));
//        set(x, value);
//        return value;
//    }
//
//
//    public float getAndSetZero(X x) {
//        return intBitsToFloat(INT.getAndSet(x, iZero));
//    }
//    public float getAndSetNaN(X x) {
//        return intBitsToFloat(INT.getAndSet(x, iNaN));
//    }
//
//    public void zero(X x) {
//        INT.set(x, iZero);
//    }
//
//
//    public float get(X x) {
//        //return intBitsToFloat(INT.get(x));
//        return INT.getFloat(x);
//    }
//    public float getOpaque(X x) {
//        return get(x); //getFloatOpaque not valilable
//        //return intBitsToFloat(INT.getOpaque(x));
//    }
//
//    public void zero(X v, FloatProcedure with) {
//        this.INT.getAndUpdate(v, x->{
//            with.value(intBitsToFloat(x));
//            return AtomicFloatFieldUpdater.iZero;
//        });
//    }
//
//
//    float getAndSetZero(X v, FloatProcedure with) {
//        return intBitsToFloat(this.INT.getAndUpdate(v, (x)->{ with.value(intBitsToFloat(x)); return AtomicFloatFieldUpdater.iZero; } ));
//    }
//
//    public boolean compareAndSet(X x, float expected, float newvalue) {
//        return INT.compareAndSet(x, floatToIntBits(expected), floatToIntBits(newvalue));
//    }
//
//
////    public float updateIntAndGet(X x, FloatToFloatFunction update, FloatToIntFunction post) {
////        return updateIntAndGet(x, (v)-> post.valueOf(update.valueOf(v)));
////    }
//
//    public float updateAndGet(X x, float y, FloatFloatToFloatFunction inner, FloatToFloatFunction outer) {
//        //return updateAndGet(x, (xx,yy)-> post.valueOf(update.apply(xx,yy)), arg);
//
//        int prev, next;
//        float nextFloat;
//        do {
//            prev = INT.get(x);
//            nextFloat = outer.valueOf(inner.apply(intBitsToFloat(prev), y));
//            next = floatToIntBits(nextFloat);
//        } while(prev!=next && !INT.compareAndSet(x, prev, next));
//        return nextFloat;
//    }
//
//
//    public void setLazy(X x, float v) {
//        INT.lazySet(x, Float.floatToIntBits(v));
//    }
//}