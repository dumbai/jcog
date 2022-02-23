package jcog.decision;

import com.google.common.collect.Streams;
import jcog.data.list.Lst;
import jcog.data.map.UnifriedMap;
import jcog.decision.impurity.GiniIndexImpurityCalculation;
import jcog.decision.impurity.ImpurityCalculator;
import org.eclipse.collections.api.block.function.primitive.IntToFloatFunction;
import org.eclipse.collections.api.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static jcog.util.StreamReplay.replay;
import static org.eclipse.collections.impl.tuple.Tuples.pair;

/**
 * Decision tree implementation.
 *
 * @author Ignas
 */
@Deprecated public class DecisionTree<K, V> {

    /**
     * When data is considered homogeneous and node becomes leaf and is labeled. If it is equal 1.0 then absolutely all
     * data must be of the same label that node would be considered a leaf.
     */
    public static final float DEFAULT_PRECISION =
        //0.5f;
        0.90f;
        //1f;


    /**
     * Impurity calculation method.
     */
    private final ImpurityCalculator impurity =
        new GiniIndexImpurityCalculation();
        //new EntropyCalculator();

    /**
     * Max depth parameter. Growth of the tree is stopped once this depth is reached. Limiting depth of the tree can
     * help with overfitting, however if depth will be set too low tree will not be acurate.
     */
    private int maxDepth = 15;
    /**
     * Root node.
     */
    private DecisionNode<V> root;

    /**
     * Returns Label if data is homogeneous.
     */
    public static <K, V> V label(K value, Stream<Function<K, V>> data, float homogenityPercentage) {

        Map<V, Long> labelCount = data.collect(groupingBy((x) -> x.apply(value), counting()));

        long totalCount = labelCount.values().stream().mapToLong(x -> x).sum();
        for (Map.Entry<V, Long> e : labelCount.entrySet()) {
            long nbOfLabels = e.getValue();
            if ((nbOfLabels / (double) totalCount) >= homogenityPercentage)
                return e.getKey();
        }
        return null;
    }

    /**
     * Split data according to if it has this feature.
     *
     * @param data Data to by split by this feature.
     * @return Sublists of split data samples.
     */
    static <K, V> Map<Object, List<Function<K,V>>> split(Function<Function<K,V>,Object> p, Supplier<Stream<Function<K,V>>> data) {
        //TODO N-way split: instead of predicate, use ToIntFunction<Function<K,V>>
        Map<Object, List<Function<K,V>>> split = data.get().collect(groupingBy(p));
        return split;
    }

