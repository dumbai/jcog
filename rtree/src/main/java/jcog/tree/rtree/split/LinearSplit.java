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

import jcog.tree.rtree.HyperRegion;
import jcog.tree.rtree.Spatialization;
import jcog.tree.rtree.node.RLeaf;
import jcog.tree.rtree.node.RNode;

/**
 * Guttmann's Linear split
 * <p>
 * Created by jcairns on 5/5/15.
 */
public class LinearSplit<X> extends Split<X> {

    public static final LinearSplit the = new LinearSplit();

    private static final int MIN = 0;
    private static final int MAX = 1;
    private static final int NRANGE = 2;

    private LinearSplit() { }

    @Override
    public RNode<X> apply(X x, RLeaf<X> leaf, Spatialization <X, ?> m) {


        X[] data = leaf.items;
        int nD = m.bounds(data[0]).dim();
        byte[][][] r = new byte[nD][NRANGE][NRANGE];

        double[] separation = new double[nD];

        short size = leaf.size;
        for (byte d = 0; d < nD; d++) {
            byte[][] rd = r[d];
            byte[] iiMin = rd[MIN];
            double daa = m.bounds(data[iiMin[MIN]]).coord(d, false);
            double dba = m.bounds(data[iiMin[MAX]]).coord(d, false);
            byte[] iiMax = rd[MAX];
            double dab = m.bounds(data[iiMax[MIN]]).coord(d, true);
            double dbb = m.bounds(data[iiMax[MAX]]).coord(d, true);

            for (byte j = 1; j < size; j++) {

                HyperRegion rj = m.bounds(data[j]);

                double rjMin = rj.coord(d, false);
                if (daa > rjMin) iiMin[MIN] = j;
                if (dba < rjMin) iiMin[MAX] = j;

                double rjMax = rj.coord(d, true);
                if (dab > rjMax) iiMax[MIN] = j;
                if (dbb < rjMax) iiMax[MAX] = j;
            }


            double width = Math.abs(
                    m.bounds(data[rd[MAX][MAX]]).coord(d, true) -
                    m.bounds(data[rd[MIN][MIN]]).coord(d, false)
            );

            separation[d] = Math.abs(
                    m.bounds(data[rd[MAX][MIN]]).coord(d, true) -
                    m.bounds(data[rd[MIN][MAX]]).coord(d, false)
            ) / (1+width);
        }
        //find extremes
        int r1Max = -1, r2Max = -1;
        double sepMax = Double.NEGATIVE_INFINITY;
        for (int d = 0; d < nD; d++) {
            double sepD = separation[d];
            if (sepD > sepMax) {
                sepMax = sepD;
                byte[][] rD = r[d];
                r1Max = rD[MAX][MIN];
                r2Max = rD[MIN][MAX];
            }
        }

        if (r1Max == r2Max) {
            r1Max = 0;
            r2Max = size-1;
        }
        assert(r1Max!=r2Max);
        return newBranch(x, m, size, r1Max, r2Max, data);

    }


}