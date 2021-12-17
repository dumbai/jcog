package alice.tuprolog;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Matteo Iuliani
 * 
 *         Test del funzionamento delle eccezioni lanciate dai predicati della
 *         BasicLibrary
 */
@Disabled
class BasicPrologLibExceptionsTestCase {

	
	@Test
	void test_set_theory_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(set_theory(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_theory", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_set_theory_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(set_theory(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_theory", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_set_theory_1_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(set_theory(a), error(syntax_error(Message), syntax_error(Goal, Line, Position, Message)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_theory", new Struct("a"))));
		NumberTerm.Int line = (NumberTerm.Int) info.getTerm("Line");
        assertEquals(1, line.intValue());
		NumberTerm.Int position = (NumberTerm.Int) info.getTerm("Line");
        assertEquals(1, position.intValue());
		Struct message = (Struct) info.getTerm("Message");
		assertTrue(message.isEqual(new Struct("The term 'a' is not ended with a period.")));
	}

	
	@Test
	void test_add_theory_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(add_theory(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("add_theory", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_add_theory_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(add_theory(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("add_theory", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_add_theory_1_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(add_theory(a), error(syntax_error(Message), syntax_error(Goal, Line, Position, Message)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("add_theory", new Struct("a"))));
		NumberTerm.Int line = (NumberTerm.Int) info.getTerm("Line");
        assertEquals(1, line.intValue());
		NumberTerm.Int position = (NumberTerm.Int) info.getTerm("Line");
        assertEquals(1, position.intValue());
		Struct message = (Struct) info.getTerm("Message");
		assertTrue(message.isEqual(new Struct("The term 'a' is not ended with a period.")));
	}

	
//	@Test public void test_agent_1_1() throws Exception {
//		Prolog engine = new Prolog();
//		String goal = "catch(agent(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
//		Solution info = engine.solve(goal);
//		assertTrue(info.isSuccess());
//		Struct g = (Struct) info.getTerm("Goal");
//		assertTrue(g.isEqual(new Struct("agent", new Var("X"))));
//		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
//        assertEquals(1, argNo.intValue());
//	}

	
//	@Test public void test_agent_1_2() throws Exception {
//		Prolog engine = new Prolog();
//		String goal = "catch(agent(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
//		Solution info = engine.solve(goal);
//		assertTrue(info.isSuccess());
//		Struct g = (Struct) info.getTerm("Goal");
//		assertTrue(g.isEqual(new Struct("agent", new NumberTerm.Int(1))));
//		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
//        assertEquals(1, argNo.intValue());
//		Struct validType = (Struct) info.getTerm("ValidType");
//		assertTrue(validType.isEqual(new Struct("atom")));
//		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
//        assertEquals(1, culprit.intValue());
//	}

	
	@Test
	void test_agent_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(agent(X, a), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("agent", new Var("X"), new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_agent_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(agent(a, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g
				.isEqual(new Struct("agent", new Struct("a"), new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_agent_2_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(agent(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("agent", new NumberTerm.Int(1), new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_agent_2_4() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(agent(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("agent", new Struct("a"), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("struct")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_expression_comparison_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=:='(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Var("X"),
				new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_expression_comparison_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=:='(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new NumberTerm.Int(1),
				new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_expression_comparison_2_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=:='(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Struct("a"),
				new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_expression_comparison_2_4() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=:='(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new NumberTerm.Int(1),
				new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_expression_comparison_2_5() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=\\='(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Var("X"),
				new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_expression_comparison_2_6() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=\\='(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new NumberTerm.Int(1),
				new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_expression_comparison_2_7() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=\\='(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Struct("a"),
				new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_expression_comparison_2_8() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=\\='(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new NumberTerm.Int(1),
				new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_expression_comparison_2_9() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('>'(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_than",
				new Var("X"), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_expression_comparison_2_10() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('>'(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_than", new NumberTerm.Int(1),
				new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_expression_comparison_2_11() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('>'(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_than", new Struct(
				"a"), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_expression_comparison_2_12() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('>'(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_than", new NumberTerm.Int(1),
				new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_expression_comparison_2_13() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('<'(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_than", new Var("X"),
				new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_expression_comparison_2_14() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('<'(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_than", new NumberTerm.Int(1),
				new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_expression_comparison_2_15() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('<'(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_than",
				new Struct("a"), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_expression_comparison_2_16() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('<'(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_than", new NumberTerm.Int(1),
				new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_expression_comparison_2_17() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('>='(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_or_equal_than",
				new Var("X"), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_expression_comparison_2_18() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('>='(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_or_equal_than",
				new NumberTerm.Int(1), new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_expression_comparison_2_19() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('>='(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_or_equal_than",
				new Struct("a"), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_expression_comparison_2_20() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('>='(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_or_equal_than",
				new NumberTerm.Int(1), new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_expression_comparison_2_21() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=<'(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_or_equal_than",
				new Var("X"), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_expression_comparison_2_22() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=<'(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_or_equal_than",
				new NumberTerm.Int(1), new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_expression_comparison_2_23() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=<'(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_or_equal_than",
				new Struct("a"), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_expression_comparison_2_24() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=<'(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_or_equal_than",
				new NumberTerm.Int(1), new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_expression_comparison_2_25() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=:='(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new NumberTerm.Int(1),
				new Struct("/", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	@Test
	void test_expression_comparison_2_26() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=\\='(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new NumberTerm.Int(1),
				new Struct("/", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	@Test
	void test_expression_comparison_2_27() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('>'(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_than", new NumberTerm.Int(1),
				new Struct("/", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	@Test
	void test_expression_comparison_2_28() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('<'(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_than", new NumberTerm.Int(1),
				new Struct("/", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	@Test
	void test_expression_comparison_2_29() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('>='(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_or_equal_than",
				new NumberTerm.Int(1), new Struct("/", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	@Test
	void test_expression_comparison_2_30() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=<'(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_or_equal_than",
				new NumberTerm.Int(1), new Struct("/", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	@Test
	void test_expression_comparison_2_31() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=:='(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new NumberTerm.Int(1),
				new Struct("//", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	@Test
	void test_expression_comparison_2_32() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=\\='(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new NumberTerm.Int(1),
				new Struct("//", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	@Test
	void test_expression_comparison_2_33() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('>'(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_than", new NumberTerm.Int(1),
				new Struct("//", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	@Test
	void test_expression_comparison_2_34() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('<'(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_than", new NumberTerm.Int(1),
				new Struct("//", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	@Test
	void test_expression_comparison_2_35() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('>='(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_or_equal_than",
				new NumberTerm.Int(1), new Struct("//", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	@Test
	void test_expression_comparison_2_36() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=<'(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_or_equal_than",
				new NumberTerm.Int(1), new Struct("//", new NumberTerm.Int(1), new NumberTerm.Int(0)))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	
	@Test
	void test_expression_comparison_2_37() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=:='(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Struct(
				"div", new NumberTerm.Int(1), new NumberTerm.Int(0)), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	
	@Test
	void test_expression_comparison_2_38() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=\\='(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Struct(
				"div", new NumberTerm.Int(1), new NumberTerm.Int(0)), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	
	@Test
	void test_expression_comparison_2_39() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('>'(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_than", new Struct(
				"div", new NumberTerm.Int(1), new NumberTerm.Int(0)), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	
	@Test
	void test_expression_comparison_2_40() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('<'(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_than", new Struct(
				"div", new NumberTerm.Int(1), new NumberTerm.Int(0)), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	
	@Test
	void test_expression_comparison_2_41() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('>='(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_or_equal_than",
				new Struct("div", new NumberTerm.Int(1), new NumberTerm.Int(0)), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	
	@Test
	void test_expression_comparison_2_42() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch('=<'(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_or_equal_than",
				new Struct("div", new NumberTerm.Int(1), new NumberTerm.Int(0)), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	
	@Test
	void test_text_concat_3_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(text_concat(X, a, b), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("text_concat", new Var("X"),
				new Struct("a"), new Struct("b"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_text_concat_3_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(text_concat(a, X, b), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("text_concat", new Struct("a"),
				new Var("X"), new Struct("b"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_text_concat_3_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(text_concat(1, a, b), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("text_concat", new NumberTerm.Int(1), new Struct(
				"a"), new Struct("b"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_text_concat_3_4() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(text_concat(a, 1, b), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("text_concat", new Struct("a"),
				new NumberTerm.Int(1), new Struct("b"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_num_atom_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(num_atom(a, X), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("num_atom", new Struct("a"), new Var(
				"X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("number")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_num_atom_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(num_atom(1, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("num_atom", new NumberTerm.Int(1), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_num_atom_2_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(num_atom(1, a), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g
				.isEqual(new Struct("num_atom", new NumberTerm.Int(1), new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validDomain = (Struct) info.getTerm("ValidDomain");
		assertTrue(validDomain.isEqual(new Struct("num_atom")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_arg_3_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(arg(X, p(1), 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("arg_guard", new Var("X"), new Struct(
				"p", new NumberTerm.Int(1)), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_arg_3_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(arg(1, X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("arg_guard", new NumberTerm.Int(1), new Var("X"),
				new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_arg_3_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(arg(a, p(1), 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("arg_guard", new Struct("a"),
				new Struct("p", new NumberTerm.Int(1)), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("integer")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_arg_3_4() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(arg(1, p, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("arg_guard", new NumberTerm.Int(1),
				new Struct("p"), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("compound")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("p")));
	}

	
	@Test
	void test_arg_3_5() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(arg(0, p(0), 1), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("arg_guard", new NumberTerm.Int(0), new Struct(
				"p", new NumberTerm.Int(0)), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidDomain");
		assertTrue(validType.isEqual(new Struct("greater_than_zero")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(0, culprit.intValue());
	}

	
	@Test
	void test_clause_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(clause(X, true), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("clause_guard", new Var("X"),
				new Struct("true"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_call_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(call(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("call_guard", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_call_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(call(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("call_guard", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("callable")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_findall_3_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(findall(a, X, L), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("all_solutions_predicates_guard",
				new Struct("a"), new Var("X"), new Var("L"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_findall_3_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(findall(a, 1, L), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("all_solutions_predicates_guard",
				new Struct("a"), new NumberTerm.Int(1), new Var("L"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("callable")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_setof_3_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(setof(a, X, L), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("all_solutions_predicates_guard",
				new Struct("a"), new Var("X"), new Var("L"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_setof_3_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(setof(a, 1, L), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("all_solutions_predicates_guard",
				new Struct("a"), new NumberTerm.Int(1), new Var("L"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("callable")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_bagof_3_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(bagof(a, X, L), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("all_solutions_predicates_guard",
				new Struct("a"), new Var("X"), new Var("L"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_bagof_3_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(bagof(a, 1, L), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("all_solutions_predicates_guard",
				new Struct("a"), new NumberTerm.Int(1), new Var("L"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("callable")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_assert_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(assert(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("assertz", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_assert_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(assert(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
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
	void test_retract_1_1() throws MalformedGoalException {
		Prolog engine = new Prolog();
		String goal = "catch(retract(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		/*Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("retract_guard", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());*/
	}

	
	@Test
	void test_retract_1_2() throws MalformedGoalException {
		Prolog engine = new Prolog();
		String goal = "catch(retract(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		/*Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("retract_guard", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("clause")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());*/
	}

	
	@Test
	void test_retractall_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(retractall(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("retract_guard", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_retractall_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(retractall(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("retract_guard", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("clause")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_member_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(member(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("member_guard", new Struct("a"),
				new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_reverse_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(reverse(a, []), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("reverse_guard", new Struct("a"),
                Struct.emptyList())));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_delete_3_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(delete(a, a, []), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("delete_guard", new Struct("a"),
				new Struct("a"), Struct.emptyList())));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_element_3_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(element(1, a, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("element_guard", new NumberTerm.Int(1),
				new Struct("a"), new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

}