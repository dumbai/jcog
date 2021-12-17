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

import jcog.tree.rtree.point.HyperPoint;
import jcog.tree.rtree.rect.RectDouble;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by jcovert on 6/16/15.
 */
class Rect2DTest {

    @Test
    void centroidTest() {

        RectDouble rect = new RectDouble(0, 0, 4, 3);

        HyperPoint centroid = rect.center();
        double x = centroid.coord(0);
        double y = centroid.coord(1);
        assertEquals(2.0d, x, () -> "Bad X-coord of centroid - expected " + 2.0 + " but was " + x);
        assertEquals(1.5d, y, () -> "Bad Y-coord of centroid - expected " + 1.5 + " but was " + y);
    }

    @Test
    void mbrTest() {

        RectDouble rect = new RectDouble(0, 0, 4, 3);

        
        RectDouble rectInside = new RectDouble(0, 0, 1, 1);
        RectDouble mbr = rect.mbr(rectInside);
        double expectedMinX = rect.min.x;
        double expectedMinY = rect.min.y;
        double expectedMaxX = rect.max.x;
        double expectedMaxY = rect.max.y;
        double actualMinX = mbr.min.x;
        double actualMinY = mbr.min.y;
        double actualMaxX = mbr.max.x;
        double actualMaxY = mbr.max.y;
        assertEquals(actualMinX, expectedMinX, "Bad minX - Expected: " + expectedMinX + " Actual: " + actualMinX);
        assertEquals(actualMinY, expectedMinY, "Bad minY - Expected: " + expectedMinY + " Actual: " + actualMinY);
        assertEquals(actualMaxX, expectedMaxX, "Bad maxX - Expected: " + expectedMaxX + " Actual: " + actualMaxX);
        assertEquals(actualMaxY, expectedMaxY, "Bad maxY - Expected: " + expectedMaxY + " Actual: " + actualMaxY);

        
        RectDouble rectOverlap = new RectDouble(3, 1, 5, 4);
        mbr = rect.mbr(rectOverlap);
        expectedMinX = 0.0d;
        expectedMinY = 0.0d;
        expectedMaxX = 5.0d;
        expectedMaxY = 4.0d;
        actualMinX = mbr.min.x;
        actualMinY = mbr.min.y;
        actualMaxX = mbr.max.x;
        actualMaxY = mbr.max.y;
        assertEquals(actualMinX, expectedMinX, "Bad minX - Expected: " + expectedMinX + " Actual: " + actualMinX);
        assertEquals(actualMinY, expectedMinY, "Bad minY - Expected: " + expectedMinY + " Actual: " + actualMinY);
        assertEquals(actualMaxX, expectedMaxX, "Bad maxX - Expected: " + expectedMaxX + " Actual: " + actualMaxX);
        assertEquals(actualMaxY, expectedMaxY, "Bad maxY - Expected: " + expectedMaxY + " Actual: " + actualMaxY);
    }

    @Test
    void rangeTest() {

        RectDouble rect = new RectDouble(0, 0, 4, 3);

        double xRange = rect.range(0);
        double yRange = rect.range(1);
        assertEquals(4.0d, xRange, () -> "Bad range in dimension X - expected " + 4.0 + " but was " + xRange);
        assertEquals(3.0d, yRange, () -> "Bad range in dimension Y - expected " + 3.0 + " but was " + yRange);
    }


    @Test
    void containsTest() {

        RectDouble rect = new RectDouble(0, 0, 4, 3);

        
        RectDouble rectOutsideNotContained = new RectDouble(4, 2, 5, 3);
        assertFalse(rect.contains(rectOutsideNotContained), "Shares an edge but should not be 'contained'");

        
        RectDouble rectInsideNotContained = new RectDouble(0, 1, 4, 5);
        assertFalse(rect.contains(rectInsideNotContained), "Shares an edge but should not be 'contained'");

        
        RectDouble rectInsideContained = new RectDouble(0, 1, 1, 2);
        assertTrue(rect.contains(rectInsideContained), "Shares an edge and should be 'contained'");

        
        RectDouble rectIntersects = new RectDouble(3, 2, 5, 4);
        assertFalse(rect.contains(rectIntersects), "Intersects but should not be 'contained'");

        
        RectDouble rectContained = new RectDouble(1, 1, 2, 2);
        assertTrue(rect.contains(rectContained), "Contains and should be 'contained'");

        
        RectDouble rectNotContained = new RectDouble(5, 0, 6, 1);
        assertFalse(rect.contains(rectNotContained), "Does not contain and should not be 'contained'");
    }

    @Test
    void intersectsTest() {

        RectDouble rect = new RectDouble(0, 0, 4, 3);

        
        RectDouble rectOutsideIntersects = new RectDouble(4, 2, 5, 3);
        assertTrue(rect.intersects(rectOutsideIntersects), "Shares an edge and should 'intersect'");

        
        RectDouble rectInsideIntersects = new RectDouble(0, 1, 4, 5);
        assertTrue(rect.intersects(rectInsideIntersects), "Shares an edge and should 'intersect'");

        
        RectDouble rectInsideIntersectsContained = new RectDouble(0, 1, 1, 2);
        assertTrue(rect.intersects(rectInsideIntersectsContained), "Shares an edge and should 'intersect'");

        
        RectDouble rectIntersects = new RectDouble(3, 2, 5, 4);
        assertTrue(rect.intersects(rectIntersects), "Intersects and should 'intersect'");

        
        RectDouble rectContained = new RectDouble(1, 1, 2, 2);
        assertTrue(rect.intersects(rectContained), "Contains and should 'intersect'");

        
        RectDouble rectNotIntersects = new RectDouble(5, 0, 6, 1);
        assertFalse(rect.intersects(rectNotIntersects), "Does not intersect and should not 'intersect'");
    }

    @Test
    void costTest() {

        RectDouble rect = new RectDouble(0, 0, 4, 3);
        double cost = rect.cost();
        assertEquals(12.0d, cost, () -> "Bad cost - expected " + 12.0 + " but was " + cost);
    }
}
