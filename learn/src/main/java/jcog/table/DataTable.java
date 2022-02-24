package jcog.table;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import jcog.TODO;
import jcog.data.bit.MetalBitSet;
import jcog.data.list.Lst;
import jcog.decision.FloatTable;
import jcog.util.ArrayUtil;
import org.jetbrains.annotations.Nullable;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;
import tech.tablesaw.table.Rows;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * specified semantics of a data record / structure
 * TODO move most of this to 'MutableDataTable' implementation of interface DataTable
 * see: https://github.com/jdmp/java-data-mining-package/blob/master/jdmp-core/src/main/java/org/jdmp/core/dataset/DataSet.java
 **/
public class DataTable extends Table implements Externalizable {

    private final Map<String, String[]> nominalCats;

    public DataTable() {
        super("");
//        this.attribute_names = new FasterList<>();
//        this.attrTypes = new HashMap<>();
        this.nominalCats = new HashMap<>();
    }

    /**
     * see: Table.copy()
     */
    public DataTable(Table copy) {
        super(copy.name());

        int rc = copy.rowCount();
        for (Column<?> column : copy.columns()) {
            addColumns(column.emptyCopy(rc));
        }

        int[] rows = new int[rc];
        for (int i = 0; i < rc; i++) {
            rows[i] = i;
        }
        Rows.copyRowsToTable(rows, copy, this);
        nominalCats = new HashMap<>();
    }

    public DataTable(DataTable copyMetadataFrom) {
        super("");
        addColumns(columnArray());
//        this.attribute_names = copyMetadataFrom.attribute_names;
//        this.attrTypes = copyMetadataFrom.attrTypes;
        this.nominalCats = copyMetadataFrom.nominalCats;
    }

    public static Table collapseEqualColumns(Table d) {
        var cols = d.columnNames();
        for (String c : cols) {
            if (!d.containsColumn(c))
                continue;
            var cc = d.column(c);

            Set<String> equal = new TreeSet();
            for (var y : cols) {
                if (y == c) continue;
                if (!d.containsColumn(y))
                    continue;
                var yy = d.column(y);
                if (cc.type().equals(yy.type())) {
                    if (Arrays.equals(cc.asObjectArray(), yy.asObjectArray())) {
                        String yyn = yy.name();
                        equal.add(yyn);
                        d = d.removeColumns(yyn);
                        //cols.remove(yyn);
                    }
                }
            }
            if (!equal.isEmpty()) {
                equal.add(c);
                String newName = Joiner.on("=").join(equal);
                cc.setName(newName);
            }
        }
        return d;
    }

    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        //HACK
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        write().csv(new GZIPOutputStream(o));
        objectOutput.writeInt(o.size());
        objectOutput.write(o.toByteArray());
    }

    @Override
    public void readExternal(ObjectInput i) throws IOException {
        //HACK
        int size = i.readInt();
        byte[] b = new byte[size];
        i.readFully(b);
        Table csv = read().csv(new GZIPInputStream(new ByteArrayInputStream(b)));
        addColumns(csv.columnArray());
    }

    boolean equalSchema(DataTable other) {

        return (this == other) ||

                (Arrays.equals(columnTypes(), other.columnTypes())
                        &&
//                (attribute_names.equals(other.attribute_names) &&
//                        attrTypes.equals(other.attrTypes) &&
                        nominalCats.equals(other.nominalCats)
                );
    }

    /**
     * Get the name of an attribute.
     */
    public String attrName(int idx) {
        return column(idx).name();
    }


