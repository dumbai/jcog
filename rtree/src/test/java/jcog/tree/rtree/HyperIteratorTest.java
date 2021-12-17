package jcog.tree.rtree;

import jcog.Str;
import jcog.Util;
import jcog.sort.QuickSort;
import jcog.tree.rtree.node.RNode;
import jcog.tree.rtree.rect.RectDouble;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;
import org.junit.jupiter.api.Test;

import static jcog.tree.rtree.RTree2DTest.DefaultSplits.QUADRATIC;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HyperIteratorTest {

    static double distanceCartesian(RectDouble r, double x, double y) {
        return Math.sqrt( Util.sqr(r.center(0) - x) + Util.sqr(r.center(1) - y) );
    }

    @Test
    void test1() {
        final int entryCount = 100;
        RectDouble[] rects = RTree2DTest.generateRandomRects(entryCount);

        RTree<RectDouble> t = RTree2DTest.createRect2DTree(QUADRATIC, 4);
        for (int i = 0; i < rects.length; i++)
            t.add(rects[i]);

//        t.forEach(System.out::println); System.out.println();

        double tx = 100, ty = 100;

        int width = 5;
        HyperIterator<RectDouble> i = new HyperIterator<>(width, new RectDouble[width]) {
            @Override
            protected float rankNode(RNode<RectDouble> o, float min) {
                return (float) (1.0 / distanceCartesian((RectDouble) (o.bounds()), tx, ty));
            }

            @Override
            protected float rankItem(RectDouble o, float min) {
                return (float) (1.0 / distanceCartesian(o, tx, ty));
            }
        };

        i.add(t);
        i.bfs();
        System.out.println("beam results:");
        int count = 0;
        double prevDist = Double.NEGATIVE_INFINITY;
        for (RectDouble x : i.values) {
            assertNotNull(x);
            count++;
            double xDist = distanceCartesian(x, tx, ty);
            System.out.println(Str.n4(xDist) + " " + x);

            assertTrue(xDist >= prevDist);
            prevDist = xDist;
        }
        assertTrue(count >= width);

        System.out.println("\nexhaustive results:");
        QuickSort.sort(rects, 0, rects.length, (FloatFunction<RectDouble>) (r)->(float)distanceCartesian(r, tx, ty));
        for (int b = 0; b < count; b++) {
            RectDouble x = rects[b];
            System.out.println(Str.n4(distanceCartesian(x, tx, ty)) + " " + x);
        }
    }
}