package jcog.nn;

import jcog.Fuzzy;
import jcog.Is;
import jcog.Util;
import jcog.data.DistanceFunction;
import jcog.data.list.Lst;
import jcog.predict.Predictor;
import jcog.pri.NLink;
import jcog.pri.PLink;
import jcog.pri.bag.impl.ArrayBag;
import jcog.pri.bag.impl.PriArrayBag;
import jcog.pri.op.PriMerge;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.util.ArrayUtil;
import org.eclipse.collections.api.tuple.Twin;
import org.hipparchus.linear.RealMatrix;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToDoubleBiFunction;
import java.util.function.UnaryOperator;

import static org.eclipse.collections.impl.tuple.Tuples.twin;


/**
 * NEAT-like (neuroevolutionary someting) Hack
 *  TODO make decision tree predictor that shares the example memory with this
 */
public class NEAT implements Predictor {

    /**
     * epsilon
     */
    static final double stableThreshold = Float.MIN_NORMAL;
    static final private boolean learnContinously = true;
    @Deprecated
    private static final boolean deduplicateExhaustive = true;
    final int inputs, outputs;
    final int memoryCapacity;
    final int populationSize;
    /**
     * training example memory bag TODO abstract for different storage types:
     * list
     * set
     * k-means cluster centroids
     */
    final ArrayDeque<NLink<Twin<double[]>>> memory;
    /**
     * genome bag
     */
    final PriArrayBag<NLink<Genome>> population;

    private final ToDoubleBiFunction<double[], double[]> errFn =
            DistanceFunction::distanceManhattan;

    Random rng = new XoRoShiRo128PlusRandom();
    /**
     * elitism
     */
    private float survivalRate = 0.1f;
    /**
     * super-elitism
     */
    private int untouchables = 1;

    public NEAT(int inputs, int outputs, int populationSize, int memoryCapacity) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.memoryCapacity = memoryCapacity;
        this.populationSize = populationSize;
        memory = new ArrayDeque<>(memoryCapacity);

