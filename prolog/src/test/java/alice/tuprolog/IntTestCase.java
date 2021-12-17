package alice.tuprolog;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntTestCase {
	
	@Test
    void testIsAtomic() {
		assertTrue(new NumberTerm.Int(0).isAtom());
	}
	
	@Test
    void testIsAtom() {
		assertFalse(new NumberTerm.Int(0).isAtomic());
	}
	
	@Test
    void testIsCompound() {
		assertFalse(new NumberTerm.Int(0).isCompound());
	}
	
	@Test
    void testEqualsToStruct() {
		Struct s = Struct.emptyList();
		NumberTerm.Int zero = new NumberTerm.Int(0);
        assertNotEquals(zero, s);
	}
	
	@Test
    void testEqualsToVar() throws InvalidTermException {
		Var x = new Var("X");
		NumberTerm.Int one = new NumberTerm.Int(1);
        assertNotEquals(one, x);
	}
	
	@Test
    void testEqualsToInt() {
		NumberTerm.Int zero = new NumberTerm.Int(0);
		NumberTerm.Int one = new NumberTerm.Int(1);
        assertNotEquals(zero, one);
		NumberTerm.Int anotherZero = new NumberTerm.Int(1-1);
		assertEquals(anotherZero, zero);
	}
	
	@Test
    void testEqualsToLong() {
		
	}
	
	@Test
    void testEqualsToDouble() {
		NumberTerm.Int integerOne = new NumberTerm.Int(1);
		NumberTerm.Double doubleOne = new NumberTerm.Double(1);
        assertNotEquals(integerOne, doubleOne);
	}
	
	@Test
    void testEqualsToFloat() {
		
	}

}
