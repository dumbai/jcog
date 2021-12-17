package jcog.pri.bag.impl.hijack;

import jcog.pri.Prioritizable;
import jcog.pri.bag.impl.HijackBag;
import jcog.pri.op.PriMerge;
import jcog.signal.NumberX;
import jcog.util.PriReturn;

/**
 * Created by me on 2/17/17.
 */
public abstract class PriHijackBag<K,V extends Prioritizable> extends HijackBag<K, V> {


    protected PriHijackBag(PriMerge merge, int reprobes) {
        this(merge, 0, reprobes);
    }

    protected PriHijackBag(PriMerge merge, int cap, int reprobes) {
        super(cap, reprobes);
        merge(merge);
    }

    @Override
    protected V merge(V existing, V incoming, NumberX overflowing) {
        float overflow = merge().apply(existing, incoming.pri(), PriReturn.Overflow);
        if (overflow > Float.MIN_NORMAL && overflowing != null)
            overflowing.add(overflow);
        return existing; 
    }

    @Override
    public final float pri(V key) {
        return key.pri();
    }


}