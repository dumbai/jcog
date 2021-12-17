///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package jcog.signal.meter.event;
//
//import jcog.Util;
//import jcog.math.RecycledSummaryStatistics;
//import jcog.signal.meter.Metered;
//
///**
// * Measures the period between hit() calls to determine frequency-related
// * statistics of hit intervals.
// * millisecond resolution performs better than nanoseconds but both are
// * converted to a double value (nanosecond unixtime) for calculation.
// * <p>
// * the results can be returned either as period duration (sec) or frequency (1/period).
// */
//public class PeriodMeter extends Metered.MapMetrics {
//
//    final StatisticalSummary stat;
//    private final String id;
//    private long prev;
//    private final boolean frequency;
//
//    public PeriodMeter(String id, int window) {
//        this(id, window, false);
//    }
//
//    public PeriodMeter(String id, int window, boolean asFrequency) {
//        super(id, true, ".min", ".max", ".mean", ".stddev");
//
//        this.id = id;
//        this.stat = window > 0 ? new DescriptiveStatistics(window) : new RecycledSummaryStatistics();
//        this.frequency = asFrequency;
//
//        this.prev = now();
//
//        clear();
//    }
//
//    public static long now() {
//        return System.nanoTime();
//    }
//
//    public void hit() {
//        hit(1);
//    }
//
//    public void hit(int n) {
//        long now = now();
//        if (prev == prev) {
//            long dt = now - prev;
//            for (int i = 0; i < n; i++)
//                hitNano(dt);
//        }
//        prev = now;
//    }
//
//    /**
//     * record a specific duration
//     */
//    public void hit(double dt) {
//        if (stat instanceof DescriptiveStatistics)
//            ((DescriptiveStatistics) stat).addValue(dt);
//        else
//            ((RecycledSummaryStatistics) stat).accept(dt);
//    }
//
//    public void hitNano(long nanos) {
//        hit(nanos / 1.0E9);
//    }
//    public void hitNano(double nanos) {
//        hit(nanos / 1.0E9);
//    }
//
//    public void clear() {
//        if (stat instanceof DescriptiveStatistics)
//            ((DescriptiveStatistics) stat).clear();
//        else
//            ((RecycledSummaryStatistics) stat).clear();
//    }
//
//    @Override
//    public Double getValue(Object key, int index) {
//        if (stat.getN() == 0) return null;
//        switch (index) {
//            case 0:
//                return f(stat.getMin());
//            case 1:
//                return f(stat.getMax());
//            case 2:
//                return f(stat.getMean());
//            case 4:
//                return stat.getStandardDeviation();
//        }
//        return null;
//    }
//
//    protected double f(double period) {
//        if (frequency) {
//            if (period == 0) return Double.POSITIVE_INFINITY;
//            return 1.0 / period;
//        }
//        return period;
//    }
//
//
//    public double mean() {
//        return this.stat.getMean();
//    }
//
//    public double sum() {
//        return this.stat.getSum();
//    }
//
//    @Override
//    public String toString() {
//        return toString(-1);
//    }
//
//    public String toString(int decimals) {
//        return id + '{' +
//                Util.secondStr(stat.getMean(), decimals) + "AVG" +
//                    ((stat instanceof DescriptiveStatistics) ?
//                        ("") :
//                        (" x " + stat.getN() + "~= " + Util.secondStr(stat.getSum(), decimals)))
//                 + '}';
//    }
//
//    public String toStringMicro() {
//        return toString(6);
//    }
//
//    public long getN() {
//        return stat.getN();
//    }
//}
