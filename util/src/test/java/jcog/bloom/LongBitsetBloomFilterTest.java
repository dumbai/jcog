package jcog.bloom;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by me on 8/6/15.
 */
class LongBitsetBloomFilterTest {
    private static final int COUNT = 100;
    private Random rand = new Random(123);

    @Test
    void testBloomIllegalArg1() {
        assertThrows(AssertionError.class, ()-> {

            LongBitsetBloomFilter bf = new LongBitsetBloomFilter(0, 0);
        });
    }

    @Test
    void testBloomIllegalArg2() {
        assertThrows(AssertionError.class, ()->{LongBitsetBloomFilter bf = new LongBitsetBloomFilter(0, 0.1);});
    }

    @Test
    void testBloomIllegalArg3() {
        assertThrows(AssertionError.class, ()->{LongBitsetBloomFilter bf = new LongBitsetBloomFilter(1, 0.0);});
    }

    @Test
    void testBloomIllegalArg4() {
        assertThrows(AssertionError.class, ()->{LongBitsetBloomFilter bf = new LongBitsetBloomFilter(1, 1.0);});
    }

    @Test
    void testBloomIllegalArg5() {
        assertThrows(AssertionError.class, ()->{LongBitsetBloomFilter bf = new LongBitsetBloomFilter(-1, -1);});
    }


    @Test
    void testBloomNumBits() {
        assertEquals(0, LongBitsetBloomFilter.optimalNumOfBits(0, 0));
        assertEquals(1549, LongBitsetBloomFilter.optimalNumOfBits(1, 0));
        assertEquals(0, LongBitsetBloomFilter.optimalNumOfBits(0, 1));
        assertEquals(0, LongBitsetBloomFilter.optimalNumOfBits(1, 1));
        assertEquals(7, LongBitsetBloomFilter.optimalNumOfBits(1, 0.03));
        assertEquals(72, LongBitsetBloomFilter.optimalNumOfBits(10, 0.03));
        assertEquals(729, LongBitsetBloomFilter.optimalNumOfBits(100, 0.03));
        assertEquals(7298, LongBitsetBloomFilter.optimalNumOfBits(1000, 0.03));
        assertEquals(72984, LongBitsetBloomFilter.optimalNumOfBits(10000, 0.03));
        assertEquals(729844, LongBitsetBloomFilter.optimalNumOfBits(100000, 0.03));
        assertEquals(7298440, LongBitsetBloomFilter.optimalNumOfBits(1000000, 0.03));
        assertEquals(6235224, LongBitsetBloomFilter.optimalNumOfBits(1000000, 0.05));
    }

    @Test
    void testBloomNumHashFunctions() {
        assertEquals(1, LongBitsetBloomFilter.optimalNumOfHashFunctions(-1, -1));
        assertEquals(1, LongBitsetBloomFilter.optimalNumOfHashFunctions(0, 0));
        assertEquals(1, LongBitsetBloomFilter.optimalNumOfHashFunctions(10, 0));
        assertEquals(1, LongBitsetBloomFilter.optimalNumOfHashFunctions(10, 10));
        assertEquals(7, LongBitsetBloomFilter.optimalNumOfHashFunctions(10, 100));
        assertEquals(1, LongBitsetBloomFilter.optimalNumOfHashFunctions(100, 100));
        assertEquals(1, LongBitsetBloomFilter.optimalNumOfHashFunctions(1000, 100));
        assertEquals(1, LongBitsetBloomFilter.optimalNumOfHashFunctions(10000, 100));
        assertEquals(1, LongBitsetBloomFilter.optimalNumOfHashFunctions(100000, 100));
        assertEquals(1, LongBitsetBloomFilter.optimalNumOfHashFunctions(1000000, 100));
    }

