package jcog.rl.misc;

import jcog.Util;
import jcog.math.optimize.MyAsyncCMAESOptimizer;
import jcog.rl.Policy;
import org.hipparchus.optim.InitialGuess;
import org.hipparchus.optim.MaxEval;
import org.hipparchus.optim.SimpleBounds;
import org.hipparchus.optim.nonlinear.scalar.GoalType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Random;

import static jcog.Str.n2;
import static jcog.Str.n4;

/**
 * searches steady-state point solutions using CMAES
 */
public class CMAESPolicy implements Policy {

    /** how many iterations to try each individual for */
    int individualIterations = 10; //TODO tune

    /** population size */
    final int capacity = 4;

    float SIGMA = 0.1f; //TODO tune

    MyAsyncCMAESOptimizer opt = null;

    /**
     * population, representing policies
     */
    double[][] pi = null;


    /** reward accumulator per individual */
    transient double[] individualReward = null;

    transient int iteration = 0, individualCurrent = 0;

    @Override
    public void clear(Random rng) {
        iteration = 0;
        individualCurrent = 0;
        individualReward = new double[capacity];
    }

    @Override
    public double[] learn(@Nullable double[] xPrev_ignored, double[] actionPrev, double reward, double[] x, float pri) {

        int actions = actionPrev.length;

        if (opt == null) {
            double[] sigma = new double[actions]; Arrays.fill(sigma, SIGMA);

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
            min = new double[actions]; //Arrays.fill(min, 0);
            max = new double[actions]; Arrays.fill(max, 1);
            mid = new double[actions]; Arrays.fill(mid, 0.5f);

            opt.optimize(//func,
                    GoalType.MAXIMIZE,
                    new MaxEval(Integer.MAX_VALUE /* HACK */),
                    new SimpleBounds(min, max),
                    new InitialGuess(mid)
            );
        }

        if (this.pi == null) {
            //HACK not initialized yet
            double[] a = new double[actions];
            return a;
        }


        //accumulate reward for the current individual
        individualReward[individualCurrent] += reward /* * pri ? */; //TODO -reward if minimize?

        if ((iteration + 1) % individualIterations == 0) {

            //accumulate rewards for the population individual
            int individualNext = (individualCurrent + 1) % pi.length;

            //on returning to the individual #0, commit accumulated rewards and iterate
            if (individualNext == 0) {
                System.out.println(
                        "CMAES " +
                        "avg=" + n4(Util.mean(individualReward)) +
                        " max=" + n4(Util.max(individualReward)) +
                        " best=" + n2(opt.best()));

                opt.commit(individualReward); //finished batch
                opt.doOptimize();
            }

            individualReward[individualNext] = 0; //reset
            individualCurrent = individualNext;
        }

        iteration++;

        return act(x, this.pi[individualCurrent]);
    }


    /**
     * determine an action vector for the state applied to a given policy
     */
    private double[] act(double[] x /* state */, double[] policy) {
        return policy; //direct, ignores state
        //return f(policy, x); //function of state
        //TODO other impl
    }

}
