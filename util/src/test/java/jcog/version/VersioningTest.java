package jcog.version;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TODO test capacity restriction
 */
class VersioningTest {

    final Versioned a = new UniVersioned(), b = new UniVersioned();

//    private
//    MultiVersioned a = new MultiVersioned(8);
//    private @NotNull
//    MultiVersioned b = new MultiVersioned(8);
//    @Test
//    void testRevision() {
//        Versioning<Object> w = new Versioning(10, 10);
//        VersionMap<Object,Object> m = new MultiVersionMap(w, 4);
//        m.set("x", "a");
//        assertEquals("{x=a}", m.toString());
//        assertEquals(1, w.size);
//        m.set("x", "b");
//
//        assertEquals("{x=b}", m.toString());
//
//        MultiVersioned mvx = (MultiVersioned) m.map.get("x");
//
//        assertEquals("(a, b)", mvx.toStackString());
//        assertEquals(2, w.size);
//        assertEquals(2, mvx.size());
//
//        w.revert(2);
//        assertEquals(2, w.size);
//        assertEquals("(a, b)", mvx.toStackString());
//        assertEquals(2, mvx.size());
//        assertEquals(2, w.size);
//
//        w.revert(1);
//        assertEquals(1, w.size);
//        assertEquals("{x=a}", m.toString());
//        assertEquals("(a)", mvx.toStackString());
//        assertEquals(1, mvx.size());
//        assertEquals(1, w.size);
//
//        w.revert(0);
//        assertEquals(0, w.size);
//        assertEquals(0, w.size);
//        assertEquals(0, mvx.size());
//        assertEquals("{}", m.toString());
//
//        assertNull(m.get("x"));
//
//
//    }

    @Test void testLimitedChanges() {
        Versioning<Object> w = new Versioning<>(10, 10);
        VersionMap<Object,Object> m = new VersionMap<>(w);
        boolean a = m.set("x", "a");
        assertTrue(a);
        boolean b = m.set("x", "b");
        assertFalse(b);
        assertEquals("{x=a}", m.toString());

//        boolean ok = m.force("x", "c");
//        assertTrue(ok);
//        assertEquals("{x=c}", m.toString());

        w.pop();
        boolean b2 = m.set("x", "b");
        assertTrue(b2);

    }
//
//    private void sequence1(Versioning v, boolean print) {
//
//        if (print) System.out.println(v); assertTrue(a.set("a0",v));
//        if (print) System.out.println(v);
//        ((Versioned) a).set("a1", v);
//        if (print) System.out.println(v);
//        ((Versioned) b).set("b0", v);
//        if (print) System.out.println(v);
//        ((Versioned) a).set("a2", v);
//        if (print) System.out.println(v);
//        ((Versioned) a).set("a3", v);
//        if (print) System.out.println(v);
//        ((Versioned) b).set("b1", v);
//
//    }

    @Disabled
    @Test
    void test2() {

        Versioning v = new Versioning(10, 10);

//        sequence1(v);

        Supplier<String> s = () -> a + " " + b;

        System.out.println(v);
        assertEquals(6, v.size); assertEquals("a3 b1", s.get());

        v.revert(5); System.out.println(v);
        assertEquals(5, v.size); assertEquals("a3 b0", s.get());

        v.revert(4); System.out.println(v);
        assertEquals(4, v.size); assertEquals("a2 b0", s.get());

        v.revert(3); System.out.println(v);
        assertEquals(3, v.size); assertEquals("a1 b0", s.get());

        v.revert(2); System.out.println(v);
        assertEquals(2, v.size);  assertEquals("a1 null", s.get());

        v.revert(1); System.out.println(v);
        assertEquals(1, v.size);  assertEquals("a0 null", s.get());

        v.revert(0); System.out.println(v);
        assertEquals(0, v.size); assertEquals("null null", s.get());

    }
//
//    private void sequence1(Versioning v) {
//        sequence1(v, false);
//    }


    @Disabled @Test
    void testRevert() {


        Versioning v = new Versioning(10, 10);
//        sequence1(v);

        Supplier<String> s = () -> a + " " + b;

        System.out.println(v);
        assertEquals(6, v.size); assertEquals("a3 b1", s.get());
        assertEquals(6, v.size);

        System.out.println("revert to 3");

        
        v.revert(3); System.out.println(v);
        assertEquals(3, v.size);
        assertEquals(3, v.size); assertEquals("a1 b0", s.get());

        v.revert(2);  System.out.println(v);
        assertEquals(2, v.size);
        assertEquals(2, v.size); assertEquals("a1 null", s.get());

    }

}