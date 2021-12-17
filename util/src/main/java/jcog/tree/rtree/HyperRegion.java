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


/**
 * An N dimensional rectangle or "hypercube" that is a representation of a data entry.
 * <p>
 * Created by jcairns on 4/30/15.
 */
public interface HyperRegion {

    /**
     * Calculate the resulting mbr when combining param HyperRect with this HyperRect
     * use custom implementations of mbr(HyperRect[]) when possible, it is potentially more efficient
     *
     * @param r - mbr to addAt
     * @return new HyperRect representing mbr of both HyperRects combined
     */
    HyperRegion mbr(HyperRegion r);


//    static <X> HyperRegion mbr(Spatialization<X, ?> model, X[] rect) {
//        //return mbr(model::bounds, rect, (short) rect.length);
//        return model.mbr(rect);
//    }

    default HyperRegion mbr(HyperRegion[] h) {
        HyperRegion b = h[0];
        for (int k = 1; k < h.length; k++)
            b = b.mbr(h[k]);
        return b;
    }

    /**
     * Get number of dimensions used in creating the HyperRect
     *
     * @return number of dimensions
     */
    int dim();


    /**
     * returns coordinate scalar at the given extremum and dimension
     *
     * @param dimension which dimension index
     * @param maxOrMin  true = max, false = min
     */
    double coord(int dimension, boolean maxOrMin);

    default double center(int d) {
        return (coord(d, true) + coord(d, false)) / 2;
    }

    /**
     * Calculate the distance between the min and max HyperPoints in given dimension
     *
     * @param dim - dimension to calculate
     * @return double - the numeric range of the dimention (min - max)
     */
    default double range(int dim) {
        return Math.abs(coord(dim, true) - coord(dim, false));
    }


    default double cost(int dim) {
        return rangeIfFinite(dim, 0);
    }

    default double rangeIfFinite(int dim, double elseValue) {
        double r = range(dim);
        if (!Double.isFinite(r)) {
            return elseValue;
        } else {
            assert (r >= 0);
            return r;
        }
    }


    /**
     * Determines if this HyperRect fully contains parameter HyperRect
     *
     * @param r - HyperRect to test
     * @return true if contains, false otherwise; a region contains itself
     */
    default boolean contains(HyperRegion x) {
        if (this == x) return true;
        int d = dim();
        for (int i = 0; i < d; i++)
            if (coord(i, false) > x.coord(i, false) ||
                    coord(i, true) < x.coord(i, true))
                return false;
        return true;
    }


    /**
     * Determines if this HyperRect intersects parameter HyperRect
     *
     * @param r - HyperRect to test
     * @return true if intersects, false otherwise
     */
    default boolean intersects(HyperRegion x) {
        if (this == x) return true;
        int d = dim();
        for (int i = 0; i < d; i++) {
            if (coord(i, false) > x.coord(i, true) ||
                    coord(i, true) < x.coord(i, false))
                return false;
        }
        return true;
    }


    /**
     * Calculate the "cost" of this HyperRect -
     * generally this is computed as the area/volume/hypervolume of this region
     *
     * @return - cost
     */
    default double cost() {
        int n = dim();
        double a = 1.0;
        for (int d = 0; d < n; d++)
            a *= (1 + cost(d));
        //a *= cost(d);
        return a;
    }

    /**
     * Calculate the perimeter of this HyperRect - across all dimensions
     *
     * @return - perimeter
     */
    default double perimeter() {
        double p = 0.0;
        int n = this.dim();
        for (int d = 0; d < n; d++)
            p += this.cost(d);
        return p;
    }


}