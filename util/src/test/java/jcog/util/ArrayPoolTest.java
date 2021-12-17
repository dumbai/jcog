package jcog.util;

import jcog.data.pool.ArrayPool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ArrayPoolTest {

    @Test
    void testBytePool() {
        ArrayPool<byte[]> p = ArrayPool.bytes();
        byte[] b1 = p.getMin(1);
        assertEquals(1, b1.length);
        byte[] b2 = p.getMin(2);
        assertEquals(2, b2.length);
        p.put(b1);
        byte[] b1again = p.getMin(1);
        assertSame(b1, b1again);

    }
}