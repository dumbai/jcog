package jcog.pri.bag.impl;

import jcog.pri.Prioritizable;
import jcog.pri.op.PriMerge;
import org.jetbrains.annotations.Nullable;

public class PriArrayBag<X extends Prioritizable> extends ArrayBag<X,X> {

    public PriArrayBag(PriMerge merge, int capacity) {
        this(merge, new ListArrayBagModel<>());
        setCapacity(capacity);
    }
    public PriArrayBag(PriMerge merge) {
        this(merge, new ListArrayBagModel<>());
    }

    public PriArrayBag(PriMerge merge, ArrayBagModel<X, X> m) {
        super(merge, m);
    }

//    @Override
//    protected void removed(X x) {
//        //dont affect the result
//    }

    @Override
    public @Nullable X key(X k) {
        return k;
    }


}