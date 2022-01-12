package jcog.decide;

import jcog.Is;
import jcog.Util;
import jcog.WTF;
import jcog.math.FloatSupplier;
import jcog.pri.Prioritized;
import org.eclipse.collections.api.block.function.primitive.IntToFloatFunction;

import java.util.Random;

/**
 * roulette select
 */
@Is("Fitness_proportionate_selection") public enum Roulette {
    ;

    public static int selectRoulette(float[] x, FloatSupplier rng) {
        return selectRoulette(x.length, n -> x[n], rng);
    }

    public static int selectRoulette(int weightCount, IntToFloatFunction weight, Random rng) {
        return selectRoulette(weightCount, weight, rng::nextFloat);
    }

    /**
     * Returns the selected index based on the weights(probabilities)
     */
    public static int selectRoulette(int weightCount, IntToFloatFunction weight, FloatSupplier rng) {
        switch (weightCount) {
            case 1:
                return 0;//valid(weight.valueOf(0)) ? 0 : -1;
            case 2:
                return select2(weight.valueOf(0), weight.valueOf(1), rng);
            default:
                assert (weightCount > 0);
                double weightSum = Util.sumIfPositive(weightCount, weight);
                return weightSum < Prioritized.EPSILON ?
                        selectFlat(weightCount, rng) :
                        selectRouletteUnidirectionally(weightCount, weight, weightSum, rng);
        }
    }

    private static int selectFlat(int weightCount, FloatSupplier rng) {
        return Util.bin(rng.asFloat() * weightCount, weightCount);
    }

    public static int selectRouletteCached(int weightCount, IntToFloatFunction weight, Random rng) {
        return selectRouletteCached(weightCount, weight, rng::nextFloat);
    }

    /** returns -1 if no option (not any weight==NaN, or non-positive) */
    public static int selectRouletteCached(int weightCount, IntToFloatFunction weight, FloatSupplier rng) {
        return switch (weightCount) {
            case 1  -> valid(weight.valueOf(0)) ? 0 : -1;
            case 2  -> select2(weight.valueOf(0), weight.valueOf(1), rng);
            default -> selectN(weightCount, weight, rng);
        };
    }

    private static int selectN(int weightCount, IntToFloatFunction weight, FloatSupplier rng) {
        float[] w = new float[weightCount];
        int lastValid = -1;
        for (int i = 0; i < weightCount; i++) {
            float wi = weight.valueOf(i);
            if (valid(wi)) {
                w[i] = wi;
                lastValid = lastValid == -1 ? i : -2; //first, or > 1
            }
        }

        if (lastValid == -1)
            return -1;
        else if (lastValid != -2)
            return lastValid;
        else
            return selectRoulette(weightCount, i -> w[i], rng);
    }

    private static int select2(float rx, float ry, FloatSupplier rng) {
        boolean bx = valid(rx), by = valid(ry);
        if (bx && by)
            return rng.asFloat() <= (Util.equals(rx, ry, Float.MIN_NORMAL) ?
                    0.5f : (((double)rx) / (rx + ry))) ?
                    0 : 1;
        else if (!bx && !by) return -1;
        else if (bx /*&& !by*/) return 0;
        else return 1;
    }

    private static boolean valid(float w) {
        return w==w && w >= 0;
    }

    private static int selectRouletteUnidirectionally(int count, IntToFloatFunction weight, double weight_sum, FloatSupplier rng) {
        double distance = rng.asFloat() * weight_sum;
        int i = selectFlat(count, rng);

        int safetyLimit = count;
        while ((distance -= Math.max(0, weight.valueOf(i))) > Float.MIN_NORMAL && --safetyLimit > 0) {

            if (++i == count) i = 0; //wrap-around

        }
        if (safetyLimit<0) throw new WTF();

        return i;
    }

//    /** not sure if this offers any improvement over the simpler unidirectional iieration.
//     * might also be biased to the edges or middle because it doesnt start at random index though this can be tried */
//    private static int selectRouletteBidirectionally(int count, IntToFloatFunction weight, float weight_sum, FloatSupplier rng) {
//        float x = rng.asFloat();
//        int i;
//        boolean dir;
//        if (x <= 0.5f) {
//            dir = true;
//            i = 0; //count up
//        } else {
//            dir = false;
//            i = count - 1; //count down
//            x = 1 - x;
//        }
//
//        float distance = x * weight_sum;
//
//        int limit = count;
//        while ((distance -= weight.valueOf(i)) > Float.MIN_NORMAL) {
//            if (dir) {
//                if (++i == count) i = 0;
//            } else {
//                if (--i == -1) i = count - 1;
//            }
//            if (--limit==0)
//                throw new WTF();
//        }
//
//        return i;
//    }

}