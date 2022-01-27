package jcog.decide;

import jcog.Util;
import jcog.signal.FloatRange;

import java.util.random.RandomGenerator;

/**
 * https:
 * For high temperatures ( {\displaystyle \tau \to \infty } \tau \to \infty ),
 * all actions have nearly the same probability and the lower the temperature,
 * the more expected rewards affect the probability.
 * <p>
 * For a low temperature ( {\displaystyle \tau \to 0^{+}} \tau \to 0^{+}),
 * the probability of the action with the highest expected reward tends to 1.
 *
 * TODO https://www.deeplearningbook.org/slides/04_numerical.pdf page 34
 */
public class DecideSoftmax implements Decide {

    public final RandomGenerator random;
    /**
     * whether to exclude negative values
     */

//    private boolean normalize = false;
    public final FloatRange temperature;
    private final float minTemperature;
    private final float temperatureDecay;
    /**
     * normalized motivation
     */
    private float[] mot;
    private float conf;

    public DecideSoftmax(float constantTemp, RandomGenerator random) {
        this(constantTemp, 0, 1f, random);
    }

    private DecideSoftmax(float initialTemperature, float minTemperature, float decay, RandomGenerator random) {
        this.temperature = new FloatRange(initialTemperature, 0.01f, 1);
        this.minTemperature = minTemperature;
        this.temperatureDecay = decay;
        this.random = random;
    }

    @Override
    public int applyAsInt(float[] vector) {

        float tmp;
        temperature.set(tmp = Math.max(minTemperature, temperature.asFloat() * temperatureDecay));

        int actions = vector.length;
        if (mot == null) {
//            mot = new float[actions];
            mot = new float[actions];
        }

        double sumMotivation = Util.sum(vector);
        if (!(sumMotivation > Float.MIN_NORMAL * actions))
            return random.nextInt(vector.length);

//            if (normalize) {
//                float[] minmax = Util.minmax(vector);
//                float min = minmax[0], max = minmax[1];
//                for (int i = 0; i < actions; i++)
//                    mot[i] = Util.normalize(vector[i], min, max);
//            } else {
        System.arraycopy(vector, 0, mot, 0, actions);
//            }

        /* http://www.cse.unsw.edu.au/~cs9417ml/RL1/source/RLearner.java */
        for (int i = 0; i < actions; i++)
            mot[i] = (float) Util.softmax(mot[i], tmp);

        int i = Roulette.selectRoulette(mot, random::nextFloat);

        conf = (float) (vector[i] / sumMotivation);

        return i;
    }

    public float decisiveness() {
        return conf;
    }
}