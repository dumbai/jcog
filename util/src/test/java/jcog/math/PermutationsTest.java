package jcog.math;

import jcog.random.RandomBits;
import jcog.random.XorShift128PlusRandom;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.math.IntMath.factorial;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * from http:
 */
class PermutationsTest {

    @Test
    void testPerm1() {
        Permutations perm = new Permutations().restart(3);
        int count = 0;
        String x = "";
        while(perm.hasNext()){
            String aa = Arrays.toString(perm.next());
            System.out.println(aa + ' ' +perm.hasNext());
            x += aa + ' ';
            count++;
        }
        System.out.println("total: " + count);
        assertEquals(6, count);
        assertEquals("[0, 1, 2] [0, 2, 1] [1, 0, 2] [1, 2, 0] [2, 0, 1] [2, 1, 0] ", x);
    }

    @Test
    void testShuffleReset2() {
        testShuffleReset(2, 2);
        testShuffleReset(2, 1);
    }
    @Test
    void testShuffleReset3() {
        testShuffleReset(3, 2);
        testShuffleReset(3, 1);
    }
    @Test
    void testShuffleReset4() {
        testShuffleReset(4, 5);
        testShuffleReset(4, 4);
        testShuffleReset(4, 3);
        testShuffleReset(4, 2);
        testShuffleReset(4, 1);
    }
    @Test
    void testShuffleReset5() {
        testShuffleReset(5, 5);
        testShuffleReset(5, 4);
        testShuffleReset(5, 3);
        testShuffleReset(5, 2);
        testShuffleReset(5, 1);
    }

    private static void testShuffleReset(int size, int selected) {

        int expected = factorial(size);
        int attempts = (1+expected/selected)*(1+expected/selected); 

        Set<String> sequences = new HashSet();
        Set<String> arrays = new TreeSet();

        RandomBits rng =
                new RandomBits(new XorShift128PlusRandom(2));

        int[] n = new int[size];

        ShuffledPermutations perm = new ShuffledPermutations();

        int attempt;
        for (attempt = 1; attempt < attempts; attempt++) {
            perm.restart(size, rng);

            StringBuilder sb = new StringBuilder();
            for (int x = 0; perm.hasNext() && x < selected; x++) {
                String aa = Arrays.toString(perm.nextPermute(n));
                arrays.add(aa);
                sb.append(aa).append(' ');
            }

            /*System.out.println(
                    " " + sb.toString());*/
            sequences.add(sb.toString());
            if (arrays.size() == expected) break;
        }

        System.out.println(size + " exhausted all " + expected + " permutations after " + attempt + " attempts when sets of " + selected + " are selected");

        
        assertTrue(sequences.size() >= 1);

        

        assertTrue(expected >= arrays.size());
    }

}