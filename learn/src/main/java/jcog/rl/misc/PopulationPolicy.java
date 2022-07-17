package jcog.rl.misc;

import jcog.Fuzzy;
import jcog.TODO;
import jcog.Util;
import jcog.activation.SigmoidActivation;
import jcog.math.optimize.MyAsyncCMAESOptimizer;
import jcog.math.optimize.MyCMAESOptimizer;
import jcog.nn.RecurrentNetwork;
import jcog.rl.Policy;
import jcog.signal.FloatRange;
import org.hipparchus.optim.nonlinear.scalar.GoalType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static jcog.Str.n4;

public class PopulationPolicy implements Policy {

    final Population pop;

    /** explore vs. exploit rate.  0..1 */
    float explore =
        1;
        //0.95f;
        //0.75f;
        //0.5f

    /** how many iterations to try each individual for
     *  TODO tune this in proportion to aggregate reward variance, starting with a small value
     * */
    int episodePeriod = 1024; //TODO tune

    /** population size */
    final int capacity = 256;

    /** reward accumulator per individual */
    private transient double[] individualRewards = null;

    private transient int iteration = 0, individualCurrent = 0;

    private transient double[] piBest = null;
    private transient boolean exploring = false;
    private transient int timeUntilExplore = 0;

    float weightRange =
        Util.PHIf * 16;
        //Util.PHIf * 2;
        //Util.PHIf;

    public RecurrentNetwork fn;

    public PopulationPolicy(Population p) {
        pop = p;
    }

    @Override
    public void clear(Random rng) {
        iteration = 0;
        individualCurrent = 0;
        individualRewards = new double[capacity];
    }

    @Override
    public double[] learn(@Nullable double[] xPrev_ignored, double[] actionPrev, double reward, double[] x, float pri) {
        double[] policy;

        int actions = actionPrev.length;

        if (exploring || piBest == null || timeUntilExplore-- <= 0) {

//            if (!exploring)
//                System.out.println("explore start");

            exploring = true;

            if (!pop.initialized) {
                int parameters = fn(x.length, actions);
                pop.init(parameters, capacity, weightRange);
                pop.initialized = true;
            }

//            if (this.pi == null)
//                return new double[actions]; //HACK not initialized yet  //TODO randomize?

            //accumulate reward for the current individual
            individualRewards[individualCurrent] += reward /* * pri ? */; //TODO -reward if minimize?

            if ((iteration + 1) % episodePeriod == 0) {

                //accumulate rewards for the population individual
                int individualNext = (individualCurrent + 1) % capacity;

                /* after evaluating the entire population (upon returning to the individual #0),
                   commit accumulated rewards and iterate */
                if (individualNext == 0) {

                    Util.mul(1f/ episodePeriod, individualRewards); //normalize to reward per iteration

                    piBest = pop.best();
                    System.out.println(
                        PopulationPolicy.class.getSimpleName() + "\t"
                                /*"avg="*/ + n4(Util.mean(individualRewards)) +
                                /*" max="*/ "," + n4(Util.max(individualRewards))
                                //+ " best=" + n2(piBest)
                    );

                    pop.commit(individualRewards); //finished batch

                    exploring = false;
                    timeUntilExplore = (int) Math.ceil((1 - explore) * (episodePeriod * capacity));
                }

                individualRewards[individualNext] = 0; //reset
                individualCurrent = individualNext;
            }

            iteration++;
            policy = pop.get(individualCurrent);
        } else {
            policy = piBest;
        }

        return act(x, policy);
    }


    /** returns # of parameters */
    private int fn(int inputs, int actions) {
//        if (direct)
//            return actions; //DIRECT

        if (fn == null) {
            boolean recurrent = false;
            boolean inputsDirectToOutput = false;
            int loops = recurrent ? 3 : 2;
            int hidden =
                //actions + 1;
                //actions;
                //actions * 2;
                Fuzzy.mean(inputs/4, actions);
                //inputs + actions;

            this.fn = new RecurrentNetwork(inputs, actions, hidden, loops);

            fn.activationFn(
                //LeakyReluActivation.the,
                SigmoidActivation.the,
                //LeakyReluActivation.the
                //SigLinearActivation.the
                SigmoidActivation.the
                //TanhActivation.the;
                //ReluActivation.the;
            );

            //HACK to calculate weightsEnabled subset, establish full connectivity
            if (!recurrent) {
                fn.connect[1][1] = 0;
                fn.connect[2][0] = fn.connect[2][1] = fn.connect[2][2] = 0;
            }
            if (inputsDirectToOutput)
                fn.connect[0][2] = 1;

            for (int i = 0; i < fn.connect.length; i++)
                for (int j = 0; j < fn.connect[0].length; j++) {
                    var ij = fn.connect[i][j];
                    if (ij > 0)
                        fn.connect[i][j] = 1;
                }
            fn.clear(ThreadLocalRandom.current());
        }
        return fn.weights.weightsEnabled();
    }


    /**
     * determine an action vector for the state applied to a given policy
     */
    private double[] act(double[] x /* state */, double[] policy) {
        return
//        direct ?
//            policy : //DIRECT ignores state
            fn.weights(policy).get(x); //function of state

        //TODO other impl?
    }

    abstract static class Population {

        public boolean initialized = false;

        public abstract void init(int parameters, int populationSize, float weightRange);

        public abstract double[] best();

        /** weighted sum of populations by rewards */
        public double[] bestComposite() {
            throw new TODO();
        }

        public abstract void commit(double[] individualRewards);

        public abstract double[] get(int individual);
    }

    /** TODO */
    abstract public static class NEATPopulation extends Population {

    }

    public static class CMAESPopulation extends Population {
        private MyAsyncCMAESOptimizer opt = null;
        private MyCMAESOptimizer.FitEval iter;

        /**
         * population, representing policies
         */
        private double[][] pi = null;


        /**
         * Standard deviation for all parameters (all parameters must be scaled accordingly).
         * Defines the search space as the std dev from an initial x0.
         * Larger values will sample a initially wider Gaussian.
         * TODO tune
         */
        public final FloatRange SIGMA = new FloatRange(0.1f, 0.0001f, 4f);


        @Override
        public void init(int parameters, int populationSize, float weightRange) {

            System.out.println("CMAES+NEAT: " + parameters + " parameters");

            double[] sigma = new double[parameters];
            Arrays.fill(sigma, SIGMA.floatValue());

            opt = new MyAsyncCMAESOptimizer(populationSize, sigma) {
                @Override
                protected boolean apply(double[][] p) {
                    pi = p;
                    return true;
                }
            };

            double[] min = new double[parameters]; Arrays.fill(min, -weightRange);
            double[] max = new double[parameters]; Arrays.fill(max, +weightRange);
            iter = opt.iterator(GoalType.MAXIMIZE, min, max);

//            iter = opt.iterator(GoalType.MAXIMIZE, new double[parameters]); //unbounded

            iter.iterate(); //INITIALIZE
        }

        @Override
        public double[] get(int individual) {
            return pi[individual];
        }

        @Override
        public double[] best() {
            return opt.best();
        }

        @Override
        public void commit(double[] individualRewards) {
            opt.commit(individualRewards);
            iter.iterate();
        }

    }
}
