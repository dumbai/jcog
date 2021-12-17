package jcog;

import com.google.common.escape.Escapers;
import org.HdrHistogram.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utilities for process String and Text creation, transform, input/output, etc
 */
public enum Str {
    ;

    static final String NaN2 = "NaN";
    static final String NaN4 = "NNaaNN";


    private static final Escapers.Builder quoteEscaper = Escapers.builder().addEscape('\"', "\\\"");


    /**
     * @author http:
     */
    public static int levenshteinDistance(CharSequence a, CharSequence b) {
        if (a.equals(b)) return 0;

        int len0 = a.length() + 1;
        int len1 = b.length() + 1;
        int[] cost = new int[len0];
        for (int i = 0; i < len0; i++) {
            cost[i] = i;
        }
        int[] newcost = new int[len0];
        for (int j = 1; j < len1; j++) {
            newcost[0] = j;
            char bj = b.charAt(j - 1);
            for (int i = 1; i < len0; i++) {
                int match = (a.charAt(i - 1) == bj) ? 0 : 1;
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;

                int c = cost_insert;
                if (cost_delete < c) c = cost_delete;
                if (cost_replace < c) c = cost_replace;

                newcost[i] = c;
            }
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }
        return cost[len0 - 1];
    }

    public static String n1(float x) {
        if (x != x) return "NaN";
        //return oneDecimal.get().format(x);
        return DoubleFormatUtil.formatDoubleFast(x, 1);
    }

    public static String n3(float x) {
        if (x != x) return "NaN";
        //return threeDecimal.get().format(x);
        return DoubleFormatUtil.formatDoubleFast(x, 3); //fourDecimal.get().format(x);

    }

//    public static String n3(double x) {
//        return threeDecimal.get().format(x);
//    }

    public static String n4(float x) {
        return x == x ? DoubleFormatUtil.formatDoubleFast(x, 4) : NaN4;
        //fourDecimal.get().format(x);
    }

    public static String n4(double x) {
        return x == x ? DoubleFormatUtil.formatDoubleFast(x, 4) : NaN4;
        //return fourDecimal.get().format(x);
    }

    public static long hundredths(float d) {
        return (long) ((d * 100.0f + 0.5f));
    }

    public static int tens(float d) {
        return (int) ((d * 10.0f + 0.5f));
    }

    public static String n2(float x) {
        if (x != x)
            return NaN2;


        if ((x < 0) || (x > 1.0f)) {
            //return twoDecimal.get().format(x);
            return DoubleFormatUtil.formatDoubleFast(x, 2); //fourDecimal.get().format(x);
        }

        int hundredths = (int) hundredths(x);
        switch (hundredths) {

            case 100:
                return "1.0";
            case 99:
                return ".99";
            case 90:
                return ".90";
            case 50:
                return ".50";
            case 0:
                return "0.0";
        }

        if (hundredths > 9) {
            int tens = hundredths / 10;
            return new String(new char[]{
                    '.', (char) ('0' + tens), (char) ('0' + hundredths % 10)
            });
        } else {
            return new String(new char[]{
                    '.', '0', (char) ('0' + hundredths)
            });
        }
    }

//    /**
//     * 1 character representing a 1 decimal of a value between 0..1.0;
//     * representation; 0..9
//     */
//    public static char n1char(float x) {
//        int i = tens(x);
//        if (i >= 10)
//            i = 9;
//        return (char) ('0' + i);
//    }

//    public static int compare(CharSequence s, CharSequence t) {
//        if ((s instanceof String) && (t instanceof String)) {
//            return ((String) s).compareTo((String) t);
//        }
//        if ((s instanceof CharBuffer) && (t instanceof CharBuffer)) {
//            return ((CharBuffer) s).compareTo((CharBuffer) t);
//        }
//
//        int i = 0;
//
//        int sl = s.length();
//        int tl = t.length();
//
//        while (i < sl && i < tl) {
//            char a = s.charAt(i);
//            char b = t.charAt(i);
//
//            int diff = a - b;
//
//            if (diff != 0)
//                return diff;
//
//            i++;
//        }
//
//        return sl - tl;
//    }

    public static String n2(double p) {
        return p == p ? n2((float) p) : NaN2;
    }

