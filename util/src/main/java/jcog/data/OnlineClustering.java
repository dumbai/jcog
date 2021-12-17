package jcog.data;

public interface OnlineClustering<C extends Centroid> {

    /** trains and returns nearest centroid */
    C put(double[] x);

    /** nearest centroid */
    C get(double[] x);

    Iterable<C> centroids();

}
