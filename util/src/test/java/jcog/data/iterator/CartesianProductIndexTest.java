package jcog.data.iterator;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CartesianProductIndexTest {

    /**
     * Usage example. Prints out
     *
     * <pre>
     * [0, 0, 0] a, NANOSECONDS, 1
     * [0, 0, 1] a, NANOSECONDS, 2
     * [0, 0, 2] a, NANOSECONDS, 3
     * [0, 0, 3] a, NANOSECONDS, 4
     * [0, 1, 0] a, MICROSECONDS, 1
     * [0, 1, 1] a, MICROSECONDS, 2
     * [0, 1, 2] a, MICROSECONDS, 3
     * [0, 1, 3] a, MICROSECONDS, 4
     * [0, 2, 0] a, MILLISECONDS, 1
     * [0, 2, 1] a, MILLISECONDS, 2
     * [0, 2, 2] a, MILLISECONDS, 3
     * [0, 2, 3] a, MILLISECONDS, 4
     * [0, 3, 0] a, SECONDS, 1
     * [0, 3, 1] a, SECONDS, 2
     * [0, 3, 2] a, SECONDS, 3
     * [0, 3, 3] a, SECONDS, 4
     * [0, 4, 0] a, MINUTES, 1
     * [0, 4, 1] a, MINUTES, 2
     * ...
     * </pre>
     */
    @Test void test() {
        String[] list1 = { "a", "b", "c", };
        TimeUnit[] list2 = TimeUnit.values();
        int[] list3 = { 1, 2, 3, 4 };

        int count = 0;
        for (int[] indices : new CartesianProductIndex(new int[]{ list1.length, list2.length, list3.length })) {
//            System.out.println(Arrays.toString(indices) //
//                    + " " + list1[indices[0]] //
//                    + ", " + list2[indices[1]] //
//                    + ", " + list3[indices[2]]);
            count++;
        }
        assertEquals(84, count);
    }
}