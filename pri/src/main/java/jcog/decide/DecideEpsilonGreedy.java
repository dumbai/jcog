package jcog.decide;

import jcog.util.ArrayUtil;

import java.util.Random;

/**
 * Created by me on 6/9/16.
 */
public class DecideEpsilonGreedy implements Decide {

    private final Random random;

    /** TODO FloatRange */
    private float epsilonRandom;

    /*
    TODO - decaying epsilon:
            epsilonRandom *= epsilonRandomDecay;
            epsilonRandom = Math.max(epsilonRandom, epsilonRandomMin);
     */

    public DecideEpsilonGreedy(float epsilonRandom, Random random) {
        this.epsilonRandom = epsilonRandom;
        this.random = random;
    }

    private int[] motivationOrder;

    @Override
    public int applyAsInt(float[] vector) {
        int actions = vector.length;

        if (motivationOrder == null || motivationOrder.length!=actions) {
            motivationOrder = new int[actions];
            for (int i = 0; i < actions; i++)
                motivationOrder[i] = i;
        }

        if (epsilonRandom > 0 && random.nextFloat() < epsilonRandom)
            return random.nextInt(actions);


        ArrayUtil.shuffle(motivationOrder, random);

        float nextMotivation = Float.NEGATIVE_INFINITY;
        int nextAction = -1;
        for (int j = 0; j < actions; j++) {
            int i = motivationOrder[j];
            float m = vector[i];

            if (m > nextMotivation) {
                nextAction = i;
                nextMotivation = m;
            }


        }
        
        int a = nextAction;
        if (a < 0)
            return random.nextInt(actions); //all <= NEGATIVE_INFINITY
        
        return a;
    }
}