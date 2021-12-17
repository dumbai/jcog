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


import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import jcog.Util;
import jcog.tree.rtree.HyperRegion;
import jcog.tree.rtree.RInsertion;
import jcog.tree.rtree.Spatialization;
import jcog.tree.rtree.util.CounterRNode;
import jcog.tree.rtree.util.Stats;
import jcog.util.ArrayUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.System.arraycopy;

/**
 * RTree node that contains leaf nodes
 * <p>
 * Created by jcairns on 4/30/15.
 */
public class RBranch<X> extends AbstractRNode<X,RNode<X>> {

    public RBranch(int cap, RNode<X>[] data) {
        super(data.length == cap ? data : Arrays.copyOf(data, cap)); //TODO assert all data are unique; cache hash?
        //assert (cap >= 2);
        this.size = (short) data.length;
        assert(size > 1);
        this.bounds = mbr(data);
    }

    public static HyperRegion mbr(RNode[] h) {
        HyperRegion b = h[0].bounds();
        for (int k = 1; k < h.length; k++) {
            RNode hk = h[k];
            if (hk == null)
                break; //null terminator
            b = b.mbr(hk.bounds());
        }
        return b;
    }

    @Override
    public boolean contains(X x, HyperRegion b, Spatialization/*<X, ?>*/ model) {

        if (!this.bounds.contains(b))
            return false;

//        int s = size;
//        if (s > 0) {
//            Node<X>[] c = this.data;
            for (RNode c : items) {
        //            for (int i = 0; i < s; i++) {
                if (c == null)
                    break; //null-terminator
                if (c.contains(x, b, model))
                    return true;

            }
        //        }

        return false;
    }


    /**
     * Add a new node to this branch's list of children
     *
     * @param n node to be added (can be leaf or branch)
     * @return position of the added node
     */
    private void addChild(RInsertion x) {
        RNode<X> n = x.model.newLeaf().add(x);
        items[this.size++] = n;
        HyperRegion b = this.bounds;
        HyperRegion nb = n.bounds();
        this.bounds = b==null ? nb : Util.maybeEqual(b.mbr(nb), b);
    }


    /**
     * Adds a data entry to one of the child nodes of this branch
     *
     * @param parent
     * @param x
     * @return Node that the entry was added to
     */
    @Override
    public RNode<X> add(RInsertion<X> x) {


        boolean addOrMerge = x.addOrMerge; //save here before anything

        //1. test containment
        if (addOrMerge)
            x.addOrMerge = false; //temporarily set to contain/merge mode

        RNode<X>[] data = this.items;
        int s = this.size;
        if (s > 0 && x.maybeContainedBy(bounds)) {

//            boolean merged = false;
            for (int i = 0; i < s; i++) {
                RNode<X> ci = data[i];
                if (ci == null) continue; //HACK??
                RNode<X> di = ci.add(x);
                if (ci!=di) {
                    x.write(); //probably already in write lock
                    if (items==data && data[i]==ci) {
//                        if (di != null && di.size() == 0) {
//                            data[i] = null;
//                            ArrayUtil.sortNullsToEnd(items, 0, s);
//                            size--;
//                            return this;
//                        }
                        if (di != null)
                            data[i] = di;
                        //x.setMerged();
                        if (x.stretched)
                            updateBounds();
                        //if (x.removals>0) ??
//                        if (size == data.length) {
//                            return consolidate(x);
//                        }
                    }

                    return null;

                }
            }
        }

        return addOrMerge ? insert(x) : this;
    }

    private RNode<X> insert(RInsertion<X> x) {

        x.addOrMerge = true; //restore to add mode

        int cap = items.length;

        x.write();

//        if (size == cap) {
//            //attempt reconsolidation
//            RNode<X> reconsolidated = consolidate(x);
//            if (reconsolidated!=null)
//                return reconsolidated.add(x);
//        }

        clean();

        if (size < cap) {
            addChild(x);
        } else {
            int l = chooseLeaf(x.xBounds);
            items[l] = items[l].add(x);
            clean();
        }

        if (size==1)
            return items[0];
        else {
            updateBounds();
            return this;
        }
    }

//    private RNode<X> consolidate(RInsertion<X> x) {
//        int[] removed = new int[1];
//        RNode<X> reconsolidated = consolidate(x.model, removed);
//
//        x.removals += removed[0];
//        return reconsolidated;
//    }

