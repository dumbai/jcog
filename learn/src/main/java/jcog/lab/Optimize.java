package jcog.lab;

import jcog.Log;
import jcog.Util;
import jcog.WTF;
import jcog.data.list.Lst;
import jcog.decision.TableDecisionTree;
import jcog.lab.var.FloatVar;
import jcog.math.optimize.MyCMAESOptimizer;
import jcog.table.ARFF;
import jcog.table.DataTable;
import org.hipparchus.exception.MathIllegalStateException;
import org.hipparchus.optim.InitialGuess;
import org.hipparchus.optim.MaxEval;
import org.hipparchus.optim.SimpleBounds;
import org.hipparchus.optim.SimpleValueChecker;
import org.hipparchus.optim.nonlinear.scalar.GoalType;
import org.hipparchus.optim.nonlinear.scalar.ObjectiveFunction;
import org.hipparchus.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.hipparchus.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.hipparchus.util.MathArrays;
import org.slf4j.Logger;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * procedure optimization context
 *   sets controls, runs an experiment procedure, records the observation.
 *
 * @param S subject of the experiment
 * @param E experiment containing the subject
 *          S and E may be the same type if no distinction is necessary
 *
 * goal (score) is in column 0 and it assumed to be maximized. for minimized, negate model's score function
 *
 * TODO - max timeout parameter w/ timer that kills if time exceeded
 *
 * https://ax.dev/docs/core.html
 */
public class Optimize<S, X> extends Lab<X>  {

    private static final Logger logger = Log.log(Optimize.class);

    /**
     * history of experiments. TODO use ranking like TopN etc
     */
    public DataTable data = new ARFF();

    private final Supplier<S> seed;
    private final Function<Supplier<S>, X> subjectBuilder;

    /** active sensors */
    private final Lst<Sensor<X, ?>> in = new Lst();

    /** active variables */
    public final List<Var<S, ?>> out;

    /** internal feedback: out -> in */
    private final List<Sensor<S, ?>> varSensors;

    /** objective */
    public final Goal<X> goal;


    private double[] inc;
    private double[] min;
    private double[] max;
    private double[] mid;

    public Optimize(Supplier<S> seed,
                    Function<Supplier<S>, X> subjectBuilder,
                    Goal<X> g,
                    List<Var<S, ?>> v /* unsorted */,
                    List<Sensor<X, ?>> s) {
        this.seed = seed;

        for (Var vv : v)
            vars.put(vv.id, vv);

        this.goal = g;

        this.out = new Lst<>(v).sortThis();

        /* sorted */
        List<Sensor<S, ?>> list = out.stream().map(Var::sense).collect(toList());
        this.varSensors = list;

        this.in.addAll( s );
        this.in.sortThis();


        this.subjectBuilder = subjectBuilder;

    }

//    /** repeats a function N times, and returns the mean of the finite-valued attempts */
//    public static <X> FloatFunction<Supplier<X>> repeat(FloatFunction<Supplier<X>> f, int repeats, boolean parallel) {
//        return X -> {
//            IntStream s = IntStream.range(0, repeats);
//            if (parallel) s = s.parallel();
//            return (float)(s.mapToDouble(i -> f.floatValueOf(X))
//                .filter(Double::isFinite)
//                .average().getAsDouble());
//        };
//    }

    @Override
    public Optimize<S, X> sense(Sensor sensor) {
        super.sense(sensor);
        in.add(sensor); //HACK
        return this;
    }

    public Optimize<S, X> runSync(int maxIters) {
        return runSync(optimizer(maxIters));
    }

    public Optimize<S, X> runSync(OptimizationStrategy strategy) {
        run(strategy);
        return this;
    }

    private void run(OptimizationStrategy strategy) {

        //initialize numeric or numeric-able variables
        int numVars = out.size();

        mid = new double[numVars];
        min = new double[numVars];
        max = new double[numVars];
        inc = new double[numVars];

        S example = seed.get();
        int i = 0;
        for (Var w: out) {
            FloatVar s = (FloatVar) w;


            Object guess = s.get(example);


            double mi = min[i] = s.getMin();
            double ma = max[i] = s.getMax();
            double inc = this.inc[i] = s.getInc();

            if (guess!=null && (mi!=mi || ma!=ma || inc!=inc)) {
                float x = (float)guess;
                //HACK assumption
                mi = min[i] = x/2;
                ma = max[i] = x*2;
                inc = this.inc[i] = x/4;
            }

            mid[i] = guess != null ? Util.clamp((float) guess, mi, ma) : (mi + ma) / 2f;

            if ((mid[i] < min[i]) || (max[i] < mid[i])) throw new WTF();

            i++;
        }

        synchronized(data) {
            goal.register(data);
            for (Sensor<S, ?> varSensor : varSensors)
                varSensor.register(data);
            for (Sensor<X, ?> s : in)
                s.register(data);

            if (logger.isTraceEnabled()) {
                String s = data.columnNames().toString();
                logger.trace("{}", s.substring(1, s.length()-1));
            }
        }

        strategy.run(this);

    }

