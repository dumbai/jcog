package jcog.agent;

import jcog.data.list.Lst;
import jcog.math.FloatSupplier;
import org.eclipse.collections.api.block.function.primitive.IntIntToObjectFunction;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;

import java.util.List;
import java.util.function.IntConsumer;

import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;

/** TODO test */
public class AgentBuilder extends SensorBuilder {
    final List<IntObjectPair<? extends IntConsumer>> actions = new Lst();
    final FloatSupplier reward;
//    float durations = 1f;

    /** whether to add an extra NOP action */
    boolean NOP_ACTION = false;

    public AgentBuilder(FloatSupplier reward) {
        this.reward = reward;
    }

    /** builds the constructed agent */
    public Agenterator agent(IntIntToObjectFunction<Agent> controller) {
        return new Agenterator(controller, sensor(), reward, actions, NOP_ACTION, act);
    }



    final IntConsumer act = (rawAction) -> {
        //deserialize the raw action id to the appropriate action group
        // TODO do this with a stored skip count int[]
        int rawAction1 = rawAction;
        for (IntObjectPair<? extends IntConsumer> aa: actions) {
            int bb = aa.getOne();
            if (rawAction1 >= bb) {
                rawAction1 -= bb;
            } else if (rawAction1 >= 0) {
                aa.getTwo().accept(rawAction1);
            }
        }
    };

//    public AgentBuilder durations(float runEveryDurations) {
//        this.durations = runEveryDurations;
//        return this;
//    }

    public AgentBuilder out(Runnable decision) {
        actions.add(pair(1, (x) -> decision.run()));
        return this;
    }

    public AgentBuilder out(int decisions, IntConsumer decision) {
        actions.add(pair(decisions, decision));
        return this;
    }

    @Override
    public String toString() {
        return "AgentBuilder{" +
            "sensors=" + sensors +
            ", actions=" + actions +
            ", reward=" + reward +
            ", act=" + act +
            '}';
    }
}
