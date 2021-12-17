package jcog.pri.bag.impl.hijack;

import jcog.pri.PLink;
import jcog.pri.op.PriMerge;


public class PLinkHijackBag<K> extends PriHijackBag<K, PLink<K>> {

    public PLinkHijackBag(PriMerge merge, int capacity, int reprobes) {
        super(PriMerge.plus, capacity, reprobes);
        merge(merge);
    }

    @Override
    public final K key(PLink<K> value) {
        return value.id;
    }

}