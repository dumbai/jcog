package jcog.data.list;

import com.google.common.base.Joiner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetalConcurrentQueueTest {

	@Test void testQueuing() {
		MetalRing<Integer> i = new MetalConcurrentQueue<>(4);
		i.offer(1); i.offer(2); i.offer(3);
		Joiner j = Joiner.on(',');
		assertEquals("1,2,3", j.join(i.stream().iterator()));
		assertEquals(1, i.peek());
		assertEquals(2, i.getHead(1));
		assertEquals(1, i.poll());
		assertEquals("2,3", j.join(i.stream().iterator()));
		assertTrue(i.offer(1));
		assertEquals("2,3,1", j.join(i.stream().iterator()));
		assertEquals(2, i.poll());
		assertEquals(3, i.poll());
		assertTrue(i.offer(2));
		assertTrue(i.offer(2));
		assertEquals("1,2,2", j.join(i.stream().iterator()));
		assertEquals(1, i.poll());
		assertTrue(i.offer(2));
		assertTrue(i.offer(2));
		assertEquals("2,2,2,2", j.join(i.stream().iterator()));
		assertEquals(4, i.size());
		assertFalse(i.offer(2));
		for (int n = 0; n < 4; n++)
			assertEquals(2, i.getHead(n));
		i.clear();
		assertEquals("", j.join(i.stream().iterator()));

	}
}