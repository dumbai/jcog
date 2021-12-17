package jcog.signal.anomaly;

import jcog.Util;
import org.eclipse.collections.api.block.procedure.primitive.FloatProcedure;

import java.util.Arrays;

/**
 *
 * Histogram-based Anomaly Estimation for Uniform Interval
 *
 * https://www.dfki.de/web/forschung/publikationen/renameFileForDownload?filename=HBOS-KI-2012.pdf&file_id=uploads_1716
 * https://github.com/Markus-Go/rapidminer-anomalydetection/blob/master/src/de/dfki/madm/anomalydetection/evaluator/statistical_based/HistogramEvaluator.java
 */
public class HistogramAnomaly implements FloatProcedure /*StreamingAnomaly*/ {
    long count = 0;
//    int max = 0;
    final int[] bin;

    public HistogramAnomaly(int bins) {
        this.bin = new int[bins];
    }

    public float acceptAndGetAnomaly(float v) {
        int b = bin(v);
        _value(b);
        return _anomaly(b);
    }

    public float anomaly(float v) {
        return _anomaly(bin(v));
    }

    @Override public void value(float v) {
        _value(bin(v));
    }

    private float _anomaly(int bin) {
        double score = ((double)this.bin[bin]) / count;
        //return (float) Math.log10(1/score);
        return (float)(1-score);
    }

    private int bin(float v) {
        return Util.bin(v, bins());
    }

    private /*synchronized*/ void _value(int bin) {
        if (++count >= Integer.MAX_VALUE)
            reset(); //HACK
//        if (max >= Integer.MAX_VALUE-1)
//            reset();

        //max = Math.max(max, ++this.bin[bin]);
        this.bin[bin]++;
    }

//    public long total() {
//        return count;
//    }

    public int bins() {
        return bin.length;
    }

    public void reset() {
        //max = 0;
        count = 0;
        Arrays.fill(bin, 0);
    }
}