    @Override
    public RNode<X> remove(X x, HyperRegion xBounds, Spatialization <X, ?> model, int[] removed) {

        int nsize = this.size;
        if (nsize == 0 || !(/*model.canMerge() ? bounds.intersects(xBounds) : */bounds.contains(xBounds)))
            return this; //not here

        RNode<X>[] data = this.items;
        for (int i = 0; i < nsize; i++) {
            RNode<X> nBefore = data[i];

            @Nullable RNode<X> nAfter = nBefore.remove(x, xBounds, model, removed);
            if (nAfter!=null && nAfter.size()==0) {
                nAfter = null;
            }

            if (nAfter!=nBefore) {
                data[i] = nAfter;

                if (nAfter == null) {
                    --size;
                    switch (size) {
                        case 0:
                            //emptied
                            return null;
                        case 1:
                            //return the only remaining item
                            RNode<X> only = firstNonNull();
                            data[i] = null;
                            size = 0;
                            return only;
                        default:
                            //sort nulls to end
                            if (i < size) {
                                arraycopy(data, i + 1, data, i, size - i);
                                data[size] = null;
                            }
                            break;
                    }

                }

                clean();
                updateBounds();
                return this;
            }
        }

        return this;
    }

    private RNode<X> firstNonNull() {
        for (RNode d : items) {
            if (d!=null)
                return d;
        }
        throw new UnsupportedOperationException();
    }

//    /** consolidate under-capacity leafs by reinserting
//     * @return*/
//    private RNode<X> consolidate(Spatialization model, int[] removed) {
//         /* algorithm:
//         * assuming that the items are sorted by the split,
//         * visit each item and if it's a leaf with a neighbor node that can hold its capacity,
//         * reinsert its items there
//         *  */
//
//         int sBefore = size;
//         RNode<X>[] d = items;
//         for (int i = 0; i < sBefore; i++) {
//             RNode<X> n = d[i];
//             if (n instanceof RLeaf) {
//                 RLeaf<X> l = (RLeaf<X>)n;
//                 int ls = l.size;
//
//                 RNode<X> left = i > 0 ? d[i - 1] : null;
//                 int lAvail = left!=null ? left.available() : 0;
//                 RNode<X> right = i < sBefore-1 ? d[i + 1] : null;
//                 int rAvail = right!=null ? right.available() : 0;
//                 if (lAvail> 0 && (lAvail < ls || rAvail > lAvail)) lAvail = 0;
//                 if (rAvail > 0 && (rAvail < ls || lAvail >= rAvail)) rAvail = 0;
//
//                 assert(lAvail==0 || rAvail==0);
//
//                 if (lAvail > 0) {
//                     if ((d[i-1] = reinsert(l, left, model, removed))==null)
//                         size--;
//                 } else if (rAvail > 0){
//                     if ((d[i+1] = reinsert(l, right, model, removed))==null)
//                         size--;
//                 } else
//                     continue;
//
//                 d[i] = null; //moved
//                 size--;
//             }
//         }
//         //HACK
//        for (int i = 0; i < sBefore; i++) {
//            RNode<X> n = d[i];
//            if (n!=null && n.size() == 0) {
//                d[i] = null;
//                size--;
//            }
//        }
//
//         if (sBefore!=size) {
//             ArrayUtil.sortNullsToEnd(items, 0, sBefore);
//             //updateBounds(); //<- bounds should be same
//         }
//         if (size==1)
//             return items[0];
//
//         return null; //continue with this
//
////        int childItems = 0, leafs = 0;
////        for (RNode<X> x : items) {
////            if (x == null) break;
////            if (!(x instanceof RLeaf)) continue;
////            short ls = ((RLeaf<X>) x).size;
////            childItems += ls;
////            leafs++;
////        }
////        if (childItems <=1 || leafs <= 1 || (childItems > (leafs - 1) * model.nodeCapacity)) {
////            return null;
////        }
////
////        FasterList<X> xx = new FasterList<>(childItems);
////        Consumer<X> adder = xx::addWithoutResize;
////        for (int i = 0, dataLength = items.length; i < dataLength; i++) {
////            RNode<X> x = items[i];
////            if (x == null) break;
////            if (!(x instanceof RLeaf)) continue;
////            x.forEach(adder);
////            items[i] = null;
////            size--;
////        }
////        RNode<X> target;
////        switch (size) {
////            case 0:
////                bounds = null;
////                target = model.newLeaf();
////                break;
////            case 1:
////                target = firstNonNull();
////                size = 0;
////                break;
////            default:
////                ArrayUtil.sortNullsToEnd(items);
////                updateBounds();
////                target = this;
////                break;
////        }
////
////
////        for (X xxx : xx)
////            target = reinsert(xxx, target, model, removed);
////
////        return target;
////
//    }

//    private RNode<X> reinsert(Spatialization<X> model, int[] removed) {
////        RNode<X>[] d = data.clone();
////        Arrays.fill(data, null);
////        size = 0;
//        RNode<X> t = null;
//
//        Iterator<X> v = iterateValues();
//        while (v.hasNext()) {
//            X x = v.next();
//            /*parent.  ? */
//
//            if (t == null) {
//                t = model.newLeaf();
//                ((RLeaf)t).data[0] = x;  //HACK
//                ((RLeaf)t).size = 1; //HACK
//                ((RLeaf)t).bounds = model.bounds(x);
//            } else {
//                t = reinsert(x, t, model, removed);
//            }
//
//
//        }
//
//
//        //return ArrayUtil.equalsIdentity(data, t.data) ? this : t;
//        return t;
//    }

