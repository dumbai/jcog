package jcog.pri;

import jcog.Util;

import java.lang.ref.SoftReference;

import static jcog.Str.n4;

/** prioritized proxy pair, can be used to represent cached memoizable operation with input X and output Y */
public interface PriProxy<X, Y> extends Prioritizable {
    /**
     * 'x', the parameter to the function
     */
    X x();
    Y y();


    /** equaity/hash on X, supplies Y */
    final class StrongProxy<X, Y> extends HashedPLink<X> implements PriProxy<X, Y> {

        private final Y y;

        public StrongProxy(X x, Y y, float pri) {
            super(x, pri);
            this.y = y;
        }

        @Override
        public X x() {
            return id;
        }

        @Override
        public Y y() {
            return y;
        }
    }

    /** TODO needs tested for correct behavior on reclamation.  equailty/hash on X, supplies Y
     *  TODO needs ScalarValue.AtomicScalarValue support.  this doesnt have it by extending SoftReference already cant extend Pri like the Strong impl
     * */
    final class SoftProxy<X, Y> extends SoftReference<Y> implements PriProxy<X, Y> {

        final X x;
        private final int hash;

        /** TODO atomic */
        private volatile float pri;

        public SoftProxy(X x, Y y, float pri) {
            super(y);
            this.x = x;
            this.hash = x.hashCode();
            this.pri = pri;
        }

        @Override
        public final X x() {
            return x;
        }

        @Override
        public final Y y() {
            Y y = super.get();
            if (y == null) {
                this.pri = Float.NaN;
                return null;
            } else
                return y;
        }

        @Override
        public boolean equals(Object obj) {
            return x.equals(obj);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public void pri(float p) {
            if (p == p)
                p = Util.unitize(p);
            this.pri = p;
        }

        @Override
        public String toString() {
            return '$' + n4(pri) + ' ' + get();
        }

        @Override
        public boolean delete() {
            float p = pri;
            if (p == p) {
                this.pri = Float.NaN;
                clear();
                return true;
            }
            return false;
        }



        @Override
        public final float pri() {
            return pri;
        }

    }
}