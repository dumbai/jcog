package jcog.tree.interval;

import jcog.math.ImmLongInterval;
import jcog.math.LongInterval;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by me on 9/4/15.
 */
class IntervalTreeTest {

    private static @NotNull IntervalTree<Integer, String> makeIntervalTree() {
        IntervalTree<Integer, String> t = new IntervalTree<>();
        t.put(0, 10, "0-10");
        t.put(5, 15, "5-15");
        t.put(10, 20, "10-20");
        t.put(15, 25, "15-25");
        t.put(20, 30, "20-30");

        return t;
    }

    @Test
    void testIntersectionAdjacent() {

        LongInterval x = ImmLongInterval.intersection(1, 2, 2, 3);
        assertNotNull(x);
        assertEquals(1, x.range());
    }

    @Test
    void testCreate() {
        new IntervalTree<Double, String>();
    }

    @Test
    void testSinglePut() {
        IntervalTree<Double, String> t = new IntervalTree<>();
        t.put(30.0, 50.0, "test");
    }

    @Test
    void testSingleContainsValue() {
        IntervalTree<Double, String> t = new IntervalTree<>();
        t.put(30.0, 50.0, "test");
        t.containsValue("test");
    }

    @Test
    void testSingleKeyContains() {
        IntervalTree<Double, String> t = new IntervalTree<>();
        t.put(30.0, 50.0, "test");
        assertFalse(t.searchContaining(30.0, 45.0).isEmpty());
    }

    @Test
    void testSingleKeyContainsNotOverlap() {
        IntervalTree<Double, String> t = new IntervalTree<>();
        t.put(30.0, 50.0, "test");
        assertTrue(t.searchContaining(20.0, 40.0).isEmpty());
    }

    @Test
    void testSingleKeyOverlapping() {
        IntervalTree<Double, String> t = new IntervalTree<>();
        t.put(30.0, 50.0, "test");
        assertFalse(t.searchOverlapping(20.0, 40.0).isEmpty());
    }

    @Test
    void testClear() {
        IntervalTree<Integer, String> t = makeIntervalTree();
        t.clear();
        assertTrue(t.isEmpty());
    }

    @Test
    void testMultiMake() {
        makeIntervalTree();
    }

    @Test
    void testMultiContainsValue() {
        IntervalTree<Integer, String> t = makeIntervalTree();
        assertTrue(t.containsValue("15-25"));
        assertTrue(t.containsValue("5-15"));
        assertTrue(t.containsValue("20-30"));
    }

    @Test
    void testMultiSearchContaining() {
        IntervalTree<Integer, String> t = makeIntervalTree();
        Collection<String> res = t.searchContaining(0, 6);
        assertTrue(res.contains("0-10"));
        assertEquals(1, res.size());
    }

    @Test
    void testMultiSearchContaining2() {
        IntervalTree<Integer, String> t = makeIntervalTree();
        Collection<String> res = t.searchContaining(4, 16);
        assertFalse(res.contains("5-15"));
        assertTrue(res.isEmpty());
    }

    @Test
    void testMultiSearchContaining3() {
        IntervalTree<Integer, String> t = makeIntervalTree();
        Collection<String> res = t.searchContaining(7, 31);
        assertTrue(res.isEmpty());
    }

    @Test
    void testMultiSearchContainedBy() {
        IntervalTree<Integer, String> t = makeIntervalTree();
        Collection<String> res = t.searchContainedBy(0, 16);
        assertTrue(res.contains("0-10"));
        assertTrue(res.contains("5-15"));
        assertEquals(2, res.size());
    }

    @Test
    void testMultiSearchContainedBy2() {
        IntervalTree<Integer, String> t = makeIntervalTree();
        Collection<String> res = t.searchContainedBy(6, 31);
        assertFalse(res.contains("5-15"));
        assertFalse(res.isEmpty());
    }

    @Test
    void testMultiSearchContainedBy3() {
        IntervalTree<Integer, String> t = makeIntervalTree();
        Collection<String> res = t.searchContainedBy(0, 31);
        assertEquals(res.size(), t.size());
    }

    @Test
    void testRemove() {
        IntervalTree<Integer, String> t = makeIntervalTree();
        t.remove("0-10");
        t.remove("5-15");
        assertFalse(t.containsValue("0-10"));
        assertFalse(t.containsValue("5-15"));
    }


}