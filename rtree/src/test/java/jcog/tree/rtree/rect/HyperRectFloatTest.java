package jcog.tree.rtree.rect;

import jcog.tree.rtree.point.FloatND;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HyperRectFloatTest {

    @Test void testFloatNDEquality() {
        assertEquals(new FloatND(1, 1),new FloatND(1, 1));
    }
    @Test
    void testSortBoundingPoints() {
        assertEquals("(([1.0, 1.0]),([2.0, 2.0]))",  new HyperRectFloat(new float[] { 1, 1 }, new float[] { 2, 2 }).toString());
        assertEquals("(([1.0, 1.0]),([2.0, 2.0]))",  new HyperRectFloat(new float[] { 2, 2 }, new float[] { 1, 1 }).toString());
        assertEquals("(([1.0, 1.0]),([2.0, 2.0]))",  new HyperRectFloat(new float[] { 2, 1 }, new float[] { 1, 2 }).toString());
    }

}