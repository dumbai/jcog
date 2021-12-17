package jcog.pri;

import static jcog.Str.n4;

/**
 * immutable object + mutable number pair;
 * considered in a 'deleted' state when the value is NaN
 */
public class NLink<X> extends AtomicPri implements PriReference<X> {

    public final X id;

    public NLink(X x, float v) {
        super(v);
        this.id = x;
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

}