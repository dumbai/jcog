package jcog.data;

import jcog.data.set.SimpleIntSet;
import jcog.random.XoRoShiRo128PlusRandom;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SimpleIntSetTest {

    @Test
    void test1() {
        SimpleIntSet s = new SimpleIntSet();

        XoRoShiRo128PlusRandom x = new XoRoShiRo128PlusRandom(1);
        Set<Integer> h = new HashSet();
        XoRoShiRo128PlusRandom y = new XoRoShiRo128PlusRandom(1);
        int length = 1000;

        for (int i = 0; i < length; i++) {
            int xi = x.nextInt();
            if (h.add(xi)) {
                s.add(xi);
                assertFalse(s.add(xi)); 
            } else {
                assertTrue(s.contains(xi));
            }
        }

        for (int i = 0; i < length; i++) {
            int yi = y.nextInt();
            assertTrue(s.contains(yi));
            if (!h.contains(yi+1)) 
                assertFalse(s.contains(yi+1));
        }

        assertEquals(h.size(), s.size());

        s.clear();

        assertEquals(0, s.size());
    }
}