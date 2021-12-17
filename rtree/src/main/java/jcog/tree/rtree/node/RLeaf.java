package jcog.tree.rtree.node;

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

import jcog.Util;
import jcog.tree.rtree.HyperRegion;
import jcog.tree.rtree.RInsertion;
import jcog.tree.rtree.Spatialization;
import jcog.tree.rtree.util.CounterRNode;
import jcog.tree.rtree.util.Stats;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Node that will contain the data entries. Implemented by different type of SplitType leaf classes.
 * <p>
 * Created by jcairns on 4/30/15.
 * <p>
 */
public class RLeaf<X> extends AbstractRNode<X,X> {

    public RLeaf(int mMax) {
        this((X[]) new Object[mMax]);
    }

    public RLeaf(X[] x) {
        super(x);
    }

    public RLeaf(X[] x, HyperRegion bounds, int size) {
        this(x);
        this.size = (short) size;
        this.bounds = bounds;
    }

    @Override
    public boolean intersectingNodes(HyperRegion rect, Predicate<RNode<X>> t, Spatialization <X, ?> model) {
        return !rect.intersects(bounds) || t.test(this);
    }

    @Override
    public Iterator<RNode<X>> iterateNodes() {
        return Util.emptyIterator;
    }

    @Override
    public Stream<RNode<X>> streamNodes() {
        return Stream.empty();
    }

    @Override
    public final Stream<RNode<X>> streamNodesRecursively() {
        return Stream.of(this);
    }

    @Override
    public Iterator<X> iterateValues() {
        return iterateLocal();
    }

    @Override
    public Stream<X> streamValues() {
        return streamLocal();
    }


//    public double variance(int dim, Spatialization<X> model) {
//        int s = size();
//        if (s < 2)
//            return 0;
//        double mean = bounds().center(dim);
//        double sumDiffSq = 0;
//        for (int i = 0; i < s; i++) {
//            X c = get(i);
//            if (c == null) continue;
//            double diff = model.bounds(c).center(dim) - mean;
//            sumDiffSq += diff * diff;
//        }
//        return sumDiffSq / s - 1;
//    }

    @Override
    public RNode<X> add(/*@NotNull*/RInsertion<X> X) {
        int s = size;
        if (s > 0 && X.maybeContainedBy(bounds)) {
            X[] yy = this.items;
            X x = X.x;

            Spatialization<? super X, ?> m = X.model;
            if (m.canMerge()) {
                boolean merged = false;
                //test all possible merge absorptions
                for (int i = 0; i < s; i++) {
                    X y = yy[i];
                    if (y != null) {
                        X xy = X.merge(y);
                        if (xy != null) {
                            merged = true;
                            merge(X, yy, i, y, xy);
                        }
                    }
                }
                if (merged)
                    return this;
            } else {
                //test for basic identity/equality
                for (int i = 0; i < s; i++) {
                    X y = yy[i];
                    if (x.equals(y)) {
                        X.mergeEqual(y);
                        return null; //first (and hopefully only) equal instance found
                    }
                }
            }
        }

        return X.addOrMerge ? insert(X) : this;
    }

    private void merge(RInsertion<X> X, X[] yy, int i, X y, X xy) {
        if (xy != y) { //same instance?
            X.write();
            if (items == yy && yy[i] == y) {

                Spatialization<? super X, ?> m = X.model;
                int[] rr = new int[1];
                remove(i, rr, m);
                X.removals += rr[0];


                if (contains(xy, m.bounds(xy), m))
                    X.mergeEqual(xy);
                else
                    X.reinsert(xy);


//                                data[i] = null;
//                                ArrayUtil.sortNullsToEnd(data, 0, s);
//                                size--;
//                                X.removals++;
//
//                                HyperRegion newBounds = Util.maybeEqual(HyperRegion.mbr(X.model, data), bounds);
//                                if (bounds!=newBounds) {
//                                    this.bounds = newBounds;
//                                    X.stretched = true;
//                                    invalidate();
//                                }
//
//                                return null;
            }
//                            if (size > 0) //else became deleted while waiting for write?
//                                merged(xy, X, y, i);
        } else {
            if (X.x!=xy)
                X.mergeEqual(xy);
        }

    }


//    private void merged(X merged, RInsertion<X> X, X existing, int i) {
//        items[i] = merged;
//
//        //test if equal item already present
//        int s = size;
//        for (int k = 0; k < s; k++) {
//            if (k==i) continue;
//            if (items[k].equals(merged)) {
//                X.mergeEqual(items[k]);
//                X.removals++;
//                items[k] = null; //eliminated as a result of merge
//                size--;
//            }
//        }
//        if (s!=size) {
//            ArrayUtil.sortNullsToEnd(items, 0, s);
//            return;
//        }
//
//
//
//        Spatialization<? super X, ?> m = X.model;
//
//        if (!m.bounds(existing).equals(m.bounds(merged))) {
//            //recompute bounds
//            HyperRegion newBounds = Util.maybeEqual(HyperRegion.mbr(m, items), bounds);
//            if (bounds!=newBounds) {
//                this.bounds = newBounds;
//                X.stretched = true;
//                invalidate();
//            }
//        }
//    }

