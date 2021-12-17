package jcog.tree.rtree.point;

/**
 * Created by me on 12/2/16.
 */
public class Double1D implements HyperPoint {
    private final double x;

    public Double1D(double X) {
        this.x = X;
    }

    @Override
    public final int dim() {
        return 1;
    }

    @Override
    public Double coord(int d) {
        return x;
    }

    @Override
    public double distance(HyperPoint p) {
        return Math.abs(x - ((Double1D) p).x);
    }

    @Override
    public double distance(HyperPoint p, int d) {
        return distance(p);
    }
}
