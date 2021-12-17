package jcog.tree.rtree.split;

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
import jcog.tree.rtree.Spatialization;
import jcog.tree.rtree.node.RLeaf;
import jcog.tree.rtree.node.RNode;

/**
 * Guttmann's Quadratic split
 * <p>
 * Created by jcairns on 5/5/15.
 */
public class QuadraticSplit<X> extends Split<X> {

    public static final QuadraticSplit the = new QuadraticSplit();

    protected QuadraticSplit() {

    }

    /**  find the two bounds that are most wasteful */
    @Override public RNode<X> apply(X x, RLeaf<X> leaf, Spatialization <X, ?> m) {

        var size = leaf.size;
        var data = leaf.items;
        var BOUNDS = Util.map(m::bounds, new HyperRegion[size], data); //cache
        var COST = Util.arrayOf(i -> BOUNDS[i].cost(), new double[size]); //cache

        int r1 = -1, r2 = -1;
        var maxWaste = Double.NEGATIVE_INFINITY;
        for (var i = 0; i < size-1; i++) {
            var ii = BOUNDS[i];
            var iic = COST[i];
            for (var j = i + 1; j < size; j++) {
                var jj = BOUNDS[j];
                var jjc = COST[j];
                var ij = ii.mbr(jj);
                var ijc = (ij==ii ? iic : (ij == jj ? jjc : ij.cost())); //assert(ijc >= iic && ijc >= iic);
                var waste = (ijc - iic) + (ijc - jjc);
                if (waste > maxWaste) {
                    r1 = i;
                    r2 = j;
                    maxWaste = waste;
                }
            }
        }

        return newBranch(x, m, size, r1, r2, data);
    }



}
