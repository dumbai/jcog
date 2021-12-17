package alice.tuprolog;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class VarTestCase {
	
	@Test
    void testIsAtomic() {
		assertFalse(new Var("X").isAtom());
	}
	
	@Test
    void testIsAtom() {
		assertFalse(new Var("X").isAtomic());
	}
	
	@Test
    void testIsCompound() {
		assertFalse(new Var("X").isCompound());
	}

}
