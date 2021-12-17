package jcog.math;

import jcog.Str;
import org.HdrHistogram.AbstractHistogram;
import org.HdrHistogram.Histogram;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class HistogramMap<X> {

    final int digits = 4;
    final Map<X, Histogram> q = new HashMap();

    public void add(X x, float u) {
        add(x, Math.round(u * 100)); //HACK
    }
    public void add(X x, int v) {
        q.computeIfAbsent(x, (X) -> new Histogram(digits)).recordValue(v);
    }

    public void clear() {
        q.values().forEach(AbstractHistogram::reset);
    }

    public void print(PrintStream out) {
        q.forEach((k, v) -> out.println(k + "\t" + Str.histogramSummaryString(v)));
        out.println();
    }
}
