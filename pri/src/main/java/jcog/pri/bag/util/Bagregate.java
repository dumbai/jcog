package jcog.pri.bag.util;

import com.google.common.collect.Iterables;
import jcog.pri.PLink;
import jcog.pri.PriReference;
import jcog.pri.Prioritized;
import jcog.pri.bag.Bag;
import jcog.pri.bag.impl.PriReferenceArrayBag;
import jcog.pri.op.PriMerge;
import jcog.signal.FloatRange;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * a bag which wraps another bag, accepts its value as input but at a throttled rate
 * resulting in containing effectively the integrated / moving average values of the input bag
 * TODO make a PLink version of ArrayBag since quality is not used here
 */
public class Bagregate<X> implements Iterable<PriReference<X>> {

    public final Bag<?, PriReference<X>> bag;

    public final FloatRange forgetRate = new FloatRange(1f, 0, 1);

    public Bagregate(int capacity, PriMerge merge) {
        this.bag = new PriReferenceArrayBag<>(merge, capacity) {
            @Override
            public void onRemove(PriReference<X> value) {
                Bagregate.this.onRemove(value);
            }
        };
    }

    public void put(Prioritized x) {
        put((X)x, x.priElseZero());
    }
    public void put(PriReference<X> x) {
        bag.put(x);
    }
    public void put(X x, float pri) {
        bag.put(new PLink<>(x, pri));
    }

    /** TODO clarify semantics */
    @Deprecated protected void onRemove(PriReference<X> value) {

    }

    public boolean commit() {
        bag.commit(bag.forget(this.forgetRate.floatValue()));

        return true;
    }


    @Override
    public final void forEach(Consumer<? super PriReference<X>> action) {
        bag.forEach(action);
    }

    public void clear() {
        bag.clear();
    }

    @Override
    public final Iterator<PriReference<X>> iterator() {
        return bag.iterator();
    }

    /** compose */
    public Iterable<X> iterable() {
        return Iterables.transform(/*Iterables.filter(*/bag/*, Objects::nonNull)*/, Supplier::get);
    }

//    /** compose */
//    public <Y> Iterable<Y> iterable(Function<X, Y> f) {
//        return StreamSupport.stream(/*Iterables.filter(*/bag/*, Objects::nonNull).spliterator(), false).map((b) -> f.apply(b.get())).collect(Collectors.toList());
//    }

    public final void capacity(int c) {
        bag.setCapacity(c);
    }
    public final int capacity() {
        return bag.capacity();
    }
}