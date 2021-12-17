/*
 * $RCSfile$
 *
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import jcog.Util;
import jcog.signal.Tensor;

import java.io.Serializable;

//import jcog.tree.rtree.Spatialization;

/**
 * A generic 2-element tuple that is represented by single-precision
 * floating point x,y coordinates.
 */
public class v2 implements Serializable, Cloneable, Tensor {


    /**
     * The x coordinate.
     */
    public float x;

    /**
     * The y coordinate.
     */
    public float y;


    /**
     * Constructs and initializes a Tuple2f from the specified xy coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public v2(float x, float y) {
        this.x = x;
        this.y = y;
    }


//    /**
//     * Constructs and initializes a Tuple2f from the specified array.
//     *
//     * @param t the array of length 2 containing xy in order
//     */
//    public v2(float[] t) {
//        this.x = t[0];
//        this.y = t[1];
//    }


    public static v2 abs(v2 a) {
        return new v2(Math.abs(a.x), Math.abs(a.y));
    }

    public static void absToOut(v2 a, v2 out) {
        out.x = Math.abs(a.x);
        out.y = Math.abs(a.y);
    }

    public static float dot(v2 a, v2 b) {
        return a.x * b.x + a.y * b.y;
    }

    public static void negateToOut(v2 a, v2 out) {
        out.x = -a.x;
        out.y = -a.y;
    }

    public static void minToOut(v2 a, v2 b, v2 out) {
        out.x = Util.min(a.x, b.x);
        out.y = Util.min(a.y, b.y);
    }

    public static void maxToOut(v2 a, v2 b, v2 out) {
        out.x = Util.max(a.x, b.x);
        out.y = Util.max(a.y, b.y);
    }

    /**
     * Returns the angle in radians between this vector and the vector
     * parameter; the return value is constrained to the range [0,PI].
     *
     * @param v1 the other vector
     * @return the angle in radians in the range [0,PI]
     */
    public final float angle(v2 v1) {
        double vDot = this.dot(v1) / (this.lengthSquared());
        if (vDot < -1.0) vDot = -1.0;
        if (vDot > 1.0) vDot = 1.0;
        return ((float) (Math.acos(vDot)));
    }

    /**
     * Computes the dot product of the this vector and vector v1.
     *
     * @param v1 the other vector
     */
    private float dot(v2 v1) {
        return (this.x * v1.x + this.y * v1.y);
    }

    public boolean inUnit() {
        return x >= 0 && x <= 1f && y >= 0 && y <= 1f;
    }

    public boolean inUnit(float scale) {
        float xx = x * scale, yy = y * scale;
        return (xx >= 0 && xx <= 1f && yy >= 0 && yy <= 1f);
    }

//    public float minDimension() {
//        return Util.min(x, y);
//    }
//
//    public int xInt() {
//        return Math.round(x);
//    }
//
//    public int yInt() {
//        return Math.round(y);
//    }
//
//    public boolean equalsZero() {
//        return Util.equals(x, 0, ScalarValue.EPSILON) && Util.equals(y, 0, ScalarValue.EPSILON);
//    }

    public boolean isNaN() {
        return (x != x) || (y != y);
    }

    /**
     * Returns the length of this vector.
     *
     * @return the length of this vector
     */
    public final float length() {
        return (float) Math.sqrt(lengthSquaredDouble());
    }

    /**
     * Returns the squared length of this vector.
     *
     * @return the squared length of this vector
     */
    public final float lengthSquared() {
        return (float) lengthSquaredDouble();
    }

    public final double lengthSquaredDouble() {
        return Util.sqr((double) this.x) + Util.sqr((double) this.y);
    }


    /**
     * Constructs and initializes a Tuple2f from the specified Tuple2f.
     *
     * @param t1 the Tuple2f containing the initialization x y data
     */
    public v2(v2 t1) {
        this(t1.x, t1.y);
    }


//    /**
//     * Constructs and initializes a Tuple2f from the specified Tuple2d.
//     *
//     * @param t1 the Tuple2d containing the initialization x y data
//     */
//    v2(Tuple2d t1) {
//        this.x = (float) t1.x;
//        this.y = (float) t1.y;
//    }


    /**
     * Constructs and initializes a Tuple2f to (0,0).
     */
    public v2() {

    }


