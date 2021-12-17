package jcog.pri.bag.impl.hijack;

import jcog.pri.Prioritizable;
import jcog.pri.op.PriMerge;

public class PriPriHijackBag<K extends Prioritizable> extends PriHijackBag<K, K> {

    public PriPriHijackBag(PriMerge merge, int reprobes) {
        super(merge, reprobes);
    }

    public PriPriHijackBag(PriMerge merge, int cap, int reprobes) {
        super(merge, cap, reprobes);
    }

    @Override
    public final K key(K value) {
        return value;
    }
}