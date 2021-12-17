package jcog.signal.anomaly;

import jcog.Util;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HistogramAnomalyTest {

    @Test
    void test1() {
        HistogramAnomaly h = new HistogramAnomaly(4);
        h.bin[0] = 10;//h.max = 10;
        h.bin[1] = 4;
        h.bin[2] = 2;
        h.bin[3] = 1;
        h.count = Util.sum(h.bin, 0, 4);
        assertTrue(h.anomaly(0) < h.anomaly(0.33f));
        assertTrue(h.anomaly(0.33f) < h.anomaly(0.66f));
        assertTrue(h.anomaly(0.66f) < h.anomaly(1f));
//        assertEquals(0.41f, h.anomaly(0), 0.01f);
//        assertEquals(0.76f, h.anomaly(0.33f), 0.01f);
//        assertEquals(0.88f, h.anomaly(0.66f), 0.01f);
//        assertEquals(0.94f, h.anomaly(1), 0.01f);
    }
}