package jcog.pri;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriReferenceTest {

    @Test void PlainLink_a() {
        assertXHalf(new PlainLink<>("x", 0.5f));
    }

    @Test void PLink_a() {
        assertXHalf(new PLink<>("x", 0.5f));
    }

    private static void assertXHalf(PriReference<String> l) {
        assertEquals("x", l.get());
        assertEquals(0.5f, l.pri(), 0.01f);
    }


}