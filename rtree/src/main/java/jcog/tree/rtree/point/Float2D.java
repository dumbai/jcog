package jcog.tree.rtree.point;

import jcog.Util;

import static java.lang.Float.floatToIntBits;

public class Float2D implements HyperPoint, Comparable<Float2D> {
    public final float x;
    public final float y;

    public Float2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public final int dim() {
        return 2;
    }

    @Override
    public Float coord(int d) {
        return switch (d) {
            case 0 -> x;
            case 1 -> y;
            default -> throw new ArrayIndexOutOfBoundsException();
        };
    }

    @Override
    public double distance(HyperPoint p) {
        if (p == this) return 0;
        Float2D p2 = (Float2D) p;

        double dx = ((double)p2.x) - x;
        double dy = ((double)p2.y) - y;
        return  Math.sqrt(Util.sqr(dx) + Util.sqr(dy));
    }

    @Override
    public double distance(HyperPoint p, int d) {
        if (p == this) return 0;
        Float2D p2 = (Float2D) p;
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
        if (!(o instanceof Float2D float2D)) return false;

        if (Float.compare(float2D.x, x) != 0) return false;
        return Float.compare(float2D.y, y) == 0;
    }

    @Override
    public int hashCode() {
//        long temp = floatToIntBits(x);
//        int result = (int) (temp ^ (temp >>> 32));
//        temp = floatToIntBits(y);
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return Util.hashCombine(floatToIntBits(x), floatToIntBits(y));
    }

    @Override
    public int compareTo(Float2D o) {
        if (this == o) return 0;
        int a = Float.compare(x, o.x);
        if (a != 0) return a;
        return Float.compare(y, o.y);
    }

}