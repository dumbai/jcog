package jcog.tree.rtree.util;

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

import jcog.tree.rtree.HyperRegion;
import jcog.tree.rtree.RInsertion;
import jcog.tree.rtree.Spatialization;
import jcog.tree.rtree.node.RNode;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by jcovert on 6/18/15.
 */
public final class CounterRNode<X> implements RNode<X> {
    public static int searchCount;
    public static int bboxEvalCount;
    private final RNode<X> node;

    public CounterRNode(RNode<X> node) {
        this.node = node;
    }

    @Override
    public int available() {
        return node.available();
    }
//    @Override
//    public Object get(int i) {
//        return node.get(i);
//    }

    @Override
    public Stream<X> streamValues() {
        return node.streamValues();
    }

    @Override
    public Stream<RNode<X>> streamNodes() {
        return node.streamNodes();
    }

    @Override
    public Stream<?> streamLocal() {
        return node.streamLocal();
    }

    @Override
    public Iterator<?> iterateLocal() {
        return node.iterateLocal();
    }

    @Override
    public Iterator<X> iterateValues() {
        return node.iterateValues();
    }


    @Override
    public Iterator<RNode<X>> iterateNodes() {
        return node.iterateNodes();
    }


    @Override
    public HyperRegion bounds() {
        return this.node.bounds();
    }

    @Override
    public RNode<X> add(RInsertion<X> i) {
        return this.node.add(i);
    }

    @Override
    public RNode<X> remove(X x, HyperRegion xBounds, Spatialization <X, ?> model, int[] removed) {
        return this.node.remove(x, xBounds, model, removed);
    }

    @Override
    public RNode<X> replace(X told, HyperRegion oldBounds, X tnew, Spatialization <X, ?> model) {
        return this.node.replace(told, oldBounds, tnew, model);
    }

    @Override
    public boolean AND(Predicate<X> p) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean OR(Predicate<X> p) {
        throw new UnsupportedOperationException("TODO");
    }


    @Override
    public boolean containing(HyperRegion rect, Predicate<X> t, Spatialization <X, ?> model) {
        searchCount++;
        bboxEvalCount += this.node.size();
        return this.node.containing(rect, t, model);
    }






    @Override
    public int size() {
        return this.node.size();
    }

    @Override
    public void forEach(Consumer<? super X> consumer) {
        this.node.forEach(consumer);
    }

    @Override
    public boolean intersecting(HyperRegion rect, Predicate<X> t, Spatialization <X, ?> model) {
        return this.node.intersecting(rect, t, model);
    }

    @Override
    public boolean intersectingNodes(HyperRegion rect, Predicate<RNode<X>> t, Spatialization <X, ?> model) {
        return this.node.intersectingNodes(rect, t, model);
    }

    @Override
    public void collectStats(Stats stats, int depth) {
        this.node.collectStats(stats, depth);
    }

    @Override
    public RNode<X> instrument() {
        return this;
    }


    @Override
    public boolean contains(X x, HyperRegion b, Spatialization model) {
        return node.contains(x, b, model);
    }
}
