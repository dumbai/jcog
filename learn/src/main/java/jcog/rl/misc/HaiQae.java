package jcog.rl.misc;

import jcog.decide.Decide;
import jcog.decide.DecideSoftmax;
import jcog.deep.Autoencoder;

import java.util.function.BiFunction;

import static jcog.Util.toFloat;

/**
 * Created by me on 5/22/16.
 */
public class HaiQae extends HaiQ {

//    public static final Logger logger = LoggerFactory.getLogger(HaiQae.class);

    public final Autoencoder ae;
    float perceptionAlpha = 0.01f;
//    float perceptionForget;
//    public double perceptionError;


    /**
     * "horizontal" input state selection
     */
    protected Decide decideState = //DecideEpsilonGreedy.ArgMax;
                                    new DecideSoftmax(0.5f, rng);

    public HaiQae(int inputs, int outputs) {
        this(inputs,
            (s,o)->
                s
                /*(int) Math.ceil((1 + i*2))*/, outputs);
    }

    public HaiQae(int inputs, BiFunction<Integer,Integer,Integer> states, int outputs) {
        this(inputs, states.apply(inputs, outputs), outputs);
    }

    public HaiQae(int inputs, int states, int outputs) {
        super(states, outputs);

        this.ae = new Autoencoder(inputs, states, rng);
    }

    @Override
    protected int perceive(double[] input) {

        ae.put(toFloat(input), perceptionAlpha);
//        perceptionError = ae.
//            / input.length;

        return decideState.applyAsInt(ae.y);
    }

//    @Override
//    public final void apply(@Nullable double[] actionPrev, float reward, double[] input, double[] qNext) {
//        act(actionPrev, reward, input, qNext);
//    }
//    @Override public void act(double[] actionFeedback, float reward, double[] input, double[] qNext) {
//
//        //float learningRate = 1f - (pErr);
//        int p = perceive(input);
////        if (learningRate > 0) {
//
//        learn(actionFeedback, p, reward, qNext);
////        } else {
////            return rng.nextInt(actions);
////        }
//
//
//    }



}