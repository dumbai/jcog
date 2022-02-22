/*
 * Here comes the text of your license
 * Each line should be prefixed with  *
 */
package jcog.signal;


/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import jcog.TODO;
import jcog.Util;
import jcog.func.IntIntToIntFunction;
import jcog.math.FloatSupplier;
import jcog.math.NumberException;

import java.lang.invoke.VarHandle;
import java.util.function.IntSupplier;

/**
 * A mutable <code>integer</code> wrapper.
 */
public class MutableInteger extends NumberX implements Comparable, IntSupplier, FloatSupplier {

    private static final VarHandle V = Util.VAR(MutableInteger.class, "v", int.class);

    /**
     * The mutable value.
     */
    private volatile int v;

    /**
     * Constructs a new MutableDouble with the default value of zero.
     */
    public MutableInteger() {

    }

    /**
     * Constructs a new MutableDouble with the specified value.
     *
     * @param value a value.
     */
    public MutableInteger(int value) {
        this.v = value;
    }

    /**
     * Constructs a new MutableDouble with the specified value.
     *
     * @param value a value.
     * @throws NullPointerException if the object is null
     */
    public MutableInteger(Number value) {
        this(value.intValue());
    }


    /**
     * Sets the value.
     *
     * @param next the value to setAt
     * @return
     */
    public void set(int next) {
        int prev = (int) V.getAndSet(this, next);
        if (prev != next)
            changed();
    }


    @Override
    public float add(float a) {
        update(Integer::sum, Math.round(a));
        return a;
    }

    @Override
    public float mul(float x) {
        throw new TODO();
    }

    public int update(IntIntToIntFunction f, int y) {
        int prev, next;
        do {
            prev = (int) V.getAcquire(this);
            next = f.apply(prev, y);
        } while (prev != next && !V.weakCompareAndSetRelease(this, prev, next));
        return next;
    }

    public final void set(float value) {
        if (value != value)
            throw new NumberException("NaN", value);
        set(Math.round(value));
    }


    /**
     * implement in subclasses
     */
    protected void changed() {

    }

    /**
     * Sets the value from any Number instance.
     *
     * @param value the value to setAt
     * @throws NullPointerException if the object is null
     * @throws ClassCastException   if the type is not a {@link Number}
     */
    public final void set(Number value) {
        set(value.floatValue());
    }


    /**
     * Returns the value of this MutableDouble as a int.
     *
     * @return the numeric value represented by this object after conversion to
     * type int.
     */
    @Override
    public final int intValue() {
        return (int) V.getOpaque(this);
    }

    @Override
    public final int getAsInt() {
        return (int) V.getOpaque(this);
    }

    /**
     * Returns the value of this MutableDouble as a long.
     *
     * @return the numeric value represented by this object after conversion to
     * type long.
     */
    @Override
    public final long longValue() {
        return getAsInt();
    }

    /**
     * Returns the value of this MutableDouble as a float.
     *
     * @return the numeric value represented by this object after conversion to
     * type float.
     */
    @Override
    public final float floatValue() {
        return getAsInt();
    }

    /**
     * Returns the value of this MutableDouble as a double.
     *
     * @return the numeric value represented by this object after conversion to
     * type double.
     */
    @Override
    public final double doubleValue() {
        return getAsInt();
    }

    @Override
    public final float asFloat() {
        return floatValue();
    }



    /**
     * Compares this mutable to another in ascending order.
     *
     * @param obj the mutable to compare to
     * @return negative if this is less, zero if equal, positive if greater
     * @throws ClassCastException if the argument is not a MutableDouble
     */
    @Override
    public final int compareTo(Object obj) {
        return Integer.compare(intValue(), ((MutableInteger) obj).intValue());
    }

    /**
     * Returns the String value of this mutable.
     *
     * @return the mutable value as a string
     */
    public final String toString() {
        return String.valueOf(intValue());
    }

    public int getAndSet(int x) {
        return (int) V.getAndSet(this, x);
    }

    public int intValueFast() {
        return v;
    }


}