    private static <X> RNode<X> reinsert(RLeaf<X> x, RNode<X> target, Spatialization<X, ?> model, int[] removed) {
        int n = x.size;
        X[] xes = x.items;
        for (int i = 0; i < n; i++) {
            X xx = xes[i];
            target = reinsert(xx, target, model, removed);
        }
        if (target.size()==0)
            return null;
//        x.size = 0;
//        Arrays.fill(x.items, null);
//        x.invalidate();
        return target;
    }

    private static <X> RNode<X> reinsert(X x, RNode<X> target, Spatialization<X, ?> model, int[] removed) {
        RInsertion<X> reinsertion = model.insertion(x, true);
        RNode<X> u = target.add(reinsertion);
        boolean merged = reinsertion.merged;
        if (merged)
            removed[0]++;
        else {
			assert(reinsertion.added);
        }
//            if (!((reinsertion.added() && u != null) ))
//                throw new WTF("unable to add");
        if (u!=null) //HACK
            target = u;
        return target;
    }

    private void updateBounds() {
//        Node<X>[] dd = this.data;
//        HyperRegion region = dd[0].bounds();
//        for (int j = 1; j < size; j++)
//            region = region.mbr(dd[j].bounds());
//        if (bounds == null || !bounds.equals(region))
//            this.bounds = region;
        if (size > 0)
            bounds = Util.maybeEqual(mbr(items), bounds);
        else
            bounds = null;
    }

    @Override
    public RNode<X> replace(X OLD, HyperRegion oldBounds, X NEW, Spatialization <X, ?> model) {

        short s = this.size;
        if (s > 0 && oldBounds.intersects(bounds)) {
            boolean found = false;

            RNode<X>[] cc = this.items;
            HyperRegion region = null;

            for (int i = 0; i < s; i++) {
                if (!found && oldBounds.intersects(cc[i].bounds())) {
                    cc[i] = cc[i].replace(OLD, oldBounds, NEW, model);
                    found = true;
                }
                region = i == 0 ? cc[0].bounds() : region.mbr(cc[i].bounds());
            }
            if (found) {
                this.bounds = Util.maybeEqual(region, bounds);
            }
        }
        return this;
    }


    private int chooseLeaf(HyperRegion tRect) {
        RNode<X>[] cc = this.items;
        int s = size;
        if (s > 0) {

            int bestNode = -1;

            double leastEnlargement = Double.POSITIVE_INFINITY;
            double leastPerimeter = Double.POSITIVE_INFINITY;

            for (int i = 0; i < s; i++) {
                HyperRegion cir = cc[i].bounds();

                HyperRegion childMbr = tRect.mbr(cir);
                double nodeEnlargement =
                        (cir == childMbr ? 0 : childMbr.cost() - (cir.cost() /* + tCost*/));

                int dc = Double.compare(nodeEnlargement, leastEnlargement);
                if (nodeEnlargement < leastEnlargement) {
                    leastEnlargement = nodeEnlargement;
                    leastPerimeter = childMbr.perimeter();
                    bestNode = i;
                } else if (dc == 0) {
                    double perimeter = childMbr.perimeter();
                    if (perimeter < leastPerimeter) {
                        leastEnlargement = nodeEnlargement;
                        leastPerimeter = perimeter;
                        bestNode = i;
                    }
                }

            }
            if (bestNode == -1) {
                throw new RuntimeException("rtree fault");
            }

            return bestNode;
        } else {


            throw new RuntimeException("shouldnt happen");
        }
    }