    private RNode<X> insert(RInsertion<X> x) {
        x.write();
        Spatialization model = x.model;
        RNode<X> result = insert(x.x, x.xBounds, model);
        x.added = true;
//        model.commit(this);
        return result;
    }

    public final RNode<X> insert(X x, Spatialization <X, ?> model) {
        return insert(x, model.bounds(x), model);
    }

    RNode<X> insert(X x, HyperRegion bounds, Spatialization<X,?> model) {
        if (size < items.length) {
            X y = model.item(x);
            items[this.size++] = y;
            HyperRegion boundsPrev = this.bounds;
            this.bounds = boundsPrev != null ? Util.maybeEqual(boundsPrev.mbr(bounds), boundsPrev) : bounds;
            invalidate();
            return this;
        } else {
            return model.split(x, this);
        }
    }

    protected void invalidate() {
        //nothing, can be overridden to invalidate cached values on item change
    }

    @Override
    public boolean AND(Predicate<X> p) {
        for (X x : items) {
            if (x != null) {
                if (!p.test(x))
                    return false;
            } else
                break; //null-terminator reached
        }
        return true;
    }

    @Override
    public boolean OR(Predicate<X> p) {
        for (X x : items) {
            if (x != null) {
                if (p.test(x))
                    return true;
            } else
                break; //null-terminator reached
        }
        return false;
    }

    @Override
    public boolean contains(X x, HyperRegion b, Spatialization/*<X, ?>*/ model) {

        int s = size;
        if (s <= 0 || !bounds.contains(b)) //impossibleContent(b, model))
            return false;

        X[] ii = this.items;
        for (int i = 0; i < s; i++) {
            if (x.equals(ii[i])) return true;
        }
        return false;
    }


    @Override
    public RNode<X> remove(X x, HyperRegion b, Spatialization <X, ?> model, int[] removed) {

        int s = this.size;
        if (s <= 0 || !bounds.contains(b))
            //if (impossibleContent(b, model))
            return this;

        X[] d = this.items;
        for (int i = 0; i < s; i++) {
            if (x.equals(d[i])) return remove(i, removed, model) ? this : null;
        }
        return this; //not found
    }

//    public boolean impossibleContent(HyperRegion xBounds, Spatialization<X, ?> model) {
//        return model.canMergeStretch() ? !bounds.intersects(xBounds) : !bounds.contains(xBounds);
//    }

    /** returns whether the leaf is not completely empty */
    private boolean remove(int i, int[] removed, Spatialization model) {
        X[] data = this.items;
        int j = i + 1;
        if (j < size) {
            int nRemaining = size - j;
            System.arraycopy(data, j, data, i, nRemaining);
            Arrays.fill(data, size - 1, size, null);
        } else {
            Arrays.fill(data, i, size, null);
        }

        this.size--;
        removed[0]++;

        if (this.size > 0) {
            bounds = Util.maybeEqual(model.mbr(data), bounds);
            invalidate();
            return true;
        } else {
            bounds = null;
            invalidate();
            return false;
        }
    }

