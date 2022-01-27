package jcog.pri.bag.util;

import jcog.pri.bag.Bag;
import jcog.signal.NumberX;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

/**
 * proxies to a delegate bag

 * TODO find any inherited methods which would return the proxied
 * bag instead of this instance
 */
public class ProxyBag<X,Y> extends Bag<X,Y> {

    public Bag<X,Y> bag;

    public ProxyBag(Bag<X, Y> delegate) {
        set(delegate);
    }

    public final void set(Bag<X,Y> delegate) {
        bag = delegate;
    }

    @Override
    public float pri(Y key) {
        return bag.pri(key);
    }

    @Override
    public X key(Y value) {
        return bag.key(value);
    }


    @Override
    public @Nullable Y get(Object key) {
        return bag.get(key);
    }

    @Override
    public void forEach(Consumer<? super Y> action) {
        for (Y y : bag) {
            action.accept(y);
        }
    }

    @Override
    public void forEach(int max, Consumer<? super Y> action) {
        bag.forEach(max, action);
    }

    @Override
    public void forEachKey(Consumer<? super X> each) {
        bag.forEachKey(each);
    }

    @Override
    public Stream<Y> stream() {
        return bag.stream();
    }

    @Override
    public Iterator<Y> iterator() {
        return bag.iterator();
    }

    @Override
    public Spliterator<Y> spliterator() {
        return bag.spliterator();
    }

    @Override
    public void clear() {
        bag.clear();
    }

    @Override
    protected void _setCapacity(int before, int after) {
        bag.setCapacity(after);
    }

    @Override
    public @Nullable Y remove(X x) {
        return bag.remove(x);
    }

    @Override
    public Y put(Y b, NumberX overflowing) {
        return bag.put(b, overflowing);
    }

    @Override
    public @Nullable Y sample(RandomGenerator rng) {
        return bag.sample(rng);
    }

    @Override
    public final void sample(RandomGenerator rng, Function<? super Y, SampleReaction> each) {
        bag.sample(rng, each);
    }

    @Override
    public Iterator<Y> sampleUnique(RandomGenerator rng) {
        return bag.sampleUnique(rng);
    }

    @Override
    public int size() {
        return bag.size();
    }

    @Override
    public final int capacity() {
        return bag.capacity();
    }

    @Override
    public final float mass() {
        return bag.mass();
    }

    @Override
    public final float pressure() {
        return bag.pressure();
    }

    @Override
    public final void pressurize(float f) {
        bag.pressurize(f);
    }

    @Override
    public final float depressurizePct(float percentToRemove) {
        return bag.depressurizePct(percentToRemove);
    }

    @Override
    public final void depressurize(float toRemove) {
        bag.depressurize(toRemove);
    }


    @Override
    public float priMax() {
        return bag.priMax();
    }

    @Override
    public float priMin() {
        return bag.priMin();
    }

    @Override
    public void commit(@Nullable Consumer<? super Y> update) {
        bag.commit(update);
    }


    @Override
    public void onAdd(Y v) {
        bag.onAdd(v);
    }

    @Override
    public void onRemove(Y v) {
        bag.onRemove(v);
    }

    @Override
    public void onReject(Y v) {
        bag.onReject(v);
    }

}