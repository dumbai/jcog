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


import jcog.WTF;
import jcog.tree.rtree.node.REmpty;
import jcog.tree.rtree.node.RLeaf;
import jcog.tree.rtree.node.RNode;
import jcog.tree.rtree.split.Split;
import jcog.tree.rtree.util.CounterRNode;
import jcog.tree.rtree.util.Stats;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * <p>Data structure to make range searching more efficient. Indexes multi-dimensional information
 * such as geographical coordinates or rectangles. Groups information and represents them with a
 * minimum bounding rectangle (mbr). When searching through the tree, any query that does not
 * intersect an mbr can ignore any data entries in that mbr.</p>
 * <p>More information can be @see <a href="https:
 * <p>
 * Created by jcairns on 4/30/15.</p>
 */
public class RTree<X> implements Space<X> {

    //private static final MetalAtomicIntegerFieldUpdater<RTree> SIZE = new MetalAtomicIntegerFieldUpdater(RTree.class, "_size");

    private RNode<X> root;


    private int size;

    public final Spatialization<X, ?> model;

    public <R extends HyperRegion> RTree(@Nullable Function<X, R> spatialize, int mMax, Split<X> splitType) {
        this(new Spatialization<X,R>(splitType, mMax) {
            @Override public R bounds(X x) {
                return spatialize.apply(x);
            }
        });
    }

    @Override
    public final Spatialization<X,?> model() {
        return model;
    }

    public RTree(Spatialization<X, ?> model) {
        this.model = model;
        this.root = REmpty.the;
    }

    @Override
    public Stream<X> stream() {
        return root.streamValues();
    }

    @Override
    public final Iterator<X> iterator() {
        return root.iterateValues();
    }

    @Override
    public void clear() {
        if (size > 0) {
            size = 0;
            root = REmpty.the;
        }
//        SIZE.updateAndGet(this, (sizeBeforeClear) -> {
//            if (sizeBeforeClear > 0 || root == null)
//                this.root = model.newLeaf();
//            return 0;
//        });
    }

    @Override
    public boolean OR(Predicate<X> o) {
        return root().OR(o);
    }

    @Override
    public boolean AND(Predicate<X> o) {
        return root().AND(o);
    }


    public final void insert(/*@NotNull*/ RInsertion i) {
        RNode<X> nextRoot = root.add(i);

        if (i.added) size++; //SIZE.getAndIncrement(this);
        update(nextRoot, i.removals);

        Set<X> ri = i.reinserts;
        if (ri!=null) {
            i.reinserts = null;
            ri.forEach(this::insert);
        }
    }

    /**
     * @param xBounds - the bounds of t which may not necessarily need to be the same as the bounds as model might report it now; for removing a changing value
     */
    @Override
    public boolean remove(X x) {
        int before = size; //SIZE.get(this);
        if (before == 0)
            return false;

        HyperRegion bx = model.bounds(x);
        if (!root.bounds().contains(bx))
            return false;

        int[] removed = { 0 };
        @Nullable RNode<X> nextRoot = root.remove(x, bx, model, removed);
        int numRemoved = removed[0];
        update(nextRoot, numRemoved);
        return numRemoved > 0;
    }

    private void update(RNode<X> nextRoot, int removed) {

        if (removed > 0) {
            size -= removed; //SIZE.addAndGet(this, -removed[0]);
            if (size < 0)
                throw new WTF();
        }

        if (size == 0) {
            assert(nextRoot==null || nextRoot.size()==0):
            nextRoot = REmpty.the;
        }

        if (nextRoot!=null)
            root = nextRoot;
    }

    @Override
    public boolean replace(X told, X tnew) {

        if (told == tnew) return true;

        if (model.bounds(told).equals(model.bounds(tnew))) {
            root = root.replace(told, model.bounds(told), tnew, model);
            return true;
        } else {

            boolean removed = remove(told);
            if (removed) {
                boolean added = add(tnew);
                if (!added) throw new UnsupportedOperationException("error adding " + tnew);
                return true;
            } else
                return false;
        }
    }


    /**
     * @return number of data entries stored in the RTree
     */
    @Override
    public final int size() {
        return size;
    }


    @Override
    public void forEach(Consumer<? super X> consumer) {
        root.forEach(consumer);
    }

    @Override
    public boolean intersectsWhile(HyperRegion rect, Predicate<X> t) {
         return root.intersecting(rect, t, model);
    }

    public final boolean intersectingNodes(HyperRegion rect, Predicate<RNode<X>> t) {
        return root.intersectingNodes(rect, t, model);
    }

    public final boolean intersectingLeafs(HyperRegion rect, Predicate<RLeaf<X>> t) {
        return intersectingNodes(rect, (n) -> !(n instanceof RLeaf) || t.test((RLeaf<X>) n));
    }


    @Override
    public boolean containsWhile(HyperRegion rect, Predicate<X> t) {
        return root.containing(rect, t, model);
    }

    /**
     * returns how many items were filled
     */
    @Deprecated public int containedToArray(HyperRegion rect, X[] t) {
        int[] i = {0};
        root.containing(rect, (x) -> {
            t[i[0]++] = x;
            return i[0] < t.length;
        }, model);
        return i[0];
    }

    @Deprecated public Set<X> containedToSet(HyperRegion rect) {
        int s = size();
        Set<X> t = new HashSet(s);
        root.containing(rect, x -> {
            t.add(x);
            return true;
        }, model);
        return t;
    }


    void instrumentTree() {
        root = root.instrument();
        CounterRNode.searchCount = 0;
        CounterRNode.bboxEvalCount = 0;
    }

    @Override
    public Stats stats() {
        Stats stats = new Stats();
        stats.setType(model);
        stats.setMaxFill(model.nodeCapacity);

        root.collectStats(stats, 0);
        return stats;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "[size=" + size() + ']';
    }

    @Override
    public RNode<X> root() {
        return this.root;
    }

    @Override
    public boolean contains(X x, HyperRegion b, Spatialization/*<X, ?>*/ model) {
        return root.contains(x, b, model);
    }

    public boolean contains(X x) {
        return contains(x, model.bounds(x), model);
    }

    @Override
    public final HyperRegion bounds(X x) {
        return model.bounds(x);
    }


    public final Stream<RNode<X>> streamNodes() {
        return root().streamNodes();
    }
    public final Stream<RNode<X>> streamNodesRecursively() {
        return root().streamNodesRecursively();
    }


    /** for debugging/consistency testing: exhaustively counts the items in tree traversal */
    public int _size() {
        return (int) stream().count();
    }
}
