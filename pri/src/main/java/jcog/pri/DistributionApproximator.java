package jcog.pri;

import java.util.Random;

/** TODO other implementations of this */
public abstract class DistributionApproximator {

    public abstract int sampleInt(Random r);

    public abstract int sampleInt(float uniform);

    public abstract float sample(float q);

    public abstract void clear();

    public abstract void start(@Deprecated int bins);

    @Deprecated public abstract void commit(float lo, float hi, int outBins);

    abstract public void commitFlat(float lo, float hi);

    /** v >= 0 */
    public abstract void accept(float pri);
}