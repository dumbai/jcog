package jcog.pri;

import jcog.Util;
import jcog.pri.op.PriMerge;
import jcog.util.PriReturn;

/**
 * general purpose value.  consumes and supplies 32-bit float numbers
 * supports certain numeric operations on it
 * various storage implementations are possible
 * as well as the operation implementations.
 *
 * see: NumericUtils.java (lucene)
 * */
public interface Prioritizable extends Prioritized, Deleteable {

    static boolean equals(float x, float y) {
        return Util.equals(x, y, EPSILON);
    }

    /** deleted if pri()==NaN */
    @Override default boolean isDeleted() {
        float p = pri();
        return p!=p;
    }

    /** setter   */
    void pri(float p);

    /** doesnt return any value so implementations may be slightly faster than priAdd(x) */
    default void priAdd(float a) {
        throw new UnsupportedOperationException();
    }

    default void priMul(float x) {
        if (x!=1)
            pri(pri()*x); //warning: not atomic
        //throw new UnsupportedOperationException();
    }

    default <P extends Prioritizable> P withPri(Prioritizable t) {
        return withPri(t.priElseZero());
    }

    default <P extends Prioritizable> P withPri(float p) {
        pri(p);
        return (P) this;
    }

    /** set priority at-least this value */
    default void priMax(float p) {
        PriMerge.max.apply(this, p, PriReturn.Post);
    }

    default void priMin(float p) {
        PriMerge.min.apply(this, p, PriReturn.Post);
    }

}