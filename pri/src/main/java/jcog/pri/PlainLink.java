package jcog.pri;

import jcog.Util;
import jcog.signal.PlainMutableFloat;

import static jcog.Str.n4;

/**
 * copy of NLink with non-atomic mutable float for single-thread use
 */
public class PlainLink<X> extends PlainMutableFloat implements PriReference<X> {

    public final X id;

    public PlainLink(X x, float v) {
        this.id = x;
        set(v);
    }

    @Override
    protected float post(float x) {
        //return Math.min(x, 1);
        return Util.unitizeSafe(x);
    }

    @Override
    public final X get() {
        return id;
    }

    @Override
    public boolean equals(Object that) {
        return PriReference.equals(this, that);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return n4(pri()) + ' ' + id;
    }

    @Override
    public void pri(float p) {
        set(p);
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public float pri() {
        return this.v;
    }
}