        population = new PriArrayBag<>(PriMerge.replace, new ArrayBag.ListArrayBagModel<NLink<Genome>, NLink<Genome>>());
        population.capacity(populationSize);
        population.sharp = 0.5f;
    }

    /**
     * TODO abstract
     */
    protected double error(double[] y, double[] yActual) {
        return DistanceFunction.distanceManhattan(y, yActual);
        //return DistanceFunction.distanceCartesianSq(y, yActual);
    }

    /**
     * returns null if duplicate (nothing new to learn)
     * TODO optional deduplication impl using hashmap
     */
    @Nullable
    private NLink<Twin<double[]>> remember(double[] x, double[] y, float pri) {
        return rememberedAlready(x, y) ? null : _remember(x, y, pri);

    }

    private boolean rememberedAlready(double[] x, double[] y) {
        return deduplicateExhaustive && memory.stream().anyMatch(z ->
                ArrayUtil.equals(z.get().getOne(), x, stableThreshold) &&
                        ArrayUtil.equals(z.get().getTwo(), y, stableThreshold));
    }

    private NLink<Twin<double[]>> _remember(double[] x, double[] y, float pri) {
        if (memory.size() > memoryCapacity)
            memory.removeFirst();

        NLink<Twin<double[]>> ex = new NLink<>(twin(x, y), pri);
        memory.add(ex);
        return ex;
    }

    private void learnAll() {
        if (population.isEmpty()) {
            for (int i = 0; i < populationSize; i++)
                population.put(new PLink(seed(), 0));
        }

        //evaluate
        population.commit(l -> {
            Genome g = l.get();
            double err = 0;
            for (var each : memory) {
                Twin<double[]> example = each.get();
                var x = example.getOne();
                var y = example.getTwo();
                double[] yActual = g.apply(x);
                err += errFn.applyAsDouble(y, yActual);
            }

            g.score =
                    //-err;
                    1/(1+err);
            //System.out.println(memory.size() + " " + err + " " + g.score);

            l.pri((float) g.score);

            //System.out.println(g.score);
        });


        NLink<Genome> best = population.get(0);

        //System.out.println("score range: " + best.get().score + " .. " + population.get(population.size()-1).get().score);

        //cull
        population.capacity((int)Math.ceil(populationSize * survivalRate));

        //System.out.println(Joiner.on("\n").join(population));

        population.capacity(populationSize);
        population.commit(null); //just to be sure histogram is accurate for sampling

        //crossover respawn
        int survivors = population.size();
        int spawn = populationSize - survivors;
        Lst<Genome> born = new Lst(spawn);
        for (int i = 0; i < spawn; i++) {
            /* its ok if x==y because it just means only mutation will be applied */
            Genome x = population.sample(rng).get();
            Genome y = population.sample(rng).get();

            Genome xy = x.fuck(y);

//            if (x == y) {
//                //go fuck yourself
//                //TODO xy = x.clone()
//                xy.mutate();
//            }

            born.add(xy);
        }

        for (Genome xy : born)
            population.put(new PLink(xy, 0));


        //mutate survivors but not untouchables
        for (int i = untouchables; i < survivors; i++)
            population.get(i).get().mutate();

    }

    private Genome seed() {
        return new RecurrentNetworkGenome(inputs, outputs,
                //inputs+outputs,
                (int) Math.ceil((inputs + outputs) * 3),
                //(inputs+outputs+1),
                rng);
    }

    @Override
    public double[] put(double[] x, double[] y, float pri) {
        double[] yPredicted = get(x);
        putAsync(x, y, pri);
        return yPredicted;
    }

    public void putAsync(double[] x, double[] y, float pri) {
        var example = remember(x, y, pri);
        if (learnContinously || example != null)
            learnAll();
    }

    /**
     * TODO optional ensemble evaluation of top N genomes
     */
    @Override
    public double[] get(double[] x) {
        if (population.isEmpty())
            return randomOutput(); //wild guess

        return population.get(0).get().apply(x);
    }

    private double[] randomOutput() {
        return Util.arrayOf(i -> ThreadLocalRandom.current().nextDouble(), new double[outputs]);
    }

    @Override
    public void clear(Random rng) {
        this.rng = rng; //HACK borrow provided RNG
        memory.clear();
        population.clear();
    }

    /**
     * represents a potential solution to the function being approximated
     */
    abstract static class Genome implements UnaryOperator<double[]> {
        //final Ewma scoreMean;
        transient double score = 0;

        Genome() {
            //scoreMean = new Ewma().period(memoryCapacity/2 /* estimate */);
        }


        /**
         * crossover
         */
        abstract Genome fuck(Genome x);

        abstract void mutate();
    }

    /**
     * implements a potentially fully-connected graph via weight-matrix
     * with a maximum recurrent iteration limit.
     */
    class RecurrentNetworkGenome extends Genome {


        /**
         * percent of weigts mutated
         */
        static final float mutationRate = 0.05f;

        /**
         * max mutation amount relative to weight range
         */
        static final float mutationAmount = 0.5f;

        /**
         * approx how many crossover slices to merge two matrices across
         */
        @Is("Cut-up technique") static final float crossoverSlices = 3;

        final RecurrentNetwork net;

        RecurrentNetworkGenome(int inputs, int outputs, int hiddenNeurons, Random rng) {
            net = new RecurrentNetwork(inputs, outputs, hiddenNeurons, 3);
            if (rng != null)
                net.clear(rng);
        }

        @Override
        public double[] apply(double[] x) {
            return net.apply(x);
        }

        @Override
        Genome fuck(Genome _x) {

            assert (getClass() == _x.getClass());

            RecurrentNetworkGenome x = (RecurrentNetworkGenome) _x;
            assert (net.hiddens == x.net.hiddens);

            RecurrentNetworkGenome xy = new RecurrentNetworkGenome(
                    net.inputs, net.outputs,
                    net.hiddens, null);

            int n = net.n();
            RealMatrix src = null;
            RealMatrix tgt = xy.net.weights.getWeights();
            int ft = 0;
            int nextSlice = 0;
            for (int f = 0; f < n; f++) {
                for (int t = 0; t < n; t++) {

                    if (ft++ == nextSlice) {
                        //TODO slice size proportional by fitness?
                        nextSlice = ft + rng.nextInt(Math.round((n * n) / (crossoverSlices / 2)));
                        src = (rng.nextBoolean() ? this : x).net.weights.getWeights();
                    }

                    tgt.setEntry(f, t, src.getEntry(f, t));
                }
            }

            return xy;
        }

        @Override
        void mutate() {
            int n = net.n();
            for (int f = 0; f < n; f++) {
                for (int t = 0; t < n; t++) {
                    if (rng.nextFloat() < mutationRate)
                        net.weights.weightAdd(f, t,
                            mutationAmount * net.initWeightRange * Fuzzy.polarize(rng.nextFloat()));
                }
            }
        }
    }


}