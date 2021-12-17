package alice.tuprolog;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Matteo Iuliani
 * 
 *         Test del funzionamento delle eccezioni lanciate dai predicati della ISOLibrary
 */
@Disabled
class ISOPrologLibExceptionsTestCase {

	
	@Test
	void test_atom_length_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(atom_length(X, Y), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("atom_length", new Var("X"), new Var("Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_atom_length_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(atom_length(1, Y), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("atom_length", new NumberTerm.Int(1), new Var("Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}
	
	
	@Test
	void test_atom_chars_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(atom_chars(1, X), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("atom_chars", new NumberTerm.Int(1), new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}
	
	
	@Test
	void test_atom_chars_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(atom_chars(X, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("atom_chars", new Var("X"), new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}
	
	
	@Test
	void test_char_code_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(char_code(ab, X), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("char_code", new Struct("ab"), new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("character")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("ab")));
	}
	
	
	@Test
	void test_char_code_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(char_code(X, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("char_code", new Var("X"), new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("integer")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}
	
	
	@Test
	void test_sub_atom_5_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(sub_atom(1, B, C, D, E), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("sub_atom_guard", new NumberTerm.Int(1), new Var("B"),  new Var("C"),  new Var("D"),  new Var("E"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

}