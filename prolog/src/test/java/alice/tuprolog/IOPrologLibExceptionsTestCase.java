package alice.tuprolog;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Matteo Iuliani
 * 
 *         Test del funzionamento delle eccezioni lanciate dai predicati della
 *         IOLibrary
 */
@Disabled
class IOPrologLibExceptionsTestCase {

	
	@Test
	void test_see_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(see(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("see", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_see_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(see(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("see", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	
	@Test
	void test_see_1_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(see(a), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("see", new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validDomain = (Struct) info.getTerm("ValidDomain");
		assertTrue(validDomain.isEqual(new Struct("stream")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	@Test
	void test_tell_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(tell(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("tell", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_tell_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(tell(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("tell", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_put_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(put(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("put", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_put_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(put(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("put", new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("character")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_put_1_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(put(aa), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("put", new Struct("aa"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("character")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("aa")));
	}

	
	@Test
	void test_tab_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(tab(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("tab", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_tab_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(tab(a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("tab", new Struct("a"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("integer")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	
	
	@Test
	void test_read_1_1() throws java.io.FileNotFoundException, MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		PrintWriter pw = new PrintWriter("read");
		pw.print("@term.");
		pw.close();
		Prolog engine = new Prolog();
		String goal = "see(read), catch(read(X), error(syntax_error(Message), syntax_error(Goal, Line, Position, Message)), true), seen.";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("read", new Var("X"))));
		NumberTerm.Int line = (NumberTerm.Int) info.getTerm("Line");
        assertEquals(1, line.intValue());
		NumberTerm.Int position = (NumberTerm.Int) info.getTerm("Line");
        assertEquals(1, position.intValue());
		Struct message = (Struct) info.getTerm("Message");
		assertTrue(message.isEqual(new Struct("@term")));
		File f = new File("read");
		f.delete();
	}

	
	@Test
	void test_write_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(write(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("write", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_print_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(print(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("print", new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_text_from_file_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(text_from_file(X, Y), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("text_from_file", new Var("X"),
				new Var("Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_text_from_file_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(text_from_file(1, Y), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("text_from_file", new NumberTerm.Int(1), new Var(
				"Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	
	@Test
	void test_text_from_file_2_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(text_from_file(text, Y), error(existence_error(ObjectType, Culprit), existence_error(Goal, ArgNo, ObjectType, Culprit, Message)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("text_from_file", new Struct("text"),
				new Var("Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ObjectType");
		assertTrue(validType.isEqual(new Struct("stream")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("text")));
		Term message = info.getTerm("Message");
		assertFileNotFound(message);
	}

	
	@Test
	void test_agent_file_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(agent_file(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.unifiable(new Struct("text_from_file", new Var("X"), new Var(
				"Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_agent_file_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(agent_file(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.unifiable(new Struct("text_from_file", new NumberTerm.Int(1),
				new Var("Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	
	@Test
	void test_agent_file_1_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(agent_file(text), error(existence_error(ObjectType, Culprit), existence_error(Goal, ArgNo, ObjectType, Culprit, Message)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.unifiable(new Struct("text_from_file", new Struct("text"),
				new Var("Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ObjectType");
		assertTrue(validType.isEqual(new Struct("stream")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("text")));
		Term message = info.getTerm("Message");
		assertFileNotFound(message);
	}

	
	@Test
	void test_solve_file_2_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(solve_file(X, g), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term g = info.getTerm("Goal");
		assertTrue(g instanceof Struct);
		assertTrue(g.unifiable(new Struct("text_from_file", new Var("X"), new Var(
				"Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_solve_file_2_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(solve_file(1, g), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.unifiable(new Struct("text_from_file", new NumberTerm.Int(1),
				new Var("Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	
	@Test
	void test_solve_file_2_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(solve_file(text, g), error(existence_error(ObjectType, Culprit), existence_error(Goal, ArgNo, ObjectType, Culprit, Message)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.unifiable(new Struct("text_from_file", new Struct("text"),
				new Var("Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ObjectType");
		assertTrue(validType.isEqual(new Struct("stream")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("text")));
		Term message = info.getTerm("Message");
		assertFileNotFound(message);
	}

	
	@Test
	void test_solve_file_2_4() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(solve_file(text, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("solve_file_goal_guard", new Struct(
				"text"), new Var("X"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
	}

	
	@Test
	void test_solve_file_2_5() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(solve_file(text, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("solve_file_goal_guard", new Struct(
				"text"), new NumberTerm.Int(1))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(2, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("callable")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	@Test
	void test_consult_1_1() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(consult(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.unifiable(new Struct("text_from_file", new Var("X"), new Var(
				"Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
	}

	
	@Test
	void test_consult_1_2() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(consult(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.unifiable(new Struct("text_from_file", new NumberTerm.Int(1),
				new Var("Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		NumberTerm.Int culprit = (NumberTerm.Int) info.getTerm("Culprit");
        assertEquals(1, culprit.intValue());
	}

	
	
	@Test
	void test_consult_1_3() throws MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		Prolog engine = new Prolog();
		String goal = "catch(consult(text), error(existence_error(ObjectType, Culprit), existence_error(Goal, ArgNo, ObjectType, Culprit, Message)), true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.unifiable(new Struct("text_from_file", new Struct("text"),
				new Var("Y"))));
		NumberTerm.Int argNo = (NumberTerm.Int) info.getTerm("ArgNo");
        assertEquals(1, argNo.intValue());
		Struct validType = (Struct) info.getTerm("ObjectType");
		assertTrue(validType.isEqual(new Struct("stream")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("text")));
		Term message = info.getTerm("Message");
		assertFileNotFound(message);
	}

	static void assertFileNotFound(Term message) {
		assertTrue(message.toString().contains("FileNotFoundException"));
	}

}