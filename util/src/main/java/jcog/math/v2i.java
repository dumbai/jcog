/*
 * $RCSfile$
 *
 * Copyright 2005-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision: 127 $
 * $Date: 2008-02-28 17:18:51 -0300 (Thu, 28 Feb 2008) $
 * $State$
 */

package jcog.math;

import java.io.Serializable;

/**
 * A 2-element point represented by signed integer x,y
 * coordinates.
 *
 * @since vecmath 1.4
 */
public class v2i implements Serializable, Cloneable /* TODO Tensor */ {


    /**
     * The x coordinate.
     */
    public /*volatile*/ int x;
    /**
     * The y coordinate.
     */
    public /*volatile*/ int y;

    /**
     * Constructs and initializes a Point2i from the specified
     * x and y coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public v2i(int x, int y) {
        this.x = x;
        this.y = y;
    }


    /**
     * Constructs and initializes a Point2i from the array of length 2.
     *
     * @param t the array of length 2 containing x and y in order.
     */
    public v2i(int[] t) {
        this.x = t[0];
        this.y = t[1];
    }


    /**
     * Constructs and initializes a Point2i from the specified Tuple2i.
     *
     * @param t1 the Tuple2i containing the initialization x and y
     *           data.
     */
    public v2i(v2i t1) {
        this.x = t1.x;
        this.y = t1.y;
    }


