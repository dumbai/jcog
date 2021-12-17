package jcog.pri.bag.tree;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeBagTest {

    @Test
    void test1() {

        DefaultTreeBag<String,Character> t = new DefaultTreeBag<>();

        assertTrue( t.put(List.of("x", "y"), '?') );
        assertEquals(1, t.size());
        assertTrue( t.remove(List.of("x", "y"), '?') );
        assertEquals(0, t.size());
        assertTrue(t.root.isEmpty());

        t.capacity(5);
        t.put(List.of(), '*');
        t.put(List.of("x"), '?');
        t.put(List.of("x"), '@');
        t.put(List.of("y"), '+');
        t.put(List.of("z", "a"), '!');
        t.put(List.of("z", "a"), '.');

        assertEquals("[!, *, +, ., ?, @]", t.stream().sorted().collect(Collectors.toList()).toString());
        assertEquals(6, t.size());

        assertEquals("[]=* [z, a]=. [z, a]=! [y]=+ [x]=? [x]=@", t.toString().replace('\n', ' ').trim());

        t.clear();
        assertEquals(0, t.size());
        assertTrue(t.root.isEmpty());
    }
}