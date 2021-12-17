package alice.tuprolog;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TheoryTestCase {

	@Test
    void testToStringWithParenthesis() throws InvalidTheoryException {
		String before = "a :- b, (d ; e).";
		Theory theory = new Theory(before);
		String after = theory.toString();
		assertEquals(theory.toString(), new Theory(after).toString());
	}
	
	@Test
    void testAppendClauseLists() throws InvalidTheoryException, MalformedGoalException {
		Term[] clauseList = {new Struct("p"), new Struct("q"), new Struct("r")};
		Term[] otherClauseList = {new Struct("a"), new Struct("b"), new Struct("c")};
		Theory theory = new Theory(new Struct(clauseList));
		theory.append(new Theory(new Struct(otherClauseList)));
		Prolog engine = new Prolog();
		engine.setTheory(theory);
		assertTrue((engine.solve("p.")).isSuccess());
		assertTrue((engine.solve("b.")).isSuccess());
	}

}
