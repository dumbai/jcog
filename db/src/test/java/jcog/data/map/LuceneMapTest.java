package jcog.data.map;

import jcog.random.XoRoShiRo128PlusRandom;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

class LuceneMapTest {

    @Test
    void test1() {

        Map<String, String> map = new LuceneMap<>(LuceneMap.StorageLocation.RAM);

        //jjoller.lucenemap.LuceneMap<String, String> map = new jjoller.lucenemap.LuceneMap<>();


        assertTrue(map.size() == 0);
        assertTrue(map.isEmpty() );

        map.put("key", "val");

        assertTrue(map.size() == 1);
        assertTrue(map.containsKey("key"));
        assertTrue(map.containsValue("val"));
        assertFalse(map.containsKey("notkey"));
        assertFalse(map.containsValue("notkey"));

        map.put("key", "val2");
        assertTrue(map.containsKey("key"));
        assertTrue(map.containsValue("val2"));
        assertFalse(map.containsKey("notkey"));
        assertFalse(map.containsValue("val"));

        assertTrue(map.entrySet().size() == 1);
        assertTrue("key".equals(map.entrySet().iterator().next().getKey()));
        assertTrue("val2".equals(map.entrySet().iterator().next().getValue()));


        Random random = new XoRoShiRo128PlusRandom(1);
        long t = System.currentTimeMillis();
        int n = 1000;
        for (int i = 0; i < n; i++) {
            map.put(random.nextDouble() + "", random.nextDouble() + "");
        }
        System.out.println("took " + (System.currentTimeMillis() - t) + " ms to write " + n + " entries");


        t = System.currentTimeMillis();
        n = 1000;
        for (int i = 0; i < n; i++) {
            map.get(random.nextDouble());
        }
        System.out.println("took " + (System.currentTimeMillis() - t) + " ms to lookup " + n + " entries");

    }


    static void assertFalse(boolean v) {
        assertTrue(!v);
    }

    static void assertTrue(boolean v) {
        if (!v) {
            throw new IllegalStateException("Test failed");
        }
    }
}