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


import jcog.Util;
import jcog.math.v2;
import jcog.math.v3;
import jcog.tree.rtree.HyperRegion;
import jcog.tree.rtree.point.FloatND;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Function;

import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;

/**
 * Created by jcovert on 6/15/15.
 */

public class HyperRectFloat implements HyperRegion, Serializable, Comparable<HyperRectFloat> {

    public static final HyperRegion ALL_1 = HyperRectFloat.all(1);
    public static final HyperRegion ALL_2 = HyperRectFloat.all(2);
    public static final HyperRegion ALL_3 = HyperRectFloat.all(3);
    public static final HyperRegion ALL_4 = HyperRectFloat.all(4);

    public static final FloatND unbounded0 = new FloatND() {
        @Override
        public String toString() {
            return "*";
        }
    };

    public static final HyperRectFloat unbounded1 = new HyperRectFloat(1);
    public static final HyperRectFloat unbounded2 = new HyperRectFloat(2);
    public static final HyperRectFloat unbounded3 = new HyperRectFloat(3);
    public static final HyperRectFloat unbounded4 = new HyperRectFloat(4);
    public static final HyperRectFloat unbounded5 = new HyperRectFloat(5);


    public final FloatND min;
    public final FloatND max;

    private HyperRectFloat(int dimensionality) {
        min = FloatND.fill(dimensionality, POSITIVE_INFINITY);
        max = FloatND.fill(dimensionality, NEGATIVE_INFINITY);
    }

    public HyperRectFloat(FloatND p) {
        min = max = p;
    }

    public HyperRectFloat(v2 v) {
        this(v.x, v.y);
    }

    public HyperRectFloat(v3 v) {
        this(v.x, v.y, v.z);
    }

    public HyperRectFloat(float... point) {
        this(point, point);
    }

    public HyperRectFloat(float[] a, float[] b) {
        this(new FloatND(a), new FloatND(b));
    }


    public HyperRectFloat(FloatND a, FloatND b) {
        if (a!=b) {
            if (Arrays.compare(a.data, b.data)>0) {

                int dim = a.dim();

                float[] min = new float[dim];
                float[] max = new float[dim];

                float[] ad = a.data;
                float[] bd = b.data;
                for (int i = 0; i < dim; i++) {
                    float ai = ad[i];
                    float bi = bd[i];
                    min[i] = Math.min(ai, bi);
                    max[i] = Math.max(ai, bi);
                }
                a = new FloatND(min);
                b = new FloatND(max);
            }
        }

        this.min = a;
        this.max = b;
    }

    public static HyperRegion all(int i) {
        return new HyperRectFloat(FloatND.fill(i, NEGATIVE_INFINITY), FloatND.fill(i, POSITIVE_INFINITY));
    }

    public static HyperRectFloat cube(double[] center, float r) {
        return cube(Util.toFloat(center), r);
    }

    public static HyperRectFloat cube(float[] center, float r) {
        if (r == 0)
            return new HyperRectFloat(center); //point

        float[] a = center.clone(), b = center.clone();
        for (int i = 0; i < a.length; i++) {
            a[i] -= r;
            b[i] += r;
        }
        return new HyperRectFloat(a, b);
    }


    @Override
    public double cost() {
        float sigma = 1f;
        int dim = dim();
        for (int i = 0; i < dim; i++) {
            sigma *= rangeIfFinite(i, 1 /* HACK an infinite dimension can not be compared, so just ignore it */);
        }
        return sigma;
    }

    @Override
    public HyperRegion mbr(HyperRegion r) {
        if (this == r)
            return this;

        int dim = dim();
        float[] newMin = new float[dim], newMax = new float[dim];
        if (r instanceof HyperRectFloat x) {
            for (int i = 0; i < dim; i++) {
                newMin[i] = Math.min(min.data[i], x.min.data[i]);
                newMax[i] = Math.max(max.data[i], x.max.data[i]);
            }
        } else {

            for (int i = 0; i < dim; i++) {
                newMin[i] = Math.min(min.data[i], (float) r.coord(i, false));
                newMax[i] = Math.max(max.data[i], (float) r.coord(i, true));
            }
        }
        //point
        return Arrays.equals(newMin, newMax) ? new HyperRectFloat(newMin) : new HyperRectFloat(newMin, newMax);
    }


    @Override
    public double center(int dim) {
        return centerF(dim);
    }

    public float centerF(int dim) {
        float min = this.min.data[dim];
        float max = this.max.data[dim];
        if ((min == NEGATIVE_INFINITY) && (max == Float.POSITIVE_INFINITY))
            return 0;
        if (min == NEGATIVE_INFINITY)
            return max;
        if (max == Float.POSITIVE_INFINITY)
            return min;

        return (max + min) / 2f;
    }

    public FloatND center() {
        int dim = dim();
        float[] c = new float[dim];
        for (int i = 0; i < dim; i++) {
            c[i] = centerF(i);
        }
        return new FloatND(c);
    }


    @Override
    public int dim() {
        return min.dim();
    }

    @Override
    public double coord(int dimension, boolean maxOrMin) {
        return (maxOrMin ? max : min).data[dimension];
    }

    @Override
    public double range(int dim) {
        float min = this.min.data[dim];
        float max = this.max.data[dim];
        if (min == max)
            return 0;
        if ((min == NEGATIVE_INFINITY) || (max == Float.POSITIVE_INFINITY))
            return Float.POSITIVE_INFINITY;
        return (max - min);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HyperRectFloat r)) return false;
        return min.equals(r.min) && max.equals(r.max);
    }

    @Override
    public int compareTo(HyperRectFloat o) {
        if (this == o) return 0;
        int a = min.compareTo(o.min);
        if (a != 0) return a;
        return max.compareTo(o.max);
    }

    @Override
    public int hashCode() {
        int result = min.hashCode();
        result = 31 * result + max.hashCode();
        return result;
    }

    public String toString() {
        return min.equals(max) ? min.toString() : "(" +
            min +
            ',' +
            max +
            ')';
    }

    public HyperRectFloat scale(float... s) {
        int d = dim();
        assert(s.length == d);

        float[] x = new float[d];
        float[] min = new float[d], max = new float[d];
        for (int i = 0; i < x.length; i++) {
            float center = (float) center(i);
            float rangeHalf = (float)(range(i)*s[i])/2;
            min[i] = center - rangeHalf;
            max[i] = center + rangeHalf;
        }
        return new HyperRectFloat(min, max);
    }


    @Deprecated
    public static final class Builder<X extends HyperRectFloat> implements Function<X, HyperRegion> {

        @Override
        public X apply(X rect2D) {
            return rect2D;
        }

    }


}