package jcog.rl.replay;

import jcog.rl.ValuePredictAgent;

import java.util.Random;
import java.util.random.RandomGenerator;

import static jcog.Util.sqr;

/**
 * experience replay buffer
 */
public abstract class Replay {
    private static final boolean ageDecay = false;
    float replayAlpha;
    float rememberProb;
    int trainIters;
    public long t;

    public Replay(float replayAlpha, float rememberProb, int trainIters) {
        this.replayAlpha = replayAlpha;
        this.rememberProb = rememberProb;
        this.trainIters = trainIters;
    }

    protected void playback(ValuePredictAgent agent) {

        int trainIters = this.trainIters;
        double alpha = replayAlpha();
        int capacity = capacity();
        double dur = capacity / rememberProb;

        float memoryUsage = ((float)size()) / capacity;
        float memoryUsageFactor = sqr(memoryUsage);

        //trainIters = Math.min(size(), trainIters); //(int) Math.ceil(trainIters * memoryUsage);

        for (int i = 0; i < trainIters; i++) {
            ReplayMemory m = sample(agent.rng);
            float pri;
            if (ageDecay) {
                long age = t - m.t;
                double ageDiscount = Math.exp(-age / dur);
                pri = (float) (alpha * ageDiscount);
            } else {
                pri = (float) alpha;
            }

            //normalize by memory utilization
            pri *= memoryUsageFactor;

            rerun(m, pri, agent);
        }

    }

    protected void rerun(ReplayMemory m, float pri, ValuePredictAgent agent) {
        agent.run(m, pri, null);
    }

    protected abstract ReplayMemory sample(Random rng);

    abstract public int capacity();

    abstract public int size();

    float replayAlpha() {
        return this.replayAlpha;
        //* ((float)memory.size())/capacity; //discount for incompletely filled memory
    }

    public void run(ValuePredictAgent agent, double[] action, float reward, double[] i, double[] iPrev, double[] qNext) {
        if (size() > 0)
            playback(agent);

        tryRemember(agent, action, reward, i, iPrev, qNext);

        t++;
    }

    private void tryRemember(ValuePredictAgent agent, double[] action, float reward, double[] i, double[] iPrev, double[] qNext) {
        int s = size(), c = capacity();
        if (s < c || agent.rng.nextFloat() <= rememberProb) {

            if (s >= c)
                pop(agent.RNG);

            //TODO clone() e
            add(new ReplayMemory(t, iPrev.clone(), action.clone(), reward, i.clone()), qNext);
        }
    }

    protected abstract void add(ReplayMemory m, double[] qNext);

    protected abstract void pop(RandomGenerator rng);


}