package jcog.tree.rtree;

/*
 * #%L
 * Conversant RTree
 * ~~
 * Conversantmedia.com © 2016, Conversant, Inc. Conversant® is a trademark of Conversant, Inc.
 * ~~
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import jcog.tree.rtree.node.RNode;
import jcog.tree.rtree.util.Stats;
import jcog.util.LambdaStampedLock;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * Created by jcovert on 12/30/15.
 */
public class ConcurrentRTree<X> extends LambdaStampedLock implements Space<X> {

    protected final RTree<X> tree;

    public ConcurrentRTree(RTree<X> tree) {
        super();
        this.tree = tree;
    }

    @Override
    public final boolean contains(X x) {
        return testWith(RTree::contains, x);
    }

    @Override
    public boolean OR(Predicate<X> o) {
        return testWith((tr,oo)->tr.root().OR(oo), o);
    }

    @Override
    public boolean AND(Predicate<X> o) {
        return testWith((tr,oo)->tr.root().AND(oo), o);
    }

//    /**
//     * Blocking locked search
//     *
//     * @param rect - HyperRect to search
//     * @param t    - array to hold results
//     * @return number of entries found
//     */
//    @Deprecated  @Override
//    public int containedToArray(HyperRegion rect, X[] t) {
//        return read(() -> tree.containedToArray(rect, t));
//    }

    /** TODO encapsulate. should not be exposed */
    @Deprecated @Override
    public final RNode<X> root() {
        return tree.root();
    }

    /**
     * Blocking locked addAt
     *
     * @param x - entry to addAt
     */
    @Override
    public void insert(RInsertion x) {
        x.lock = readLock();
        x._lock = this;
        try {
            tree.insert(x);
        } finally {
            unlock(x.lock);
        }
    }


    /**
     * prefer this instead of add() in multithread environments, because it elides what might ordinarily involve a lock wait
     */
    @Override
    public void addAsync(X t) {
        add(t);
    }

    @Override
    public void removeAsync(X t) {
        remove(t);
    }


    /** TODO read -> write lock */
    @Override public boolean remove(X x) {
        long l = writeLock();
        try {
            return tree.remove(x);
        } finally {
            unlockWrite(l);
        }
    }

    public void removeAll(Iterable<? extends X> t) {
        write(() -> {
            for (X x : t) {
                remove(x);
            }
        });
    }


    public final void read(Consumer<RTree<X>> x) {
        long l = readLock();
        try {
            x.accept(tree);
        } finally {
            unlockRead(l);
        }
    }


    /**
     * doesnt lock, use at your own risk
     */
    protected final void readDirect(Consumer<Space<X>> x) {
        x.accept(tree);
    }

    public final void write(Consumer<Space<X>> x) {
        long l = writeLock();
        try {
            x.accept(tree);
        } finally {
            unlockWrite(l);
        }
    }

    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public final boolean write(Predicate<Space<X>> x) {
        long l = writeLock();
        try {
            return x.test(tree);
        } finally {
            unlockWrite(l);
        }
    }

    @SuppressWarnings("LambdaUnfriendlyMethodOverload")
    public final <Y> Y write(Function<Space<X>,Y> x) {
        long l = writeLock();
        try {
            return x.apply(tree);
        } finally {
            unlockWrite(l);
        }
    }

    public final <A,Y> Y writeWith(A a, BiFunction<Space<X>,A,Y> x) {
        long l = writeLock();
        try {
            return x.apply(tree, a);
        } finally {
            unlockWrite(l);
        }
    }

    public final void readOptimistic(Consumer<Space<X>> x) {
        readOptimistic(() -> x.accept(tree));
    }

    @Override
    public final HyperRegion bounds(X task) {
        return tree.bounds(task);
    }


    /**
     * Blocking locked update
     *
     * @param told - entry to update
     * @param tnew - entry with new value
     * TODO read -> write lock
     */
    @Override public final boolean replace(X told, X tnew) {
        long l = writeLock();
        try {
            return tree.replace(told, tnew);
        } finally {
            unlockWrite(l);
        }
    }

    @Override
    public final int size() {
        return tree.size();
    }

    @Override
    public void clear() {
        write(tree::clear);
    }

    @Override
    public final void forEach(Consumer<? super X> consumer) {
        long l = readLock();
        try {
            tree.forEach(consumer);
        } finally {
            unlockRead(l);
        }
    }

    public final void forEachOptimistic(Consumer<? super X> consumer) {
        readOptimistic(() -> tree.forEach(consumer));
    }

    public <Y> void readWith(BiConsumer<RTree<X>,Y> readProcedure, Y x) {
        long stamp = readLock();
        try {
            readProcedure.accept(tree, x);
        } finally {
            unlockRead(stamp);
        }
    }


    public boolean test(Predicate<RTree<X>> p) {
        long stamp = readLock();
        try {
            return p.test(tree);
        } finally {
            unlockRead(stamp);
        }
    }

    public <Y> boolean testWith(BiPredicate<RTree<X>,Y> p, Y y) {
        long stamp = readLock();
        try {
            return p.test(tree, y);
        } finally {
            unlockRead(stamp);
        }
    }

    @Override
    public boolean containsWhile(HyperRegion rect, Predicate<X> t) {
        read(() -> tree.containsWhile(rect, t));
        return false;
    }

    @Override
    public boolean intersectsWhile(HyperRegion rect, Predicate<X> t) {
        //Predicates.compose(t,)
        read(() -> tree.intersectsWhile(rect, t));
        return false;
    }

    /**
     * warning: not locked
     */
    @Override
    public Stream<X> stream() {
        return root().streamValues().filter(Objects::nonNull);
    }

    /**
     * warning: not locked
     */
    public Stream<RNode<X>> streamNodes() {
        return root().streamNodesRecursively().filter(Objects::nonNull);
    }

    /**
     * warning: not locked
     */
    @Override
    public Iterator<X> iterator() {
        return stream().iterator();
    }

    @Override
    public Stats stats() {
        return read(tree::stats);
    }

    @Override
    public String toString() {
        return tree.toString();
    }

    @Override
    public boolean contains(X x, HyperRegion b, Spatialization model) {
        long l = readLock();
        try {
            return tree.contains(x, b, model);
        } finally {
            unlockRead(l);
        }
    }


    public final Spatialization <X, ?> model() {
        return tree.model;
    }
}
