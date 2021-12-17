package jcog.data.set;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetalTreeSetTest {

	@Test void testMetalTreeSet() {
		assertSet(new MetalTreeSet());
	}
//	@Test void testSortedList() {
//		assertSet(new InsertionSortedList(8));
//	}

	private static void assertSet(Collection<Integer> s) {
		assertTrue(s.isEmpty());
		assertEquals(0, s.size());

		assertEquals("[]", Arrays.toString(s.toArray(new Integer[0])));
		assertEquals("[]", Arrays.toString(Iterables.toArray(s, Integer.class)));

		s.add(1);
		assertEquals(1, s.size());

		assertEquals("[1]", Arrays.toString(Iterables.toArray(s, Integer.class)));

		s.add(2);
		assertEquals("[1, 2]", Arrays.toString(Iterables.toArray(s, Integer.class)));
		s.add(1);
		assertEquals(2, s.size()); //unchanged
		s.add(-1);
		assertEquals("[-1, 1, 2]", Arrays.toString(Iterables.toArray(s, Integer.class)));
		assertEquals(3, s.size());
		s.add(-1);
		assertEquals(3, s.size());
		s.remove(-1);
		assertEquals(2, s.size());
		assertEquals("[1, 2]", Arrays.toString(Iterables.toArray(s, Integer.class)));
		s.add(3);
		assertEquals("[1, 2, 3]", Arrays.toString(s.toArray(new Integer[0])));
		s.add(3);
		assertEquals("[1, 2, 3]", Arrays.toString(Iterables.toArray(s, Integer.class)));
		s.add(-1);
		s.add(3);
		assertEquals("[-1, 1, 2, 3]", Arrays.toString(Iterables.toArray(s, Integer.class)));
		s.clear();
		assertTrue(s.isEmpty());
	}
}