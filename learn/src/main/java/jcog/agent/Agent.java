package jcog.agent;

import jcog.decide.Decide;
import jcog.normalize.FloatNormalizer;
import org.jetbrains.annotations.Nullable;

/**
 * lowest common denominator markov decision process / reinforcement learning agent interface
 * TODO support continuous output vectors
 */
public abstract class Agent {

    /**
     * input reward as first-order difference from previous?
     */
    @Deprecated
    static final boolean rewardDelta = false;
    public final int inputs;
    public final int actions;
    public final double[] actionPrev;
    public final double[] actionNext;

    @Nullable
    final FloatNormalizer rewardNormalizer =
            //new FloatNormalizer(...);
            null;

    public transient float reward = Float.NaN;

    protected Agent(int inputs, int actions) {
        this.inputs = inputs;
        this.actions = actions;
        this.actionPrev = new double[actions];
        this.actionNext = new double[actions];
    }


    /**
     * assumes the previous action decided had ideal compliance of the motor system and so no
     * transformation or reduction or noise etc was experienced.
     */
    @Deprecated
    public final int act(float reward, double[] input, Decide d) {
        System.arraycopy(actionNext, 0, actionPrev, 0, actionPrev.length);
        act(actionPrev, reward, input, actionNext);
        //System.out.println(Arrays.toString(actionNext));
        return d.applyAsInt(actionNext);
    }

    /**
     * @param action (input)
     * @param reward
     * @param input
     * @param qNext      (output)
     */
    public void act(double[] action, float reward, double[] input, double[] qNext) {

        if (rewardNormalizer != null) {
            reward = rewardNormalizer.valueOf(reward);
        }

        if (reward == reward) {
            double r = (rewardDelta ? rewardDelta(reward) : reward);
            apply(action, (float) r, input, qNext);
            actionFilter(qNext);
        }


        this.reward = reward;
    }

    /**
     * post-processing of action vector
     */
    protected void actionFilter(double[] actionNext) {

    }


    private double rewardDelta(double rewardNext) {
        double rewardPrev = this.reward;
        return (rewardPrev == rewardPrev) ? rewardNext - rewardPrev : 0;
    }


    /**
     * @param actionPrev actions actually acted in previous cycle
     * @param reward     reward associated with the previous cycle's actions
     * @param input      next sensory observation
     */
    protected abstract void apply(double[] actionPrev, float reward, double[] input, double[] qNext);

//    /** for reporting action vectors, when implementation supports. otherwise it will be a zero vector except one chosen entry
//     * by the basic markov process act() method */
//    public void act(float reward, double[] input, double[] outputs /* filled by this method */) {
//        throw new TODO();
//    }

    @Override
    public String toString() {
        return summary();
    }

    public String summary() {
        return getClass().getSimpleName() + "<in=" + inputs + ", act=" + actions + '>';
    }

//    /**
//     * construct a meta-agent, if possible, with the parameters of this agent exposed as controls, and metrics as sensors
//     */
//    @Nullable
//    public AgentBuilder meta() {
//        return null;
//    }

}