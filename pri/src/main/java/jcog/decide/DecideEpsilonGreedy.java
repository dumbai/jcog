package jcog.decide;

import jcog.signal.FloatRange;
import jcog.util.ArrayUtil;

import java.util.Random;

/**
 * Created by me on 6/9/16.
 */
public class DecideEpsilonGreedy implements Decide {

    private final Random random;

    public final FloatRange epsilonRandom;

    /*
    TODO - decaying epsilon subclass:
            epsilonRandom *= epsilonRandomDecay;
            epsilonRandom = Math.max(epsilonRandom, epsilonRandomMin);
     */
    public DecideEpsilonGreedy(float epsilonRandom, Random random) {
        this.epsilonRandom = FloatRange.unit(epsilonRandom);
        this.random = random;
    }


    @Override
    public int applyAsInt(float[] vector) {
        int n = vector.length;

        var epsilonRandom = this.epsilonRandom.floatValue();
        if (epsilonRandom > 0 && random.nextFloat() < epsilonRandom)
            return random.nextInt(n);

        var valueOrder = new short[n];
        for (short i = 0; i < n; i++)
            valueOrder[i] = i;

        //shuffle in case there are >1 equally maximum values.
        //TODO make this more efficient by only doing this when it becomes necessary, or some other equally fair choice strategy
        ArrayUtil.shuffle(valueOrder, random);

        float nextValue = Float.NEGATIVE_INFINITY;
        int next = -1;
        for (int j = 0; j < n; j++) {
            int i = valueOrder[j];
            float v = vector[i];

            if (v > nextValue) {
                next = i;
                nextValue = v;
            }
        }

        return next < 0 ?
                random.nextInt(n) //all values are <= NEGATIVE_INFINITY
                : next;
    }
}