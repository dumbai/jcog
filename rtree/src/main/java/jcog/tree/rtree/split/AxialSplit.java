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

import jcog.sort.QuickSort;
import jcog.tree.rtree.HyperRegion;
import jcog.tree.rtree.Spatialization;
import jcog.tree.rtree.node.RBranch;
import jcog.tree.rtree.node.RLeaf;
import org.eclipse.collections.api.block.function.primitive.IntToDoubleFunction;

import java.util.Arrays;

/**
 * Fast RTree split suggested by Yufei Tao taoyf@cse.cuhk.edu.hk
 * <p>
 * Perform an axial split
 * <p>
 * Created by jcairns on 5/5/15.
 */
public class AxialSplit<X> extends Split<X> {

    /** default stateless instance which can be re-used */
    public static final Split the = new AxialSplit<>();

    protected AxialSplit() { }

    @Override
    public RBranch<X> apply(X x, RLeaf<X> leaf, Spatialization<X, ?> model) {


        HyperRegion rCombined = leaf.bounds.mbr(model.bounds(x));

        int nD = rCombined.dim();

        int axis = 0;
        double mostCost = Double.NEGATIVE_INFINITY;
        for (int d = 0; d < nD; d++) {
            
            double axisCost = rCombined.cost(d);
            if (axisCost > mostCost) {
                axis = d;
                mostCost = axisCost;
            }
        }


        return splitAxis(x, leaf, model, axis);
    }

    static <X> RBranch<X> splitAxis(X adding, RLeaf<X> leaf, Spatialization<X, ?> m, int sortAxis) {
        short size = (short) (leaf.size+1);
        X[] x = Arrays.copyOf(leaf.items, size); //? X[] obj = (X[]) Array.newInstance(leaf.data.getClass(), size);
        x[size-1] = adding;

//        double[] strength = new double[size];
//        for (int i = 0; i < size; i++) {
//            X li = i < size-1 ? ld[i] : x;
//            double c = model.bounds(li).center(splitDimension); //TODO secondary sort by range
//            strength[i] = -c;
//        }

        if (size > 1) {
            QuickSort.sort(x, 0, size, (IntToDoubleFunction) i ->
                m.bounds(x[i]).center(sortAxis)
            );
        }

        int splitN = size/2 + (((size & 1)!=0) ? 1 : 0);

        //TODO if size is odd, maybe l1Node should have the 1 extra element rather than l2Node as this will:
        //assert (l1Node.size()+l2Node.size() == size);
        //leaf.transfer(l1Node, l2Node, x, model);
        //assert (l1Node.size()+l2Node.size() == size);

        return m.newBranch(m.transfer(x, 0, splitN), m.transfer(x, splitN, size));
    }


}