//    /**
//     * Get the type of an attribute. Currently, the attribute types are
//     * "numeric", "string", and "nominal". For nominal attributes, use getAttributeData()
//     * to retrieve the possible values for the attribute.
//     */
//    public ColumnType attrType(String name) {
//        return column(name).type();
//    }

    /**
     * Define a new attribute. Type must be one of "numeric", "string", and
     * "nominal". For nominal attributes, the allowed values
     * must also be given. This variant of defineAttribute allows to set this data.
     */
    public DataTable define(String name, ColumnType type) {

        //assert (type != Nominal);
//        if (attrTypes.put(name, type) != null)
//            throw new RuntimeException("column name collision");

        if (type == ColumnType.STRING) {
            addColumns(StringColumn.create(name));
        } else if (type == ColumnType.DOUBLE) {
            addColumns(DoubleColumn.create(name));
        } else {
            throw new TODO();
        }


        return this;
    }

    public DataTable defineText(String attr) {
        return define(attr, ColumnType.STRING);
    }

    public Table defineNumeric(String attr) {
        return define(attr, ColumnType.DOUBLE);
    }

    /**
     * Get additional information on the attribute. This data is used for
     * nominal attributes to define the possible values.
     */
    public String[] categories(String nominalAttributeName) {
        return nominalCats.get(nominalAttributeName);
    }

    /**
     * Add a data point
     * TODO check data type of each point component
     */
    public boolean add(Object... point) {
//        return addAt(Lists.immutable.of(point));
//    }
//
//    public boolean addAt(ImmutableList point) {
        if (point.length != columnCount())
            throw new UnsupportedOperationException("row structure mismatch: provided " + point.length + " != expected " + columnCount());

        List<Column<?>> l = columns();
        for (int i = 0; i < point.length; i++) {
            Column<?> c = l.get(i);
            Object p = point[i];
            //TODO use type cast graph
            //come on tablesaw
            if (p instanceof String) {
                //ok
            } else if (p instanceof Boolean) {
                //ok
            } else if (p instanceof Number n) {
                if (c instanceof LongColumn) {
                    p = n.longValue();
                } else if (c instanceof DoubleColumn) {
                    p = n.doubleValue();
                } else if (c instanceof FloatColumn) {
                    p = n.floatValue();
                } else
                    throw new TODO();
            } else
                throw new TODO();

            c.appendObj(p);
        }

        return true;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public static FloatTable<String> toFloatTable(Table x) {

        FloatTable<String> data = new FloatTable<>(x.columnNames().toArray(ArrayUtil.EMPTY_STRING_ARRAY));

        x.doWithRows(rr -> data.add(toFloatArray(x, rr)));

        return data;
    }

    private static float[] toFloatArray(Table t, Row rr) {

        int cols = rr.columnCount();
        float[] r = new float[cols];
        for (int i = 0; i < cols; i++) {
            ColumnType it = t.column(i).type();
            if (it == ColumnType.FLOAT) {
                r[i] = rr.getFloat(i);
            } else if (it == ColumnType.DOUBLE) {
                r[i] = (float) rr.getDouble(i);
            } else if (it == ColumnType.LONG) {
                // || it == ColumnType.DOUBLE || it == ColumnType.INTEGER || it == ColumnType.LONG
                r[i] = rr.getLong(i);
            } else if (it == ColumnType.INTEGER) {
                // || it == ColumnType.DOUBLE || it == ColumnType.INTEGER || it == ColumnType.LONG
                r[i] = rr.getInt(i);
            } else {
                r[i] = Float.NaN; //TODO remove these
            }
        }
        return r;
    }

    public final int size() {
        return rowCount();
    }


    public static void printCSV() {
        printCSV(new FilterOutputStream(System.out) {
            @Override
            public void close() {
                //HACKK dont close it - can cause VM shutdown
            }
        });
    }

    private static void printCSV(OutputStream o) {
        throw new TODO();
//        CsvWriteOptions.builder(o).header(true).build();
//                .write();
//        String s = data.columnNames().toString();
//        System.out.println(s.substring(/*'['*/ 1, s.length()-1 /*']'*/));
    }

    public @Nullable Row maxBy(int column) {
        double[] bestScore = {Double.NEGATIVE_INFINITY};
        Row[] best = {null};
        doWithRows(e -> {
            double s = e.getDouble(column);
            if (s > bestScore[0]) {
                best[0] = e;
                bestScore[0] = s;
            }
        });
        return best[0];
    }

//    public static class NominalColumnType extends AbstractColumnType {
//        public final String[] values;
//
//        public NominalColumnType(String name, String[] values) {
//            super(4,
//                    name,
//                    name);
//            this.values = values;
//        }
//
//        @Override
//        public Column<?> create(String name) {
//            return StringColumn.create(name);
//        }
//
//        @Override
//        public AbstractColumnParser<?> customParser(ReadOptions options) {
//            throw new TODO();
//        }
//
////        @Override
////        public StringColumn create(String name) {
////            return StringColumn.create(name);
////        }
////
////        @Override
////        public StringStringParser defaultParser() {
////            return new StringStringParser(this);
////        }
////
////        @Override
////        public StringStringParser customParser(CsvReadOptions options) {
////            return new StringStringParser(this, options);
////        }
//
//    }

    Table defineNominal(String nominalAttribute, String... categories) {
        if (categories.length < 2)
            throw new RuntimeException("nominal types require > 1 categories");

//        attribute_names.addAt(nominalAttribute);
//        attrTypes.put(nominalAttribute, Nominal);
        String[] prev = nominalCats.put(nominalAttribute, categories);

        addColumns(StringColumn.create(nominalAttribute));

        assert (prev == null);
        return this;
    }


    public static Instance instance(Row x, Table t) {

        ColumnType[] ct = t.columnTypes();
        List<Double> d = new Lst<>(ct.length);
        for (int i = 0, ctLength = ct.length; i < ctLength; i++) {
            ColumnType cti = ct[i];
            if (cti == ColumnType.BOOLEAN)
                d.add(x.getBoolean(i) ? +1.0 : -1.0);
            else {
                if (cti == ColumnType.STRING) {
                    //ignore
                    continue;
                }
//            if (t instanceof FloatColumnType)
//                d.add((double)x.getFloat(i1));
//            else if (t == DoubleColumnType)
                d.add(x.getDouble(i));
            }
        }
        return new Instance(ImmutableList.copyOf(d));
    }

    public static double[] toDouble(Row r, int columnFrom, int columnTo) {
        double[] d = new double[columnTo-columnFrom];
        for (int i = columnFrom, j = 0; i < columnTo; i++, j++)
            d[j] = r.getDouble(i);
        return d;
    }

    @Deprecated
    public static class Instance {
        public final ImmutableList data;

        public Instance(ImmutableList data) {
            this.data = data;
        }

//        @Override
//        public String toString() {
//            return DataTable.this + " " + data;
//        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }

        public double[] toDoubleArray(MetalBitSet cols) {
            double[] x = new double[cols.cardinality()];
            int j = 0;
            int n = data.size();
            for (int i = 0; i < n; i++) {
                if (!cols.test(i)) continue;

                Object o = data.get(i);
                double v;
                if (o instanceof Number) {
                    v = ((Number) o).doubleValue();
                } else {
                    throw new UnsupportedOperationException();
                }
                x[j++] = v;
            }
            return x;
        }

//        protected Table table() {
//            return DataTable.this;
//        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Instance i)) return false;

            return i.data.equals(data);// && table().equals(i.table());
        }


    }
}
/*
from: https://github.com/jdmp/java-data-mining-package/blob/master/jdmp-core/src/main/java/org/jdmp/core/variable/Variable.java
public interface Variable extends CoreObject, ListMatrix<Matrix> {
	public static final Class<?>[] VARIABLEARRAY = new Class<?>[] { new Variable[] {}.getClass() };

	public static final String TAGS = "Tags";
	public static final String TOTAL = "Total";
	public static final String URL = "URL";
	public static final String CONTENT = "Content";
	public static final String LINKS = "Links";
	public static final String TYPE = "Type";
	public static final String SOURCE = "Source";
	public static final String DIMENSION = "Dimension";
	public static final String IGNORENAN = "IgnoreNaN";
	public static final String TARGET = "Target";
	public static final String INPUT = "Input";
	public static final String DIFFERENCE = "Difference";
	public static final String RMSE = "RMSE";
	public static final String PREDICTED = "Predicted";
	public static final String WEIGHT = "Weight";
	public static final String SCORE = "Score";
	public static final String PROBABILITY = "Probability";
	public static final String COUNT = "Count";
	public static final String SUGGESTEDTAGS = "SuggestedTags";
	public static final String BYTES = "Bytes";
	public static final String SENSITIVITY = "Sensitivity";
	public static final String SPECIFICITY = "Specificity";
	public static final String PRECISION = "Precision";
	public static final String RECALL = "Recall";
	public static final String FMEASURE = "FMeasure";
	public static final String ERRORCOUNT = "ErrorCount";
	public static final String CONFUSION = "Confusion";
	public static final String ACCURACY = "Accuracy";
	public static final String FMEASUREMACRO = "FMeasureMacro";
	public static final String COMPRESSED = "Compressed";
	public static final String DECOMPRESSED = "Decompressed";
	public static final String HASH = "Hash";
	public static final String TARGETCLASS = "TargetClass";
	public static final String TARGETLABEL = "TargetLabel";

	public static final VariableFactory Factory = new DefaultVariableFactory();

	public Matrix getAsListMatrix();

	public Variable clone();

}
 */