package jcog.rl.dqn;

import jcog.Util;
import jcog.pri.PLink;
import jcog.pri.bag.impl.PriReferenceArrayBag;
import jcog.pri.op.PriMerge;
import jcog.rl.ValuePredictAgent;
import jcog.rl.replay.Replay;
import jcog.rl.replay.ReplayMemory;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * prioritized replay using a Bag<></>
 */
public class BagReplay extends Replay {

    private final PriReferenceArrayBag<ReplayMemory, PLink<ReplayMemory>> memory;

    public BagReplay(int cap, int replaysPerIteration) {
        this(cap, 1, replaysPerIteration);
    }

    public BagReplay(int cap, float rememberProb, int replaysPerIteration) {
        super(1, rememberProb, replaysPerIteration);
        this.memory = new PriReferenceArrayBag<>(PriMerge.replace, cap);
    }

    @Override
    @Nullable
    protected ReplayMemory sample(Random rng) {
        return memory.sample(rng).get();
    }

    @Override
    public int capacity() {
        return memory.capacity();
    }

    @Override
    public int size() {
        return memory.size();
    }

    protected double importance(double[] q) {
        double qMean = Util.sumAbs(q) / q.length;
        //return Math.log(1 + qMean);
        return qMean/2;
    }

    @Override
    protected void add(ReplayMemory m, double[] qNext) {
        put(m, qNext);
    }

    @Override
    protected void rerun(ReplayMemory m, float pri, ValuePredictAgent agent) {
        double[] qTmp = new double[agent.actions];
        agent.run(m, pri, qTmp);

        float nextPri = (float) importance(qTmp);
        memory.get(m).pri(nextPri);
    }

    @Override
    protected void playback(ValuePredictAgent agent) {
        super.playback(agent);
        //System.out.println(memory.size() + " " + memory.pressure() + " " + memory.mass());
        //memory.commit(null);
        memory.commit(); //with forget
    }

    private void put(ReplayMemory m, double[] qNext) {
        memory.put(new PLink<>(m, importance(qNext)));
        //memory.commit(null);
    }

    @Override
    protected void pop(RandomGenerator rng) {
//        ReplayMemory last = memory.get(memory.size() - 1).get();
//        memory.remove(last);
        //System.out.println(memory.mass());
    }
}