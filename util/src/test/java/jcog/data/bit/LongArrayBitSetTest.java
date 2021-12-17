package jcog.data.bit;

import jcog.random.XoRoShiRo128PlusRandom;
import org.eclipse.collections.api.iterator.IntIterator;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class LongArrayBitSetTest {

    @Test void testIteratorAndToArray() {
        var l = new LongArrayBitSet(
            new long[] {
                0,0,0,
                0b00000010_00000100_10000000_00000000_00000000_00000000_00000000L
            }
        );
        assertEquals(3, l.cardinality());
        short[] s = new short[3];
        l.toArray(0, 64*4, s);
        assertEquals(231, s[0]);
        assertEquals(234, s[1]);
        assertEquals(241, s[2]);
    }

    @Test
    void test1() {
        MetalBitSet b = MetalBitSet.bits(70);
        b.set(64);
        assertEquals(64, b.first(true));
    }
    @Test
    void test2() {
        MetalBitSet b = MetalBitSet.bits(70);
        b.set(5); assertTrue(b.test(5)); assertFalse(b.test(4));
        b.set(65); assertTrue(b.test(65)); assertFalse(b.test(64));
        IntIterator bi = b.iterator(0, 70);
        assertTrue(bi.hasNext()); assertTrue(bi.hasNext()); //extra call shouldnt do anything
        assertEquals(5, bi.next());
        assertTrue(bi.hasNext()); assertTrue(bi.hasNext()); //extra call shouldnt do anything
        assertEquals(65, bi.next());
        assertFalse(bi.hasNext()); assertFalse(bi.hasNext());
    }
    @Test
    void test3() {
        int words = 3;

        Random rng = new XoRoShiRo128PlusRandom(1);
        for (int j = 0; j < 100; j++) {
            int n = 64 * words;
            MetalBitSet b = MetalBitSet.bits(n);
            int written = 0;
            for (int i = 0; i < words * 32; i++) {
                int x = rng.nextInt(n);
                if (!b.getAndSet(x, true))
                    written++;

                assertTrue(b.test(x));
                assertEquals(written, b.cardinality());
            }
            IntIterator bb = b.iterator(0, n);
            int read = 0;
            while (bb.hasNext()) {
                bb.next();
                read++;
            }
            assertEquals(written, read);
        }
    }
}