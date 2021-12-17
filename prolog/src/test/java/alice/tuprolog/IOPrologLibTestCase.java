package alice.tuprolog;

import alice.tuprolog.lib.IOLibrary;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class IOPrologLibTestCase {
	
	@Test
    void testGetPrimitives() {
		PrologLib prologLib = new IOLibrary();
		Map<Integer, List<PrologPrim>> primitives = prologLib.primitives();
		assertEquals(3, primitives.size());
		assertEquals(0, primitives.get(PrologPrim.DIRECTIVE).size());
        assertFalse(primitives.get(PrologPrim.PREDICATE).isEmpty());
		assertEquals(0, primitives.get(PrologPrim.FUNCTOR).size());
	}
	
	@Test
    void testTab1() throws MalformedGoalException {
		Prolog engine = new Prolog();
		TestOutputListener l = new TestOutputListener();
		engine.addOutputListener(l);
		engine.solve("tab(5).");
		assertEquals("     ", l.output);
	}

}