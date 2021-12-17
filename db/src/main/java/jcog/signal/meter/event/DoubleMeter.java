/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcog.signal.meter.event;

import jcog.signal.meter.Metered;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * Stores the latest provided value for retrieval by a Metrics 
 */
public class DoubleMeter extends Metered.AbstractMetric implements DoubleConsumer {
    
    boolean autoReset;
    
    double val;

    public DoubleMeter(String id, boolean autoReset) {
        super(id);
        this.autoReset = autoReset;
    }

    public DoubleMeter(String id) {
        this(id, false);
    }

    public static DoubleMeter get(String id, DoubleSupplier x) {
        return new DoubleMeter(id, true) {
            @Override
            public DoubleMeter reset() {
                set(x.getAsDouble());
                return this;
            }
        };
    }


    public DoubleMeter reset() {
        set(Double.NaN);
        return this;
    }
    
    /** returns the previous value, or NaN if none were set  */
    public double set(double newValue) {
        double oldValue = val;
        val = newValue;
        return oldValue;
    }

    /** current stored value; optionally triggering any overridden update procedures */
    public double get() { return val; }

    /** whether to reset to NaN after the count is next stored in the Metrics */
    public void setAutoReset(boolean autoReset) {
        this.autoReset = autoReset;
    }
    
    public boolean getAutoReset() { return autoReset; }

    @Override
    public void accept(double value) {
        set(value);
    }

    @Override
    public void accept(MeterReader reader) {
        double next;
        reader.set(next = get());
        if (autoReset)
            reset();
        else
            val = next;
    }
}