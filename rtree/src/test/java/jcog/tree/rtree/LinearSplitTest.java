package jcog.tree.rtree;

/*
 * #%L
 * Conversant RTree
 * ~~
 * Conversantmedia.com © 2016, Conversant, Inc. Conversant® is a trademark of Conversant, Inc.
 * ~~
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import jcog.tree.rtree.node.RBranch;
import jcog.tree.rtree.node.RNode;
import jcog.tree.rtree.rect.RectDouble;
import jcog.tree.rtree.util.Stats;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by jcovert on 6/12/15.
 */
class LinearSplitTest {

    private static final RTree2DTest.DefaultSplits TYPE = RTree2DTest.DefaultSplits.LINEAR;

    /**
     * Adds enough entries to force a single split and confirms that
     * no entries are lost.
     */
    @Test
    void basicSplitTest() {

        RTree<RectDouble> rTree = RTree2DTest.createRect2DTree(TYPE);
        rTree.add(new RectDouble(0, 0, 1, 1));
        rTree.add(new RectDouble(1, 1, 2, 2));
        rTree.add(new RectDouble(2, 2, 3, 3));
        rTree.add(new RectDouble(3, 3, 4, 4));
        rTree.add(new RectDouble(4, 4, 5, 5));
        rTree.add(new RectDouble(5, 5, 6, 6));
        rTree.add(new RectDouble(6, 6, 7, 7));
        rTree.add(new RectDouble(7, 7, 8, 8));
        
        rTree.add(new RectDouble(8, 8, 9, 9));

        Stats stats = rTree.stats();
        assertEquals(1, stats.getMaxDepth(), "Unexpected max depth after basic split");
        assertEquals(1, stats.getBranchCount(), "Unexpected number of branches after basic split");
        assertEquals(2, stats.getLeafCount(), "Unexpected number of leaves after basic split");
        assertEquals(4.5, stats.getEntriesPerLeaf(), "Unexpected number of entries per leaf after basic split");
    }

    @Test
    void splitCorrectnessTest() {

        RTree<RectDouble> rTree = RTree2DTest.createRect2DTree(4, TYPE);
        rTree.add(new RectDouble(0, 0, 3, 3));
        rTree.add(new RectDouble(1, 1, 2, 2));
        rTree.add(new RectDouble(2, 2, 4, 4));
        rTree.add(new RectDouble(4, 0, 5, 1));
        
        rTree.add(new RectDouble(0, 2, 1, 4));

        RBranch<RectDouble> root = (RBranch<RectDouble>) rTree.root();
        RNode<RectDouble>[] children = root.items;
        long count = Arrays.stream(children).filter(Objects::nonNull).count();
        int childCount = (int) count;
        assertEquals(2, childCount, "Expected different number of children after split");

        RNode<RectDouble> child1 = children[0];
        RectDouble child1Mbr = (RectDouble) child1.bounds();
        RectDouble expectedChild1Mbr = new RectDouble(0, 0, 4, 4);
        assertEquals( 4, child1.size(), "Child 1 size incorrect after split");
        assertEquals(expectedChild1Mbr, child1Mbr, "Child 1 mbr incorrect after split");

        RNode<RectDouble> child2 = children[1];
        RectDouble child2Mbr = (RectDouble) child2.bounds();
        RectDouble expectedChild2Mbr = new RectDouble(4, 0, 5, 1);
        assertEquals(1, child2.size(), "Child 2 size incorrect after split");
        assertEquals(expectedChild2Mbr, child2Mbr, "Child 2 mbr incorrect after split");
    }

    /**
     * Adds several overlapping rectangles and confirms that no entries
     * are lost during insert/split.
     */
    @Test
    void overlappingEntryTest() {

        RTree<RectDouble> rTree = RTree2DTest.createRect2DTree(TYPE);
        rTree.add(new RectDouble(0, 0, 1, 1));
        rTree.add(new RectDouble(0, 0, 2, 2));
        rTree.add(new RectDouble(0, 0, 2.1, 2));
        rTree.add(new RectDouble(0, 0, 3, 3));
        rTree.add(new RectDouble(0, 0, 3.1, 3));

        rTree.add(new RectDouble(0, 0, 4, 4));
        rTree.add(new RectDouble(0, 0, 5, 5));
        rTree.add(new RectDouble(0, 0, 6, 6));
        rTree.add(new RectDouble(0, 0, 7, 7));
        rTree.add(new RectDouble(0, 0, 7, 7.1));

        rTree.add(new RectDouble(0, 0, 8, 8));
        rTree.add(new RectDouble(0, 0, 9, 9));
        rTree.add(new RectDouble(0, 1, 2, 2));
        rTree.add(new RectDouble(0, 1, 3, 3));
        rTree.add(new RectDouble(0, 1, 4, 4));

        rTree.add(new RectDouble(0, 1, 4.1, 4));
        rTree.add(new RectDouble(0, 1, 5, 5));

        
        final int expectedEntryCount = 17;

        Stats stats = rTree.stats();
        assertEquals(expectedEntryCount, stats.size(), () -> "Unexpected number of entries in " + TYPE + " split tree: " + stats.size() + " entries - expected: " + expectedEntryCount + " actual: " + stats.size());
    }


    /**
     * This test previously caused a StackOverflowException.
     * It has since been fixed, but keeping the test to ensure
     * it doesn't happen again.
     */
    @Test
    void causeLinearSplitOverflow() {
        RTree<RectDouble> rTree = RTree2DTest.createRect2DTree(8, TYPE);
        Random rand = new Random(13);
        for (int i = 0; i < 500; i++) {
            int x1 = rand.nextInt(10);
            int y1 = rand.nextInt(10);
            int x2 = x1 + rand.nextInt(200);
            int y2 = y1 + rand.nextInt(200);

            rTree.add(new RectDouble(x1, y1, x2, y2));
        }
        Stats stats = rTree.stats();
        stats.print(System.out);
    }


    @Test
    void causeLinearSplitNiceDist() {

        RTree<RectDouble> rTree = RTree2DTest.createRect2DTree(8, TYPE);
        Random rand = new Random(13);
        for (int i = 0; i < 500; i++) {
            int x1 = rand.nextInt(250);
            int y1 = rand.nextInt(250);
            int x2 = x1 + rand.nextInt(10);
            int y2 = y1 + rand.nextInt(10);

            rTree.add(new RectDouble(x1, y1, x2, y2));
        }
        Stats stats = rTree.stats();
        stats.print(System.out);
    }

}
