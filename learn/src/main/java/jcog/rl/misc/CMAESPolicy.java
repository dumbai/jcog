package jcog.rl.misc;

import jcog.Util;
import jcog.math.optimize.MyAsyncCMAESOptimizer;
import jcog.math.optimize.MyCMAESOptimizer;
import jcog.nn.RecurrentNetwork;
import jcog.rl.Policy;
import org.hipparchus.optim.InitialGuess;
import org.hipparchus.optim.MaxEval;
import org.hipparchus.optim.SimpleBounds;
import org.hipparchus.optim.nonlinear.scalar.GoalType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static jcog.Str.n4;

/**
 * searches steady-state point solutions using CMAES
 */
public class CMAESPolicy implements Policy {

    /** explore vs. exploit rate.  0..1 */
    float explore =
        0.75f;
        //0.5f

    /** how many iterations to try each individual for */
    int individualIterations = 8; //TODO tune

    /** population size */
    final int capacity = 5;

    float SIGMA = 0.1f; //TODO tune

    /** direct, or parameterized */
    @Deprecated static final boolean direct = false;

    private MyAsyncCMAESOptimizer opt = null;

    /**
     * population, representing policies
     */
    private double[][] pi = null;


    /** reward accumulator per individual */
    transient double[] individualReward = null;

    transient int iteration = 0, individualCurrent = 0;
    transient private MyCMAESOptimizer.FitEval eval;
    transient private double[] piBest = null;
    private boolean exploring = false;
    private RecurrentNetwork fn;

    @Override
    public void clear(Random rng) {
        iteration = 0;
        individualCurrent = 0;
        individualReward = new double[capacity];
    }

    @Override
    public double[] learn(@Nullable double[] xPrev_ignored, double[] actionPrev, double reward, double[] x, float pri) {
        double[] policy;

        int actions = actionPrev.length;

        if (exploring || piBest == null || opt.random.nextFloat() < explore/(individualIterations*capacity)) {

            exploring = true;

            if (opt == null) {

                int parameters = fn(x.length, actions);
                System.out.println("CMAES+NEAT: " + parameters + " parameters");

                double[] sigma = new double[parameters];
                Arrays.fill(sigma, SIGMA);

                opt = new MyAsyncCMAESOptimizer(1 /* HACK */, Double.NEGATIVE_INFINITY, capacity, sigma) {
                    @Override
                    protected boolean apply(double[][] p) {
                        pi = p;
                        return true;
                    }
                };

                //iterate(actions);
                double[] mid;
                double[] min;
                double[] max;
                min = new double[parameters]; //Arrays.fill(min, 0);
                max = new double[parameters];
                Arrays.fill(max, 1);
                mid = new double[parameters];
                Arrays.fill(mid, 0.5f);

                opt.optimize(//func,
                        GoalType.MAXIMIZE,
                        new MaxEval(Integer.MAX_VALUE /* HACK */),
                        new SimpleBounds(min, max),
                        new InitialGuess(mid)
                );
                eval = opt.newEval(null);
            }

            if (this.pi == null) {
                //HACK not initialized yet
                //TODO randomize?
                return new double[actions];
            }


            //accumulate reward for the current individual
            individualReward[individualCurrent] += reward /* * pri ? */; //TODO -reward if minimize?

            if ((iteration + 1) % individualIterations == 0) {

                //accumulate rewards for the population individual
                int individualNext = (individualCurrent + 1) % pi.length;

                //on returning to the individual #0, commit accumulated rewards and iterate
                if (individualNext == 0) {
                    piBest = opt.best();
                    System.out.println(
                            "CMAES " +
                                    "avg=" + n4(Util.mean(individualReward)) +
                                    " max=" + n4(Util.max(individualReward))
                                    //+ " best=" + n2(piBest)
                    );

                    opt.commit(individualReward); //finished batch

                    //opt.doOptimize(best);
                    eval.iterate();
                    exploring = false;
                }

                individualReward[individualNext] = 0; //reset
                individualCurrent = individualNext;
            }

            iteration++;
            policy = this.pi[individualCurrent];
        } else {
            policy = piBest;
        }

        return act(x, policy);
    }

    /** returns # of parameters */
    private int fn(int inputs, int actions) {
        if (direct)
            return actions; //DIRECT

        if (fn == null) {
            int loops = 2;
            int hidden = /*inputs +*/ actions;
            this.fn = new RecurrentNetwork(inputs, hidden, actions, loops);
            fn.clear(ThreadLocalRandom.current()); //HACK to calculate weightsEnabled subset
        }
        return fn.weights.weightsEnabled();
    }


    /**
     * determine an action vector for the state applied to a given policy
     */
    private double[] act(double[] x /* state */, double[] policy) {
        return direct ?
            policy : //DIRECT ignores state
            fn.weights(policy).get(x); //function of state

        //TODO other impl?
    }

}