    /**
     * Constructs and initializes a Point2i to (0,0).
     */
    public v2i() {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Sets the value of this tuple to the specified x and y
     * coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public final void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public final void setFloor(float x, float y) {
        set((int)x, (int)y);
    }

    public final void setRound(float x, float y) {
        set(Math.round(x), Math.round(y));
    }

    /**
     * Sets the value of this tuple to the specified coordinates in the
     * array of length 2.
     *
     * @param t the array of length 2 containing x and y in order.
     */
    public final void set(int[] t) {
        this.x = t[0];
        this.y = t[1];
    }

    /**
     * Sets the value of this tuple to the value of tuple t1.
     *
     * @param t1 the tuple to be copied
     */
    public final void set(v2i t1) {
        this.x = t1.x;
        this.y = t1.y;
    }

    /**
     * Copies the values of this tuple into the array t.
     *
     * @param t is the array
     */
    public final void get(int[] t) {
        t[0] = this.x;
        t[1] = this.y;
    }

    /**
     * Copies the values of this tuple into the tuple t.
     *
     * @param t is the target tuple
     */
    public final void get(v2i t) {
        t.x = this.x;
        t.y = this.y;
    }

    /**
     * Sets the value of this tuple to the sum of tuples t1 and t2.
     *
     * @param t1 the first tuple
     * @param t2 the second tuple
     */
    public final void add(v2i t1, v2i t2) {
        this.x = t1.x + t2.x;
        this.y = t1.y + t2.y;
    }

    /**
     * Sets the value of this tuple to the sum of itself and t1.
     *
     * @param t1 the other tuple
     */
    public final void add(v2i t1) {
        this.x += t1.x;
        this.y += t1.y;
    }

    /**
     * Sets the value of this tuple to the difference
     * of tuples t1 and t2 (this = t1 - t2).
     *
     * @param t1 the first tuple
     * @param t2 the second tuple
     */
    public final void sub(v2i t1, v2i t2) {
        this.x = t1.x - t2.x;
        this.y = t1.y - t2.y;
    }

    /**
     * Sets the value of this tuple to the difference
     * of itself and t1 (this = this - t1).
     *
     * @param t1 the other tuple
     */
    public final void sub(v2i t1) {
        this.x -= t1.x;
        this.y -= t1.y;
    }

    /**
     * Sets the value of this tuple to the negation of tuple t1.
     *
     * @param t1 the source tuple
     */
    public final void negate(v2i t1) {
        this.x = -t1.x;
        this.y = -t1.y;
    }

    /**
     * Negates the value of this tuple in place.
     */
    public final void negate() {
        this.x = -this.x;
        this.y = -this.y;
    }

    /**
     * Sets the value of this tuple to the scalar multiplication
     * of tuple t1.
     *
     * @param s  the scalar value
     * @param t1 the source tuple
     */
    public final void scale(int s, v2i t1) {
        this.x = s * t1.x;
        this.y = s * t1.y;
    }

    /**
     * Sets the value of this tuple to the scalar multiplication
     * of the scale factor with this.
     *
     * @param s the scalar value
     */
    public final void scale(int s) {
        this.x *= s;
        this.y *= s;
    }

    /**
     * Sets the value of this tuple to the scalar multiplication
     * of tuple t1 plus tuple t2 (this = s*t1 + t2).
     *
     * @param s  the scalar value
     * @param t1 the tuple to be multipled
     * @param t2 the tuple to be added
     */
    public final void scaleAdd(int s, v2i t1, v2i t2) {
        this.x = s * t1.x + t2.x;
        this.y = s * t1.y + t2.y;
    }

    /**
     * Sets the value of this tuple to the scalar multiplication
     * of itself and then adds tuple t1 (this = s*this + t1).
     *
     * @param s  the scalar value
     * @param t1 the tuple to be added
     */
    public final void scaleAdd(int s, v2i t1) {
        this.x = s * this.x + t1.x;
        this.y = s * this.y + t1.y;
    }

    /**
     * Returns a string that contains the values of this Tuple2i.
     * The form is (x,y).
     *
     * @return the String representation
     */
    public String toString() {
        return "(" + this.x + ", " + this.y + ')';
    }

    /**
     * Returns true if the Object t1 is of type Tuple2i and all of the
     * data members of t1 are equal to the corresponding data members in
     * this Tuple2i.
     *
     * @param t1 the object with which the comparison is made
     */
    public boolean equals(Object t1) {
        try {
            v2i t2 = (v2i) t1;
            return (this.x == t2.x && this.y == t2.y);
        } catch (NullPointerException | ClassCastException e2) {
            return false;
        }
    }

    /**
     * Returns a hash code value based on the data values in this
     * object.  Two different Tuple2i objects with identical data values
     * (i.e., Tuple2i.equals returns true) will return the same hash
     * code value.  Two objects with different data members may return the
     * same hash value, although this is not likely.
     *
     * @return the integer hash code value
     */
    public int hashCode() {
        long bits = 1L;
        bits = 31L * bits + x;
        bits = 31L * bits + y;
        return (int) (bits ^ (bits >> 32));
    }

    /**
     * Clamps the tuple parameter to the range [low, high] and
     * places the values into this tuple.
     *
     * @param min the lowest value in the tuple after clamping
     * @param max the highest value in the tuple after clamping
     * @param t   the source tuple, which will not be modified
     */
    public final void clamp(int min, int max, v2i t) {
        x = t.x > max ? max : Math.max(t.x, min);

        y = t.y > max ? max : Math.max(t.y, min);
    }

    /**
     * Clamps the minimum value of the tuple parameter to the min
     * parameter and places the values into this tuple.
     *
     * @param min the lowest value in the tuple after clamping
     * @param t   the source tuple, which will not be modified
     */
    public final void clampMin(int min, v2i t) {
        x = Math.max(t.x, min);

        y = Math.max(t.y, min);
    }

    /**
     * Clamps the maximum value of the tuple parameter to the max
     * parameter and places the values into this tuple.
     *
     * @param max the highest value in the tuple after clamping
     * @param t   the source tuple, which will not be modified
     */
    public final void clampMax(int max, v2i t) {
        x = Math.min(t.x, max);

        y = Math.min(t.y, max);
    }

    /**
     * Sets each component of the tuple parameter to its absolute
     * value and places the modified values into this tuple.
     *
     * @param t the source tuple, which will not be modified
     */
    public final void absolute(v2i t) {
        x = Math.abs(t.x);
        y = Math.abs(t.y);
    }

    /**
     * Clamps this tuple to the range [low, high].
     *
     * @param min the lowest value in this tuple after clamping
     * @param max the highest value in this tuple after clamping
     */
    public final void clamp(int min, int max) {
        if (x > max) {
            x = max;
        } else if (x < min) {
            x = min;
        }

        if (y > max) {
            y = max;
        } else if (y < min) {
            y = min;
        }
    }

    /**
     * Clamps the minimum value of this tuple to the min parameter.
     *
     * @param min the lowest value in this tuple after clamping
     */
    public final void clampMin(int min) {
        if (x < min)
            x = min;

        if (y < min)
            y = min;
    }

    /**
     * Clamps the maximum value of this tuple to the max parameter.
     *
     * @param max the highest value in the tuple after clamping
     */
    public final void clampMax(int max) {
        if (x > max)
            x = max;

        if (y > max)
            y = max;
    }

    /**
     * Sets each component of this tuple to its absolute value.
     */
    public final void absolute() {
        x = Math.abs(x);
        y = Math.abs(y);
    }

    /**
     * Creates a new object of the same class as this object.
     *
     * @return a clone of this instance.
     * @throws OutOfMemoryError if there is not enough memory.
     * @see Cloneable
     */
    @Override
    public Object clone() {

        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {

            throw new InternalError();
        }
    }

    /**
     * Get the <i>x</i> coordinate.
     *
     * @return the x coordinate.
     * @since vecmath 1.5
     */
    public final int getX() {
        return x;
    }

    /**
     * Set the <i>x</i> coordinate.
     *
     * @param x value to <i>x</i> coordinate.
     * @since vecmath 1.5
     */
    public final void setX(int x) {
        this.x = x;
    }

    /**
     * Get the <i>y</i> coordinate.
     *
     * @return the <i>y</i> coordinate.
     * @since vecmath 1.5
     */
    public final int getY() {
        return y;
    }

    /**
     * Set the <i>y</i> coordinate.
     *
     * @param y value to <i>y</i> coordinate.
     * @since vecmath 1.5
     */
    public final void setY(int y) {
        this.y = y;
    }
}