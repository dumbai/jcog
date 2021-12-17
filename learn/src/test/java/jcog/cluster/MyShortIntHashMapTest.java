package jcog.cluster;

import jcog.cluster.impl.MyShortIntHashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by me on 5/25/16.
 */
class MyShortIntHashMapTest {

    @Test
    void testAddToValuesAndFilter() {
        MyShortIntHashMap m = new MyShortIntHashMap();
        for (int c = 0; c < 4; c++) {
            for (int i = 0; i < 100; i++) {
                m.put((short) (Math.random() * 1000), 1);
            }

            m.addToValues(4);

            m.forEachKeyValue((k, v) -> assertEquals(5, v));

            m.filter(v -> v == 0);

            assertTrue(m.isEmpty());
        }
    }

}