    @Test
    void testBloomFilterBytes() {
        LongBitsetBloomFilter bf = new LongBitsetBloomFilter(10000);
        byte[] val = {1, 2, 3};

        assertFalse(bf.test(val));
        byte[] val1 = {1, 2, 3, 4};
        assertFalse(bf.test(val1));
        byte[] val2 = {1, 2, 3, 4, 5};
        assertFalse(bf.test(val2));
        byte[] val3 = {1, 2, 3, 4, 5, 6};
        assertFalse(bf.test(val3));
        bf.add(val);
        assertTrue(bf.test(val));
        assertFalse(bf.test(val1));
        assertFalse(bf.test(val2));
        assertFalse(bf.test(val3));
        bf.add(val1);
        assertTrue(bf.test(val));
        assertTrue(bf.test(val1));
        assertFalse(bf.test(val2));
        assertFalse(bf.test(val3));
        bf.add(val2);
        assertTrue(bf.test(val));
        assertTrue(bf.test(val1));
        assertTrue(bf.test(val2));
        assertFalse(bf.test(val3));
        bf.add(val3);
        assertTrue(bf.test(val));
        assertTrue(bf.test(val1));
        assertTrue(bf.test(val2));
        assertTrue(bf.test(val3));

        byte[] randVal = new byte[COUNT];
        for (int i = 0; i < COUNT; i++) {
            rand.nextBytes(randVal);
            bf.add(randVal);
        }

        assertTrue(bf.test(randVal));
        
        randVal[0] = 0;
        randVal[1] = 0;
        randVal[2] = 0;
        randVal[3] = 0;
        randVal[4] = 0;
        assertFalse(bf.test(randVal));

        assertEquals(7800, bf.sizeInBytes());
    }

    @Test
    void testBloomFilterByte() {
        LongBitsetBloomFilter bf = new LongBitsetBloomFilter(10000);
        byte val = Byte.MIN_VALUE;

        assertFalse(bf.testByte(val));
        byte val1 = 1;
        assertFalse(bf.testByte(val1));
        byte val2 = 2;
        assertFalse(bf.testByte(val2));
        byte val3 = Byte.MAX_VALUE;
        assertFalse(bf.testByte(val3));
        bf.addByte(val);
        assertTrue(bf.testByte(val));
        assertFalse(bf.testByte(val1));
        assertFalse(bf.testByte(val2));
        assertFalse(bf.testByte(val3));
        bf.addByte(val1);
        assertTrue(bf.testByte(val));
        assertTrue(bf.testByte(val1));
        assertFalse(bf.testByte(val2));
        assertFalse(bf.testByte(val3));
        bf.addByte(val2);
        assertTrue(bf.testByte(val));
        assertTrue(bf.testByte(val1));
        assertTrue(bf.testByte(val2));
        assertFalse(bf.testByte(val3));
        bf.addByte(val3);
        assertTrue(bf.testByte(val));
        assertTrue(bf.testByte(val1));
        assertTrue(bf.testByte(val2));
        assertTrue(bf.testByte(val3));

        byte randVal = 0;
        for (int i = 0; i < COUNT; i++) {
            randVal = (byte) rand.nextInt(Byte.MAX_VALUE);
            bf.addByte(randVal);
        }

        assertTrue(bf.testByte(randVal));

        assertFalse(bf.testByte((byte) -120));

        assertEquals(7800, bf.sizeInBytes());
    }

    @Test
    void testBloomFilterInt() {
        LongBitsetBloomFilter bf = new LongBitsetBloomFilter(10000);
        int val = Integer.MIN_VALUE;

        assertFalse(bf.testInt(val));
        int val1 = 1;
        assertFalse(bf.testInt(val1));
        int val2 = 2;
        assertFalse(bf.testInt(val2));
        int val3 = Integer.MAX_VALUE;
        assertFalse(bf.testInt(val3));
        bf.addInt(val);
        assertTrue(bf.testInt(val));
        assertFalse(bf.testInt(val1));
        assertFalse(bf.testInt(val2));
        assertFalse(bf.testInt(val3));
        bf.addInt(val1);
        assertTrue(bf.testInt(val));
        assertTrue(bf.testInt(val1));
        assertFalse(bf.testInt(val2));
        assertFalse(bf.testInt(val3));
        bf.addInt(val2);
        assertTrue(bf.testInt(val));
        assertTrue(bf.testInt(val1));
        assertTrue(bf.testInt(val2));
        assertFalse(bf.testInt(val3));
        bf.addInt(val3);
        assertTrue(bf.testInt(val));
        assertTrue(bf.testInt(val1));
        assertTrue(bf.testInt(val2));
        assertTrue(bf.testInt(val3));

        int randVal = 0;
        for (int i = 0; i < COUNT; i++) {
            randVal = rand.nextInt();
            bf.addInt(randVal);
        }

        assertTrue(bf.testInt(randVal));

        assertFalse(bf.testInt(-120));

        assertEquals(7800, bf.sizeInBytes());
    }