    /**
     * Sets the value of this tuple to the specified xy coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public v2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public v2 set(double x, double y) {
        return set((float)x, (float)y);
    }

    /**
     * Subtract another vector from this one and return result - alters this vector.
     */
    public final v2 subLocal(v2 v) {
        float x1 = -v.x;
        float y1 = -v.y;
        return added(x1, y1);
    }

    /**
     * Sets the value of this tuple from the 2 values specified in
     * the array.
     *
     * @param t the array of length 2 containing xy in order
     */
    public final void set(float[] t) {
        this.x = t[0];
        this.y = t[1];
    }


    /**
     * Sets the value of this tuple to the value of the Tuple2f argument.
     *
     * @param t the tuple to be copied
     */
    public final v2 set(v2 t) {
        this.x = t.x;
        this.y = t.y;
        return this;
    }


//    /**
//     * Sets the value of this tuple to the value of the Tuple2d argument.
//     *
//     * @param t1 the tuple to be copied
//     */
//    public final void setAt(Tuple2d t1) {
//        this.x = (float) t1.x;
//        this.y = (float) t1.y;
//    }


    /**
     * Copies the value of the elements of this tuple into the array t.
     *
     * @param t the array that will contain the values of the vector
     */
    public final void get(float[] t) {
        t[0] = this.x;
        t[1] = this.y;
    }


    /**
     * Sets the value of this tuple to the vector sum of itself and tuple t1.
     *
     * @param t1 the other tuple
     */
    public final v2 added(v2 t1) {
        return added(t1.x, t1.y);
    }

    public final v2 added(float tx, float ty) {
        this.x += tx;
        this.y += ty;
        return this;
    }



    /**
     * Sets the value of this tuple to the vector difference of
     * itself and tuple t1 (this = this - t1).
     *
     * @param t1 the other tuple
     */
    public final v2 subbed(v2 t1) {
        this.x -= t1.x;
        this.y -= t1.y;
        return this;
    }

    public final void sub(float dx, float dy) {
        this.x -= dx;
        this.y -= dy;
    }

    public static float cross(v2 a, v2 b) {
        double ax = a.x;
        double ay = a.y;
        return (float) (ax * b.y - ay * b.x);
    }

    public static v2 cross(v2 a, float s) {
        float y1 = -s * a.x;
        return new v2(s * a.y, y1);
    }

    /**
     * True if the vector represents a pair of valid, non-infinite floating point numbers.
     */
    public final boolean isValid() {
        return !Float.isNaN(x) && !Float.isInfinite(x) && !Float.isNaN(y) && !Float.isInfinite(y);
    }

    public static v2 min(v2 a, v2 b) {
        return new v2(Util.min(a.x, b.x), Util.min(a.y, b.y));
    }

    public static v2 max(v2 a, v2 b) {
        return new v2(Util.max(a.x, b.x), Util.max(a.y, b.y));
    }


    /**
     * Sets the value of this tuple to the negation of tuple t1.
     *
     * @param v the source tuple
     */
    public final v2 negated(v2 v) {
        this.x = -v.x;
        this.y = -v.y;
        return this;
    }


    /**
     * Negates the value of this vector in place.
     */
    public final v2 negated() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }


//    /**
//     * Sets the value of this tuple to the scalar multiplication
//     * of tuple t1.
//     *
//     * @param s  the scalar value
//     * @param t1 the source tuple
//     */
//    public final void scaled(float s, v2 t1) {
//        this.x = s * t1.x;
//        this.y = s * t1.y;
//    }


    public static void crossToOut(v2 a, float s, v2 out) {
        float tempy = -s * a.x;
        out.x = s * a.y;
        out.y = tempy;
    }

    public static void crossToOutUnsafe(v2 a, float s, v2 out) {
        assert (out != a);
        out.x =  s * a.y;
        out.y = -s * a.x;
    }

    public static v2 cross(float s, v2 a) {
        float x1 = -s * a.y;
        return new v2(x1, s * a.x);
    }

    public static void crossToOut(float s, v2 a, v2 out) {
        float tempY = s * a.x;
        out.x = -s * a.y;
        out.y = tempY;
    }

    public static void crossToOutUnsafe(float s, v2 a, v2 out) {
        assert (out != a);
        out.x = -s * a.y;
        out.y =  s * a.x;
    }

    /**
     * Sets the value of this tuple to the scalar multiplication
     * of itself.
     *
     * @param s the scalar value
     */
    public v2 scaled(float s) {
        return scaled(s, s);
    }