    private int clean() {
        RNode<X>[] cc = items;
        //HACK clear any empty leaves TODO find where they come from
        int s = this.size;
        for (int i = 0; i < s; i++) {
            if (cc[i].size()==0) {
                cc[i] = null;
                size--;
            }
        }
        if (size!=s) {
            ArrayUtil.sortNullsToEnd(cc, 0, s);
            s = size;
            updateBounds();
        }
        return s;
    }


    @Override
    public void forEach(Consumer<? super X> consumer) {
         int s = size;
        for (int i = 0; i < s; i++) {
            RNode<X> x = items[i];
            if (x == null)
                break; //null terminator
            x.forEach(consumer);
        }
    }

//    @Override
//    public final void forEachLocal(Consumer c) {
//        forEach(c);
//    }

    @Override
    public boolean OR(Predicate<X> p) {
        for (RNode<X> x : items) {
            if (x == null) break; //null terminator
            if (x.OR(p)) return true;
        }
        return false;
    }

    @Override
    public boolean AND(Predicate<X> p) {
        for (RNode<X> x : items) {
            if (x == null) break; //null terminator
            if (!x.AND(p)) return false;
        }
        return true;
    }

//    public boolean ANDlocal(Predicate<RNode<X>> p) {
//        short s = this.size;
//        return IntStream.range(0, s).allMatch(i -> p.test(this.data[i]));
//    }
//    public boolean ORlocal(Predicate<RNode<X>> p) {
//        short s = this.size;
//        return IntStream.range(0, s).anyMatch(i -> p.test(this.data[i]));
//    }



    @Override
    public boolean containing(HyperRegion rect, Predicate<X> t, Spatialization <X, ?> model) {
        HyperRegion b = this.bounds;
        if (b != null && rect.intersects(b)) {
            int s = size;
            for (int i = 0; i < s; i++) {
                RNode d = items[i];
            //                if (d == null)
            //                    continue;
            /*else */
                if (!d.containing(rect, t, model))
                    return false;
            }
        }
        return true;
    }

    @Override
    public boolean intersectingNodes(HyperRegion rect, Predicate<RNode<X>> t, Spatialization <X, ?> model) {
        HyperRegion b = this.bounds;
        if (b != null && rect.intersects(b) && t.test(this)) {
            int s = size;
            for (int i = 0; i < s; i++) {
                if (!items[i].intersectingNodes(rect, t, model))
                    return false;
            }
        }
        return true;
    }

    @Override
    public boolean intersecting(HyperRegion rect, Predicate<X> t, Spatialization <X, ?> model) {
        HyperRegion b = this.bounds;
        if (b != null && rect.intersects(b)) {
            int s = size;
            for (int i = 0; i < s; i++) {
                if (!items[i].intersecting(rect, t, model))
                    return false;
            }
        }
        return true;
    }

    @Override
    public Stream<RNode<X>> streamNodes() {
        return streamLocal();
    }

    @Override
    public Stream<X> streamValues() {
        return streamNodes().flatMap(RNode::streamValues);
    }

    @Override
    public Iterator<X> iterateValues() {
        return Iterators.concat(Iterators.transform(iterateLocal(), RNode::iterateValues));
    }



    @Override
    public void collectStats(Stats stats, int depth) {
        for (int i = 0; i < size; i++)
            items[i].collectStats(stats, depth + 1);
        stats.countBranchAtDepth(depth);
    }

    @Override
    public RNode<X> instrument() {
        for (int i = 0; i < size; i++)
            items[i] = items[i].instrument();
        return new CounterRNode(this);
    }

    @Override
    public String toString() {
        return "Branch" + '{' + bounds + 'x' + size + ":\n\t" + Joiner.on("\n\t").skipNulls().join(items) + "\n}";
    }


}
