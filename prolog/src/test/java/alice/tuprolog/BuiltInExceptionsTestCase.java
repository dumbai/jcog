package alice.tuprolog;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Matteo Iuliani
 * 
 *         Test del funzionamento delle eccezioni lanciate dai predicati della
 *         classe BuiltIn
 */
@Disabled class BuiltInExceptionsTestCase {

	
	@Test
	void test_asserta_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(asserta(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("asserta", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_asserta_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(asserta(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("asserta", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("clause")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
		assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_assertz_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(assertz(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("assertz", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_assertz_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(assertz(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("assertz", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("clause")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
		assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_$retract_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$retract'(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$retract", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_$retract_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$retract'(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$retract", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("clause")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
		assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_abolish_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(abolish(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("abolish", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_abolish_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(abolish(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("abolish", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("predicate_indicator")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
		assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_abolish_1_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(abolish(p(X)), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("abolish",
				new Struct("p", new Var("X")))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("predicate_indicator")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("p", new Var("X"))));
	}

	
	@Test
	void test_halt_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(halt(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("halt", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_halt_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(halt(1.5), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("halt", new NumberTerm.Double(1.5))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("integer")));
		NumberTerm.Double culprit = (NumberTerm.Double) info.getTerm("Culprit");
		assertEquals(1.5, culprit.doubleValue(), 0.01);
	}

	
	@Test
	void test_load_library_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(load_library(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("load_library", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_load_library_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(load_library(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("load_library", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
		assertEquals(1, culprit.intValue());
	}

	
	
	@Test
	void test_load_library_1_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(load_library('a'), error(existence_error(ObjectType, Culprit), existence_error(Goal, ArgNo, ObjectType, Culprit, Message)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("load_library", new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ObjectType");
		assertTrue(validType.isEqual(new Struct("class")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
		Term message = info.getTerm("Message");
		assertTrue(message.isEqual(new Struct("InvalidLibraryException: a at -1:-1")));
	}

	
	@Test
	void test_unload_library_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(unload_library(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("unload_library", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_unload_library_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(unload_library(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("unload_library", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
		assertEquals(1, culprit.intValue());
	}

	
	
	@Test
	void test_unload_library_1_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(unload_library('a'), error(existence_error(ObjectType, Culprit), existence_error(Goal, ArgNo, ObjectType, Culprit, Message)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("unload_library", new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ObjectType");
		assertTrue(validType.isEqual(new Struct("class")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
		Term message = info.getTerm("Message");
		assertTrue(message.isEqual(new Struct("InvalidLibraryException: null at 0:0")));
	}

	
	@Test
	void test_$call_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$call'(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$call", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_$call_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$call'(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$call", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("callable")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
		assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_is_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(is(X, Y), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("is", new Var("X"), new Var("Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_is_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(is(X, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("is", new Var("X"), new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}
	
	
	@Test
	void test_is_2_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(is(X, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("is", new Var("X"), new Struct("/", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
		Struct error = (Struct) info.getTerm("Error");
		assertTrue(error.isEqual(new Struct("zero_divisor")));
	}
	
	
	@Test
	void test_is_2_4() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(is(X, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("is", new Var("X"), new Struct("//", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
		Struct error = (Struct) info.getTerm("Error");
		assertTrue(error.isEqual(new Struct("zero_divisor")));
	}
	
	
	@Test
	void test_is_2_5() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(is(X, 1 div 0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("is", new Var("X"), new Struct("div", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
		Struct error = (Struct) info.getTerm("Error");
		assertTrue(error.isEqual(new Struct("zero_divisor")));
	}
	
	
	@Test
	void test_$tolist_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$tolist'(X, List), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$tolist", new Var("X"),
				new Var("List"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_$tolist_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$tolist'(1, List), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g
				.isEqual(new Struct("$tolist", new NumberTerm.Int(1), new Var("List"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("struct")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
		assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_$fromlist_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$fromlist'(Struct, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$fromlist", new Var("Struct"),
				new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_$fromlist_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$fromlist'(Struct, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$fromlist", new Var("Struct"),
				new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_$append_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$append'(a, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$append", new Struct("a"),
				new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_$append_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$append'(a, b), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$append", new Struct("a"), new Struct(
				"b"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("b")));
	}

	
	@Test
	void test_$find_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$find'(X, []), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$find", new Var("X"), Struct.emptyList())));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_$find_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$find'(p(X), a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$find", new Struct("p", new Var("X")),
				new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_set_prolog_flag_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(set_prolog_flag(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_prolog_flag", new Var("X"),
				new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_set_prolog_flag_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(set_prolog_flag(a, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_prolog_flag", new Struct("a"),
				new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_set_prolog_flag_2_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(set_prolog_flag(1, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_prolog_flag", new NumberTerm.Int(1), new NumberTerm.Int(
				1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("struct")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
		assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_set_prolog_flag_2_4() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(set_prolog_flag(a, p(X)), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_prolog_flag", new Struct("a"),
				new Struct("p", new Var("X")))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("ground")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("p", new Var("X"))));
	}

	
	
	@Test
	void test_set_prolog_flag_2_5() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(set_prolog_flag(a, 1), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_prolog_flag", new Struct("a"),
				new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validDomain = (Struct) info.getTerm("ValidDomain");
		assertTrue(validDomain.isEqual(new Struct("prolog_flag")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_set_prolog_flag_2_6() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(set_prolog_flag(bounded, a), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_prolog_flag",
				new Struct("bounded"), new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
		Struct validDomain = (Struct) info.getTerm("ValidDomain");
		assertTrue(validDomain.isEqual(new Struct("flag_value")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	
	@Disabled
	@Test
	void test_set_prolog_flag_2_7() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(set_prolog_flag(bounded, false), error(permission_error(Operation, ObjectType, Culprit), permission_error(Goal, Operation, ObjectType, Culprit, Message)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_prolog_flag",
				new Struct("bounded"), new Struct("false"))));
		Struct operation = (Struct) info.getTerm("Operation");
		assertTrue(operation.isEqual(new Struct("modify")));
		Struct objectType = (Struct) info.getTerm("ObjectType");
		assertTrue(objectType.isEqual(new Struct("flag")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("bounded")));
		Term message = info.getTerm("Message");
		assertTrue(message.isEqual(new NumberTerm.Int(0)));
	}

	
	@Test
	void test_get_prolog_flag_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(get_prolog_flag(X, Value), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("get_prolog_flag", new Var("X"),
				new Var("Value"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_get_prolog_flag_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(get_prolog_flag(1, Value), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("get_prolog_flag", new NumberTerm.Int(1), new Var(
				"Value"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("struct")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
		assertEquals(1, culprit.intValue());
	}

	
	
	@Test
	void test_get_prolog_flag_2_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(get_prolog_flag(a, Value), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("get_prolog_flag", new Struct("a"),
				new Var("Value"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validDomain = (Struct) info.getTerm("ValidDomain");
		assertTrue(validDomain.isEqual(new Struct("prolog_flag")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_$op_3_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(Priority, yfx, '+'), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new Var("Priority"), new Struct(
				"yfx"), new Struct("+"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
	}

	
	
	@Test
	void test_$op_3_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(600, Specifier, '+'), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new NumberTerm.Int(600), new Var(
				"Specifier"), new Struct("+"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_$op_3_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(600, yfx, Operator), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new NumberTerm.Int(600), new Struct("yfx"),
				new Var("Operator"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(3, argNo.intValue());
	}

	
	@Test
	void test_$op_3_4() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(a, yfx, '+'), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new Struct("a"), new Struct(
				"yfx"), new Struct("+"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("integer")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_$op_3_5() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(600, 1, '+'), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new NumberTerm.Int(600), new NumberTerm.Int(1),
				new Struct("+"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
		assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_$op_3_6() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(600, yfx, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new NumberTerm.Int(600), new Struct("yfx"),
				new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(3, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom_or_atom_list")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
		assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_$op_3_7() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(1300, yfx, '+'), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new NumberTerm.Int(1300),
				new Struct("yfx"), new Struct("+"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(1, argNo.intValue());
		Struct validDomain = (Struct) info.getTerm("ValidDomain");
		assertTrue(validDomain.isEqual(new Struct("operator_priority")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
		assertEquals(1300, culprit.intValue());
	}

	
	@Test
	void test_$op_3_8() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(600, a, '+'), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new NumberTerm.Int(600), new Struct("a"),
				new Struct("+"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
		assertEquals(2, argNo.intValue());
		Struct validDomain = (Struct) info.getTerm("ValidDomain");
		assertTrue(validDomain.isEqual(new Struct("operator_specifier")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

}