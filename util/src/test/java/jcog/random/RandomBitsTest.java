package jcog.random;

import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomBitsTest {

    @Test
    void test1() {
        RandomBits r = new RandomBits(new XoRoShiRo128PlusRandom(1));
        //System.out.println(r);
        //r.refresh();
        //System.out.println("  \t" + r);
        boolean a = r.nextBoolean();
        //System.out.println(a + "\t" + r);
        boolean b = r.nextBoolean();
        //System.out.println(b + "\t" + r);
        boolean c = r.nextBoolean();
        //System.out.println(c + "\t" + r);


        int d = r.nextBits(4);
        //System.out.println(d + "  \t" + r);
        int e = r.nextBits(3);
        //System.out.println(e + "  \t" + r);
        int f = r.nextBits(8);
        //System.out.println(f + "  \t" + r);

        assertTrue(a);
        assertFalse(b);
        assertFalse(c);
        assertEquals(46,r.bit);

        r.bit = 3; //force refresh
        int g = r.nextBits(8);
        //System.out.println(g + "  \t" + r);
        assertEquals(56, r.bit);
        assertEquals(13, g);
    }

    @Test void nextInt_4() {
        RandomBits r = new RandomBits(new XoRoShiRo128PlusRandom(1));
        int i = r.nextInt(10); //4 bits
        assertEquals(60, r.bit);
        IntHashSet seen = new IntHashSet();
        for (int j = 0; j < 1000; j++) {
            int z = r.nextInt(10);
            assertTrue(z < 10 && z >= 0, ()->z + " out of range");
            seen.add(z);
        }
        assertEquals(10, seen.size());
    }
    @Test void nextInt_9() {
        RandomBits r = new RandomBits(new XoRoShiRo128PlusRandom(1));
        int i = r.nextInt(310); //9 bits
        assertEquals(55, r.bit);
        for (int j = 0; j < 10000; j++) {
            int z = r.nextInt(310);
            assertTrue(z < 310 && z >= 0, ()->z + " out of range");
        }
    }

    @Test void nextBooleanFast() {
        RandomBits r = new RandomBits(new XoRoShiRo128PlusRandom(1));
        for (int b : new int[] { 8, 16, 32 }) {
            for (float p = 0; p <= 1; p += 0.05f) {
                assertProb(r, p, b);
            }
        }
    }

    private static void assertProb(RandomBits r, float idealProb, int bits) {
        int samples = 16*1024;
        double tolerance = 0.01;

        int trues = 0;
        for (int i = 0; i < samples; i++) {
            if (r.nextBooleanFast(idealProb, bits))
                trues++;
        }
        double actual = trues/((double)samples);
        assertEquals(idealProb, actual, tolerance);
    }
}