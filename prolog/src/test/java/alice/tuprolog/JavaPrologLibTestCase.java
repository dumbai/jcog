package alice.tuprolog;

import alice.tuprolog.lib.InvalidObjectIdException;
import alice.tuprolog.lib.OOLibrary;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Disabled /* TODO */ class JavaPrologLibTestCase {
	private String theory;

	private final Prolog prolog = new Prolog();
	private final OOLibrary lib = (OOLibrary) prolog.library("alice.tuprolog.lib.OOLibrary");
	private Solution info;
	private String result;
	private String paths;

	@Test
	void testGetPrimitives() {
		PrologLib prologLib = new OOLibrary();
		Map<Integer, List<PrologPrim>> primitives = prologLib.primitives();
		assertEquals(3, primitives.size());
		assertEquals(0, primitives.get(PrologPrim.DIRECTIVE).size());
		assertFalse(primitives.get(PrologPrim.PREDICATE).isEmpty());
		assertEquals(0, primitives.get(PrologPrim.FUNCTOR).size());
	}

	@Test
	void testAnonymousObjectRegistration() throws InvalidTheoryException, InvalidObjectIdException {

		String theory = "demo(X) :- X <- update. \n";
		prolog.setTheory(new Theory(theory));
		TestCounter counter = new TestCounter();
		
		Struct t = lib.register(counter);
		prolog.solve(new Struct("demo", t));
		assertEquals(1, counter.getValue());
		
		lib.unregister(t);
		Solution goal = prolog.solve(new Struct("demo", t));
		assertFalse(goal.isSuccess());
	}

	@Test
	void testDynamicObjectsRetrival() throws InvalidTheoryException, MalformedGoalException, NoSolutionException, InvalidObjectIdException {


		String theory = """
				demo(C) :-\s
				java_object('alice.tuprolog.TestCounter', [], C),\s
				C <- update,\s
				C <- update.\s
				""";
		prolog.setTheory(new Theory(theory));
		Solution info = prolog.solve("demo(Obj).");
		Struct id = (Struct) info.getVarValue("Obj");
		TestCounter counter = (TestCounter) lib.getRegisteredDynamicObject(id);
		assertEquals(2, counter.getValue());
	}

	
	@Test
	void test_java_object() throws IOException, InvalidTheoryException, MalformedGoalException, NoSolutionException {
		
		setPath(true);
		theory = "demo(C) :- \n" +
				"set_classpath([" + paths + "]), \n" +
				"java_object('Counter', [], Obj), \n" +
				"Obj <- inc, \n" +
				"Obj <- inc, \n" +
				"Obj <- getValue returns C.";
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo(Value).");
        assertTrue(info.isSuccess());
		NumberTerm result2 = (NumberTerm) info.getVarValue("Value");
		assertEquals(2, result2.intValue());

		
		theory = """
				demo_string(S) :-\s
				java_object('java.lang.String', ['MyString'], Obj_str),\s
				Obj_str <- toString returns S.""";
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo_string(StringValue).");
        assertTrue(info.isSuccess());
		result = info.getVarValue("StringValue").toString().replace("'", "");
		assertEquals("MyString", result);
	}
	

	@Test
	void test_java_object_2() throws IOException, InvalidTheoryException, MalformedGoalException, NoSolutionException {
		setPath(true);
		theory = "demo_hierarchy(Gear) :- \n"
					+ "set_classpath([" + paths + "]), \n" 
					+ "java_object('Bicycle', [3, 4, 5], MyBicycle), \n"
					+ "java_object('MountainBike', [5, 6, 7, 8], MyMountainBike), \n"
					+ "MyMountainBike <- getGear returns Gear.";
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo_hierarchy(Res).");
        assertFalse(info.isHalted());
		NumberTerm result2 = (NumberTerm) info.getVarValue("Res");
		assertEquals(8, result2.intValue());
	}
	
	@Test
	void test_invalid_path_java_object() throws IOException, InvalidTheoryException, MalformedGoalException {
		
		setPath(false);
		theory = "demo(Res) :- \n" +
				"set_classpath([" + paths + "]), \n" + 
				"java_object('Counter', [], Obj_inc), \n" +
				"Obj_inc <- inc, \n" +
				"Obj_inc <- inc, \n" +
				"Obj_inc <- getValue returns Res.";
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo(Value).");
        assertTrue(info.isHalted());
	}

	@Test
	void test_java_call_3() throws IOException, InvalidTheoryException, MalformedGoalException, NoSolutionException {
		
		setPath(true); 
		theory = "demo(Value) :- set_classpath([" + paths + "]), class('TestStaticClass') <- echo('Message') returns Value.";
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo(StringValue).");
        assertTrue(info.isSuccess());
		result = info.getVarValue("StringValue").toString().replace("'", "");
		assertEquals("Message", result);

		
		setPath(true);
		theory = "demo_2(Value) :- set_classpath([" + paths + "]), class('TestStaticClass').'id' <- get(Value).";
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo_2(Res).");
        assertTrue(info.isSuccess());
		assertEquals(0, Integer.parseInt(info.getVarValue("Res").toString()));
		
		theory = "demo_2(Value, NewValue) :- set_classpath([" + paths + "]), class('TestStaticClass').'id' <- set(Value), \n" +
				"class('TestStaticClass').'id' <- get(NewValue).";
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo_2(5, Val).");
        assertTrue(info.isSuccess());
		assertEquals(5, Integer.parseInt(info.getVarValue("Val").toString()));
		
	}

	@Test
	void test_invalid_path_java_call_4() throws IOException, InvalidTheoryException, MalformedGoalException {
		
		setPath(false);
		theory = "demo(Value) :- set_classpath([" + paths + "]), class('TestStaticClass') <- echo('Message') returns Value.";
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo(StringValue).");
        assertTrue(info.isHalted());
	}

	@Test
	void test_java_array() throws IOException, InvalidTheoryException, MalformedGoalException, NoSolutionException {
		
		setPath(true);
		theory =  "demo(Size) :- set_classpath([" + paths + "]), java_object('Counter', [], MyCounter), \n"
				+ "java_object('Counter[]', [10], ArrayCounters), \n"
				+ "java_array_length(ArrayCounters, Size).";

		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo(Value).");
        assertTrue(info.isSuccess());
		NumberTerm resultInt = (NumberTerm) info.getVarValue("Value");
		assertEquals(10, resultInt.intValue());

		
		setPath(true);
		theory =  "demo(Res) :- set_classpath([" + paths + "]), java_object('Counter', [], MyCounter), \n"
				+ "java_object('Counter[]', [10], ArrayCounters), \n"
				+ "MyCounter <- inc, \n"
				+ "java_array_set(ArrayCounters, 0, MyCounter), \n"
				+ "java_array_get(ArrayCounters, 0, C), \n"
				+ "C <- getValue returns Res.";

		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo(Value).");
        assertTrue(info.isSuccess());
		NumberTerm resultInt2 = (NumberTerm) info.getVarValue("Value");
		assertEquals(1, resultInt2.intValue());
	}

	@Test
	void test_set_classpath() throws IOException, InvalidTheoryException, MalformedGoalException, NoSolutionException {
		
		setPath(true);
		
		theory =  "demo(Size) :- set_classpath([" + paths + "]), \n "
				+ "java_object('Counter', [], MyCounter), \n"
				+ "java_object('Counter[]', [10], ArrayCounters), \n"
				+ "java_array_length(ArrayCounters, Size).";

		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo(Value).");
        assertTrue(info.isSuccess());
		NumberTerm resultInt = (NumberTerm) info.getVarValue("Value");
		assertEquals(10, resultInt.intValue());
	}
	
	@Test
	void test_get_classpath() throws IOException, InvalidTheoryException, MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		
		theory =  "demo(P) :- get_classpath(P).";
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo(Value).");
        assertTrue(info.isSuccess());
        assertTrue(info.getTerm("Value").isList());
		assertEquals("[]", info.getTerm("Value").toString());

		
		setPath(true);

		theory =  "demo(P) :- set_classpath([" + paths + "]), get_classpath(P).";

		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo(Value).");
        assertTrue(info.isSuccess());
        assertTrue(info.getTerm("Value").isList());
		assertEquals('[' + paths + ']', info.getTerm("Value").toString());
		






	}
	
	@Test
	void test_register_1() throws IOException, InvalidTheoryException, MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		setPath(true);
		theory = "demo(Obj) :- \n" +
				"set_classpath([" + paths + "]), \n" +
				"java_object('Counter', [], Obj), \n" +
				"Obj <- inc, \n" +
				"Obj <- inc, \n" +
				"register(Obj).";
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo(R).");
        assertTrue(info.isSuccess());
		
		theory = """
				demo2(Obj, Val) :-\s
				Obj <- inc,\s
				Obj <- getValue returns Val.""";
		prolog.input(new Theory(theory));
		String obj =  info.getTerm("R").toString();
		Solution info2 = prolog.solve("demo2(" + obj + ", V).");
        assertTrue(info2.isSuccess());
		assertEquals(3, Integer.parseInt(info2.getVarValue("V").toString()));
	
		
		theory = "demo(Obj1) :- register(Obj1).";
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo(Res).");
        assertTrue(info.isHalted());
	}
	
	
	@Test
	void test_unregister_1() throws IOException, InvalidTheoryException, MalformedGoalException, NoSolutionException, Var.UnknownVarException, InvalidObjectIdException {
		
		theory = "demo(Obj1) :- unregister(Obj1).";
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo(Res).");
        assertTrue(info.isHalted());
		
		setPath(true);
		theory = "demo(Obj) :- \n" +
				"set_classpath([" + paths + "]), \n" +
				"java_object('Counter', [], Obj), \n" +
				"Obj <- inc, \n" +
				"Obj <- inc, \n" +
				"register(Obj), unregister(Obj).";
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo(Res).");
        assertTrue(info.isSuccess());
		OOLibrary lib = (OOLibrary) prolog.library("alice.tuprolog.lib.OOLibrary");
		Struct id = (Struct) info.getTerm("Res");
		Object obj = lib.getRegisteredObject(id);
		assertNull(obj);
	}
	
	@Test
	void test_java_catch() throws IOException, InvalidTheoryException, MalformedGoalException {
		setPath(true);
		theory = "goal :- set_classpath([" + paths + "]), java_object('TestStaticClass', [], Obj), Obj <- testMyException. \n"
				+"demo(StackTrace) :- java_catch(goal, [('java.lang.IllegalArgumentException'( \n"
						+ "Cause, Msg, StackTrace),write(Msg))], \n"
						+ "true).";
				
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("demo(S).");
        assertTrue(info.isSuccess());
	}
	
	@Test
	void test_interface() throws IOException, InvalidTheoryException, MalformedGoalException {
		setPath(true);
		theory = "goal1 :- set_classpath([" + paths + "])," +
				"java_object('Pippo', [], Obj), class('Pluto') <- method(Obj).";
				
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("goal1.");
        assertTrue(info.isSuccess());
		
		theory = "goal2 :- set_classpath([" + paths + "])," +
				"java_object('Pippo', [], Obj), class('Pluto') <- method2(Obj).";
				
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("goal2.");
        assertTrue(info.isSuccess());
		
		theory = "goal3 :- java_object('Pippo', [], Obj), set_classpath([" + paths + "]), class('Pluto') <- method(Obj).";
				
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("goal3.");
        assertTrue(info.isSuccess());
		
		theory = "goal4 :- set_classpath([" + paths + "]), " +
					"java_object('IPippo[]', [5], Array), " +
					"java_object('Pippo', [], Obj), " +
					"java_array_set(Array, 0, Obj)," +
					"java_array_get(Array, 0, Obj2)," +
					"Obj2 <- met.";
		
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("goal4.");
        assertTrue(info.isSuccess());
		
		theory = "goal5 :- set_classpath([" + paths + "])," +
				"java_object('Pippo', [], Obj)," +
				"class('Pluto') <- method(Obj as 'IPippo').";
		
		prolog.setTheory(new Theory(theory));
		info = prolog.solve("goal5.");
        assertTrue(info.isSuccess());
		
	}
	
	/**
	 * @param valid: used to change a valid/invalid array of paths
	 */
	private void setPath(boolean valid) throws IOException
	{
		File file = new File(".");
		
		
		if(valid)
		{
			paths = '\'' + file.getCanonicalPath() + "'," +
                    '\'' + file.getCanonicalPath()
					+ File.separator + "test"
					+ File.separator + "unit" 
					+ File.separator + "TestURLClassLoader.jar'";
			paths += ',' + '\'' + file.getCanonicalPath()
					+ File.separator + "test"
					+ File.separator + "unit" 
					+ File.separator + "TestInterfaces.jar'";
		}
		
		else
		{
			paths = '\'' + file.getCanonicalPath() + '\'';
		}
	}
}