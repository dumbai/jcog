package jcog.tree.rtree;

import jcog.tree.rtree.rect.RectDouble;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SplitTest {

        /**
     * Adds many random entries to trees of different types and confirms that
     * no entries are lost during insert/split.
     */
    @Test
    void randomEntryTest() {

        int entryCount = 32*1024;

        RectDouble[] rects = RTree2DTest.generateRandomRects(entryCount);

        for (RTree2DTest.DefaultSplits s : RTree2DTest.DefaultSplits.values()) {
            /*for (int min : new int[]{2, 3, 4})*/ {
                for (int max : new int[]{2, 3, 8}) /*for (int max : new int[]{min, min+1, 8})*/ {

                    int TOTAL = Math.round(max/8f * rects.length);
                    if (TOTAL%2==1) TOTAL++;

                    assert(TOTAL<=entryCount); 
                    assert(TOTAL%2==0); 
                    int HALF = TOTAL/2;


                    RTree<RectDouble> t = RTree2DTest.createRect2DTree(s, max);
                    int i = 0;
                    for (int i1 = 0; i1 < HALF; i1++) {
                        RectDouble x = rects[i1];
                        boolean added = t.add(x);
                        if (!added) {
                            t.add(x);
                            fail("");
                        }

                        assertTrue(added);
                        assertTrue(t.contains(x));

                        assertEquals(++i, t.size());
                        

                        boolean tryAddingAgainToTestForNonMutation = t.add(x);
                        if (tryAddingAgainToTestForNonMutation) {
                            t.add(x);
                            fail("");
                        }
                        assertFalse(tryAddingAgainToTestForNonMutation, i + "==?" + t.size()); 
                        assertEquals(i, t.size()); 
                        
                    }


                    assertEquals(HALF, t.size());

                    System.out.println(s);
                    assertEquals(HALF, t.stats().print(System.out).size());


                    for (int k = 0; k < HALF; k++)
                        assertFalse(t.add(rects[k]));

                    for (int k = 0; k < HALF; k++) {
                        RectDouble a = rects[k];
                        RectDouble b = rects[k + HALF];
                        assertNotEquals(a,b);

                        assertFalse(t.contains(b));
                        assertTrue(t.contains(a));
                        t.replace(a, b);




                        assertFalse(t.contains(a));
                        assertTrue(t.contains(b));

                        assertEquals(HALF, t.size()); 

                    }

                    
                    assertEquals(HALF, t.size());

                    assertEquals(HALF, t.stats().size());

                    for (int k = 0; k < HALF; k++) {
                        assertTrue(t.add(rects[k])); 
                    }


                    assertEquals(TOTAL, t.size());
                    assertEquals(TOTAL, t.stats().size());

                    int[] andCount = {0};
                    assertTrue(t.root().AND(x -> {
                        andCount[0]++;
                        return true;
                    }));
                    assertEquals(TOTAL, andCount[0]);

                    int[] orCount = {0};
                    assertFalse(t.OR(x -> {
                        orCount[0]++;
                        return false;
                    }));
                    assertEquals(TOTAL, orCount[0]);

                    int[] eachCount= {0};
                    t.forEach(x -> eachCount[0]++);
                    assertEquals(TOTAL, eachCount[0]);
                }
            }
        }
    }

}
