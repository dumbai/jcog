package alice.tuprolog;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrologLibTestCase {
	
	@Test
	void testLibraryFunctor() throws InvalidLibraryException, MalformedGoalException, NoSolutionException {
		Prolog engine = new Prolog();
		engine.addLibrary(new TestLibrary());
		Solution goal = engine.solve("N is sum(1, 3).");
		assertTrue(goal.isSuccess());
		Term n = goal.getVarValue("N");
		if (n == null) {
			goal = engine.solve("N is sum(1, 3)."); //TEMPORARY
		}
		assertEquals(new NumberTerm.Int(4), n);
	}
	
	@Test
	void testLibraryPredicate() throws InvalidLibraryException, MalformedGoalException {
		Prolog engine = new Prolog();
		engine.addLibrary(new TestLibrary());
		TestOutputListener l = new TestOutputListener();
		engine.addOutputListener(l);
		engine.solve("println(sum(5)).");
		assertEquals("sum(5)", l.output);
	}

}