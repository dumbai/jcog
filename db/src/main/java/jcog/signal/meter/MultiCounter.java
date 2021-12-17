package jcog.signal.meter;

import jcog.TODO;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

/** not thread safe
 *  TODO */
public class MultiCounter implements Metered {

    long samples = 0;
    final long[] hits;
    final String[] categories;

    public MultiCounter(String... categories) {
        assert(categories.length > 1);
        this.categories = categories;
        this.hits = new long[categories.length];
    }

    public void clear() {
        samples = 0; Arrays.fill(hits, 0);
    }

    public float[] histogram() {
        int n = categories.length;
        float[] f = new float[n];
        double samplesF = samples;
        for (int i = 0; i < n; i++)
            f[i] = (float) (hits[i]/samplesF);
        return f;
    }

    /** category may be -1, in which case a sample his recorded but no particular hit is */
    public void hit(int category) {
        samples++;
        if (category >= 0) hits[category]++;
    }


    @Override
    public Map<String, Consumer<MeterReader>> metrics() {
        throw new TODO();
    }
}
