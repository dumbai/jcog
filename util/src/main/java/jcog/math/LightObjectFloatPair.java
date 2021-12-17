package jcog.math;

import org.eclipse.collections.api.tuple.primitive.ObjectFloatPair;

public class LightObjectFloatPair<X> implements ObjectFloatPair<X> {

    protected float val;
    private X the;

    public LightObjectFloatPair(X the, float val) {
        this.val = val;
        this.the = the;
    }

    public LightObjectFloatPair() {

    }

    @Override
    public X getOne() {
        return the;
    }

    @Override
    public float getTwo() {
        return val;
    }

    public void set(X x, float v) {
        this.the = x;
        this.val = v;
    }

    public void set(float v) {
        this.val = v;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(ObjectFloatPair<X> zObjectFloatPair) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return the + "=" + val;
    }
}
