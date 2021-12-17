package alice.tuprolog;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoubleTestCase {
	
	@Test
    void testIsAtomic() {
		assertTrue(new NumberTerm.Double(0).isAtom());
	}
	
	@Test
    void testIsAtom() {
		assertFalse(new NumberTerm.Double(0).isAtomic());
	}
	
	@Test
    void testIsCompound() {
		assertFalse(new NumberTerm.Double(0).isCompound());
	}
	
	@Test
    void testEqualsToStruct() {
		NumberTerm.Double zero = new NumberTerm.Double(0);
		Struct s = Struct.emptyList();
        assertNotEquals(zero, s);
	}
	
	@Test
    void testEqualsToVar() throws InvalidTermException {
		NumberTerm.Double one = new NumberTerm.Double(1);
		Var x = new Var("X");
        assertNotEquals(one, x);
	}
	
	@Test
    void testEqualsToDouble() {
		NumberTerm.Double zero = new NumberTerm.Double(0);
		NumberTerm.Double one = new NumberTerm.Double(1);
        assertNotEquals(zero, one);
		NumberTerm.Double anotherZero = new NumberTerm.Double(0.0);
        assertEquals(anotherZero, zero);
	}
	
	@Test
    void testEqualsToFloat() {
		
	}
	
	@Test
    void testEqualsToInt() {
		NumberTerm.Double doubleOne = new NumberTerm.Double(1.0);
		NumberTerm.Int integerOne = new NumberTerm.Int(1);
        assertNotEquals(doubleOne, integerOne);
	}
	
	@Test
    void testEqualsToLong() {
		
	}

}