//
//    /**
//     * multiplies each component
//     */
//    public final v2 scale(v2 z) {
//        this.x *= z.x;
//        this.y *= z.y;
//        return this;
//    }

    public final v2 scaled(float sx, float sy) {
        this.x *= sx;
        this.y *= sy;
        return this;
    }

    public final v2 scaleInto(float sx, float sy, v2 target) {
        target.set(x * sx, y * sy);
        return target;
    }

//    /**
//     * Sets the value of this tuple to the scalar multiplication
//     * of tuple t1 and then adds tuple t2 (this = s*t1 + t2).
//     *
//     * @param s  the scalar value
//     * @param t1 the tuple to be multipled
//     * @param t2 the tuple to be added
//     */
//    public final void scaledAdded(float s, v2 t1, v2 t2) {
//        this.x = s * t1.x + t2.x;
//        this.y = s * t1.y + t2.y;
//    }


//    /**
//     * Sets the value of this tuple to the scalar multiplication
//     * of itself and then adds tuple t1 (this = s*this + t1).
//     *
//     * @param s  the scalar value
//     * @param t1 the tuple to be added
//     */
//    public final void scaledAdded(float s, v2 t1) {
//        this.x = s * this.x + t1.x;
//        this.y = s * this.y + t1.y;
//    }


    /**
     * Returns a hash code value based on the data values in this
     * object.  Two different Tuple2f objects with identical data values
     * (i.e., Tuple2f.equals returns true) will return the same hash
     * code value.  Two objects with different data members may return the
     * same hash value, although this is not likely.
     *
     * @return the integer hash code value
     */
    public int hashCode() {
        long bits = 1L;
        bits = 31L * bits + VecMathUtil.floatToIntBits(x);
        bits = 31L * bits + VecMathUtil.floatToIntBits(y);
        return (int) (bits ^ (bits >> 32));
    }


    /**
     * Returns true if all of the data members of Tuple2f t1 are
     * equal to the corresponding data members in this Tuple2f.
     *
     * @param t the vector with which the comparison is made
     * @return true or false
     */
    public boolean equals(v2 t, float epsilon) {
        return (this == t) ||
            (
                Util.equals(this.x, t.x, epsilon) && Util.equals(this.y, t.y, epsilon)
            );
    }

    public boolean equals(v2 t1) {
        return equals(t1, Float.MIN_NORMAL);
    }

    /**
     * Returns true if the Object t1 is of type Tuple2f and all of the
     * data members of t1 are equal to the corresponding data members in
     * this Tuple2f.
     *
     * @param x the object with which the comparison is made
     * @return true or false
     */
    public boolean equals(Object x) {
        if (x == this)
            return true;

        if (!(x instanceof v2 other)) return false;
        if (Float.floatToIntBits(this.x) != Float.floatToIntBits(other.x)) return false;
        return Float.floatToIntBits(y) == Float.floatToIntBits(other.y);
    }

