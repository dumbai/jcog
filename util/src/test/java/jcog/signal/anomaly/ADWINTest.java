package jcog.signal.anomaly;

import jcog.signal.anomaly.adwin.AdwinAnomalyzer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TODO microbenchmark from https://github.com/abifet/adwin/tree/master/parallel-adwin/src/main/java/de/tub/bdapro/adwin/benchmark
 */
class ADWINTest {
    @Test
    void test1() {



        AdwinAnomalyzer adwin = new AdwinAnomalyzer(256, .01); // Init AdwinAnomalyze with delta=.01
        for (int p = 0; p < 1000; p++) {
            if (adwin.add(f(p))) //Input data into AdwinAnomalyze
                System.out.println("Change Detected: " + p);
        }
        //Get information from AdwinAnomalyze
        assertEquals(500, adwin.mean());
        System.out.println("Mean:" + adwin.mean());
        System.out.println("Variance:" + adwin.variance());
        System.out.println("Stand. dev:" + Math.sqrt(adwin.variance()));
//        System.out.println("Width:" + adwin.width());
    }

    private static double f(int p) {
        //Gradual Change
        //return (double) 3*p;
        //Abrupt Change
        return (p < 500 ? 1000 : 500);


    }
}