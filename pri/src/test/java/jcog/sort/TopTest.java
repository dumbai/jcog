package jcog.sort;

import jcog.data.list.Lst;
import org.junit.jupiter.api.Test;

import static java.lang.Float.NEGATIVE_INFINITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** tests for Top, TopN, and RankedN
 *  TODO test for adding items with rank NaN (ignored)
 */
class TopTest {

    @Test
    void testTop() {
        Top<String> c = new Top<>(String::length);
        assertAdd(c, "x", "x");
        assertAdd(c, "xx", "xx");
        assertAdd(c, "y", "xx"); //unchanged, less
        assertAdd(c, "yy", "xx"); //unchanged, equal
        assertAdd(c, "yyy", "yyy");

    }

    @Test
    void testTopN() {
        TopN<String> c = new TopN<>(new String[3], String::length);
        assertEquals(NEGATIVE_INFINITY, c.minValueIfFull());
        assertAdd(c, "a", "[a]");
//        assertAdd(c, "a", "[a, a]"); //duplicate kept
        assertEquals(1, c.minValue());
        assertEquals(NEGATIVE_INFINITY, c.minValueIfFull());
        c.clear();
        assertAdd(c, "a", "[a]");
        assertAdd(c, "bbb", "[bbb, a]");
        assertAdd(c, "cc", "[bbb, cc, a]");
        assertEquals(1, c.minValueIfFull());
        assertAdd(c, "dd", "[bbb, cc, dd]");
        assertEquals(2, c.minValueIfFull());
        assertAdd(c, "eee", "[bbb, eee, cc]");
        assertAdd(c, "ff", "[bbb, eee, cc]");  //disallow replacement of equal to weakest
        assertAdd(c, "BBB", "[bbb, eee, BBB]");
        assertAdd(c, "xxxx", "[xxxx, bbb, eee]");
        assertAdd(c, "yyyyy", "[yyyyy, xxxx, bbb]");
        c.clear();
        assertEquals(NEGATIVE_INFINITY, c.minValueIfFull());
    }

    @Test
    void testRankedN() {
        RankedN<String> c = new RankedN<>(new String[3], String::length);
        assertTrue(c.isSorted());

        assertAdd(c, "a", "[a]");
        //assertAdd(c, "a", "[a]"); //duplicate absorbed
        assertAdd(c, "bbb", "[bbb, a]");
        assertAdd(c, "cc", "[bbb, cc, a]");
        assertAdd(c, "dd", "[bbb, cc, dd]");
        assertAdd(c, "eee", "[bbb, eee, cc]");
        assertAdd(c, "ff", "[bbb, eee, cc]");  //disallow replacement of equal to weakest
        c.pop(); assertEquals("[eee, cc]", str(c));
        assertAdd(c, "f", "[eee, cc, f]");
        c.remove(1); assertEquals("[eee, f]", str(c));
        assertAdd(c, "gg", "[eee, gg, f]");

    }

    private static String str(TopFilter c) {
        return new Lst(c).toString();
    }

    private static void assertAdd(Top<String> c, String x, String expect) {
        c.accept(x); assertEquals(expect, c.the);
    }
    private static void assertAdd(TopN<String> c, String x, String expect) {
        c.accept(x); assertEquals(expect, str(c));  assertTrue(c.isSorted());
    }
//    private static void assertAdd(CachedTopN<String> c, String x, String expect) {
//        c.accept(x); assertEquals(expect, new FasterList(c.list).toString());
//    }
}