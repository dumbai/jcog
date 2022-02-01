package jcog.math;

public interface Discretize1D  {

    public Discretize1D BooleanDiscretization = new Discretize1D() {

        @Override
        public void reset(int levels, double min, double max) {}

        @Override
        public void put(double value) {}

        @Override
        public void commit() {}

        @Override
        public int index(double value) {
            return value > 0.5 ? 1 : 0;
        }

        @Override
        public double[] value(int v) {
            if (v == 0) return new double[] { 0, 0.5 };
            else return new double[] { 0.5, 1 };
        }
    };
    String[] BooleanLabels = new String[]{ "false", "true"};

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