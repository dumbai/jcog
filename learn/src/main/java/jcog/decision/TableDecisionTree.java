package jcog.decision;

import jcog.decision.feature.DiscreteFeature;
import jcog.decision.feature.EnumFeature;
import jcog.decision.feature.QuantizedScalarFeature;
import jcog.math.QuantileDiscretize1D;
import org.eclipse.collections.api.block.function.primitive.IntToFloatFunction;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static jcog.math.Discretize1D.BooleanDiscretization;

/**
 * computes a decision tree from DataTable
 * <p>
 * TODO abstract this by extracting the numeric table into its own class,
 * then this becomes a DecisionTreeBuilder which can be used to generate different trees
 * from a common builder instance
 */
public class TableDecisionTree extends DecisionTree<Integer, Object> {

    public final Table table;
    public final DiscreteFeature[] col;

    /* default: i >= 1
     * gradually reduces pressure on leaf precision
     */
    final IntToFloatFunction depthToPrecision;


    public TableDecisionTree(Table table, String predictCol, int maxDepth, int discretization) {
        this(table, table.columnIndex(predictCol), maxDepth, discretization);
    }

    public TableDecisionTree(Table table, int predictCol, int maxDepth, int discretization) {
        super();

        int columns = table.columnCount();
        assert columns > 1;
        assert maxDepth > 1;
        assert table.rowCount() > 0;

        this.table = table;

        maxDepth(maxDepth);

        depthToPrecision = i ->
            0.9f;
            //0.9f / (1 + (i - 1) / (float) maxDepth);


        this.col = IntStream.range(0, columns).mapToObj(c -> {
            Column<?> C = table.column(c);
            String name = C.name();
            String colType = C.type().name();
            return switch (colType) {
                case "STRING" -> new EnumFeature(c, name) {
                    @Override
                    public void learn(Row r) {
                        learn((String) C.get(r.getRowNumber()));
                    }
                };
                case "DOUBLE" -> new QuantizedScalarFeature(c, name, discretization, new QuantileDiscretize1D()) {
                    @Override
                    public void learn(Row r) {
                        learn(((Double) C.get(r.getRowNumber())).floatValue());
                    }
                };
                case "FLOAT" -> new QuantizedScalarFeature(c, name, discretization, new QuantileDiscretize1D()) {
                    @Override
                    public void learn(Row r) {
                        learn((Float) C.get(r.getRowNumber()));
                    }
                };
                case "INTEGER" -> new QuantizedScalarFeature(c, name, discretization, new QuantileDiscretize1D()) {
                    @Override
                    public void learn(Row r) {
                        learn(((Integer) (C.get(r.getRowNumber()))).floatValue());
                    }
                };
                case "BOOLEAN" -> new QuantizedScalarFeature(c, name, 2, BooleanDiscretization) {
                    @Override
                    public void learn(Row r) {
                        //learn( (Double) ((Boolean) C.get(r.getRowNumber())) ? 1.0 : 0.0);
                    }
                };
                default -> throw new UnsupportedOperationException(colType + " unsupported");
            };
        }).toArray(DiscreteFeature[]::new);


//        table.forEach(row -> {
//            for (int i = 0; i < columns; i++)
//                col[i].learn(row);
//        });
        table.forEach(row -> {
            //pre-train the classifiers
            for (int i = 0; i < columns; i++)
                col[i].learn(row);
        });
        for (DiscreteFeature c : col)
            c.commit();

        update(IntStream.range(0, table.rowCount()).mapToObj(table::row), predictCol);

    }


    void update(Stream<Row> rows, int column) {

        put(column, rows.map(r -> r::getObject), //TODO use direct column access this is slow string key lookup wtf

            Stream.of(col).
                filter(x -> x.id != column).
                flatMap((Function<DiscreteFeature, Stream<Function<Function<Integer, Object>, Object>>>)
                        discreteFeature -> discreteFeature.classifiers()),

            depthToPrecision
        );
    }
//        //selects column value from integer key
//        //Stream<Function<Integer, Object>> data = Streams.stream(table.iterator()).map(r
//
//        Stream<Predicate<Function<Integer, Object>>> features = IntStream.range(0, col.length)
//            .filter(c -> c!=predictCol)
//            .mapToObj(c-> col[c])
//            .flatMap(f -> f.classifiers());
//
//        table.forEach((Row row) ->
//            put(predictCol, Stream.of((Integer i) -> row.getObject(i)), features, depthToPrecision)
//        );
//    }



//    public DecisionNode<Float> min() {
//        return leaves().min(centroidComparator).get();
//    }
//
//    public DecisionNode<Float> max() {
//        return leaves().max(centroidComparator).get();
//    }
//
//    static final Comparator<DecisionNode<Float>> centroidComparator = (a, b) -> Float.compare(a.label, b.label);

}