package jcog.tree.rtree.rect;

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
import jcog.tree.rtree.point.DoubleND;
import jcog.util.ArrayUtil;

import java.io.Serializable;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

/**
 * Created by jcovert on 6/15/15.
 */

public class HyperRectDouble implements HyperRegion, Serializable {

    public static final HyperRegion ALL_1 = HyperRectDouble.all(1);
    public static final HyperRegion ALL_2 = HyperRectDouble.all(2);
    public static final HyperRegion ALL_3 = HyperRectDouble.all(3);
    public static final HyperRegion ALL_4 = HyperRectDouble.all(4);
    public static final DoubleND VOID = new DoubleND(ArrayUtil.EMPTY_DOUBLE_ARRAY) {
        @Override
        public String toString() {
            return "*";
        }
    };
    public final DoubleND min;
    public final DoubleND max;

    public HyperRectDouble() {
        min = VOID;
        max = VOID;
    }

    public HyperRectDouble(DoubleND p) {
        min = p;
        max = p;
    }

    public HyperRectDouble(double[] a, double[] b) {
        this(new DoubleND(a), new DoubleND(b));
    }

    @Override
    public double coord(int dimension, boolean maxOrMin) {
        return (maxOrMin ? max : min).coord[dimension];
    }

    public HyperRectDouble(DoubleND a, DoubleND b) {
        int dim = a.dim();

        double[] min = new double[dim];
        double[] max = new double[dim];

        double[] ad = a.coord;
        double[] bd = b.coord;
        for (int i = 0; i < dim; i++) {
            double ai = ad[i];
            double bi = bd[i];
            min[i] = Math.min(ai, bi);
            max[i] = Math.max(ai, bi);
        }
        this.min = new DoubleND(min);
        this.max = new DoubleND(max);
    }

    public static HyperRegion all(int i) {
        return new HyperRectDouble(DoubleND.fill(i, NEGATIVE_INFINITY), DoubleND.fill(i, POSITIVE_INFINITY));
    }





























    @Override
    public double cost() {
        int dim = dim();
        double sigma = IntStream.range(0, dim).mapToDouble(i -> rangeIfFinite(i, 1 /* an infinite dimension can not be compared, so just ignore it */)).reduce(1f, (a, b) -> a * b);
        return sigma;
    }

    @Override
    public HyperRegion mbr(HyperRegion r) {
        HyperRectDouble x = (HyperRectDouble) r;

        int dim = dim();
        double[] newMin = new double[dim];
        double[] newMax = new double[dim];
        for (int i = 0; i < dim; i++) {
            newMin[i] = Math.min(min.coord[i], x.min.coord[i]);
            newMax[i] = Math.max(max.coord[i], x.max.coord[i]);
        }
        return new HyperRectDouble(newMin, newMax);
    }

    @Override
    public double center(int dim) {
        return centerF(dim);
    }

    public double centerF(int dim) {
        double min = this.min.coord[dim];
        double max = this.max.coord[dim];
        if ((min == NEGATIVE_INFINITY) && (max == Double.POSITIVE_INFINITY))
            return 0;
        if (min == NEGATIVE_INFINITY)
            return max;
        if (max == Double.POSITIVE_INFINITY)
            return min;

        return (max + min) / 2;
    }

    public DoubleND center() {
        int dim = dim();
        double[] c = new double[dim];
        for (int i = 0; i < dim; i++)
            c[i] = centerF(i);
        return new DoubleND(c);
    }


    @Override
    public int dim() {
        return min.dim();
    }

    @Override
    public double range(int dim) {
        double min = this.min.coord[dim];
        double max = this.max.coord[dim];
        if (min == max)
            return 0;
        if ((min == NEGATIVE_INFINITY) || (max == Double.POSITIVE_INFINITY))
            return Double.POSITIVE_INFINITY;
        return (max - min);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null /*|| getClass() != o.getClass()*/) return false;

        HyperRectDouble r = (HyperRectDouble) o;
        return min.equals(r.min) && max.equals(r.max);
    }

    @Override
    public int hashCode() {
        int result = min.hashCode();
        result = 31 * result + max.hashCode();
        return result;
    }

    public String toString() {
		return min.equals(max) ? min.toString() : "(" + min + ',' + max + ')';
    }


    public static final class Builder<X extends HyperRectDouble> implements Function<X, HyperRegion> {

        @Override
        public X apply(X rect2D) {
            return rect2D;
        }

    }


}