package alice.tuprolog;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrologTestCase {
    private final Prolog engine = new Prolog();

    @Test
    void testEngineInitialization() {

        assertNotNull(engine.library("alice.tuprolog.lib.BasicLibrary"));
        assertNotNull(engine.library("alice.tuprolog.lib.ISOLibrary"));
        assertNotNull(engine.library("alice.tuprolog.lib.IOLibrary"));
    }

    @Test
    void testLoadLibraryAsString() throws InvalidLibraryException {

        engine.addLibrary("alice.tuprolog.StringLibrary");
        assertNotNull(engine.library("alice.tuprolog.StringLibrary"));
    }


    @Test
    void testGetLibraryWithName() {
        Prolog engine = new Prolog("alice.tuprolog.TestLibrary");
        assertNotNull(engine.library("TestLibraryName"));
    }

    @Test
    void testUnloadLibraryAfterLoadingTheory() throws InvalidTheoryException, InvalidLibraryException {

        assertNotNull(engine.library("alice.tuprolog.lib.IOLibrary"));
        Theory t = new Theory("a(1).\na(2).\n");
        engine.setTheory(t);
        engine.removeLibrary("alice.tuprolog.lib.IOLibrary");
        assertNull(engine.library("alice.tuprolog.lib.IOLibrary"));
    }

    @Test
    void testAddTheory() throws InvalidTheoryException {

        Theory t = new Theory("test :- notx existing(s).");
        try {
            engine.input(t);
            fail("");
        } catch (InvalidTheoryException expected) {
            assertEquals("", engine.getTheory().toString());
        }
    }


    @Test
    void testLibraryListener() throws InvalidLibraryException {
        Prolog engine = new Prolog(new String[]{});
        engine.addLibrary("alice.tuprolog.lib.BasicLibrary");
        engine.addLibrary("alice.tuprolog.lib.IOLibrary");
        TestPrologEventAdapter a = new TestPrologEventAdapter();
        engine.addLibraryListener(a);
        engine.addLibrary("alice.tuprolog.lib.OOLibrary");
        assertEquals("alice.tuprolog.lib.OOLibrary", a.firstMessage);
        engine.removeLibrary("alice.tuprolog.lib.OOLibrary");
        assertEquals("alice.tuprolog.lib.OOLibrary", a.firstMessage);
    }

    @Test
    void testTheoryListener() throws InvalidTheoryException {

        TestPrologEventAdapter a = new TestPrologEventAdapter();
        engine.addTheoryListener(a);
        Theory t = new Theory("a(1).\na(2).\n");
        engine.setTheory(t);
        assertEquals("", a.firstMessage);
        assertEquals("a(1).\n\na(2).\n\n", a.secondMessage);
        t = new Theory("a(3).\na(4).\n");
        engine.input(t);
        assertEquals("a(1).\n\na(2).\n\n", a.firstMessage);
        assertEquals("a(1).\n\na(2).\n\na(3).\n\na(4).\n\n", a.secondMessage);
    }

    @Test
    void testQueryListener() throws InvalidTheoryException, MalformedGoalException, NoMoreSolutionException {

        TestPrologEventAdapter a = new TestPrologEventAdapter();
        engine.addQueryListener(a);
        engine.setTheory(new Theory("a(1).\na(2).\n"));
        engine.solve("a(X).");
        assertEquals("a(X)", a.firstMessage);
        assertEquals("yes.\nX / 1", a.secondMessage);
        engine.solveNext();
        assertEquals("a(X)", a.firstMessage);
        assertEquals("yes.\nX / 2", a.secondMessage);
    }

}