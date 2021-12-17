package alice.tuprolog;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolutionTestCase {

	@Test
    void testGetSubsequentQuery() {
		Prolog engine = new Prolog();
		Term query = new Struct("is", new Var("X"), new Struct("+", new NumberTerm.Int(1), new NumberTerm.Int(2)));
		Solution result = engine.solve(query);
		assertTrue(result.isSuccess());
		assertEquals(query, result.getQuery());
		query = new Struct("functor", new Struct("p"), new Var("Name"), new Var("Arity"));
		result = engine.solve(query);
		assertTrue(result.isSuccess());
		assertEquals(query, result.getQuery());
	}

}
