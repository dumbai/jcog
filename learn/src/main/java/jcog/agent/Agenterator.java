package jcog.agent;

import jcog.decide.Decide;
import jcog.decide.DecideSoftmax;
import jcog.math.FloatSupplier;
import jcog.random.XoRoShiRo128PlusRandom;
import org.eclipse.collections.api.block.function.primitive.IntIntToObjectFunction;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;

import java.util.List;
import java.util.function.IntConsumer;

/**
 *  analogous to Iterator<X>, this wraps an agent as a special "iterator" of
 *  markov-decision sensorimotor frames.
 *
 *  </X>constructs a 'compiled' / 'hardwired' iterable agent, only need to call next() on each iteration */
public class Agenterator implements FloatSupplier {

    final Decide decider = new DecideSoftmax(0.5f, new XoRoShiRo128PlusRandom());

    public final Agent agent;
    protected final IntConsumer action;
    protected final FloatSupplier reward;
    private final SensorTensor in;

    public Agenterator(IntIntToObjectFunction<Agent> agentBuilder, SensorTensor in, FloatSupplier reward, List<IntObjectPair<? extends IntConsumer>> actions, boolean NOP_ACTION, IntConsumer action) {
        this.in = in;
        this.reward = reward;
        this.action = action;
        this.agent = agentBuilder.value(in.volume(),
            actions.stream().mapToInt(IntObjectPair::getOne).sum() + (NOP_ACTION ? 1 : 0)
        );
    }



    /** returns the next reward value */
    @Override public float asFloat() {
        float r;
        float reward1 = r = reward.asFloat();
        action.accept(agent.act(reward1, in.update().doubleArray(), decider));
        return r;
    }

}