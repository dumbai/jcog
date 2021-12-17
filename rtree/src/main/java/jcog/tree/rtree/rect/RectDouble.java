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
import jcog.tree.rtree.HyperRegion;
import jcog.tree.rtree.Spatialization;
import jcog.tree.rtree.point.Double2D;

/**
 * Created by jcovert on 6/15/15.
 */
public class RectDouble implements HyperRegion, Comparable<RectDouble> {
    public final Double2D min;
    public final Double2D max;

    public RectDouble(Double2D p) {
        min = p;
        max = p;
    }

    public RectDouble(double x1, double y1, double x2, double y2) {
        if (x2 < x1) {
            double t = x2;
            x2 = x1;
            x1 = t;
        }
        if (y2 < y1) {
            double t = y2;
            y2 = y1;
            y1 = t;
        }

        min = new Double2D(x1, y1);
        max = new Double2D(x2, y2);
    }

    public RectDouble(Double2D p1, Double2D p2) {
        double minX;

		minX = Util.min(p1.x, p2.x);
        double maxX = p2.x;

        double minY;
		minY = Util.min(p1.y, p2.y);
        double maxY = p2.y;

        min = new Double2D(minX, minY);
        max = new Double2D(maxX, maxY);
    }


    @Override
    public RectDouble mbr(HyperRegion r) {
        RectDouble r2 = (RectDouble) r;
        double minX = Util.min(min.x, r2.min.x);
        double minY = Util.min(min.y, r2.min.y);
        double maxX = Util.max(max.x, r2.max.x);
        double maxY = Util.max(max.y, r2.max.y);

        return new RectDouble(minX, minY, maxX, maxY);

    }

    @Override
    public int dim() {
        return 2;
    }

    public Double2D center() {
        double dx = center(0);
        double dy = center(1);

        return new Double2D(dx, dy);
    }

    @Override
    public double center(int d) {
        if (d == 0) {
            double minX = min.x;
            return minX + (max.x - minX) / 2.0;
        } else {
            assert (d == 1);
            double minY = min.y;
            return minY + (max.y - minY) / 2.0;
        }
    }


    @Override
    public double coord(int dimension, boolean maxOrMin) {
        Double2D e = (maxOrMin ? max : min);
        assert(dimension==0 || dimension==1);
        return dimension==0 ? e.x : e.y;
    }


    @Override
    public double range(int dim) {
        return switch (dim) {
            case 0 -> max.x - min.x;
            case 1 -> max.y - min.y;
            default -> throw new IllegalArgumentException("Invalid dimension");
        };
    }

    @Override
    public boolean contains( HyperRegion r) {
        RectDouble r2 = (RectDouble) r;

        return min.x <= r2.min.x &&
                max.x >= r2.max.x &&
                min.y <= r2.min.y &&
                max.y >= r2.max.y;

    }

    @Override
    public boolean intersects(HyperRegion r) {
        RectDouble r2 = (RectDouble) r;

        return !((min.x > r2.max.x) || (r2.min.x > max.x) ||
                (min.y > r2.max.y) || (r2.min.y > max.y));
    }

    @Override
    public double cost() {
        double dx = max.x - min.x;
        double dy = max.y - min.y;
        return Math.abs(dx * dy);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RectDouble rect2D = (RectDouble) o;

        return Util.equals(min.x, rect2D.min.x, Spatialization.EPSILON) &&
                Util.equals(max.x, rect2D.max.x, Spatialization.EPSILON) &&
                Util.equals(min.y, rect2D.min.y, Spatialization.EPSILON) &&
                Util.equals(max.y, rect2D.max.y, Spatialization.EPSILON);
    }

    @Override
    public int hashCode() {
        int result = min.hashCode();
        result = 31 * result + max.hashCode();
        return result;
    }

    public String toString() {

        return "(" +
                min.x +
                ',' +
                min.y +
                ')' +
                ' ' +
                '(' +
                max.x +
                ',' +
                max.y +
                ')';
    }

    @Override
    public int compareTo(RectDouble o) {
        int a = min.compareTo(o.min);
        if (a != 0) return a;
        return max.compareTo(o.max);
    }

}