    /**
     * Differs from getLabel() that it always return some label and does not look at homogenityPercentage parameter. It
     * is used when tree growth is stopped and everything what is left must be classified so it returns majority label for the data.
     */
    public static <K, V> V majority(K value, Stream<Function<K, V>> data) {
        return data.collect(groupingBy(x -> x.apply(value), counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
    }

    private static void printSubtree(DecisionNode<?> node, PrintStream o) {
//        if (node.size()==2) {
//            print(node.getFirst(), true, "", o);
//            print(node, o);
//            print(node.getLast(), false, "", o);
//        } else {
            print(node, o);
            node.keyValuesView().forEach((k)->{
                o.print(k.getOne() + "\t");
                print(k.getTwo(), o);
            });
//        }
    }

    private static void print(DecisionNode node, PrintStream o) {
        o.print(node);
        o.println();
    }

//    private static <K> void print(DecisionNode<?> node, K indent, PrintStream o) {
//        if (!node.isEmpty() && node.get(0) != null) {
//            print(node.get(0), true, indent + (isRight ? "        " : " |      "), o);
//        }
//        o.print(indent);
//        if (isRight) {
//            o.print();
//        } else {
//            o.print(" \\");
//        }
//        o.print("----- ");
//        print(node, o);
//        if (node.size() > 1 && node.get(1) != null) {
//            print(node.get(1), false, indent + (isRight ? " |      " : "        "), o);
//        }
//    }

    public DecisionTree maxDepth(int d) {
        this.maxDepth = d;
        return this;
    }

    /**
     * Get root.
     */
    public DecisionNode<V> root() {
        return root;
    }

    public Stream<DecisionNode.LeafNode<V>> leaves() {
        return root == null ? Stream.empty() :
            root.recurse().filter(DecisionNode::isLeaf).map(n -> (DecisionNode.LeafNode<V>) n).distinct();
    }

    /**
     * Trains tree on training data for provided features.
     *
     * @param value        The value column being learned
     * @param trainingData List of training data samples.
     * @param features     List of possible features.
     */
    public DecisionNode<V> put(K value, Stream<Function<K,V>> trainingData, Stream<Function<Function<K,V>,Object>> features, IntToFloatFunction precision) {
        root = put(value, replay(trainingData), features, 1, precision);
        return root;
    }

    /**
     * constant precision
     */
    public DecisionNode<V> put(K value, Collection<Function<K,V>> data, Stream<Function<Function<K,V>,Object>> features, float precision) {
        return put(value, data.stream(), features, (depth) -> precision);
    }

    public DecisionNode<V> put(K value, Collection<Function<K,V>> data, Collection<Function<Function<K,V>,Object>> features) {
        return put(value, data, features.stream());
    }

    public DecisionNode<V> put(K value, Collection<Function<K,V>> data, Iterable<Function<Function<K,V>,Object>> features) {
        return put(value, data, Streams.stream(features));
    }

    /**
     * default constant precision
     */
    public DecisionNode<V> put(K value, Collection<Function<K,V>> data, Stream<Function<Function<K,V>,Object>> features) {
        return put(value, data, features, DEFAULT_PRECISION);
    }

    /**
     * Grow tree during training by splitting data recusively on best feature.
     *
     * @param data     List of training data samples.
     * @param features List of possible features.
     * @return Node after split. For a first invocation it returns tree root node.
     */
    @Nullable protected DecisionNode<V> put(K key, Supplier<Stream<Function<K,V>>> data, Stream<Function<Function<K,V>,Object>> features, int currentDepth, IntToFloatFunction depthToPrecision) {

        V currentNodeLabel;
        if ((currentNodeLabel = label(key, data.get(), depthToPrecision.valueOf(currentDepth))) != null)
            return new DecisionNode.LeafNode<>(currentNodeLabel);


        boolean stoppingCriteriaReached = currentDepth >= maxDepth;
        if (stoppingCriteriaReached)
            return new DecisionNode.LeafNode<>(majority(key, data.get()));


        Supplier<Stream<Function<Function<K, V>, Object>>> f = replay(features);
        Function<Function<K,V>, Object> split = bestSplit(key, data, f.get());
        if (split == null)
            return new DecisionNode.LeafNode<>(majority(key, data.get()));
        else {


            Map<Object, List<Function<K, V>>> splitted = split(split, data);
            assert (splitted.size() >= 1);

            DecisionNode<V> branch = new DecisionNode<>(split);
            splitted.forEach((key1, value) -> branch.put(key1, put(key,
                    value::stream,
                    f.get().filter(p -> !p.equals(split)), currentDepth + 1, depthToPrecision)));
            //return branch;
            return branch.size() == 1 ? branch.getFirst() : branch;
        }
    }

    /**
     * Classify a sample.
     *
     * @param value Data sample
     * @return Return label of class.
     */
    public V get(Function<K, V> value) {
        DecisionNode<V> node = root;
        while (!node.isLeaf()) {
            node = node.get(node.feature.apply(value));
        }
        return node.label;
    }

    /**
     * Finds best feature to split on which is the one whose split results in lowest impurity measure.
     */
    protected Function<Function<K, V>,Object> bestSplit(K value, Supplier<Stream<Function<K, V>>> data, Stream<Function<Function<K, V>,Object>> features) {
        double[] purest = {Double.POSITIVE_INFINITY};

        return features.reduce(null, (bestSplit, feature) -> {
            double featureImpurity =
                    split(feature, data).values().stream()
                        .mapToDouble(functions -> impurity.impurity(value, functions::stream)).average().orElse(Double.POSITIVE_INFINITY);
            if (featureImpurity < purest[0]) {
                purest[0] = featureImpurity;
                return feature;
            } else
                return bestSplit;
        });
    }

    public void explain(BiConsumer<List<Pair<DecisionNode<V>,Object>>, DecisionNode.LeafNode<V>> c) {
        root.explain(c, new Lst());
    }

    public void print() {
        print(System.out);
    }

    public void print(PrintStream o) {
        printSubtree(root, o);
    }

    /**
     * requires V to be Comparable
     */
    public SortedMap<DecisionNode.LeafNode<V>, List<Pair<DecisionNode<V>,Object>>> explanations() {
        SortedMap<DecisionNode.LeafNode<V>, List<Pair<DecisionNode<V>,Object>>> explanations = new TreeMap();
        explain((path, result) -> explanations.computeIfAbsent(result, (z)->new Lst() /* clone it */).add(pair(result,new Lst(path))));
        return explanations;
    }

    /** var is the name of the target value */
    public SortedMap<DecisionNode.LeafNode<V>, List<String>> explainedConditions() {
        SortedMap<DecisionNode.LeafNode<V>, List<String>> map = new TreeMap<>();
        explanations().forEach((result, value) -> {
            List<String> cond = value.stream().map(c ->
                c.getOne().condition(c.getTwo())
            ).filter(x -> !x.equals("false")).collect(toList());
            if (!cond.isEmpty())
                map.put(result, cond);
        });
        return map;
    }

    public static class DecisionNode<V> extends UnifriedMap<Object,DecisionNode<V>> implements Comparable<V> {



        /**
         * Node's feature used to split it further.
         */
        public final Function feature;

        public final V label;
        private final int hash;

        DecisionNode(Function feature) {
            this(feature, null);
        }

        DecisionNode(V label) {
            this(null, label);
        }

        private DecisionNode(@Nullable Function feature, @Nullable V label) {
            super();
            this.label = label;
            this.feature = feature;
            this.hash = Objects.hash(label, feature);
        }

        public Stream<DecisionNode<V>> recurse() {

            return Stream.concat(Stream.of(this), values().stream().flatMap(DecisionNode::recurse));
        }

//        public Stream<Pair<Object,DecisionNode<V>>> stream() { return entrySet().stream(); }
//
//        @Override
//        public Iterator<DecisionNode<V>> iterator() {
//            return children.iterator();
//        }
//
//        public int size() { return children.size(); }
//
//        public boolean isEmpty() {
//            return children.isEmpty();
//        }

        public boolean isLeaf() {
            return feature==null || isEmpty();
        }

        public String toString() {
            return (feature != null ? feature : label).toString();
        }

        @Override
        public final int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object that) {
            if (this == that) return true;

            DecisionNode dThat = (DecisionNode) that;
            return hash==dThat.hash && Objects.equals(feature, dThat.feature) && Objects.equals(label, (dThat.label));
        }

        @Override
        public int compareTo(Object o) {
            if (o == this) return 0;
            DecisionNode n = (DecisionNode) o;
            if (feature != null)
                return ((Comparable)feature).compareTo(n.feature);
            else
                return ((Comparable) label).compareTo(n.label);
        }

        public void explain(BiConsumer<List<Pair<DecisionNode<V>,Object>>, LeafNode<V>> c, Lst<Pair<DecisionNode<V>,Object>> path) {
            for (Pair<Object,DecisionNode<V>> x : keyValuesView())
                explain(c, path, x);
        }

        /** compose an expression that matches the condition of the decision of this node */
        public String condition(Object x) {
            return x.toString();
//            assert(feature!=null);
//
//            if (feature instanceof CentroidMatch)
//                return ((CentroidMatch)feature).condition(isTrue);
//            else if (feature instanceof EnumFeature.StringMatch) {
//                return ((EnumFeature.StringMatch)feature).condition(isTrue);
//            }
//
//            throw new TODO();
        }

        public void explain(BiConsumer<List<Pair<DecisionNode<V>, Object>>, LeafNode<V>> c, Lst<Pair<DecisionNode<V>,Object>> path, Pair<Object,DecisionNode<V>> child) {
            path.add(pair(this, child));
            child.getTwo().explain(c, path);
            path.removeLastFast();
        }

        public static class LeafNode<V> extends DecisionNode<V> {
            public LeafNode(V label) {
                super(label);
            }

            @Override
            public void explain(BiConsumer<List<Pair<DecisionNode<V>,Object>>, LeafNode<V>> c, Lst<Pair<DecisionNode<V>,Object>> path) {
                c.accept(path, this);
            }

//            @Override
//            public boolean equals(Object that) {
//                return this == that;
//            }

            @Override
            public int compareTo(Object o) {
                if (this == o) return 0;
                if (!(o instanceof LeafNode)) return -1;
                return ((Comparable) label).compareTo(((DecisionNode) o).label);
            }

            @Override
            public String toString() {
                return label.toString();
            }


        }
    }

    public void printExplanations() {
        printExplanations(System.out);
    }

    public void printExplanations(PrintStream out) {
        explainedConditions().forEach((leaf, path) -> out.println(leaf + "\n\t" + path));
    }
}