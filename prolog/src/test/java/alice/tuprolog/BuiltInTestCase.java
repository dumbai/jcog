package alice.tuprolog;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuiltInTestCase {
	
	@Test
	void testConvertTermToGoal() throws InvalidTermException {
		Term t = new Var("T");
		Struct result = new Struct("call", t);
		assertEquals(result, BuiltIn.convertTermToGoal(t));
		assertEquals(result, BuiltIn.convertTermToGoal(new Struct("call", t)));
		
		t = new NumberTerm.Int(2);
		assertNull(BuiltIn.convertTermToGoal(t));
		
		t = new Struct("p", new Struct("a"), new Var("B"), new Struct("c"));
		result = (Struct) t;
		assertEquals(result, BuiltIn.convertTermToGoal(t));
		
		Var linked = new Var("X");
		linked.setLink(new Struct("!"));
		Term[] arguments = { linked, new Var("Y") };
		Term[] results = { new Struct("!"), new Struct("call", new Var("Y")) };
		assertEquals(new Struct(";", results), BuiltIn.convertTermToGoal(new Struct(";", arguments)));
		assertEquals(new Struct(",", results), BuiltIn.convertTermToGoal(new Struct(",", arguments)));
		assertEquals(new Struct("->", results), BuiltIn.convertTermToGoal(new Struct("->", arguments)));
	}
	
	
	@Test
	void testGroupingConjunctions() throws InvalidTheoryException, MalformedGoalException {
		Prolog engine = new Prolog();
		engine.setTheory(new Theory("g1. g2."));
		Solution info = engine.solve("(g1, g2), (g3, g4).");
		assertFalse(info.isSuccess());
		engine.setTheory(new Theory("g1. g2. g3. g4."));
		info = engine.solve("(g1, g2), (g3, g4).");
		assertTrue(info.isSuccess());
	}

}