    private double run(double[] point) {

        try {
            Object[] ss = new Object[1], xx = new Object[1];
            Supplier<X> y = () -> {
                var x = subjectBuilder.apply(() -> {
                    S s = subject(seed.get(), point);
                    ss[0] = s; //for measurement
                    return s;
                });
                xx[0] = x;
                return x;
            };

            double score = goal.apply(y).doubleValue();

            Object[] row = row((S)ss[0], (X)xx[0], score);

            synchronized(data) {
                data.add(row);
            }

            if (logger.isTraceEnabled()) {
                String rs = Arrays.toString(row);
                logger.trace("{}", rs.substring(1, rs.length()-1));
            }

            return score;

        } catch (RuntimeException t) {
            //System.err.println(t.getMessage());
            /* enable to print exceptions */
            logger.error("", t);
            return Double.NaN;
        }

    }

    private Object[] row(S x, X y, double score) {
        Object[] row = new Object[1 + out.size() + in.size()];
        int j = 0;
        row[j++] = score;
        for (Sensor v: varSensors) row[j++] = v.apply(x);
        for (Sensor s: in)         row[j++] = s.apply(y);
        return row;
    }


    /**
     * builds an experiment subject (input)
     * TODO handle non-numeric point entries
     */
    private S subject(S x, double[] point) {
        for (int i = 0, dim = point.length; i < dim; i++)
            point[i] = ((Var<S, Float>) out.get(i)).set(x, (float) point[i]);
        return x;
    }

    public Row best() {
        return sorted().iterator().next();
    }

    public DataTable sorted() {
        return new DataTable(data.sortDescendingOn(data.column(0).name()));
    }

    public Optimize<S, X> print() {
        return print(System.out);
    }

    public Optimize<S, X> print(PrintStream out) {
        out.println(data.print(Integer.MAX_VALUE));
        return this;
    }

    public TableDecisionTree tree(int discretization, int maxDepth, int predictColumn) {
        return tree(data, discretization, maxDepth, predictColumn);
    }

    public static TableDecisionTree tree(Table data, int discretization, int maxDepth, int predictColumn) {
        return data.isEmpty() ? null :
                new TableDecisionTree(data,
                        predictColumn /* score */, maxDepth, discretization);
    }



    /** string representing the variables manipulated in this experiment */
    public String varKey() {
        return out.toString();
    }


//    /**
//     * remove entries below a given percentile
//     */
//    public void cull(float minPct, float maxPct) {
//
//        int n = data.data.size();
//        if (n < 6)
//            return;
//
//        Quantiler q = new Quantiler((int) Math.ceil((n - 1) / 2f));
//        data.forEach(r -> {
//            q.addAt(((Number) r.get(0)).floatValue());
//        });
//        float minValue = q.quantile(minPct);
//        float maxValue = q.quantile(maxPct);
//        data.data.removeIf(r -> {
//            float v = ((Number) r.get(0)).floatValue();
//            return v <= maxValue && v >= minValue;
//        });
//    }

//    public List<DecisionTree> forest(int discretization, int maxDepth) {
//        if (data.isEmpty())
//            return null;
//
//        List<DecisionTree> l = new FasterList();
//        int attrCount = data.attrCount();
//        for (int i = 1; i < attrCount; i++) {
//            l.addAt(
//                    new RealDecisionTree(data.toFloatTable(0, i),
//                            0 /* score */, maxDepth, discretization));
//        }
//        return l;
//    }


    public abstract static class OptimizationStrategy {

        public abstract void run(Optimize eOptimize);
    }

    abstract static class ApacheCommonsMathOptimizationStrategy extends OptimizationStrategy {


        @Override
        public void run(Optimize o) {
            run(o, new ObjectiveFunction(o::run));
        }

        protected abstract void run(Optimize o, ObjectiveFunction func);
    }

    public static class SimplexOptimizationStrategy extends ApacheCommonsMathOptimizationStrategy {
        private final int maxIter;

        public SimplexOptimizationStrategy(int maxIter) {
            this.maxIter = maxIter;
        }

