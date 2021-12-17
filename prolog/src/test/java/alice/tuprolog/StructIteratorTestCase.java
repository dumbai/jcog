package alice.tuprolog;


import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 
 * @author <a href="mailto:giulio.piancastelli@unibo.it">Giulio Piancastelli</a>
 */
class StructIteratorTestCase {
	
	@Test
    void testEmptyIterator() {
		Struct list = Struct.emptyList();
		Iterator<? extends Term> i = list.listIterator();
		assertFalse(i.hasNext());
		try {
			i.next();
			fail("");
		} catch (NoSuchElementException expected) {}
	}
	
	@Test
    void testIteratorCount() {
		Struct list = new Struct(new Term[] {new NumberTerm.Int(1), new NumberTerm.Int(2), new NumberTerm.Int(3), new NumberTerm.Int(5), new NumberTerm.Int(7)});
		Iterator<? extends Term> i = list.listIterator();
		int count = 0;
		for (; i.hasNext(); count++)
			i.next();
		assertEquals(5, count);
		assertFalse(i.hasNext());
	}
	
	@Test
    void testMultipleHasNext() {
		Struct list = new Struct(new Term[] {new Struct("p"), new Struct("q"), new Struct("r")});
		Iterator<? extends Term> i = list.listIterator();
		assertTrue(i.hasNext());
		assertTrue(i.hasNext());
		assertTrue(i.hasNext());
		assertEquals(new Struct("p"), i.next());
	}
	
	@Test
    void testMultipleNext() {
		Struct list = new Struct(new Term[] {new NumberTerm.Int(0), new NumberTerm.Int(1), new NumberTerm.Int(2), new NumberTerm.Int(3), new NumberTerm.Int(5), new NumberTerm.Int(7)});
		Iterator<? extends Term> i = list.listIterator();
		assertTrue(i.hasNext());
		i.next(); 
		assertEquals(new NumberTerm.Int(1), i.next());
		assertEquals(new NumberTerm.Int(2), i.next());
		assertEquals(new NumberTerm.Int(3), i.next());
		assertEquals(new NumberTerm.Int(5), i.next());
		assertEquals(new NumberTerm.Int(7), i.next());
		
		assertFalse(i.hasNext());
		try {
			i.next();
			fail("");
		} catch (NoSuchElementException expected) {}
	}
	
	@Test
    void testRemoveOperationNotSupported() {
		Struct list = new Struct(new NumberTerm.Int(1), Struct.emptyList());
		Iterator<? extends Term> i = list.listIterator();
		assertNotNull(i.next());
		try {
			i.remove();
			fail("");
		} catch (UnsupportedOperationException expected) {}
	}

}
