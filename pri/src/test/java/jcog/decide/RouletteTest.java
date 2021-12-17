package jcog.decide;

import jcog.Util;
import jcog.pri.Prioritized;
import jcog.random.XoRoShiRo128PlusRandom;
import org.hipparchus.stat.Frequency;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouletteTest {

    @Test
    void testSelectRouletteFlat() {
        Random rng = new XoRoShiRo128PlusRandom(1);
        int uniques = 4;
        int samples = 500;

        Frequency f = new Frequency();
        for (int i = 0; i < samples; i++)
            f.addValue(Roulette.selectRoulette(uniques, (k) -> 0.5f, rng));


        System.out.println(f);

        assertEquals(uniques, f.getUniqueCount());
        float total = f.getSumFreq();
        for (int i = 0; i < uniques; i++)
            assertEquals(1f / uniques, f.getCount(i) / total, 1f / (4 * uniques));
    }

    @Test
    void testMutableRouletteFlat() {
        int uniques = 8;

        float[] w = new float[uniques];
        Arrays.fill(w, 0.5f);

        int samples = 1500;
        testMutableRoulette(new XoRoShiRo128PlusRandom(1), uniques, samples, w);
    }


    @Test
    void testMutableRouletteRandom() {
        int repeats = 100;
        for (int n = 1; n < 10; n++) {
            for (int seed = 1; seed < 3; seed++) {
                testMutableRouletteRandom(n, n * repeats, seed);
            }
        }
    }


    private static void testMutableRouletteRandom(int uniques, int samples, int seed) {
        XoRoShiRo128PlusRandom rng = new XoRoShiRo128PlusRandom(seed);

        float[] w = new float[uniques];
        for (int i = 0; i < uniques; i++)
            w[i] = Prioritized.EPSILON + rng.nextFloat();

        testMutableRoulette(rng, uniques, samples, w);
    }

    private static void testMutableRoulette(Random rng, int uniques, int samples, float[] w) {
        MutableRoulette m = new MutableRoulette(uniques, (i) -> w[i], rng);

        Frequency f = new Frequency();
        for (int i = 0; i < samples; i++) {
            f.addValue(m.next());
        }

//        System.out.println(Arrays.toString(w));
//        System.out.println(f);
//        System.out.println();

        float wSum = (float) Util.sum(w);
        assertEquals(uniques, f.getUniqueCount());
        for (int i = 0; i < uniques; i++)
            assertEquals(w[i]/wSum, ((float)f.getCount(i)) / samples, 0.5f / (uniques));
    }

    @Test
    void testDecideRouletteTriangular() {
        Random rng = new XoRoShiRo128PlusRandom(1);
        int uniques = 10;
        int samples = 5000;

        Frequency f = new Frequency();
        for (int i = 0; i < samples; i++)
            f.addValue(Roulette.selectRoulette(uniques, (k) -> (k + 1f) / (uniques), rng));

        System.out.println(f);
        assertEquals(uniques, f.getUniqueCount());
        float total = f.getSumFreq();
        for (int i = 0; i < uniques; i++)
            assertEquals(f.getCount(i) / total, (i + 1f) / (uniques * uniques / 2f), 1f / (4 * uniques));
    }
}