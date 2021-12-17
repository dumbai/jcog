package jcog.search;

import jcog.search.impl.HashPriorityQueue;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;


class DatastructuresTest {

	@Test
    void hashPriorityQueueTest() {

		
		
		class InconsistentComparator implements Comparator<Integer> {
			public int compare(Integer x, Integer y) {
				return 0;
			}
		}
		HashPriorityQueue<Integer, Integer> Q = new HashPriorityQueue<>(
				new InconsistentComparator());

		Q.add(0, 0);
		Q.add(1, 1);
		Q.add(2, 2);
		Q.add(3, 3);

		
		
		Q.remove(1, 1);
        assertTrue(Q.contains(0));
        assertFalse(Q.contains(1));
        assertTrue(Q.contains(2));
        assertTrue(Q.contains(3));
		Q.remove(0, 0);
        assertFalse(Q.contains(0));
        assertTrue(Q.contains(2));
        assertTrue(Q.contains(3));
		Q.remove(3, 3);
        assertTrue(Q.contains(2));
        assertFalse(Q.contains(3));

		Q.clear();
		Q.add(0, 0);
		Q.add(1, 1);
		Q.add(2, 2);
		Q.add(3, 3);

		int x = Q.poll();
		
		
		assertEquals(0, Q.size());

		
		
		
		
		
		
		

	}

}
