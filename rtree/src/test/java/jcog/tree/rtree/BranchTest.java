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

import jcog.tree.rtree.rect.RectDouble;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by jcovert on 6/12/15.
 */
class BranchTest {

    /**
     * Code was added to Branch remove an extra that's created during splitting.
     */
    @Test
    void branchOptimizationTest() {

        for(RTree2DTest.DefaultSplits type : RTree2DTest.DefaultSplits.values()) {
            RTree<RectDouble> rTree = RTree2DTest.createRect2DTree(type);
            RectDouble[] rects = RTree2DTest.generateRandomRects(80);

            int i = 0;
            
            while (i < 8) {
                rTree.add(rects[i++]);
            }
            assertEquals(0, rTree.stats().getBranchCount(), () -> "[" + type + "] Expected 0 branches at this time");

            
            rTree.add(rects[i++]);
            assertEquals(1, rTree.stats().getBranchCount(), () -> "[" + type + "] Expected 1 branch at this time");

            
            while (i < 10) {
                rTree.add(rects[i++]);
                assertEquals(i, rTree.size());
            }
            assertEquals(1, rTree.stats().getBranchCount(), () -> "[" + type + "] Expected 1 branch at this time:\n" + rTree.stats());

            
            while (i < 80) {
                rTree.add(rects[i++]);
                assertEquals(i, rTree.size());
            }
            assertTrue(3 <= rTree.stats().getBranchCount(), () -> "[" + type + "] Expected branches at this time");
        }
    }
}
