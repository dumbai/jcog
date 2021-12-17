package jcog.data.set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArrayHashRingTest {

    @Test
    void test1() {
        ArrayHashRing a = new ArrayHashRing(3);
        a.add("x");
        a.add("y");
        a.add("z");
        assertEquals("[x, y, z]", a.toString());
        a.add("z");
        assertEquals("[x, y, z]", a.toString());
        a.add("w");
        assertEquals("[y, z, w]", a.toString());

        assertEquals(a.setSize(), a.size());


    }
}