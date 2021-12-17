package jcog.tree.rtree.point;


import java.io.Serializable;
import java.util.Arrays;


/**
 * Created by me on 12/21/16.
 */
public class LongND implements HyperPoint, Serializable, Comparable<LongND> {

    public final long[] coord;
    private final int hash;

    public LongND(LongND copy) {
        this(copy.coord.clone());
    }

    public LongND(long... coord) {
        this.coord = coord;
        this.hash = Arrays.hashCode(coord);
    }

    public static LongND fill(int dims, long value) {
        long[] a = new long[dims];
        Arrays.fill(a, value);
        return new LongND(a);
    }

    @Override
    public int dim() {
        return coord.length;
    }

    @Override
    public Long coord(int d) {
        return coord[d];
    }

    @Override
    public double distance(HyperPoint h) {
        LongND p = (LongND) h;
        double sumSq = 0;
        for (int i = 0; i < coord.length; i++) {
            long x = coord[i];
            long y = p.coord[i];
            long xMinY = x - y;
            sumSq += xMinY * xMinY;
        }
        return Math.sqrt(sumSq);
    }

    @Override
    public double distance(HyperPoint p, int i) {
        return Math.abs(coord[i] - ((LongND) p).coord[i]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LongND longND)) return false;

        return hash == longND.hashCode() && Arrays.equals(coord, longND.coord);







    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return '(' + Arrays.toString(coord) + ')';
    }


    @Override
    public int compareTo(LongND o) {
        return Arrays.compare(coord, o.coord);
    }
}