    @Test
    void testBloomFilterLong() {
        LongBitsetBloomFilter bf = new LongBitsetBloomFilter(10000);
        long val = Long.MIN_VALUE;

        assertFalse(bf.testLong(val));
        long val1 = 1;
        assertFalse(bf.testLong(val1));
        long val2 = 2;
        assertFalse(bf.testLong(val2));
        long val3 = Long.MAX_VALUE;
        assertFalse(bf.testLong(val3));
        bf.addLong(val);
        assertTrue(bf.testLong(val));
        assertFalse(bf.testLong(val1));
        assertFalse(bf.testLong(val2));
        assertFalse(bf.testLong(val3));
        bf.addLong(val1);
        assertTrue(bf.testLong(val));
        assertTrue(bf.testLong(val1));
        assertFalse(bf.testLong(val2));
        assertFalse(bf.testLong(val3));
        bf.addLong(val2);
        assertTrue(bf.testLong(val));
        assertTrue(bf.testLong(val1));
        assertTrue(bf.testLong(val2));
        assertFalse(bf.testLong(val3));
        bf.addLong(val3);
        assertTrue(bf.testLong(val));
        assertTrue(bf.testLong(val1));
        assertTrue(bf.testLong(val2));
        assertTrue(bf.testLong(val3));

        long randVal = 0;
        for (int i = 0; i < COUNT; i++) {
            randVal = rand.nextLong();
            bf.addLong(randVal);
        }

        assertTrue(bf.testLong(randVal));

        assertFalse(bf.testLong(-120));

        assertEquals(7800, bf.sizeInBytes());
    }

    @Test
    void testBloomFilterFloat() {
        LongBitsetBloomFilter bf = new LongBitsetBloomFilter(10000);
        float val = Float.NEGATIVE_INFINITY;

        assertFalse(bf.testFloat(val));
        float val1 = 1.1f;
        assertFalse(bf.testFloat(val1));
        float val2 = 2.2f;
        assertFalse(bf.testFloat(val2));
        float val3 = Float.POSITIVE_INFINITY;
        assertFalse(bf.testFloat(val3));
        bf.addFloat(val);
        assertTrue(bf.testFloat(val));
        assertFalse(bf.testFloat(val1));
        assertFalse(bf.testFloat(val2));
        assertFalse(bf.testFloat(val3));
        bf.addFloat(val1);
        assertTrue(bf.testFloat(val));
        assertTrue(bf.testFloat(val1));
        assertFalse(bf.testFloat(val2));
        assertFalse(bf.testFloat(val3));
        bf.addFloat(val2);
        assertTrue(bf.testFloat(val));
        assertTrue(bf.testFloat(val1));
        assertTrue(bf.testFloat(val2));
        assertFalse(bf.testFloat(val3));
        bf.addFloat(val3);
        assertTrue(bf.testFloat(val));
        assertTrue(bf.testFloat(val1));
        assertTrue(bf.testFloat(val2));
        assertTrue(bf.testFloat(val3));

        float randVal = 0;
        for (int i = 0; i < COUNT; i++) {
            randVal = rand.nextFloat();
            bf.addFloat(randVal);
        }

        assertTrue(bf.testFloat(randVal));

        assertFalse(bf.testFloat(-120.2f));

        assertEquals(7800, bf.sizeInBytes());
    }

