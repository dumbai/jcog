package jcog.sort;

import jcog.random.XorShift128PlusRandom;
import jcog.util.ArrayUtil;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;


class IntifySmoothSort2Test {

    @Test
    void test1() {
        Random rng = new XorShift128PlusRandom(1);

        for (int g = 0; g < 1000; g++) {

            int s = rng.nextInt(32)+1;
            short[] l = new short[s];
            for (int i = 0; i < s; i++) l[i] = (short) rng.nextInt();

            new IntifySmoothSort2(l, x -> x).sort(0, l.length);
            assertTrue(ArrayUtil.isSorted(l));
        }
    }
}