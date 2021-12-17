package jcog.signal.tensor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TensorRingTest {

    @Test
    void test1() {
        TensorRing t = new TensorRing(1, 3);
        t.setSpin(1); assertEquals(0, t.target()); assertEquals("1.0,0.0,0.0", t.toString());
        t.setSpin(2); assertEquals(1, t.target()); assertEquals("2.0,1.0,0.0", t.toString());
        t.setSpin(3); assertEquals(2, t.target()); assertEquals("3.0,2.0,1.0", t.toString());
        t.setSpin(4); assertEquals(0, t.target()); assertEquals("4.0,3.0,2.0", t.toString());
    }
}