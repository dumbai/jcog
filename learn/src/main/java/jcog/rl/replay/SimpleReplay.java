package jcog.rl.replay;

import jcog.data.list.Lst;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.random.RandomGenerator;

public class SimpleReplay extends Replay {

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
    protected void pop(RandomGenerator rng) {
        memory.removeRandom(rng);
    }

}