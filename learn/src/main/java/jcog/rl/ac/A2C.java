package jcog.rl.ac;

import jcog.Util;
import jcog.activation.ReluActivation;
import jcog.activation.SigLinearActivation;
import jcog.activation.TanhActivation;
import jcog.nn.MLP;
import jcog.nn.optimizer.AdamOptimizer;
import jcog.rl.Policy;
import jcog.signal.FloatRange;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/** TODO not working yet */
public class A2C implements Policy {

    float actorAlpha = 4e-4f, criticAlpha = 4e-3f;

    public final FloatRange gamma = FloatRange.unit(0.9f);

    public final MLP actor;
    public final MLP critic;

    public A2C(int inputs, int internal, int actions) {
        this.actor = new MLP(inputs,
            new MLP.Dense(internal, TanhActivation.the),
//                //nn.Linear(64, 32), //nn.Tanh(),
                new MLP.Dense(actions, new SigLinearActivation())

            );

        this.critic = new MLP(inputs,
                new MLP.Dense(internal, ReluActivation.the),
//                #nn.Linear(64, 32), nn.ReLU(),
                new MLP.Dense(1, TanhActivation.the)
        );

    }

    @Override
    public void clear(Random rng) {
        actor.clear(rng);
        critic.clear(rng);
    }

    @Override
    public synchronized double[] learn(@Nullable double[] xPrev, double[] actionPrev, double reward, double[] x, float pri) {
        /* action probabilities */
        double[] action = predict(xPrev);

        /* normalize actions */ double actionSum = AdamOptimizer.epsilon /* HACK */ + Util.sum(action);Util.mul(1/actionSum, action);

        /*
        TODO for discrete choice:
        softmax,
        dist = torch.distributions.Categorical(probs=probs)
        action = dist.sample()
        */

        double done = 0; //TODO episode control

        double td_target = reward + (1 - done) * gamma.asFloat() * critic(x);

        double value = critic(xPrev);

        /* aka TD_error */
        double advantage = td_target - value;

        double critic_loss =
                //Util.sqr(advantage);
                advantage;

        //System.out.println(value + " " + advantage + " " + critic_loss);
        this.critic.putDelta(new double[] { critic_loss }, criticAlpha); //TODO check polarity


        double[] actor_loss = new double[action.length];
        for (int i = 0; i < action.length; i++)
            actor_loss[i] = advantage * -Math.log(action[i]);

        actor.putDelta(actor_loss, actorAlpha);
        /*

        actor_loss = -dist.log_prob(action)*advantage.detach()
        adam_actor.zero_grad()
        actor_loss.backward()
        adam_actor.step()
         */
        return action;
    }

    private double critic(double[] xPrev) {
        return critic.get(xPrev)[0];
    }

    public double[] predict(double[] input) {
        return actor.get(input);
    }
}