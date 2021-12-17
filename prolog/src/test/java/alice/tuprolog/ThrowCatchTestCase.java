package alice.tuprolog;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Matteo Iuliani
 * 
 *         Test del funzionamento dei predicati throw/1 e catch/3
 */
@Disabled
class ThrowCatchTestCase {

	
	
	@Test
	void test_catch_3_1() throws InvalidTheoryException, MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String theory = "p(0) :- p(1). p(1) :- throw(error).";
		engine.setTheory(new Theory(theory));
		String goal = "atom_length(err, 3), catch(p(0), E, (atom_length(E, Length), X is 2+3)), Y is X+5.";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct e = (Struct) info.getTerm("E");
		assertTrue(e.isEqual(new Struct("error")));
		NumberTerm.Int length = (NumberTerm.Int) info.getTerm("Length");
        assertEquals(5, length.intValue());
		NumberTerm.Int x = (NumberTerm.Int) info.getTerm("X");
        assertEquals(5, x.intValue());
		NumberTerm.Int y = (NumberTerm.Int) info.getTerm("Y");
        assertEquals(10, y.intValue());
	}

	
	
	@Test
	void test_catch_3_2() throws InvalidTheoryException, MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String theory = "p(0) :- throw(error). p(1).";
		engine.setTheory(new Theory(theory));
		String goal = "catch(p(1), E, fail), catch(p(0), E, atom_length(E, Length)).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct e = (Struct) info.getTerm("E");
		assertTrue(e.isEqual(new Struct("error")));
		NumberTerm.Int length = (NumberTerm.Int) info.getTerm("Length");
        assertEquals(5, length.intValue());
	}

	
	
	
	@Test
	void test_catch_3_3() throws InvalidTheoryException, MalformedGoalException {
		Prolog engine = new Prolog();
		String theory = "p(0) :- throw(error).";
		engine.setTheory(new Theory(theory));
		String goal = "catch(p(0), error(X), true).";
		Solution info = engine.solve(goal);
		assertFalse(info.isSuccess());
		assertTrue(info.isHalted());
	}

	
	@Test
	void test_catch_3_4() throws InvalidTheoryException, MalformedGoalException {
		Prolog engine = new Prolog();
		String theory = "p(0) :- throw(error).";
		engine.setTheory(new Theory(theory));
		String goal = "catch(p(0), E, E == err).";
		Solution info = engine.solve(goal);
		assertFalse(info.isSuccess());
	}

	
	
	
	
	@Test
	void test_catch_3_5() throws InvalidTheoryException, MalformedGoalException, NoMoreSolutionException {
		Prolog engine = new Prolog();
		String theory = "p(0). p(1) :- throw(error). p(2).";
		engine.setTheory(new Theory(theory));
		String goal = "catch(p(X), E, E == error).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		assertTrue(info.hasOpenAlternatives());
		info = engine.solveNext();
		assertTrue(info.isSuccess());
		assertFalse(info.hasOpenAlternatives());
	}

	
	@Test
	void test_catch_3_6() throws InvalidTheoryException, MalformedGoalException {
		Prolog engine = new Prolog();
		String theory = "p(0) :- throw(error).";
		engine.setTheory(new Theory(theory));
		String goal = "catch(p(0), E, throw(err)).";
		Solution info = engine.solve(goal);
		assertFalse(info.isSuccess());
		assertTrue(info.isHalted());
	}

}