    @Test
    void testBloomFilterDouble() {
        LongBitsetBloomFilter bf = new LongBitsetBloomFilter(10000);
        double val = Double.MIN_VALUE;

        assertFalse(bf.testDouble(val));
        double val1 = 1.1d;
        assertFalse(bf.testDouble(val1));
        double val2 = 2.2d;
        assertFalse(bf.testDouble(val2));
        double val3 = Double.MAX_VALUE;
        assertFalse(bf.testDouble(val3));
        bf.addDouble(val);
        assertTrue(bf.testDouble(val));
        assertFalse(bf.testDouble(val1));
        assertFalse(bf.testDouble(val2));
        assertFalse(bf.testDouble(val3));
        bf.addDouble(val1);
        assertTrue(bf.testDouble(val));
        assertTrue(bf.testDouble(val1));
        assertFalse(bf.testDouble(val2));
        assertFalse(bf.testDouble(val3));
        bf.addDouble(val2);
        assertTrue(bf.testDouble(val));
        assertTrue(bf.testDouble(val1));
        assertTrue(bf.testDouble(val2));
        assertFalse(bf.testDouble(val3));
        bf.addDouble(val3);
        assertTrue(bf.testDouble(val));
        assertTrue(bf.testDouble(val1));
        assertTrue(bf.testDouble(val2));
        assertTrue(bf.testDouble(val3));

        double randVal = 0;
        for (int i = 0; i < COUNT; i++) {
            randVal = rand.nextDouble();
            bf.addDouble(randVal);
        }

        assertTrue(bf.testDouble(randVal));

        assertFalse(bf.testDouble(-120.2d));

        assertEquals(7800, bf.sizeInBytes());
    }

    @Test
    void testBloomFilterString() {
        LongBitsetBloomFilter bf = new LongBitsetBloomFilter(10000);
        String val = "bloo";

        assertFalse(bf.testString(val));
        String val1 = "bloom fil";
        assertFalse(bf.testString(val1));
        String val2 = "bloom filter";
        assertFalse(bf.testString(val2));
        String val3 = "cuckoo filter";
        assertFalse(bf.testString(val3));
        bf.addString(val);
        assertTrue(bf.testString(val));
        assertFalse(bf.testString(val1));
        assertFalse(bf.testString(val2));
        assertFalse(bf.testString(val3));
        bf.addString(val1);
        assertTrue(bf.testString(val));
        assertTrue(bf.testString(val1));
        assertFalse(bf.testString(val2));
        assertFalse(bf.testString(val3));
        bf.addString(val2);
        assertTrue(bf.testString(val));
        assertTrue(bf.testString(val1));
        assertTrue(bf.testString(val2));
        assertFalse(bf.testString(val3));
        bf.addString(val3);
        assertTrue(bf.testString(val));
        assertTrue(bf.testString(val1));
        assertTrue(bf.testString(val2));
        assertTrue(bf.testString(val3));

        long randVal = 0;
        for (int i = 0; i < COUNT; i++) {
            randVal = rand.nextLong();
            bf.addString(Long.toString(randVal));
        }

        assertTrue(bf.testString(Long.toString(randVal)));

        assertFalse(bf.testString(Long.toString(-120)));

        assertEquals(7800, bf.sizeInBytes());
    }

    @Test
    void testMerge() {
        LongBitsetBloomFilter bf = new LongBitsetBloomFilter(10000);
        String val = "bloo";
        bf.addString(val);
        String val1 = "bloom fil";
        bf.addString(val1);
        String val2 = "bloom filter";
        bf.addString(val2);
        String val3 = "cuckoo filter";
        bf.addString(val3);

        LongBitsetBloomFilter bf2 = new LongBitsetBloomFilter(10000);
        String v = "2_bloo";
        bf2.addString(v);
        String v1 = "2_bloom fil";
        bf2.addString(v1);
        String v2 = "2_bloom filter";
        bf2.addString(v2);
        String v3 = "2_cuckoo filter";
        bf2.addString(v3);

        assertTrue(bf.testString(val));
        assertTrue(bf.testString(val1));
        assertTrue(bf.testString(val2));
        assertTrue(bf.testString(val3));
        assertFalse(bf.testString(v));
        assertFalse(bf.testString(v1));
        assertFalse(bf.testString(v2));
        assertFalse(bf.testString(v3));

        bf.merge(bf2);

        assertTrue(bf.testString(val));
        assertTrue(bf.testString(val1));
        assertTrue(bf.testString(val2));
        assertTrue(bf.testString(val3));
        assertTrue(bf.testString(v));
        assertTrue(bf.testString(v1));
        assertTrue(bf.testString(v2));
        assertTrue(bf.testString(v3));
    }
}