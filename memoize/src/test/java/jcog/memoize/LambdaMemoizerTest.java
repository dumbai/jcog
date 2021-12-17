package jcog.memoize;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LambdaMemoizerTest {

    @Test
    void test1() {


        LambdaMemoizer.MemoizeBuilder<Integer> m =
                f -> new HijackMemoize<>(f, 16, 3);
        Function<Object[], Integer> cachingSlowFunction = LambdaMemoizer.memoize(
                LambdaMemoizerTest.class, "slowFunction", new Class[]{int.class}, m);

        /* warmup */
        cachingSlowFunction.apply(new Object[] { 3 });

        long startSlow = System.currentTimeMillis();
        int result = cachingSlowFunction.apply(new Object[] { 2 });
        long endSlow = System.currentTimeMillis();

        
        long startFast = System.currentTimeMillis();
        int result2 = cachingSlowFunction.apply(new Object[] { 2 });
        long endFast = System.currentTimeMillis();

        assertEquals(4, result);
        assertEquals(4, result2);

        long first = endSlow - startSlow;
        System.out.printf("The first time took %dms%n", first);
        long second = endFast - startFast;
        System.out.printf("The second time took %dms%n", second);
        assertTrue(first - second > 25 /* ms speedup */);
    }

    public static int slowFunction(int input) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return input * 2;
    }

}