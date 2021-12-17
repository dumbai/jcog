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


import jcog.data.list.Lst;
import jcog.tree.rtree.node.RNode;
import jcog.tree.rtree.util.Stats;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.EMPTY_LIST;

/**
 * Created by jcovert on 12/30/15.
 */
public interface Space<X> extends RegionContainer<X> {

    /**
     * Update entry in tree
     *
     * @param told - Entry to update
     * @param tnew - Entry to update it to
     */
    boolean replace(X told, X tnew);

    /**
     * Get the number of entries in the tree
     *
     * @return entry count
     */
    int size();

    boolean OR(Predicate<X> o);

    boolean AND(Predicate<X> o);

    void forEach(Consumer<? super X> consumer);

    enum BoundsMatch {
        //TODO meet, equal, disjoint, etc...

        /** allows everything; filtering must rely on sorting */
        ANY {
            @Override
            public boolean acceptItem(HyperRegion target, HyperRegion something) {
                return true;
            }
            @Override
            public boolean acceptNode(HyperRegion target, HyperRegion something) {
                return true;
            }
        },
        INTERSECT {
            @Override
            public boolean acceptItem(HyperRegion target, HyperRegion something) {
                return target.intersects(something);
            }

            @Override
            public boolean acceptNode(HyperRegion target, HyperRegion container) {
                return target.intersects(container);
            }
        },
        CONTAINS {
            @Override
            public boolean acceptItem(HyperRegion target, HyperRegion something) {
                return target.contains(something);
            }

            @Override
            public boolean acceptNode(HyperRegion target, HyperRegion container) {
                return target.contains(container);
            }
        };



        public abstract boolean acceptItem(HyperRegion target, HyperRegion x);

        public abstract boolean acceptNode(HyperRegion target, HyperRegion x);
    }


    /**
     * continues finding intersecting regions until the predicate returns false
     */
    boolean intersectsWhile(HyperRegion rect, Predicate<X> t);

    /**
     * continues finding containing regions until the predicate returns false
     */
    boolean containsWhile(HyperRegion rect, Predicate<X> t);

//    /**
//     * Search for entries intersecting given bounding rect
//     *
//     * @param rect - Bounding rectangle to use for querying
//     * @param t    - Array to store found entries
//     * @return Number of results found
//     */
//    @Deprecated
//    int containedToArray(HyperRegion rect, X[] t);


    Stats stats();

    boolean contains(X t);

    default boolean isEmpty() {
        return size() == 0;
    }

    void clear();

    /**
     * Add the data entry to the SpatialSearch structure
     *
     * @param x Data entry to be added
     * @return whether the item was added, or false if it wasn't (ex: duplicate or some other prohibition)
     */
    default /* final */ boolean add(X x) {
        return insert(x).added;
    }

    Spatialization model();

    default RInsertion insert(/*@NotNull*/ X x) {
        RInsertion i = model().insertion(x, true);
        insert(i);
        return i;
    }

    void insert(/*@NotNull*/ RInsertion x);

    /**
     * adds, deferred if necessary until un-busy
     */
    default void addAsync(X t) {
        throw new UnsupportedOperationException();
    }

    /**
     * Remove the data entry from the SpatialSearch structure
     *
     * @param x Data entry to be removed
     * @return whether the item was added, or false if it wasn't (ex: duplicate or some other prohibition)
     */
    boolean remove(/*@NotNull*/ X x);


    /**
     * removes, deferred if necessary until un-busy
     */
    default void removeAsync(X x) {
        throw new UnsupportedOperationException();
    }


    RNode<X> root();


    HyperRegion bounds(X task);

    Stream<X> stream();

    Iterator<X> iterator();

//    default Iterator<X> iterator() {
//        int s = size();
//        if (s == 0)
//            return Util.emptyIterator;
//
//
//        return stream().iterator();
//    }

    default List<X> asList() {
        int s = size();
        if (s > 0) {
            Lst<X> l = new Lst<>(s);
            forEach(/*l::addFast*/ l::add);
            return l;
        } else {
            return EMPTY_LIST;
        }
    }



}
