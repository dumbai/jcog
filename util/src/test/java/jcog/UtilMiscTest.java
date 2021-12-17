package jcog;

import jcog.random.XoRoShiRo128PlusRandom;
import org.hipparchus.stat.Frequency;
import org.hipparchus.stat.descriptive.StreamingStatistics;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author me
 */
class UtilMiscTest {

    @Test void testLargestPow2() {
        assertEquals(1, Util.largestPowerOf2NoGreaterThan(1));
        assertEquals(2, Util.largestPowerOf2NoGreaterThan(2));
        assertEquals(4, Util.largestPowerOf2NoGreaterThan(3));

        assertEquals(64, Util.largestPowerOf2NoGreaterThan(63));
        assertEquals(128, Util.largestPowerOf2NoGreaterThan(65));
    }

    @Test void testBinDistribution() {
        int seed = 1;
        int samples = 5000;

        Random r = new XoRoShiRo128PlusRandom(seed);
        for (int bins = 2; bins < 15; bins++) {
            Frequency f = new Frequency();
            for (int s = 0; s < samples; s++)
                f.addValue( Util.bin(r.nextFloat(), bins) );
            assertEquals(bins, f.getUniqueCount());
            StreamingStatistics s = new StreamingStatistics();
            for (int b = 0; b < bins; b++)
                s.addValue(f.getPct(b));
            System.out.println(f);
            System.out.println(s);
            assertTrue(s.getVariance() < 0.01f);
        }
    }

    @Test void testRoundLong_bigger() {
        assertEquals( 545203130003L, Util.round(+545203130L*1000, 7));
        assertEquals( 5452031300002L, Util.round(+545203130L*10000, 7));
        assertEquals( 54520312999999L, Util.round(+545203130L*100000, 7));
        assertEquals( 545203129999997L, Util.round(+545203130L*1000000, 7));
    }

    @Test void testRoundLong_big() {
        assertEquals( 545203130, Util.round(+545203130, 2));
        assertEquals(-545203130, Util.round(-545203130, 2));
        assertEquals( 545203131, Util.round(+545203130, 3));
        assertEquals(-545203131, Util.round(-545203130, 3));
        assertEquals( 545203127, Util.round(+545203130, 7));
        assertEquals(-545203127, Util.round(-545203130, 7));
    }



}