    @Override
    public RNode<X> replace(X told, HyperRegion oldBounds, X tnew, Spatialization <X, ?> model) {
        int s = size;
        if (s > 0 && bounds.contains(oldBounds)) {
            X[] data = this.items;
            HyperRegion r = null;
            for (int i = 0; i < s; i++) {
                X d = data[i];
                if (d.equals(told))
                    data[i] = tnew;

                r = i == 0 ? model.bounds(data[0]) : r.mbr(model.bounds(data[i]));
            }

            this.bounds = r;
            invalidate();
        }
        return this;
    }


    @Override
    public boolean intersecting(HyperRegion rect, Predicate<X> t, Spatialization <X, ?> model) {
        short s = this.size;
        if (s > 0 && rect.intersects(bounds)) {
            boolean containsAll = s > 1 && rect.contains(bounds);
            X[] data = this.items;
            for (int i = 0; i < s; i++) {
                X d = data[i];
                if (/*d != null && */ (containsAll || rect.intersects(model.bounds(d))) && !t.test(d))
                    return false;
            }
        }
        return true;
    }

    @Override
    public boolean containing(HyperRegion rect, Predicate<X> t, Spatialization <X, ?> model) {
        short s = this.size;
        if (s > 0 && rect.intersects(bounds)) {
            boolean fullyContained = s > 1 && rect.contains(bounds);
            X[] data = this.items;
            for (int i = 0; i < s; i++) {
                X d = data[i];
                if (/*d != null && */(fullyContained || rect.contains(model.bounds(d))) && !t.test(d))
                    return false;
            }
        }
        return true;
    }


    @Override
    public final void forEach(Consumer<? super X> consumer) {
        int s = size;
        for (int i = 0; i < s; i++) {
            X x = items[i];
//            if (x != null)
                consumer.accept(x);
//            else
//                break; //null-terminator reached
        }
    }

//    @Override
//    public final void forEachLocal(Consumer c) {
//        forEach(c);
//    }

    @Override
    public void collectStats(Stats stats, int depth) {
        if (depth > stats.getMaxDepth()) stats.setMaxDepth(depth);
        stats.countLeafAtDepth(depth);
        stats.countEntriesAtDepth(size, depth);
    }

    @Override
    public RNode<X> instrument() {
        return new CounterRNode(this);
    }

    @Override
    public String toString() {
        return "Leaf" + '{' + bounds + 'x' + size + '}';
    }


    public double variance(Spatialization<X,?> model) {
        short s = this.size;
        if (s <= 1) return 0;
        HyperRegion b = this.bounds;
        double costMbr = b.cost();
        double costLoss = 0;
        X[] xes = this.items;
        for (int i = 0; i < s; i++)
            costLoss += Math.max(0, costMbr - model.bounds(xes[i]).cost());
        return costLoss / s;

//        int d = b.dim();
//        double v = 0;
//        double[][] bounds = new double[d][];
//
//        X[] items = this.items;
//        for (int i = 0; i < s; i++) {
//            HyperRegion ii = model.bounds(items[i]);
//            for (int j = 0; j < d; j++) {
//                double jMin, jMax;
//                double[] bj = bounds[j];
//                if (bj == null) {
//                    jMin = b.coord(j, false); jMax = b.coord(j, true);
//                    bounds[j] = new double[] { jMin,  jMax };
//                } else {
//                    jMin = bj[0]; jMax = bj[1];
//                }
//
//                double jRange = jMax-jMin;
//                if (jRange < Float.MIN_NORMAL)
//                    continue;
//
//                double dMin = abs(ii.coord(j, false) - jMin);
//                double dMax = abs(ii.coord(j, true) - jMax);
//                v += (dMin + dMax)/jRange;
//                if (v > exitIfExceeds)
//                    return Float.NaN;
//            }
//        }
//
//        return v;
    }

//    /** caches certain calculations */
//    public static class RCacheLeaf<X> extends RLeaf<X> {
//
//        private transient double _variance = Double.NaN;
//
//        public RCacheLeaf(X[] x) {
//            super(x);
//        }
//
//        @Override
//        protected void invalidate() {
//            _variance = Double.NaN;
//        }
//
//
//        @Override
//        public double variance(Spatialization<X, ?> model) {
//            double v = this._variance;
//            if (v!=v)
//                this._variance = v = super.variance(model);
//            return v;
//        }
//    }
}
