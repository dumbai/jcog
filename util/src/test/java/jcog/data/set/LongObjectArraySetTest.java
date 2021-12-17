package jcog.data.set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LongObjectArraySetTest {

    @Test
    void testSort() {
        LongObjectArraySet l = new LongObjectArraySet();
        l.add(2L, "x");
        l.add(0L, "y");
        l.sortThis();
        assertEquals(0L, l.when(0));
        assertEquals(2L, l.when(1));
        assertEquals("y", l.get(0));
        assertEquals("x", l.get(1));

        l.add(-1L, "z");
        l.sortThis();
        assertEquals(-1L, l.when(0));
        assertEquals("z", l.get(0));

        l.add(4L, "w");
        assertEquals(4L, l.when(3));

        assertEquals("-1:z,0:y,2:x,4:w", l.toString());

        l.add(3L, "a");
        l.sortThis();
        assertEquals("-1:z,0:y,2:x,3:a,4:w", l.toString());

    }
}