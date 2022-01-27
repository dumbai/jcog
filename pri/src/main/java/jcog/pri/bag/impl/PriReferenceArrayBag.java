package jcog.pri.bag.impl;

import jcog.pri.PriReference;
import jcog.pri.op.PriMerge;

public class PriReferenceArrayBag<X,Y extends PriReference<X>> extends ArrayBag<X, Y> {

    public PriReferenceArrayBag(PriMerge mergeFunction) {
        this(mergeFunction, 0);
    }

    @Deprecated public PriReferenceArrayBag(PriMerge mergeFunction, int capacity) {
        super(mergeFunction);
        setCapacity(capacity);
    }

    protected PriReferenceArrayBag(PriMerge merge, ArrayBag.ArrayBagModel<X, Y> model) {
        super(merge, model);
    }


    @Override
    public final X key(Y v) {
        return v.get();
    }

//    public static class FloatArrayBag<X> extends ArrayBag<X, PriReference<X>> {
//
//        protected FloatArrayBag(PriMerge merge, int cap) {
//            super(merge, cap);
//        }
//
//        @Override
//        public X key(PriReference<X> v) {
//            return v.get();
//        }
//
//        public static class FloatArrayBagMapModel<X,Y> extends AbstractBagMapModel<X,Y> {
//
//            float[] f = null;
//
//            @Nullable
//            @Override
//            public Y get(short index) {
//                return new PLink<>(get()f[index];
//            }
//
//            @Override
//            protected Y remove(short x) {
//                return null;
//            }
//
//            @Override
//            protected void alloc(int cap) {
//
//            }
//
//            @Override
//            protected void set(int xy, Y y) {
//
//            }
//        }
}