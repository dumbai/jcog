package jcog.data.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CompactArrayMapTest {

    @Test
    void testPutGetRemove() {
        CompactArrayMap m = new CompactArrayMap();
        assertEquals(0, m.size());
        assertNull(m.get("?"));
        m.put("a", "x");
        m.put("b", "y");
        m.put("c", "z");
        assertEquals(3, m.size());
        assertEquals("x", m.get("a"));
        assertEquals("y", m.get("b"));
        assertEquals("z", m.get("c"));
        assertNull(m.get("d"));
        assertEquals("y", m.put("b", "w"));
        assertEquals("w", m.get("b"));

        m.put("d", "_");
        assertEquals("_", m.remove("d")); //removal from end
        assertEquals(3, m.size());

        assertEquals("w", m.remove("b")); //removal from middle
        assertEquals(2, m.size());

        assertNull(m.get("b")); //check integrity after removal; item not present any more
        assertEquals("z", m.get("c")); //check integrity after removal

        assertEquals("z", m.remove("c"));
        assertEquals(1, m.size());
        assertEquals("x", m.remove("a"));
        assertEquals(0, m.size());

    }
}