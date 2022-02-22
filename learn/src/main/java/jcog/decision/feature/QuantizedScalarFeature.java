package jcog.decision.feature;

import jcog.Str;
import jcog.math.Discretize1D;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class QuantizedScalarFeature extends DiscreteFeature<Float> implements Function<Function,Object>  {

    final Discretize1D discretizer;
    private final int arity;
    private final String[] rangeLabels;
    public final CentroidMatch[] centroid;

    public QuantizedScalarFeature(int id, String name, int arity, Discretize1D d) {
        super(id, name);
        this.arity = arity;
        this.discretizer = d;
        this.rangeLabels = d==Discretize1D.BooleanDiscretization ?
                Discretize1D.BooleanLabels :
                rangeLabels(arity);

        assert (rangeLabels == null || rangeLabels.length == 0 || rangeLabels.length == arity);
        this.centroid = IntStream.range(0, arity).mapToObj(
            rangeLabels != null && rangeLabels.length == arity ?
                i -> new CentroidMatch(i, rangeLabels[i]) :
                i -> new CentroidMatch(i, null)
        ).toArray(CentroidMatch[]::new);

        d.reset(arity);
    }


    @Override
    public void learn(Float x) {
        discretizer.put(x);
    }

    @Override
    public void commit() {
        discretizer.commit();
    }

    @Override
    public Stream<Function<Function,Object>> classifiers() {
        return Stream.of(this);
    }

    @Override
    public Object apply(Function rr) {
        Object x = rr.apply(id);
        Number n =
            x instanceof Boolean ? (((Boolean) x).booleanValue() ? 1 : 0) :
            (Number) x;
        return centroid[discretize(n)];
    }

    public int discretize(Number n) {
        return discretizer.index(n.doubleValue());
    }

    public class CentroidMatch {

        private final String label;
        private final int v;

        CentroidMatch(int v, String label) {
            this.v = v;
            this.label = label;
        }

        @Override
        public String toString() {
            double[] interval = value();
            return name + '=' + ((label != null ? (label + '[') : "") + Str.n4(interval[0]) + ".." + Str.n4(interval[1]) + ']');
        }

        /** computes a bounded estimate interval */
        public double[] value() {
            return discretizer.value(v);
        }


        public String condition(boolean isTrue) {

            String var = name;
            double[] range = value();
            if (isTrue) {
                if (range[0] == Double.NEGATIVE_INFINITY && range[1] == Double.POSITIVE_INFINITY)
                    return "true";
                if (range[0] == Double.NEGATIVE_INFINITY)
                    return var + " <= " + range[1];
                else if (range[1] == Double.POSITIVE_INFINITY)
                    return var + " >= " + range[0];
                else
                    return var + " >= " + range[0] + " && " + var + " <= " + range[1];

            } else {
                //return var + " < " + range[0] + " || " + var + " > " + range[1];

                if (range[0] == Double.NEGATIVE_INFINITY && range[1] == Double.POSITIVE_INFINITY)
                    return "false";
                if (range[0] == Double.NEGATIVE_INFINITY)
                    return "false"; //var + " <= " + range[1];
                else if (range[1] == Double.POSITIVE_INFINITY)
                    return "false"; //var + " >= " + range[0];
                else
                    return var + " < " + range[0] + " || " + var + " > " + range[1];
            }

        }
    }

    static String[] rangeLabels(int discretization) {
        return switch (discretization) {
            case 2 -> new String[]{"LO", "HI"};
            case 3 -> new String[]{"LO", "MD", "HI"};
            case 4 -> new String[]{"LO", "ML", "MH", "HI"};
            case 5 -> new String[]{"LO", "ML", "MM", "MH", "HI"};
            default -> null;
        };
    }

}