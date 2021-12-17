package jcog.pri.bag.impl;

import jcog.pri.PriMap;
import jcog.pri.Prioritizable;
import jcog.pri.Prioritized;
import jcog.pri.bag.Bag;
import jcog.pri.bag.util.ProxyBag;
import jcog.pri.op.PriMerge;
import jcog.signal.NumberX;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * concurrent buffering bag wrapper
 */
public abstract class BufferedBag<X, B, Y extends Prioritized & Prioritizable> extends ProxyBag<X, Y> {

    /**
     * pre-bag accumulating buffer
     */
    public final PriMap<B> pre;
    private final AtomicBoolean busy = new AtomicBoolean(false);

    public BufferedBag(Bag<X, Y> bag, PriMap<B> pre) {
        super(bag);
        super.merge(bag.merge()); //by default.  changing this later will set pre and bag's merges
        this.pre = pre;
    }


    @Override
    public void clear() {
        pre.clear();
        super.clear();
    }


    @Override
    public final void commit(@Nullable Consumer<? super Y> update) {

        if (busy.compareAndSet(false, true)) {
            try {

                bag.commit(update);

                pre.drain(bag::put, this::valueInternal);

            } finally {
                busy.set(false);
            }
        }

    }

    protected abstract Y valueInternal(B b);

//    @Override
//    public int size() {
//        return Math.max(bag.size(), pre.size());
//    }

    @Override
    public final Y put(Y y, @Nullable NumberX overflowingIgnored) {
        return (Y) pre.put((B) y,
                merge(),
                this::pressurize
        );
    }


    public Bag<X,Y> merge(PriMerge nextMerge) {
        super.merge(nextMerge);
        bag.merge(nextMerge);
        return this;
    }


}