    /**
     * character to a digit, or -1 if it wasnt a digit
     */
    public static int i(char c) {
        return ((c >= '0') && (c <= '9')) ? (c - '0') : -1;
    }

    public static int i(CharSequence s) throws NumberFormatException {
        return s instanceof String ?  i((String)s) : i(s.toString());
    }

    /**
     * fast parse an int under certain conditions, avoiding Integer.parse if possible
     */
    public static int i(String s) throws NumberFormatException {

        switch (s.length()) {
            case 0 -> throw new UnsupportedOperationException();
            case 1 -> {
                char c = s.charAt(0);
                int i = i(c);
                if (i != -1) return i;
            }
            case 2 -> {
                int dig1 = i(s.charAt(1));
                if (dig1 != -1) {
                    int dig10 = i(s.charAt(0));
                    if (dig10 != -1)
                        return dig10 * 10 + dig1;
                }
            }
        }

        return Integer.parseInt(s);

    }

    /**
     * fast parse an int under certain conditions, avoiding Integer.parse if possible
     */
    public static long l(String s) throws NumberFormatException {
        int sl = s.length();
        switch (sl) {
            case 1 -> {
                char c = s.charAt(0);
                int i = i(c);
                if (i != -1) return i;
            }
            case 2 -> {
                int dig1 = i(s.charAt(1));
                if (dig1 != -1) {
                    int dig10 = i(s.charAt(0));
                    if (dig10 != -1)
                        return dig10 * 10 + dig1;
                }
            }
        }
        return Long.parseLong(s);
    }

