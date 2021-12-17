/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcog.math;

import jcog.Fuzzy;
import jcog.Util;
import jcog.signal.anomaly.ewma.Ewma;
import org.eclipse.collections.api.block.function.primitive.FloatToFloatFunction;

import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/**
 *
 * @author me
 */
@FunctionalInterface
public interface FloatSupplier extends DoubleSupplier {

    float asFloat();
    
    @Override
    default double getAsDouble() { return asFloat(); }



    default FloatSupplier compose(FloatToFloatFunction g) {
        return () -> g.valueOf(asFloat());
    }

    /** NaN is pass-thru */
    default FloatSupplier composeIfFinite(FloatToFloatFunction g) {
        return () -> {
            float x = asFloat();
            return x == x ? g.valueOf(x) : Float.NaN;
        };
    }

    static FloatToFloatFunction compose(FloatToFloatFunction f, FloatToFloatFunction g) {
        return (x) -> g.valueOf(f.valueOf(x));
    }


    default FloatSupplier plus(float x) {
        if (x == 0) return this;
        return () -> x + FloatSupplier.this.asFloat();
    }

    default FloatSupplier times(float x) {
        if (x == 0) return ()->0;
        if (x == 1) return this;
        return () -> x * FloatSupplier.this.asFloat();
    }

    default FloatSupplier pow(float exp) {
        return exp==1 ? this : compose(z -> (float) Math.pow(z, exp));
    }

    default FloatSupplier clamp(float min, float max) {
        return () -> Util.clamp(asFloat(), min, max);
    }

    default FloatSupplier unpolarize() {
        return () -> Fuzzy.unpolarize(asFloat());
    }

    default FloatSupplier diff(LongSupplier clock) {
        return new FloatDifference(this, clock);
    }

    default FloatSupplier abs() {
        return ()-> Math.abs(FloatSupplier.this.asFloat());
    }

    default FloatSupplier ewma(int halfLife) {
        return ewma(halfLife, halfLife);
    }

    default FloatSupplier ewma(int halfLifeExpand, int halfLifeContract) {
        return new Ewma().period(halfLifeExpand, halfLifeContract).on(this);
    }
}