//    /**
//     * Returns true if the L-infinite distance between this tuple
//     * and tuple t1 is less than or equal to the epsilon parameter,
//     * otherwise returns false.  The L-infinite
//     * distance is equal to MAX[abs(x1-x2), abs(y1-y2)].
//     *
//     * @param t1      the tuple to be compared to this tuple
//     * @param epsilon the threshold value
//     * @return true or false
//     */
//    public boolean epsilonEquals(v2 t1, float epsilon) {
//
//        float diff = x - t1.x;
//        if (Float.isNaN(diff)) return false;
//        if ((diff < 0 ? -diff : diff) > epsilon) return false;
//
//        diff = y - t1.y;
//        if (Float.isNaN(diff)) return false;
//        return (diff < 0 ? -diff : diff) <= epsilon;
//
//    }

    /**
     * Returns a string that contains the values of this Tuple2f.
     * The form is (x,y).
     *
     * @return the String representation
     */
    public String toString() {
        return ("(" + this.x + ", " + this.y + ')');
    }


    /**
     * Clamps the tuple parameter to the range [low, high] and
     * places the values into this tuple.
     *
     * @param min the lowest value in the tuple after clamping
     * @param max the highest value in the tuple after clamping
     * @param t   the source tuple, which will not be modified
     */
    public final void clamp(float min, float max, v2 t) {
        x = Util.clamp(t.x, min, max);
        y = Util.clamp(t.y, min, max);
    }

    public void clamp(float xMin, float yMin, float xMax, float yMax) {
        set(Util.clamp(x, xMin, xMax), Util.clamp(y, yMin, yMax));
    }

    /**
     * Sets each component of the tuple parameter to its absolute
     * value and places the modified values into this tuple.
     *
     * @param t the source tuple, which will not be modified
     */
    public final void absolute(v2 t) {
        set(Math.abs(t.x),Math.abs(t.y));
    }


    /**
     * Clamps this tuple to the range [low, high].
     *
     * @param min the lowest value in this tuple after clamping
     * @param max the highest value in this tuple after clamping
     */
    public final void clamp(float min, float max) {
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
     * Sets each component of this tuple to its absolute value.
     */
    public final void absolute() {
        x = Math.abs(x);
        y = Math.abs(y);
    }



    public v2 clone() {
        return new v2(x, y);
    }

    /**
     * Normalizes this vector in place.
     */
    public final float normalize() {
        return (float) normalize(1f);
    }


    public final double normalize(float scale) {
        double magSqr = lengthSquaredDouble();
        if (magSqr >= Util.MIN_NORMALsqrt) {
            double mag = Math.sqrt(magSqr);
            double norm = scale / mag;
            set(norm * x, norm * y);
            return mag;
        } else {
            this.x = this.y = 0;
            return 0;
        }
    }



    public float distanceSq(v2 v) {
        return (float) distanceSqDouble(v);
    }

    public final double distanceSqDouble(v2 v) {
        return this==v ? 0 : Util.sqr((double) x - v.x) + Util.sqr((double) y - v.y);
    }

    public final void setZero() {
        set(0, 0);
    }

    public v2 addToNew(v2 u) {
        return new v2(x + u.x, y + u.y);
    }

    public v2 subClone(v2 u) {
        return new v2(x - u.x, y - u.y);
    }

    public v2 scaleClone(float s) {
        return new v2(x * s, y * s);
    }

    public v2 scaleClone(float sx, float sy) {
        return new v2(x * sx, y * sy);
    }

    public boolean setIfChanged(float x, float y, float epsilon) {
        if (!Util.equals(this.x, x, epsilon) || !Util.equals(this.y, y, epsilon)) {
            set(x, y);
            return true;
        }
        return false;
    }

    public float distance(v2 x) {
        return (float) Math.sqrt(distanceSqDouble(x));
    }

    /**
     * move linear
     */
    public void move(v2 other, float rate) {
        setAnim(other, rate, false);
    }

    /**
     * move lerp (exponential)
     */
    public void lerp(v2 other, float rate) {
        setAnim(other, rate, true);
    }

    /**
     * returns true if changed
     */
    private boolean setAnim(v2 other, float rate, boolean lerpOrLinear) {
        if (rate < Float.MIN_NORMAL || this == other)
            return false;

        float nx = other.x, ny = other.y;
        float px = this.x, py = this.y;
        float dx = nx - px;
        if (dx < Float.MIN_NORMAL) {
            px = this.x = nx;
            dx = 0;
        }
        float dy = ny - py;
        if (dy < Float.MIN_NORMAL) {
            py = this.y = ny;
            dy = 0;
        }

        if (dx == 0 && dy == 0)
            return false;

        if (lerpOrLinear) {
            set(Util.lerp(rate, px, nx), Util.lerp(rate, py, ny));
        } else {

            float lenSq = dx * dx + dy * dy;

            if (lenSq < rate * rate) {
                set(nx, ny); //finished
            } else {
                added(new v2(dx, dy).scaled((float) (rate / Math.sqrt(lenSq))));
            }

        }

        return true;

    }

    @Override
    public final float getAt(int linearCell) {
        switch(linearCell) {
            case 0: return x;
            case 1: return y;
        }
        throw new ArrayIndexOutOfBoundsException(linearCell);
    }

    @Override
    public final int[] shape() {
        return v2Shape;
    }

    public void setSub(v2 a, v2 b) {
        x = a.x - b.x;
        y = a.y - b.y;
    }
}