package jcog.rl.misc;

import jcog.agent.Agent;
import jcog.decide.Decide;
import jcog.decide.DecideSoftmax;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.signal.FloatRange;

import java.util.Random;

/**
 * q-learning + SOM agent, cognitive prosthetic. designed by patham9
 */
@Deprecated public abstract class HaiQ extends Agent {

    protected final Random rng;

    public double[][] q;
    public double[][] et;

    /*
     * Gamma is the value of future reward. It can affect learning quite a bit,
     * and can be a dynamic or static value. If it is equal to one, the agent
     * values future reward JUST AS MUCH as current reward. This means, in ten
     * actions, if an agent does something good this is JUST AS VALUABLE as
     * doing this action directly. So learning doesn't work at that well at high
     * gamma values. Conversely, a gamma of zero will cause the agent to only
     * value immediate rewards, which only works with very detailed reward
     * functions.
     */
    public final FloatRange Gamma = new FloatRange(0, 0, 1);

    /**
     * the rate of decay (in conjunction with gamma) of the eligibility trace.
     * This is the amount by which the eligibility of a state is reduced each
     * time step that it is not being visited. A low lambda causes a lower
     * reward to propagate back to states farther from the goal. While this can
     * prevent the reinforcement of a path which is not optimal, it causes a
     * state which is far from the goal to receive very little reward. This
     * slows down convergence, because the agent spends more time searching for
     * a path if it starts far from the goal. Conversely, a high lambda allows
     * more of the path to be updated with higher rewards. This suited our
     * implementation, because our high initial epsilon was able to correct any
     * state values which might have been incorrectly reinforced and create a
     * more defined path to the goal in fewer episodes.
     */
    public final FloatRange Lambda = new FloatRange(0, 0, 1);

    /**
     * qlearning Alpha is the learning rate. If the reward or transition
     * function is stochastic (random), then alpha should change over time,
     * approaching zero at infinity. This has to do with approximating the
     * expected outcome of a inner product (T(transition)*R(reward)), when one
     * of the two, or both, have random behavior.
     */
    public final FloatRange Alpha = new FloatRange(0, 0, 1);

//    /**
//     * input selection; HaiQAgent will not use this in its override of perceive
//     */
//    public final Decide decideInput;


    /**
     * "vertical" action selection
     */
    public final Decide decideAction;
    int lastState;

    public HaiQ(int inputs, int actions) {
        super(inputs, actions);

        q = new double[inputs][actions];
        et = new double[inputs][actions];

        setQ(0.02f, 0.5f, 0.75f);
        rng = new XoRoShiRo128PlusRandom(1);


//        decideInput =
//                Decide.Greedy;


        decideAction =
            //new DecideEpsilonGreedy(0.03f, rng);
            new DecideSoftmax(0.5f, rng);


    }

    //    private void etScale(float s) {
//        for (int i = 0; i < inputs; i++) {
//            double[] eti = et[i];
//            for (int k = 0; k < actions; k++) {
//                eti[k] *= s;
//            }
//        }
//    }

    protected int nextAction(int state) {
        return decideAction.applyAsInt(q[state]);
    }

//    private int randomAction() {
//        return rng.nextInt(actions);
//    }

    private void update(float deltaQ, float alpha) {

        double[][] q = this.q;
        double[][] et = this.et;

        float alphaDelta = alpha * deltaQ;
        float gammaLambda = Gamma.floatValue() * Lambda.floatValue();


        for (int i = 0; i < inputs; i++) {
            double[] eti = et[i];
            double[] qi = q[i];

            for (int k = 0; k < actions; k++) {
                qi[k] += alphaDelta * eti[k];
                eti[k] *= gammaLambda;
            }
        }
    }


    public void setQ(float alpha, float gamma, float lambda) {
        Alpha.set(alpha);
        Gamma.set(gamma);
        Lambda.set(lambda);
    }

    /**
     * main control function
     */
    @Override
    public void apply(double[] actionPrev, float reward, double[] input, double[] qNext) {
        int state = perceive(input);


        if (reward != reward)
            reward = 0;


        int action = nextAction(state); //decide ? nextAction(state) : -1;
        //Arrays.fill(qNext, 0); qNext[action] = 1;
        System.arraycopy(q[state], 0, qNext, 0, qNext.length);
//
        int lastState1 = this.lastState;

        float alpha = Alpha.floatValue();
        double deltaSum = 0;
        for (int lastAction = 0, actionFeedbackLength = actionPrev.length; lastAction < actionFeedbackLength; lastAction++) {
            double f = actionPrev[lastAction];
            double ff = Math.abs(f);
            if (ff > Float.MIN_NORMAL) {
                double lastQ = q[lastState1][lastAction];
                double delta = reward + f * ((Gamma.floatValue() * q[state][action]) - lastQ);
                deltaSum += delta;
                double nextQ = lastQ + alpha * delta;
                q[state][action] = (float) nextQ;
                et[lastState1][lastAction] += f * alpha;
            }

        }
        update((float)deltaSum, alpha);


//        if (decide) {
//            this.lastState = state;
//        }

//        return action;
    }

    /** encode the input vector to an internal discrete state */
    abstract protected int perceive(double[] input);
//    {
//        return decideInput.applyAsInt(input);
//    }


    /**
     * TODO make abstract
     */
    /*protected int perceive(double[] input) {
        som.learn(input);
		return som.winnerx + (som.winnery * som.SomSize);
	}*/

    public HaiQ alpha(float a) {
        this.Alpha.set(a);
        return this;
    }

    public HaiQ lambda(float a) {
        this.Lambda.set(a);
        return this;
    }

    public HaiQ gamma(float a) {
        this.Gamma.set(a);
        return this;
    }

}