package jcog.tree.rtree.point;

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
import jcog.tree.rtree.rect.RectDouble;

import java.util.function.Function;

/**
 * Created by jcovert on 6/15/15.
 */
public class Double2D implements HyperPoint, Comparable<Double2D> {
    public final double x;
    public final double y;

    public Double2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int dim() {
        return 2;
    }

    @Override
    public Double coord(int d) {
        return switch (d) {
            case 0 -> x;
            case 1 -> y;
            default -> throw new ArrayIndexOutOfBoundsException();
        };
    }

    @Override
    public double distance(HyperPoint p) {
        Double2D p2 = (Double2D) p;

        double dx = p2.x - x;
        double dy = p2.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public double distance(HyperPoint p, int d) {
        Double2D p2 = (Double2D) p;
        return switch (d) {
            case 0 -> Math.abs(p2.x - x);
            case 1 -> Math.abs(p2.y - y);
            default -> throw new ArrayIndexOutOfBoundsException();
        };
    }

    @Override
    public String toString() {
        return "<" + x + ',' + y + '>';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Double2D double2D)) return false;

        if (Double.compare(double2D.x, x) != 0) return false;
        return Double.compare(double2D.y, y) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(x);
        int result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public int compareTo( Double2D o) {
        int a = Double.compare(x, o.x);
        if (a != 0) return a;
        return Double.compare(y, o.y);
    }

    public static final class Builder implements Function<Double2D, HyperRegion> {

        @Override
        public HyperRegion apply(Double2D point) {
            return new RectDouble(point);
        }







    }
}