package jcog.pri.bag.impl;

import jcog.pri.PriReference;
import jcog.pri.op.PriMerge;

@Deprecated public class PLinkArrayBag<X> extends PriReferenceArrayBag<X,PriReference<X>> {

    public PLinkArrayBag(PriMerge mergeFunction) {
        super(mergeFunction);
    }

    public PLinkArrayBag(PriMerge mergeFunction, ArrayBag.ArrayBagModel<X, PriReference<X>> model) {
        super(mergeFunction, model);
    }

    public PLinkArrayBag(PriMerge mergeFunction, int cap) {
        this(mergeFunction);
        setCapacity(cap);
    }

}