    /**
     * fast parse a non-negative int under certain conditions, avoiding Integer.parse if possible
     * TODO parse negative values with a leading '-'
     */
    public static int i(String s, int ifMissing) {
        switch (s.length()) {
            case 0:
                return ifMissing;
            case 1:
                return i1(s, ifMissing);
            case 2:
                return i2(s, ifMissing);
            case 3:
                return i3(s, ifMissing);
            default:


                for (int i = 0; i < s.length(); i++)
                    if (i(s.charAt(i)) == -1)
                        return ifMissing;


                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    return ifMissing;

                }
        }
    }

    public static String i(byte[] b, int radix) {
        return i(b, 0, b.length, radix);
    }

    public static String i(byte[] b, int from, int to, int radix) {
        assert (radix == 16); //TODO other options
        byte[] c = new byte[(to - from) * 2];
        int i = 0;
        for (int j = from; j < to; j++) {
            byte x = b[j];
            c[i++] = (byte) ((x / radix) + '0');
            c[i++] = (byte) ((x % radix) + '0');
        }
        return new String(c);
    }

    public static int i(String s, int offset, int ifMissing) {
        int sl = s.length() - offset;
        if (sl <= 0)
            return ifMissing;

        switch (sl) {
            case 1:
                return i1(s, offset, ifMissing);
            case 2:
                return i2(s, offset, ifMissing);
            case 3:
                return i3(s, offset, ifMissing);
            default:
                try {
                    return Integer.parseInt(offset != 0 ? s.substring(offset) : s);
                } catch (NumberFormatException e) {
                    return ifMissing;
                }
        }
    }

    private static int i3(String s, int ifMissing) {
        return i3(s, 0, ifMissing);
    }

    private static int i3(String s, int offset, int ifMissing) {
        int dig100 = i(s.charAt(offset));
        if (dig100 == -1) return ifMissing;

        int dig10 = i(s.charAt(offset + 1));
        if (dig10 == -1) return ifMissing;

        int dig1 = i(s.charAt(offset + 2));
        return dig1 == -1 ? ifMissing : dig100 * 100 + dig10 * 10 + dig1;

    }

    private static int i2(String s, int ifMissing) {
        return i2(s, 0, ifMissing);
    }

    private static int i2(String s, int offset, int ifMissing) {
        int dig10 = i(s.charAt(offset));
        if (dig10 == -1) return ifMissing;

        int dig1 = i(s.charAt(offset + 1));
        return dig1 == -1 ? ifMissing : dig10 * 10 + dig1;

    }

    private static int i1(String s, int ifMissing) {
        return i1(s, 0, ifMissing);
    }

    private static int i1(String s, int offset, int ifMissing) {
        int dig1 = i(s.charAt(offset));
        return dig1 == -1 ? ifMissing : dig1;
    }

    public static float f(String s) {
        return f(s, Float.NaN);
    }

    /**
     * fast parse for float, checking common conditions
     */
    public static float f(String s, float ifMissing) {

        switch (s) {
            case "0":
            case "0.00":
                return 0;
            case "1":
            case "1.00":
                return 1.0f;
            case "0.90":
            case "0.9":
                return 0.9f;
            case "0.5":
                return 0.5f;
            default:
                try {
                    return Float.parseFloat(s);
                } catch (NumberFormatException e) {
                    return ifMissing;
                }
        }

    }

    public static float f(String s, float min, float max) {
        float x = f(s, Float.NaN);
        if ((x < min) || x > max)
            return Float.NaN;
        return x;
    }

    public static String arrayToString(Object... signals) {
        if (signals == null) return "";
        int slen = signals.length;
        if (slen > 1)
            return Arrays.toString(signals);
        else if (slen > 0)
            return signals[0].toString();
        else
            return "";
    }

    public static CharSequence n(double x, int decimals) {
        return x == x ? DoubleFormatUtil.formatDoubleFast(x, decimals) : "NaN";
    }

    public static CharSequence n(float x, int decimals) {
        return switch (decimals) {
            case 1 -> n1(x);
            case 2 -> n2(x);
            case 3 -> n3(x);
            case 4 -> n4(x);
            default -> x == x ? DoubleFormatUtil.formatDoubleFast(x, decimals) : "NaN";
        };


    }

    public static int countRows(String s, char x) {
        int bound = s.length();
        long count = 0L;
        for (int i = 0; i < bound; i++) {
            if (s.charAt(i) == x)
                count++;
        }
        return (int) count;
    }

    public static int countCols(String next) {
        int cols = 0;
        int n = 0, nn = 0;
        while ((nn = next.indexOf('\n', n)) != -1) {
            cols = Math.max(cols, nn - n);
            n = nn + 1;
        }
        return cols;
    }

    public static String n2(float... v) {
        assert (v.length > 0);
        StringBuilder sb = new StringBuilder(v.length * 4 + 2 /* approx */);
        int s = v.length;
        for (int i = 0; i < s; i++) {
            sb.append(n2(v[i]));
            if (i != s - 1) sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * prints an array of numbers separated by tab, suitable for a TSV line
     */
    public static String n4(double... v) {
        StringBuilder sb = new StringBuilder(v.length * 6 + 2 /* approx */);
        int s = v.length;
        for (int i = 0; i < s; i++) {
            sb.append(n4(v[i]));
            if (i != s - 1) sb.append('\t');
        }
        return sb.toString();
    }

    /**
     * prints an array of numbers separated by tab, suitable for a TSV line
     */
    public static String n4(float... v) {
        StringBuilder sb = new StringBuilder(v.length * 6 + 2 /* approx */);
        int s = v.length;
        for (int i = 0; i < s; i++) {
            sb.append(n4(v[i]));
            if (i != s - 1) sb.append('\t');
        }
        return sb.toString();
    }

    /**
     * prints an array of numbers separated by tab, suitable for a TSV line
     */
    public static String n2(byte... v) {
        int s = v.length;
        return IntStream.range(0, s).mapToObj(i -> Integer.toHexString(Byte.toUnsignedInt(v[i])) + ' ').collect(Collectors.joining());
    }

    /**
     * Return formatted Date String: yyyy.MM.dd HH:mm:ss
     * Based on Unix's time() input in seconds
     *
     * @param timestamp seconds since start of Unix-time
     * @return String formatted as - yyyy.MM.dd HH:mm:ss
     * from: https:
     */
    public static String dateStr(long timestamp) {
        Date date = new Date(timestamp * 1000);
        return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(date);
    }

    /**
     * string repr of an amount of nanoseconds
     * from: https:
     */
    public static String timeStr(double ns) {
        assert (Double.isFinite(ns));
        boolean neg = ns < 0;
        return (neg ? "-" : "") + _timeStr(Math.abs(ns));
    }


    private static String _timeStr(double ns) {
        if (ns < 1000) return n4(ns) + "ns";
        if (ns < 1_000_000) return n4(ns / 1_000d) + "us";
        if (ns < 1_000_000_000) return n4(ns / 1_000_000d) + "ms";

        if (ns < 1_000_000_000_000d) return n2(ns / 1_000_000_000d) + 's';
        long sec = Math.round(ns / 1_000_000_000d);
        if (sec < 5 * 60) return (sec / 60) + "m" + (sec % 60) + 's';
        long min = sec / 60;
        if (min < 60) return min + "m";
        long hour = min / 60;
        if (min < 24 * 60) return hour + "h" + (min % 60) + 'm';
        long day = hour / 24;
        return day + "d" + (day % 24) + 'h';
    }

    /**
     * from: https:
     */
    public static String byteCountString(long size) {
        if (size < 2 * (1L << 10)) return size + "b";
        if (size < 2 * (1L << 20)) return String.format("%dKb", size / (1L << 10));
        if (size < 2 * (1L << 30)) return String.format("%dMb", size / (1L << 20));
        return String.format("%dGb", size / (1L << 30));
    }

    /**
     * from: https:
     */
    public static String repeat(String s, int n) {

        if (s.length() == 1) {
            char c = s.charAt(0);
            if (c < 0xff) {
                byte[] bb = new byte[n];
                Arrays.fill(bb, (byte) c);
                return new String(bb);
            }
        }

        return s.repeat(Math.max(0, n));
    }

    /**
     * pad with leading zeros
     * TODO can be made faster
     */
    public static String iPad(long v, int digits) {
        String s = String.valueOf(v);
        while (s.length() < digits)
            s = ' ' + s;
        return s;
    }

    public static String n2percent(float rate) {
        return n2(100f * rate) + '%';
    }

    public static void histogramDecodeExact(AbstractHistogram h, String header, int linearStep, BiConsumer<String, Object> x) {
        int digits = (int) (1 + Math.log10(h.getMaxValue()));
        for (HistogramIterationValue p : h.linearBucketValues(linearStep)) {
            x.accept(header + " [" +
                            iPad(p.getValueIteratedFrom(), digits) + ".." + iPad(p.getValueIteratedTo(), digits) + ']',
                    p.getCountAddedInThisIterationStep());
        }
    }

    public static void histogramDecode(AbstractHistogram h, String header, int steps, BiConsumer<String, Long> x) {
        long min = h.getMinValue(), max = h.getMaxValue();
        char[] order = {'a'};
        long unitsPerBucket = Math.max(1, Math.round(((double) (max - min)) / steps));
        for (var p : h.linearBucketValues(unitsPerBucket)) {
            long count = p.getCountAddedInThisIterationStep();
            if (count > 0) {
                x.accept(header + ' ' + (order[0]++) +
                                '[' + n4(p.getValueIteratedFrom()) + ".." + n4(p.getValueIteratedTo()) + ']',
                        count);
            }
        }
    }

    public static void histogramDecode(DoubleHistogram h, String header, int steps, BiConsumer<String, Object> x) {
        double min = h.getMinValue(), max = h.getMaxValueAsDouble();
        char[] order = {'a'};
        for (DoubleHistogramIterationValue p : h.linearBucketValues((max-min)/steps)) {
            x.accept(header + ' ' + (order[0]++) +
                            '[' + n4(p.getValueIteratedFrom()) + ".." + n4(p.getValueIteratedTo()) + ']',
                    p.getCountAddedInThisIterationStep());
        }
    }

    public static void histogramDecodePercentiles(AbstractHistogram h, String header, BiConsumer<String, Object> x) {
        for (HistogramIterationValue p : h.percentiles(1)) {
            x.accept(header + " [" +
                            p.getValueIteratedFrom() + ".." + p.getValueIteratedTo() + ']',
                    p.getCountAddedInThisIterationStep());
        }
    }

    public static String histogramString(AbstractHistogram h, boolean percentiles) {
        StringBuilder sb = new StringBuilder(256);
        histogramPrint(h, percentiles, sb);
        return sb.toString();
    }

    public static void histogramPrint(AbstractHistogram h, Appendable out) {
        histogramPrint(h, true, out);
    }

    public static void histogramPrint(AbstractHistogram h, boolean percentiles, Appendable out) {
        if (h instanceof AtomicHistogram)
            h = h.copy();

        try {
            out.append("[n=")
                    .append(String.valueOf(h.getTotalCount()))
                    .append(" avg=").append(n4(h.getMean()))
                    .append(", min=").append(n4(h.getMinValue()))
                    .append(", max=").append(n4(h.getMaxValue()))
                    .append(", stdev=").append(n4(h.getStdDeviation()))
                    .append(']');

            if (percentiles) {

                out.append('\n');

                histogramDecode(h, "", 5, (label, value) -> {
                    try {
                        out.append(label).append(' ').append(String.valueOf(value)).append('\n');
                    } catch (IOException e) {
                        throw new WTF(e);
                    }
                });
            }
        } catch (IOException e) {
            throw new WTF(e);
        }
    }


    public static String quote(String s) {
        int length = s.length();

        if (length == 0)
            return "\"\"";

        if (s.charAt(0) == '\"' && s.charAt(length - 1) == '\"') {
            if (length == 1) {
                s = "\"\\\"\"";
            } else {

            }
        } else {
            s = ('"' + quoteEscaper.build().escape(s) + '"');
        }

        return s;
    }


    public static String unquote(String x) {
        while (true) {
            int len = x.length();
            if (len > 0 && x.charAt(0) == '\"' && x.charAt(len - 1) == '\"') {
                x = x.substring(1, len - 1);
            } else {
                return x;
            }
        }
    }

    /**
     * 2 decimal representation of values between 0 and 1. only the tens and hundredth
     * decimal point are displayed - not the ones, and not a decimal point.
     * for compact display.
     * if the value=1.0, then 'aa' is the result
     */
    public static String n2u(float x) {
        if ((x < 0) || (x > 1)) throw new RuntimeException("values >=0 and <=1");
        int hundreds = (int) hundredths(x);
        return x == 100 ? "aa" : hundreds < 10 ? "0" + hundreds : Integer.toString(hundreds);
    }

    /**
     * returns lev distance divided by max(a.length(), b.length()
     */
    public static float levenshteinFraction(CharSequence a, CharSequence b) {
        int len = Math.max(a.length(), b.length());
        return len == 0 ? 0f : levenshteinDistance(a, b) / ((float) len);
    }

    public static void indent(int amount) {
        for (int i = 0; i < amount; i++)
            System.out.print(' ');
    }

    public static String histogramSummaryString(Histogram v) {
        return n4(v.getMean()) + "x" + v.getTotalCount();
    }


    /** https://www.mathsisfun.com/data/confidence-interval.html */
    public static String toString(Histogram h, boolean sum, boolean Mean, boolean count) {
        double mean = h.getMean();
        double Z = 0.9; //90%
        long N = h.getTotalCount();

        //total time, estimate
        StringBuilder s = new StringBuilder(128);
        if (sum) {
            s.append(timeStr(mean * N));
            s.append('=');
        }
        if (Mean) {
            s.append(timeStr(Math.round(mean)));

            double confInterval = Z * h.getStdDeviation() / Math.sqrt(N) / mean * 100;
            s.append('±').append(n2(confInterval)).append('%');
        }
        if (count) {
            s.append('x').append(N);
        }
        return s.toString();
        //+ Texts.histogramString(timeCopy, false);
    }

    /** HACK TODO better
     * @return*/
    public static String n2(double[] x) {
        return n2(Util.toFloat(x));
    }


    /**
     * This class implements fast, thread-safe format of a double value
     * with a given number of decimal digits.
     * <p>
     * The contract for the format methods is this one:
     * if the source is greater than or equal to 1 (in absolute value),
     * use the decimals parameter to define the number of decimal digits; else,
     * use the precision parameter to define the number of decimal digits.
     * <p>
     * A few examples (consider decimals being 4 and precision being 8):
     * <ul>
     * <li>0.0 should be rendered as "0"
     * <li>0.1 should be rendered as "0.1"
     * <li>1234.1 should be rendered as "1234.1"
     * <li>1234.1234567 should be rendered as "1234.1235" (note the trailing 5! Rounding!)
     * <li>1234.00001 should be rendered as "1234"
     * <li>0.00001 should be rendered as "0.00001" (here you see the effect of the "precision" parameter)
     * <li>0.00000001 should be rendered as "0.00000001"
     * <li>0.000000001 should be rendered as "0"
     * </ul>
     * <p>
     * Originally authored by Julien Aym&eacute;.
     * https://apache.googlesource.com/xml-graphics-commons/+/e512124c8367dd166ac8594dd6a0800b80fde820/src/java/org/apache/xmlgraphics/util/DoubleFormatUtil.java
     */
    enum DoubleFormatUtil {
        ;

        /**
         * Most used power of ten (to avoid the cost of Math.pow(10, n)
         */
        private static final long[] POWERS_OF_TEN_LONG = new long[19];
        private static final double[] POWERS_OF_TEN_DOUBLE = new double[30];

        static {
            POWERS_OF_TEN_LONG[0] = 1L;
            for (int i = 1; i < POWERS_OF_TEN_LONG.length; i++) {
                POWERS_OF_TEN_LONG[i] = POWERS_OF_TEN_LONG[i - 1] * 10L;
            }
            for (int i = 0; i < POWERS_OF_TEN_DOUBLE.length; i++) {
                POWERS_OF_TEN_DOUBLE[i] = Double.parseDouble("1e" + i);
            }
        }

        /**
         * Rounds
         * Rounds the given source value at the given precision
         * and writes the rounded value into the given target
         *
         * @param source    the source value to round
         * @param decimals  the decimals to round at (use if abs(source) &ge; 1.0)
         * @param precision the precision to round at (use if abs(source) &lt; 1.0)
         * @param target    the buffer to write to
         */
        public static void formatDouble(double source, int decimals, int precision, StringBuilder target) {
            int scale = (Math.abs(source) >= 1.0) ? decimals : precision;
            if (tooManyDigitsUsed(source, scale) || tooCloseToRound(source, scale)) {
                formatDoublePrecise(source, decimals, precision, target);
            } else {
                formatDoubleFast(source, decimals, precision, target);
            }
        }

        /**
         * Rounds the given source value at the given precision
         * and writes the rounded value into the given target
         * <p>
         * This method internally uses the String representation of the source value,
         * in order to avoid any double precision computation error.
         *
         * @param source    the source value to round
         * @param decimals  the decimals to round at (use if abs(source) &ge; 1.0)
         * @param precision the precision to round at (use if abs(source) &lt; 1.0)
         * @param target    the buffer to write to
         */
        public static void formatDoublePrecise(double source, int decimals, int precision, StringBuilder target) {
            if (isRoundedToZero(source, decimals, precision)) {
                // Will always be rounded to 0
                target.append('0');
                return;
            } else if (!Double.isFinite(source)) {
                // Cannot be formated
                target.append(source);
                return;
            }
            boolean negative = source < 0.0;
            if (negative) {
                source = -source;
                // Done once and for all
                target.append('-');
            }
            int scale = (source >= 1.0) ? decimals : precision;
            // The only way to format precisely the double is to use the String
            // representation of the double, and then to do mathematical integer operation on it.
            String s = Double.toString(source);
            if (source >= 1e-3 && source < 1e7) {
                // Plain representation of double: "intPart.decimalPart"
                int dot = s.indexOf('.');
                String decS = s.substring(dot + 1);
                int decLength = decS.length();
                if (scale >= decLength) {
                    if ("0".equals(decS)) {
                        // source is a mathematical integer
                        target.append(s, 0, dot);
                    } else {
                        target.append(s);
                        // Remove trailing zeroes
                        for (int l = target.length() - 1; l >= 0 && target.charAt(l) == '0'; l--) {
                            target.setLength(l);
                        }
                    }
                    return;
                } else if (scale + 1 < decLength) {
                    // ignore unnecessary digits
                    decS = decS.substring(0, scale + 1);
                }
                long intP = Long.parseLong(s.substring(0, dot));
                long decP = Long.parseLong(decS);
                format(target, scale, intP, decP);
            } else {
                // Scientific representation of double: "x.xxxxxEyyy"
                int dot = s.indexOf('.');
                assert dot >= 0;
                int exp = s.indexOf('E');
                assert exp >= 0;
                int exposant = Integer.parseInt(s.substring(exp + 1));
                String intS = s.substring(0, dot);
                String decS = s.substring(dot + 1, exp);
                int decLength = decS.length();
                if (exposant >= 0) {
                    int digits = decLength - exposant;
                    if (digits <= 0) {
                        // no decimal part,
                        // no rounding involved
                        target.append(intS);
                        target.append(decS);
                        target.append("0".repeat(-digits));
                    } else if (digits <= scale) {
                        // decimal part precision is lower than scale,
                        // no rounding involved
                        target.append(intS).append(decS, 0, exposant).append('.').append(decS.substring(exposant));
                    } else {
                        // decimalDigits > scale,
                        // Rounding involved
                        long intP = Long.parseLong(intS) * tenPow(exposant) + Long.parseLong(decS.substring(0, exposant));
                        long decP = Long.parseLong(decS.substring(exposant, exposant + scale + 1));
                        format(target, scale, intP, decP);
                    }
                } else {
                    // Only a decimal part is supplied
                    exposant = -exposant;
                    int digits = scale - exposant + 1;
                    if (digits < 0) {
                        target.append('0');
                    } else if (digits == 0) {
                        long decP = Long.parseLong(intS);
                        format(target, scale, 0L, decP);
                    } else if (decLength < digits) {
                        long decP = Long.parseLong(intS) * tenPow(decLength + 1) + Long.parseLong(decS) * 10;
                        format(target, exposant + decLength, 0L, decP);
                    } else {
                        long subDecP = Long.parseLong(decS.substring(0, digits));
                        long decP = Long.parseLong(intS) * tenPow(digits) + subDecP;
                        format(target, scale, 0L, decP);
                    }
                }
            }
        }

        /**
         * Returns true if the given source value will be rounded to zero
         *
         * @param source    the source value to round
         * @param decimals  the decimals to round at (use if abs(source) &ge; 1.0)
         * @param precision the precision to round at (use if abs(source) &lt; 1.0)
         * @return true if the source value will be rounded to zero
         */
        private static boolean isRoundedToZero(double source, int decimals, int precision) {
            // Use 4.999999999999999 instead of 5 since in some cases, 5.0 / 1eN > 5e-N (e.g. for N = 37, 42, 45, 66, ...)
            return source == 0.0 || Math.abs(source) < 4.999999999999999 / tenPowDouble(Math.max(decimals, precision) + 1);
        }

        /**
         * Returns ten to the power of n
         *
         * @param n the nth power of ten to get
         * @return ten to the power of n
         */
        public static long tenPow(int n) {
            assert n >= 0;
            return n < POWERS_OF_TEN_LONG.length ? POWERS_OF_TEN_LONG[n] : (long) Math.pow(10, n);
        }

        private static double tenPowDouble(int n) {
            assert n >= 0;
            return n < POWERS_OF_TEN_DOUBLE.length ? POWERS_OF_TEN_DOUBLE[n] : Math.pow(10, n);
        }

        /**
         * Helper method to do the custom rounding used within formatDoublePrecise
         *
         * @param target the buffer to write to
         * @param scale  the expected rounding scale
         * @param intP   the source integer part
         * @param decP   the source decimal part, truncated to scale + 1 digit
         */
        private static void format(StringBuilder target, int scale, long intP, long decP) {
            if (decP != 0L) {
                // decP is the decimal part of source, truncated to scale + 1 digit.
                // Custom rounding: add 5
                decP += 5L;
                decP /= 10L;
                if (decP >= tenPowDouble(scale)) {
                    intP++;
                    decP -= tenPow(scale);
                }
                if (decP != 0L) {
                    // Remove trailing zeroes
                    while (decP % 10L == 0L) {
                        decP /= 10L;
                        scale--;
                    }
                }
            }
            target.append(intP);
            if (decP != 0L) {
                target.append('.');
                // Use tenPow instead of tenPowDouble for scale below 18,
                // since the casting of decP to double may cause some imprecisions:
                // E.g. for decP = 9999999999999999L and scale = 17,
                // decP < tenPow(16) while (double) decP == tenPowDouble(16)
                while (scale > 0 && (decP < (scale > 18 ? tenPowDouble(--scale) : tenPow(--scale)))) {
                    // Insert leading zeroes
                    target.append('0');
                }
                target.append(decP);
            }
        }

        public static String formatDoubleFast(double source, int decimals) {
//			return Double.toString(Util.round(source, Math.pow(10, -decimals)));
            StringBuilder target = new StringBuilder(decimals * 2 /* estimate */);
            formatDoubleFast(source, decimals, target);
            return target.toString();
        }

        public static void formatDoubleFast(double source, int decimals, StringBuilder target) {
            formatDouble(source, decimals, decimals, target);
        }

        /**
         * Rounds the given source value at the given precision
         * and writes the rounded value into the given target
         * <p>
         * This method internally uses double precision computation and rounding,
         * so the result may not be accurate (see formatDouble method for conditions).
         *
         * @param source    the source value to round
         * @param decimals  the decimals to round at (use if abs(source) &ge; 1.0)
         * @param precision the precision to round at (use if abs(source) &lt; 1.0)
         * @param target    the buffer to write to
         */
        public static void formatDoubleFast(double source, int decimals, int precision, StringBuilder target) {
            if (isRoundedToZero(source, decimals, precision)) {
                // Will always be rounded to 0
                target.append('0');
                return;
            } else if (Double.isNaN(source) || Double.isInfinite(source)) {
                // Cannot be formated
                target.append(source);
                return;
            }
            boolean isPositive = source >= 0.0;
            source = Math.abs(source);
            int scale = (source >= 1.0) ? decimals : precision;
            long intPart = (long) Math.floor(source);
            double tenScale = tenPowDouble(scale);
            double fracUnroundedPart = (source - intPart) * tenScale;
            long fracPart = Math.round(fracUnroundedPart);
            if (fracPart >= tenScale) {
                intPart++;
                fracPart = Math.round(fracPart - tenScale);
            }
            if (fracPart != 0L) {
                // Remove trailing zeroes
                while (fracPart % 10L == 0L) {
                    fracPart /= 10L;
                    scale--;
                }
            }
            if (intPart != 0L || fracPart != 0L) {
                // non-zero value
                if (!isPositive) {
                    // negative value, insert sign
                    target.append('-');
                }
                // append integer part
                target.append(intPart);
                if (fracPart != 0L) {
                    // append fractional part
                    target.append('.');
                    // insert leading zeroes
                    while (scale > 0 && fracPart < tenPowDouble(--scale)) {
                        target.append('0');
                    }
                    target.append(fracPart);
                }
            } else {
                target.append('0');
            }
        }

//        /**
//         * Returns the exponent of the given value
//         *
//         * @param value the value to get the exponent from
//         * @return the value's exponent
//         */
//        public static int getExponant(double value) {
//            // See Double.doubleToRawLongBits javadoc or IEEE-754 spec
//            // to have this algorithm
//            long exp = Double.doubleToRawLongBits(value) & 0x7ff0000000000000L;
//            exp >>= 52;
//            return (int) (exp - 1023L);
//        }

        /**
         * Returns true if the rounding is considered to use too many digits
         * of the double for a fast rounding
         *
         * @param source the source to round
         * @param scale  the scale to round at
         * @return true if the rounding will potentially use too many digits
         */
        private static boolean tooManyDigitsUsed(double source, int scale) {
            // if scale >= 308, 10^308 ~= Infinity
            return scale >= 308 || Math.log10(source) + scale >= 14.5;
        }

        /**
         * Returns true if the given source is considered to be too close
         * of a rounding value for the given scale.
         *
         * @param source the source to round
         * @param scale  the scale to round at
         * @return true if the source will be potentially rounded at the scale
         */
        private static boolean tooCloseToRound(double source, int scale) {
            source = Math.abs(source);
            long intPart = (long) Math.floor(source);
            double fracPart = (source - intPart) * tenPowDouble(scale);
            double decExp = Math.log10(source);
            double range = decExp + scale >= 12 ? .1 : .001;
            double distanceToRound1 = Math.abs(fracPart - Math.floor(fracPart));
            double distanceToRound2 = Math.abs(fracPart - Math.floor(fracPart) - 0.5);
            return distanceToRound1 <= range || distanceToRound2 <= range;
            // .001 range: Totally arbitrary range,
            // I never had a failure in 10e7 random tests with this value
            // May be JVM dependent or architecture dependent
        }
    }
}