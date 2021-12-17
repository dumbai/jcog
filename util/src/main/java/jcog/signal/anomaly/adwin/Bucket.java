package jcog.signal.anomaly.adwin;

/**
 * The basic bucket is used in the histogram to contain the basic informations over elements.
 */
class Bucket {

    private final double total;
    private final double variance;
    private final int numElements;

    Bucket(double total, double variance, int numElements) {
        this.total = total;
        this.variance = variance;
        this.numElements = numElements;
    }

    public double sum() {
        return total;
    }

    public double variance() {
        return variance;
    }

    public int size() {
        return numElements;
    }

    public double mean() {
        return total / numElements;
    }
}