        @Override
        protected void run(Optimize o, ObjectiveFunction func) {

            /* cache the points, becaues if integers are involved, it could confuse the simplex solver when it makes duplicate samples */
            //TODO discretization must be applied correctly here for this to work
//            func = new ObjectiveFunction( cache( func.getObjectiveFunction() ) );

//            try {
                int dim = o.inc.length;
                //double[] range = new double[dim];
                double[] step = new double[dim];
                double[] init = new double[dim];
                for (int i = 0; i < dim; i++) {
                    double min = o.min[i];
                    double max = o.max[i];
                    double range = max - min;
                    step[i] = range / o.inc[i];
                    init[i] = (Math.random() * range) + min;
                }


                try {
                    new SimplexOptimizer(new SimpleValueChecker(1e-10, 1e-30, maxIter))
                            .optimize(
                                    new MaxEval(maxIter),
                                    func,
                                    GoalType.MAXIMIZE,
                                    new InitialGuess(init),
                                    //new MultiDirectionalSimplex(steps)
                                    new NelderMeadSimplex(step)
                            );
                } catch (MathIllegalStateException e) {

                }

        }

//        private static MultivariateFunction cache(MultivariateFunction objectiveFunction) {
//            return new MultivariateFunction() {
//
//                final ObjectDoubleHashMap<ImmutableDoubleList> map = new ObjectDoubleHashMap<>();
//
//                @Override
//                public double value(double[] point) {
//                    return map.getIfAbsentPutWithKey(DoubleLists.immutable.of(point),
//                            p-> objectiveFunction.value(p.toArray()));
//                }
//            };
//
//        }

    }

    public static class CMAESOptimizationStrategy extends ApacheCommonsMathOptimizationStrategy {
        private final int maxIter;

        public CMAESOptimizationStrategy(int maxIter) {
            this.maxIter = maxIter;
        }

        @Override
        protected void run(Optimize o, ObjectiveFunction func) {

            int popSize =
                    (int) Math.ceil(4 + 3 * Math.log(o.out.size()));


            double[] sigma = MathArrays.scale(1f, o.inc);

            MyCMAESOptimizer m = new MyCMAESOptimizer(maxIter, Double.NaN, popSize, sigma);
            m.optimize(func,
                    GoalType.MAXIMIZE,
                    new MaxEval(maxIter),
                    new SimpleBounds(o.min, o.max),
                    new InitialGuess(o.mid)
            );


        }
    }

    public TableDecisionTree tree(int discretization, int maxDepth) {
        return tree(discretization, maxDepth, 0);
    }
    public Optimize report() {

        var e = this;
        for (int d = 2; d < 7; d++) {
            var t = e.tree(4, d);
            if (t!=null) {
                t.print();
                t.printExplanations();
                System.out.println();
            }
        }

        var data = e.data.sortDescendingOn(e.goal.id);

        try {
            File f = new File("/tmp/x." + System.currentTimeMillis() + ".csv");
            data.write().csv(f);
            System.out.println("written: " + f.getAbsolutePath());
        } catch (IOException ee) {
            ee.printStackTrace();
        }

        System.out.println(data.printAll());
        data.write().csv(System.out);
        return this;
    }

    //
//    public static class GPOptimizationStrategy extends OptimizationStrategy {
//        //TODO
//    }
}

//package jcog.lab;
//
//import jcog.io.arff.ARFF;
//import jcog.data.list.FasterList;
//import jcog.lab.var.VarFloat;
//import jcog.lab.util.MyCMAESOptimizer;
//import org.eclipse.collections.api.tuple.primitive.ObjectFloatPair;
//import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.Callable;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.Future;
//import java.util.function.Function;
//import java.util.function.Supplier;
//
///**
// * Optimization solver wrapper w/ lambdas
// * instance of an experiment
// */
//public class Optimize<X> {
//
//    /**
//     * if a tweak's 'inc' (increment) is not provided,
//     * use the known max/min range divided by this value as 'inc'
//     * <p>
//     * this controls the exploration rate
//     */
//    private final static Logger logger = LoggerFactory.getLogger(Optimize.class);
//    final List<Tweak<X, ?>> tweaks;
//    final Supplier<X> subject;
//
//    protected Optimize(Supplier<X> subject,  List<Tweak<X, ?>> t) {
//        this.subject = subject;
//        this.tweaks = t;
//    }
//
//
//    /**
//     * TODO support evaluator that collects data during execution, and return score as one data field
//     *
//     * @param data
//     * @param maxIterations
//     * @param repeats
//     * @param seeks
//     * @param exe
//     * @return
//     */
//    public <Y> Lab.Result run(final ARFF data, int maxIterations, int repeats,
//
//                              Function<X,Y> experiment,
//                              Sensor<Y, ?>[] seeks,
//                              Function<Callable,Future> exe) {
//
//
//        assert (repeats >= 1);
//
//
//        experimentStart();
//
//        try {
//            solve(numTweaks, func, mid, min, max, inc, maxIterations);
//        } catch (RuntimeException t) {
//            logger.info("solve {} {}", func, t);
//        }
//
//        return new Lab.Result(data);
//    }
//
//    void solve(int dim, ObjectiveFunction func, double[] mid, double[] min, double[] max, double[] inc, int maxIterations) {
//        if (dim == 1) {
//
//        } else {
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
//        }
//
//    }
//
//    /**
//     * called before experiment starts
//     */
//    protected void experimentStart() {
//    }
//

//
//}
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
//
//
//
//
//