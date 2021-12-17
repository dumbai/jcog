package alice.tuprolog;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StructTestCase {
	
	@Test
	void testStructWithNullArgument() {
		try {
			new Struct("p", (Term) null);
			fail("");
		} catch (InvalidTermException expected) {}
		try {
			new Struct("p", new NumberTerm.Int(1), null);
			fail("");
		} catch (InvalidTermException expected) {}
		try {
			new Struct("p", new NumberTerm.Int(1), new NumberTerm.Int(2), null);
			fail("");
		} catch (InvalidTermException expected) {}
		try {
			new Struct("p", new NumberTerm.Int(1), new NumberTerm.Int(2), new NumberTerm.Int(3), null);
			fail("");
		} catch (InvalidTermException expected) {}
		try {
			new Struct("p", new NumberTerm.Int(1), new NumberTerm.Int(2), new NumberTerm.Int(3), new NumberTerm.Int(4), null);
			fail("");
		} catch (InvalidTermException expected) {}
		try {
			new Struct("p", new NumberTerm.Int(1), new NumberTerm.Int(2), new NumberTerm.Int(3), new NumberTerm.Int(4), new NumberTerm.Int(5), null);
			fail("");
		} catch (InvalidTermException expected) {}
		try {
			new Struct("p", new NumberTerm.Int(1), new NumberTerm.Int(2), new NumberTerm.Int(3), new NumberTerm.Int(4), new NumberTerm.Int(5), new NumberTerm.Int(6), null);
			fail("");
		} catch (InvalidTermException expected) {}
		try {
			Term[] args = {new Struct("a"), null, new Var("P")};
			new Struct("p", args);
			fail("");
		} catch (InvalidTermException expected) {}
	}
	
	@Test
	void testStructWithNullName() {
		try {
			new Struct(null, new NumberTerm.Int(1), new NumberTerm.Int(2));
			fail("");
		} catch (InvalidTermException expected) {}
	}
	
	/** Structs with an empty name can only be atoms. */
	@Test
	void testStructWithEmptyName() {
		try {
			new Struct("", new NumberTerm.Int(1), new NumberTerm.Int(2));
			fail("");
		} catch (InvalidTermException expected) {}
		assertEquals(0, new Struct("").name().length());
	}
	
	@Test
	void testEmptyList() {
		Struct list = Struct.emptyList();
		assertTrue(list.isList());
		assertTrue(list.isEmptyList());
		assertEquals(0, list.listSize());
		assertEquals("[]", list.name());
		assertEquals(0, list.subs());
	}

	/** Another correct method of building an empty list */
	@Test
	void testEmptyListAsSquaredStruct() {
		Struct emptyList = new Struct("[]");
		assertTrue(emptyList.isList());
		assertTrue(emptyList.isEmptyList());
		assertEquals("[]", emptyList.name());
		assertEquals(0, emptyList.subs());
		assertEquals(0, emptyList.listSize());
	}
	
	/** A wrong method of building an empty list */
	@Test
	void testEmptyListAsDottedStruct() {
		Struct notAnEmptyList = new Struct(".");
		assertFalse(notAnEmptyList.isList());
		assertFalse(notAnEmptyList.isEmptyList());
		assertEquals(".", notAnEmptyList.name());
		assertEquals(0, notAnEmptyList.subs());
	}
	
	/** Use dotted structs to builder lists with content */
	@Test
	void testListAsDottedStruct() {
		Struct notAnEmptyList = new Struct(".", new Struct("a"), new Struct(".", new Struct("b"), Struct.emptyList()));
		assertTrue(notAnEmptyList.isList());
		assertFalse(notAnEmptyList.isEmptyList());
		assertEquals(".", notAnEmptyList.name());
		assertEquals(2, notAnEmptyList.subs());
	}
	
	@Test
	void testListFromArgumentArray() {
		assertEquals(Struct.emptyList(), new Struct(Term.EmptyTermArray));
		
		Term[] args = new Term[2];
		args[0] = new Struct("a");
		args[1] = new Struct("b");
		Struct list = new Struct(args);
		assertEquals(Struct.emptyList(), list.listTail().listTail());
	}
	
	@Test
	void testListSize() {
		Struct list = new Struct(new Struct("a"),
				       new Struct(new Struct("b"),
				           new Struct(new Struct("c"), Struct.emptyList())));
		assertTrue(list.isList());
		assertFalse(list.isEmptyList());
		assertEquals(3, list.listSize());
	}
	
	@Test
	void testNonListHead() throws InvalidTermException {
		Struct s = new Struct("f", new Var("X"));
		try {
			assertNotNull(s.listHead()); 
			fail("");
		} catch (UnsupportedOperationException e) {
			assertEquals("The structure " + s + " is not a list.", e.getMessage());
		}
	}
	
	@Test
	void testNonListTail() {
		Struct s = new Struct("h", new NumberTerm.Int(1));
		try {
			assertNotNull(s.listTail()); 
			fail("");
		} catch (UnsupportedOperationException e) {
			assertEquals("The structure " + s + " is not a list.", e.getMessage());
		}
	}
	
	@Test
	void testNonListSize() throws InvalidTermException {
		Struct s = new Struct("f", new Var("X"));
		try {
			assertEquals(0, s.listSize()); 
			fail("");
		} catch (UnsupportedOperationException e) {
			assertEquals("The structure " + s + " is not a list.", e.getMessage());
		}
	}
	
	@Test
	void testNonListIterator() {
		Struct s = new Struct("f", new NumberTerm.Int(2));
		try {
			assertNotNull(s.listIterator()); 
			fail("");
		} catch (UnsupportedOperationException e) {
			assertEquals("The structure " + s + " is not a list.", e.getMessage());
		}
	}
	
	@Test
	void testToList() {
		Struct emptyList = Struct.emptyList();
		Struct emptyListToList = new Struct(new Struct("[]"), Struct.emptyList());
		assertEquals(emptyListToList, emptyList.toList());
	}
	
	@Test
	void testToString() throws InvalidTermException {
		Struct emptyList = Struct.emptyList();
		assertEquals("[]", emptyList.toString());
		Struct s = new Struct("f", new Var("X"));
		assertEquals("f(X)", s.toString());
		Struct list = new Struct(new Struct("a"),
		          new Struct(new Struct("b"),
		        	  new Struct(new Struct("c"), Struct.emptyList())));
		assertEquals("[a,b,c]", list.toString());
	}
	
	@Test
	void testAppend() {
		Struct emptyList = Struct.emptyListMutable();
		Struct list = new Struct(new Struct("a"),
				          new Struct(new Struct("b"),
				        	  new Struct(new Struct("c"), Struct.emptyListMutable())));
		emptyList.append(new Struct("a"));
		emptyList.append(new Struct("b"));
		emptyList.append(new Struct("c"));
		assertEquals(list, emptyList);
		Struct tail = new Struct(new Struct("b"),
                          new Struct(new Struct("c"), Struct.emptyList()));
		assertEquals(tail, emptyList.listTail());

		emptyList = Struct.emptyListMutable();
		emptyList.append(Struct.emptyListMutable());
		assertEquals(new Struct(Struct.emptyList(), Struct.emptyList()), emptyList);

		Struct anotherList = new Struct(new Struct("d"),
				                 new Struct(new Struct("e"), Struct.emptyListMutable()));
		list.append(anotherList);
		assertEquals(anotherList, list.listTail().listTail().listTail().listHead());
	}
	
	@Test
	void testIteratedGoalTerm() {
		Var x = new Var("X");
		Struct foo = new Struct("foo", x);
		Struct term = new Struct("^", x, foo);
		assertEquals(foo, term.iteratedGoalTerm());
	}
	
	@Test
	void testIsList() {
		Struct notList = new Struct(".", new Struct("a"), new Struct("b"));
		assertFalse(notList.isList());
	}
	
	@Test
	void testIsAtomic() {
		Struct emptyList = Struct.emptyList();
		assertTrue(emptyList.isAtom());
		Struct atom = new Struct("atom");
		assertTrue(atom.isAtom());
		Struct list = new Struct(new Term[] {new NumberTerm.Int(0), new NumberTerm.Int(1)});
		assertFalse(list.isAtom());
		Struct compound = new Struct("f", new Struct("a"), new Struct("b"));
		assertFalse(compound.isAtom());
		Struct singleQuoted = new Struct("'atom'");
		assertTrue(singleQuoted.isAtom());
		Struct doubleQuoted = new Struct("\"atom\"");
		assertTrue(doubleQuoted.isAtom());
	}
	
	@Test
	void testIsAtom() {
		Struct emptyList = Struct.emptyList();
		assertTrue(emptyList.isAtomic());
		Struct atom = new Struct("atom");
		assertTrue(atom.isAtomic());
		Struct list = new Struct(new Term[] {new NumberTerm.Int(0), new NumberTerm.Int(1)});
		assertFalse(list.isAtomic());
		Struct compound = new Struct("f", new Struct("a"), new Struct("b"));
		assertFalse(compound.isAtomic());
		Struct singleQuoted = new Struct("'atom'");
		assertTrue(singleQuoted.isAtomic());
		Struct doubleQuoted = new Struct("\"atom\"");
		assertTrue(doubleQuoted.isAtomic());
	}
	
	@Test
	void testIsCompound() {
		Struct emptyList = Struct.emptyList();
		assertFalse(emptyList.isCompound());
		Struct atom = new Struct("atom");
		assertFalse(atom.isCompound());
		Struct list = new Struct(new Term[] {new NumberTerm.Int(0), new NumberTerm.Int(1)});
		assertTrue(list.isCompound());
		Struct compound = new Struct("f", new Struct("a"), new Struct("b"));
		assertTrue(compound.isCompound());
		Struct singleQuoted = new Struct("'atom'");
		assertFalse(singleQuoted.isCompound());
		Struct doubleQuoted = new Struct("\"atom\"");
		assertFalse(doubleQuoted.isCompound());
	}
	
	@Test
	void testEqualsToObject() {
		Struct s = new Struct("id");
        assertNotEquals(s, new Object());
	}

}
