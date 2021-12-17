///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package jcog.signal.meter;
//
//import com.google.common.base.Function;
//import com.google.common.collect.Iterators;
//import jcog.Texts;
//import jcog.signal.meter.event.DoubleMeter;
//import jcog.signal.meter.event.HitMeter;
//import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
//
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.PrintStream;
//import java.lang.reflect.Field;
//import java.util.*;
//import java.util.function.Predicate;
//
///**
// * TODO use TableSaw Table
// */
//@Deprecated public class Metrics<RowKey> implements Iterable<Object[]> {
//
//
//
//
//
//
//    public static void printJSONArray(PrintStream out, Object[] row, boolean includeBrackets) {
//
//
//
//
//
//
//
//
//
//    }
//
//
////    /**
////    *  Calculates a 2-tuple with the following data:
////    *   0: minimum value among all columns in given signals
////    *   1: maximum value among all columns in given signals
////    *
////    * @param data
////    * @return
////    */
////    public static double[] getBounds(Iterable<SignalData> data) {
////        double min = Double.POSITIVE_INFINITY;
////        double max = Double.NEGATIVE_INFINITY;
////
////        for (SignalData ss : data) {
////            double a = ss.getMin();
////            double b = ss.getMax();
////            if (a < min) min = a;
////            if (b > max) max = b;
////        }
////        return new double[] { min, max };
////    }
//
//    protected void setMin(int signal, double n) {
//        getSignal(signal).setMin(n);
//    }
//    protected void setMax(int signal, double n) {
//        getSignal(signal).setMax(n);
//    }
//
//    public double getMin(int signal) {
//        ScalarColumn s = getSignal(signal);
//        if (s == null) return Double.NaN;
//        return s.getMin();
//    }
//    public double getMax(int signal) {
//        ScalarColumn s = getSignal(signal);
//        if (s == null) return Double.NaN;
//        return s.getMax();
//    }
//
//
//    public void updateBounds(int signal) {
//
//        ScalarColumn s = getSignal(signal);
//        s.resetBounds();
//        double min = Double.POSITIVE_INFINITY;
//        double max = Double.NEGATIVE_INFINITY;
//
//
//        for (Object[] objects : this) {
//            Object e = objects[signal];
//            if (e instanceof Number) {
//                double d = ((Number) e).doubleValue();
//                if (d < min) min = d;
//                if (d > max) max = d;
//            }
//        }
//        s.setMin(min);
//        s.setMax(max);
//    }
//
//    /** adds all meters which exist as fields of a given object (via reflection) */
//    public void addViaReflection(Object obj) {
//        Class c = obj.getClass();
//        Class meter = Meters.class;
//        for (Field f : c.getFields()) {
//
//
//
//            if ( meter.isAssignableFrom( f.getType() ) ) {
//                Meters m = null;
//                try {
//                    m = (Meters)f.get(obj);
//                } catch (IllegalAccessException e) {
//
//                }
//                add(m);
//            }
//        }
//    }
//
//
////    public SignalData newSignalData(String n) {
////        ScalarColumn s = getSignal(n);
////        if (s == null) return null;
////        return new SignalData(this, s);
////    }
//
//    public Metrics addViaReflection(Meters... c) {
//        for (Meters x : c)
//            add(x);
//        return this;
//    }
//
//    public <M extends Meters<?>> M getMeter(String id) {
//        int i = indexOf(id);
//        if (i == -1) return null;
//        return (M) meters.get(i);
//    }
//
//    private static class firstColumnIterator implements Function<Object[], Object[]> {
//        final Object[] next;
//        final int thecolumn;
//
//        public firstColumnIterator(int... columns) {
//            next = new Object[1];
//            thecolumn = columns[0];
//        }
//
//        @Override public Object[] apply(Object[] f) {
//            next[0] = f[thecolumn];
//            return next;
//        }
//    }
//
//    private static class nColumnIterator implements Function<Object[], Object[]> {
//
//        final Object[] next;
//        private final int[] columns;
//
//        public nColumnIterator(int... columns) {
//            this.columns = columns;
//            next = new Object[columns.length];
//        }
//
//        @Override
//        public Object[] apply(Object[] f) {
//
//            int j = 0;
//            for (int c : columns) {
//                next[j++] = f[c];
//            }
//            return next;
//        }
//
//    }
//
//
//    /** generates the value of the first entry in each row */
//    class RowKeyMeter extends FunctionMeter {
//
//        public RowKeyMeter() {
//            super("key");
//        }
//
//        @Override
//        public RowKey getValue(Object key, int index) {
//            return nextRowKey;
//        }
//
//    }
//
//    private RowKey nextRowKey;
//
//    /** the columns of the table */
//    private final List<Meters<?>> meters = new ArrayList<>();
//    private final ArrayDeque<Object[]> rows = new ArrayDeque<>();
//
//    private transient List<ScalarColumn> signalList = new ArrayList<>();
//    private transient Map<String, Integer> signalIndex = new HashMap();
//
//    int numColumns;
//
//    /** capacity */
//    int history;
//
//    /** unlimited size */
//    public Metrics() {
//        this(-1);
//    }
//
//    /** fixed size */
//    public Metrics(int historySize) {
//        history = historySize;
//
//        add(new RowKeyMeter());
//    }
//
//
//    public void clear() {
//        clearData();
//        clearSignals();
//    }
//
//    public void clearSignals() {
//        numColumns = 0;
//        signalList = null;
//        signalIndex = null;
//        meters.clear();
//    }
//
//    public void clearData() {
//        rows.clear();
//    }
//
//    @SafeVarargs
//    public final <M extends Meters<C>, C> void addAll(M... m) {
//        for (M mm : m)
//            add(mm);
//    }
//
//    public <M extends Meters<C>, C> M add(M m) {
//        for (ScalarColumn s : m.getSignals()) {
//            if (getSignal(s.id)!=null)
//                throw new RuntimeException("Signal " + s.id + " already exists in "+ this);
//        }
//
//        meters.add(m);
//        numColumns+= m.numSignals();
//
//        signalList = null;
//        signalIndex = null;
//        return m;
//    }
//
//
//    /** generate the next row.  key can be a time number, or some other unique-like identifying value */
//    public synchronized <R extends RowKey> void update(R key) {
//        nextRowKey = key;
//
//        boolean[] extremaToInvalidate = new boolean[ numColumns ];
//
//        Object[] nextRow = new Object[ numColumns ];
//        append(nextRow, extremaToInvalidate);
//
//        int c = 0;
//        for (Meters m : meters) {
//            Object[] v = m.sample(key);
//            if (v == null) continue;
//            int vl = v.length;
//
//            if (c + vl > nextRow.length)
//                throw new RuntimeException("column overflow: " + m + ' ' + c + '+' + vl + '>' + nextRow.length);
//
//            switch (vl) {
//                case 1:
//                    nextRow[c++] = v[0];
//                    break;
//                case 2:
//                    nextRow[c++] = v[0];
//                    nextRow[c++] = v[1];
//                    break;
//                default:
//                    System.arraycopy(v, 0, nextRow, c, vl);
//                    c += vl;
//                    break;
//            }
//
//        }
//
//        invalidateExtrema(true, nextRow, extremaToInvalidate);
//
//
//        for (int i = 0; i < getSignals().size(); i++) {
//            if (i == 0) extremaToInvalidate[0] = true;
//            if (extremaToInvalidate[i]) {
//                updateBounds(i);
//            }
//
//        }
//
//    }
//
//    private void invalidateExtrema(boolean added, Object[] row, boolean[] extremaToInvalidate) {
//        for (int i = 0; i < row.length; i++) {
//            Object ri = row[i];
//            if (!(ri instanceof Number)) continue;
//
//            double n = ((Number)row[i]).doubleValue();
//            if (Double.isNaN(n)) continue;
//
//            double min = getMin(i);
//            double max = getMax(i);
//
//            boolean minNAN = Double.isNaN(min);
//            boolean maxNAN = Double.isNaN(max);
//
//            if (added) {
//
//                if ((minNAN) || (n < min))  {
//                    setMin(i, n);
//                }
//                if ((maxNAN) || (n > max))  {
//                    setMax(i, n);
//                }
//            }
//            else {
//
//                if (minNAN || (n == min))  { extremaToInvalidate[i] = true; continue; }
//                if (maxNAN || (n == max))  { extremaToInvalidate[i] = true;
//                }
//            }
//
//        }
//    }
//
//
//    protected void append(Object[] next, boolean[] invalidatedExtrema) {
//        if (next==null) return;
//
//        if (history > 0) {
//            while (rows.size() >= history) {
//                Object[] prev = rows.removeFirst();
//                invalidateExtrema(false, prev, invalidatedExtrema);
//            }
//        }
//
//        rows.addLast(next);
//
//    }
//
//    public List<ScalarColumn> getSignals() {
//        if (signalList == null) {
//            signalList = new ArrayList(numColumns);
//            for (Meters<?> m : meters)
//                signalList.addAll(m.getSignals());
//        }
//        return signalList;
//    }
//
//    public Map<String,Integer> getSignalIndex() {
//        if (signalIndex == null) {
//            signalIndex = new HashMap(numColumns);
//            int i = 0;
//            for (ScalarColumn s : getSignals()) {
//                signalIndex.put(s.id, i++);
//            }
//        }
//        return signalIndex;
//    }
//
//    public int indexOf(ScalarColumn s) {
//        return indexOf(s.id);
//    }
//
//    public int indexOf(String signalID) {
//        Integer i = getSignalIndex().get(signalID);
//        if (i == null) return -1;
//        return i;
//    }
//
//    public ScalarColumn getSignal(int index) {
//       return getSignals().get(index);
//    }
//    public ScalarColumn getSignal(String s) {
//       if (s == null) return null;
//       int ii = indexOf(s);
//       if (ii == -1) return null;
//       return getSignals().get(ii);
//    }
//
//    public Object[] rowFirst() { return rows.getFirst(); }
//    public Object[] rowLast() { return rows.getLast(); }
//    public int numRows() { return rows.size(); }
//
//    @Override
//    public Iterator<Object[]> iterator() {
//        return rows.iterator();
//    }
//
//    public Iterator<Object[]> reverseIterator() {
//        return rows.descendingIterator();
//    }
//
//
//    public Object[] getData(int signal, Object[] c) {
//        if ((c == null) || (c.length != numRows() ))
//            c = new Object[ numRows() ];
//
//        int r = 0;
//        for (Object[] row : this) {
//            c[r++] = row[signal];
//        }
//
//        return c;
//    }
//
//    public Object[] getData(int signal) {
//        return getData(signal, null);
//    }
//
//
//
//    /*    public Signal getSignal(int column) {
//        int p = 0;
//        for (Meter<?> m : meters) {
//            int s = m.numSignals();
//            if (column < p + s) {
//                return m.getSignals().get(column - p);
//            }
//            p += s;
//        }
//        return null;
//    }*/
//
//    public static Iterator<Object> iterateSignal(int column, boolean reverse) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Iterator<Object[]> iterator(int... columns) {
//        if (columns.length == 1) {
//
//            return Iterators.transform(iterator(), new firstColumnIterator(columns));
//        }
//
//        return Iterators.transform(iterator(), new nColumnIterator(columns));
//    }
//
//    public static DoubleArrayList doubles(Iterable<Object> l) {
//        DoubleArrayList r = new DoubleArrayList();
//        for (Object o : l)
//            if (o instanceof Number) r.add(((Number)o).doubleValue());
//        return r;
//    }
//
//    public static double[] doubleArray(Object... l) {
//        DoubleArrayList r = new DoubleArrayList(l.length);
//        for (Object o : l)
//            if (o instanceof Number) r.add(((Number)o).doubleValue());
//        return r.toArray();
//    }
//
//    public double[] doubleArray(int col) {
//        return doubleArray(getData(col));
//    }
//    public double[] doubleArray(String signal) {
//        return doubleArray(indexOf(signal));
//    }
//    public double[] doubleArray(ScalarColumn s) {
//        return doubleArray(indexOf(s.id));
//    }
//
//
//
//    public List<Object> getNewSignalValues(int column, int num) {
//        List<Object> l = new ArrayList(num);
//        Iterator<Object[]> r = reverseIterator();
//        while (r.hasNext() && num > 0) {
//            l.add(r.next()[column]);
//            num--;
//        }
//        return l;
//    }
//
//    public String[] getSignalIDs() {
//        String[] r = new String[getSignals().size()];
//        int i = 0;
//        for (ScalarColumn s : getSignals()) {
//            r[i++] = s.id;
//        }
//        return r;
//    }
//
//    public void printCSVHeader(PrintStream out) {
//
//        printCSVRow(out, getSignalIDs());
//    }
//
//
//
//
//
//
//    public void printCSV(PrintStream out) {
//
//
//
//
//
//
//        printCSV4(out);
//    }
//
//    /** print CSV with numbers up to 4 decimal points accuracy */
//    public void printCSV4(PrintStream out) {
//        printCSVHeader(out);
//        for (Object[] row : this) {
//            printCSVRow4(out, row);
//        }
//        out.flush();
//    }
//    public void printCSV4(String filename) throws FileNotFoundException {
//        printCSV4(new PrintStream(new FileOutputStream(filename)));
//    }
//
//    private static void printCSVRow(PrintStream out, Object[] row) {
//
//
//
//
//
//        printCSVRow4(out, row);
//    }
//
//    private static void printCSVRow4(PrintStream out, Object[] row) {
//        for (Object o : row) {
//
//            if (o instanceof Number) {
//                Number n = (Number)o;
//                out.append(Texts.n4(n.floatValue()));
//            } else {
//                out.append('"').append(String.valueOf(o)).append('"');
//            }
//            out.append(',');
//        }
//        out.append('\n');
//    }
//
//    public void printCSV(String filepath) throws FileNotFoundException {
//        printCSV(new PrintStream(new FileOutputStream(filepath)));
//    }
//
//
//    public String name() {
//        return getClass().getSimpleName();
//    }
//
//
//    public void printARFF(PrintStream out) {
//        printARFF(out, null);
//    }
//
//    public void printARFF(PrintStream out, Predicate<Object[]> p) {
//
//        out.println("@RELATION " + name());
//
//
//        int n = 0;
//        for (Meters<?> m : meters) {
//            for (ScalarColumn s : m.getSignals()) {
//                if (n == 0) {
//
//                    out.println("@ATTRIBUTE " + s.id + " STRING");
//                }
//                else if ((m instanceof DoubleMeter) || (m instanceof HitMeter)) {
//                    out.println("@ATTRIBUTE " + s.id + " NUMERIC");
//                }
//                else {
//                    out.println("@ATTRIBUTE " + s.id + " STRING");
//
//                }
//                n++;
//            }
//        }
//
//        out.print('%');
//        printCSVHeader(out);
//
//        out.println("@DATA");
//        for (Object[] x : this) {
//            if (p!=null)
//                if (!p.test(x)) continue;
//            for (int i = 0; i < numColumns; i++) {
//                if (i < x.length) {
//                    Object y = x[i];
//                    if (y == null)
//                        out.print('?');
//                    else if (y instanceof Number)
//                        out.print(y);
//                    else
//                        out.print('\"' + y.toString() + '\"');
//                }
//                else {
//
//                    out.print('?');
//                }
//                if (i!=numColumns-1)
//                    out.print(',');
//                else
//                    out.println();
//            }
//
//        }
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//}
