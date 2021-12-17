package jcog.math;

import jcog.data.list.Lst;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** TODO more exhasutive tests */
class GilbertCurveTest {

    @Test void testSizes() {
        for (int w = 1; w < 9; w++)
            for (int h = 1; h < 9; h++)
                testGilbert(w, h);
    }


    private static void testGilbert(int w, int h) {
        Set<IntIntPair> pointsUnique = new TreeSet();
        List<IntIntPair> pointsOrdered = new Lst<>(w*h);

        GilbertCurve.gilbertCurve(w, h, (x, y) -> {
            IntIntPair xy = pair(x, y);
            pointsUnique.add(xy);
            pointsOrdered.add(xy);
        });

        assertEquals(w*h, pointsUnique.size(), "expected # of unique points");

        //test that each sequential point has manhattan distance <= 2 from previous
        for (int i =0; i < pointsOrdered.size()-1; i++) {
            IntIntPair a = pointsOrdered.get(i);
            IntIntPair b = pointsOrdered.get(i+1);
            assertTrue( Math.abs(a.getOne() - b.getOne()) + Math.abs(a.getTwo() - b.getTwo()) <= 2);
        }
    }
}