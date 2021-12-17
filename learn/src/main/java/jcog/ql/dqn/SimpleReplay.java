package jcog.ql.dqn;

import jcog.data.list.Lst;
import jcog.random.RandomBits;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

class SimpleReplay extends Replay {

    final Lst<ReplayMemory> memory;

    public SimpleReplay(int cap, float rememberProb, int trainIters) {
        super(1, rememberProb, trainIters);
        this.memory = new Lst<>(cap);
    }

    @Override
    @Nullable
    protected ReplayMemory sample(Random rng) {
        return memory.get(rng);
    }

    @Override
    public int capacity() {
        return memory.capacity();
    }

    @Override
    public int size() {
        return memory.size();
    }

    @Override
    protected void add(ReplayMemory m, double[] qNext) {
        memory.add(m);
    }

    @Override
    protected void pop(RandomBits rng) {
        memory.removeRandom(rng);
    }

}