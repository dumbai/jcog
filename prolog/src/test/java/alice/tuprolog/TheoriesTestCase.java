package alice.tuprolog;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

class TheoriesTestCase {

    private final Prolog engine = new Prolog();

    @Test
    void testAssertNotBacktrackable() throws MalformedGoalException {

        Solution firstSolution = engine.solve("assertz(a(z)).");
        assertTrue(firstSolution.isSuccess());
        assertFalse(firstSolution.hasOpenAlternatives());
    }

    @Test
    void testAbolish() throws InvalidTheoryException {

        String theory = "test(A, B) :- A is 1+2, B is 2+3.";
        engine.setTheory(new Theory(theory));
        Theories manager = engine.theories;
        Struct testTerm = new Struct("test", new Struct("a"), new Struct("b"));
        Deque<ClauseInfo> testClauses = manager.find(testTerm);
        assertEquals(1, testClauses.size());
        manager.abolish(new Struct("/", new Struct("test"), new NumberTerm.Int(2)));
        testClauses = manager.find(testTerm);


        assertEquals(0, testClauses.size());
    }

    @Test
    void testAbolish2() throws InvalidTheoryException, MalformedGoalException {

        engine.setTheory(new Theory("""
                fact(new).
                fact(other).
                """));

        Solution info = engine.solve("abolish(fact/1).");
        assertTrue(info.isSuccess());
        info = engine.solve("fact(V).");
        assertFalse(info.isSuccess());
    }


    @Test
    void testRetractall() throws MalformedGoalException, NoSolutionException, NoMoreSolutionException {

        Solution info = engine.solve("assert(takes(s1,c2)), assert(takes(s1,c3)).");
        assertTrue(info.isSuccess());
        info = engine.solve("takes(s1, N).");
        assertTrue(info.isSuccess());
        assertTrue(info.hasOpenAlternatives());
        assertEquals("c2", info.getVarValue("N").toString());
        info = engine.solveNext();
        assertTrue(info.isSuccess());
        assertEquals("c3", info.getVarValue("N").toString());

        info = engine.solve("retractall(takes(s1,c2)).");

        assertTrue(info.isSuccess());
        info = engine.solve("takes(s1, N).");
        assertTrue(info.isSuccess());
        if (info.hasOpenAlternatives())
            System.err.println(engine.solveNext());

        assertEquals("c2", info.getVarValue("N").toString());
    }


    /** this has to do with the variables not being fully cached.  */
    @Disabled
    @Test
    void testRetract() throws InvalidTheoryException, MalformedGoalException {
        assert(engine.onOut.isEmpty());
        TestOutputListener listener = new TestOutputListener();
        engine.addOutputListener(listener);
        engine.setTheory(new Theory("insect(ant). insect(bee)."));
        Solution info = engine.solve("retract(insect(I)), write(I), retract(insect(bee)).");
        assertFalse(info.isSuccess());
        assertEquals("antbee", listener.output);

    }

}