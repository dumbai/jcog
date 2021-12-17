package jcog.math;

public interface Discretize1D  {

    default void reset(int levels) {
        reset(levels, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    void reset(int levels, double min, double max);

    /** trains a value */
    void put(double value);

    void commit();

    /** calculates the (current) associated index of a value */
    int index(double value);

    /** estimates the (current) interval range of an index */
    double[] value(int v);

}
