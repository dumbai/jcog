package jcog;

import jcog.data.list.Lst;
import jcog.math.ImmLongInterval;
import org.apache.lucene.document.DoubleRange;
import org.apache.lucene.search.Query;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;
import static jcog.User.BOUNDS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {

    private static User testPutGet(Object obj) {
        User u = new User();
        u.put("key", obj);
        List l = new Lst();
        u.get("key", l::add);
        assertEquals(1, l.size());
        if (obj instanceof byte[]) {
            assertArrayEquals((byte[]) obj, (byte[]) l.get(0));
        } else {
            assertEquals(obj, l.get(0));
        }
        return u;
    }

    @Test
    void testPutGetString() {
        testPutGet("value");
    }

    @Test
    void testPutGetByteArray() {
        testPutGet(new byte[]{1, 2, 3, 4, 5});
    }

    @Test
    void testPutGetSerialized() {
        testPutGet(Maps.mutable.of("x", 6, "y", Lists.mutable.of("z", "z")));
    }

    @Test
    void testTimeIndex() {
        User u = new User();
        u.put("x", new MyEvent("x", 0, 4));
        u.put("y", new MyEvent("y", 4, 8));


        Query q = DoubleRange.newIntersectsQuery(BOUNDS,
                new double[]{-1, NEGATIVE_INFINITY, NEGATIVE_INFINITY, NEGATIVE_INFINITY},
                new double[]{3, POSITIVE_INFINITY, POSITIVE_INFINITY, POSITIVE_INFINITY});
        u.query(q, 8, (Predicate)(d) -> {
            System.out.println(d);
            return true;
        });
    }

    static class MyEvent extends ImmLongInterval {

        MyEvent(String name, long a, long b) {
            super(a, b);
        }
    }
}