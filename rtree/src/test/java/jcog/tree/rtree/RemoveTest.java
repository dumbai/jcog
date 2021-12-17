package jcog.tree.rtree;

import jcog.tree.rtree.node.RBranch;
import jcog.tree.rtree.node.RLeaf;
import jcog.tree.rtree.rect.RectDouble;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RemoveTest {

    @Test
    void testRemoveRebalance() {
        final int LEAF_CAP = 3;
        RTree<RectDouble> r = RTree2DTest.createRect2DTree(RTree2DTest.DefaultSplits.AXIAL, LEAF_CAP);

        RectDouble[] rects = RTree2DTest.generateRandomRects(7);

        for (RectDouble x : rects)
            r.add(x);
        assertEquals(7, r.size());

        assertEquals(rects.length, r.size());

//        r.stats().print();
//        r.streamNodesRecursively().forEach(x1 -> System.out.println(x1.getClass() + " " + x1.size()));

        //assertBranchLeafCounts(r, 2, 4); //either 1 branch and 3 leaves, or 2 branches and 4 leaves


//        assertEquals(6, r.streamNodesRecursively().count());

        {
            boolean removed = r.remove(rects[0]);
            assertTrue(removed);
            assertEquals(rects.length - 1, r.size());

            assertEquals(6, r.size());

            assertFalse(r.remove(rects[0])); //already removed
        }

        assertTrue(r.remove(rects[4]));         assertEquals(5, r.size());  assertEquals(5, r._size());

        assertTrue(r.remove(rects[5]));         assertEquals(4, r.size());  assertEquals(4, r._size());

//        r.stats().print();

//        assertBranchLeafCounts(r, 2, 3);

    }

    private void assertBranchLeafCounts(RTree<RectDouble> r, int branchCount, int leafCount) {
        assertEquals(branchCount, r.streamNodesRecursively().filter(x ->x instanceof RBranch).count());
        assertEquals(leafCount, r.streamNodesRecursively().filter(x ->x instanceof RLeaf).count());
    }
}
