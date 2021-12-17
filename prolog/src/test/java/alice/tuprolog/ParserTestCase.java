package alice.tuprolog;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTestCase {
	
	@Test
	void testReadingTerms() throws InvalidTermException {
		Parser p = new Parser("hello.");
		Struct result = new Struct("hello");
		assertEquals(result, p.nextTerm(true));
	}
	
	@Test
	void testReadingEOF() throws InvalidTermException {
		Parser p = new Parser("");
		assertNull(p.nextTerm(false));
	}
	
	@Test
	void testUnaryPlusOperator() {
		Parser p = new Parser("n(+100).\n");
        
		
		
		try {
			assertNotNull(p.nextTerm(true));
			fail("");
		} catch (InvalidTermException e) {}
	}
	
	@Test
	void testUnaryMinusOperator() throws InvalidTermException {
		Parser p = new Parser("n(-100).\n");
		
		
		
		
		Struct result = new Struct("n", new NumberTerm.Int(-100));
		result.resolveTerm();
		assertEquals(result, p.nextTerm(true));
	}
	
	@Test
	void testBinaryMinusOperator() throws InvalidTermException {
		String s = "abs(3-11)";
		Parser p = new Parser(s);
		Struct result = new Struct("abs", new Struct("-", new NumberTerm.Int(3), new NumberTerm.Int(11)));
		assertEquals(result, p.nextTerm(false));
	}
	
	@Test
	void testListWithTail() throws InvalidTermException {
		Parser p = new Parser("[p|Y]");
		Struct a = new Struct(new Struct("p"), new Var("Y"));
		
		Term b = p.nextTerm(false);
		a.resolveTerm(((Var)((Struct)b).sub(1)).timestamp);
		assertEquals(a, b);
	}
	
	@Test
	void testBraces() throws InvalidTermException {
		String s = "{a,b,[3,{4,c},5],{a,b}}";
		Parser parser = new Parser(s);
		assertEquals(s, parser.nextTerm(false).toString());
	}
	
	@Test
	void testUnivOperator() throws InvalidTermException {
		Parser p = new Parser("p =.. q.");
		Struct result = new Struct("=..", new Struct("p"), new Struct("q"));
		assertEquals(result, p.nextTerm(true));
	}
	
	@Test
	void testDotOperator() throws InvalidTermException {
        PrologOperators.DefaultOps om = new PrologOperators.DefaultOps();
		om.opNew(".", "xfx", 600);
        String s = "class('java.lang.Integer').'MAX_VALUE'";
        Parser p = new Parser(s, om);
		Struct result = new Struct(".", new Struct("class", new Struct("java.lang.Integer")),
				                        new Struct("MAX_VALUE"));
		assertEquals(result, p.nextTerm(false));
	}
	
	@Test
	void testBracketedOperatorAsTerm() throws InvalidTermException {
        PrologOperators.DefaultOps om = new PrologOperators.DefaultOps();
		om.opNew("u", "fx", 200);
		om.opNew("b1", "yfx", 400);
		om.opNew("b2", "yfx", 500);
		om.opNew("b3", "yfx", 300);
        String s = "u (b1) b2 (b3)";
        Parser p = new Parser(s, om);
		Struct result = new Struct("b2", new Struct("u", new Struct("b1")), new Struct("b3"));
		assertEquals(result, p.nextTerm(false));
	}
	
	@Test
	void testBracketedOperatorAsTerm2() throws InvalidTermException {
        PrologOperators.DefaultOps om = new PrologOperators.DefaultOps();
		om.opNew("u", "fx", 200);
		om.opNew("b1", "yfx", 400);
		om.opNew("b2", "yfx", 500);
		om.opNew("b3", "yfx", 300);
        String s = "(u) b1 (b2) b3 a";
        Parser p = new Parser(s, om);
		Struct result = new Struct("b1", new Struct("u"), new Struct("b3", new Struct("b2"), new Struct("a")));
		assertEquals(result, p.nextTerm(false));
	}
	
	@Test
	void testIntegerBinaryRepresentation() throws InvalidTermException {
		String n = "0b101101";
		Parser p = new Parser(n);
		NumberTerm result = new NumberTerm.Int(45);
		assertEquals(result, p.nextTerm(false));
        try {
            String invalid = "0b101201";
            new Parser(invalid).nextTerm(false);
			fail("");
		} catch (InvalidTermException expected) {}
	}
	
	@Test
	void testIntegerOctalRepresentation() throws InvalidTermException {
		String n = "0o77351";
		Parser p = new Parser(n);
		NumberTerm result = new NumberTerm.Int(32489);
		assertEquals(result, p.nextTerm(false));
        try {
            String invalid = "0o78351";
            new Parser(invalid).nextTerm(false);
			fail("");
		} catch (InvalidTermException expected) {}
	}
	
	@Test
	void testIntegerHexadecimalRepresentation() throws InvalidTermException {
		String n = "0xDECAF";
		Parser p = new Parser(n);
		NumberTerm result = new NumberTerm.Int(912559);
		assertEquals(result, p.nextTerm(false));
        try {
            String invalid = "0xG";
            new Parser(invalid).nextTerm(false);
			fail("");
		} catch (InvalidTermException expected) {}
	}
	
	@Test
	void testEmptyDCGAction() throws InvalidTermException {
		String s = "{}";
		Parser p = new Parser(s);
		Struct result = new Struct("{}");
		assertEquals(result, p.nextTerm(false));
	}
	
	@Test
	void testSingleDCGAction() throws InvalidTermException {
		String s = "{hello}";
		Parser p = new Parser(s);
		Struct result = new Struct("{}", new Struct("hello"));
		assertEquals(result, p.nextTerm(false));
	}
	
	@Test
	void testMultipleDCGAction() throws InvalidTermException {
		String s = "{a, b, c}";
		Parser p = new Parser(s);
		Struct result = new Struct("{}",
                                   new Struct(",", new Struct("a"),
                                       new Struct(",", new Struct("b"), new Struct("c"))));
		assertEquals(result, p.nextTerm(false));
	}
	
	 
	@Test
	void testDCGActionWithOperators() {
        Struct result = new Struct("{}",
                            new Struct(",", new Struct("=..", new Var("A"), new Var("B")),
                                new Struct(",", new Struct("hotel"), new NumberTerm.Int(2))));
        result.resolveTerm();
        String input = "{A =.. B, hotel, 2}";
        Parser p = new Parser(input);
        
		assertEquals(result.toString(), p.nextTerm(false).toString());
	}
	
	@Test
	void testMissingDCGActionElement() {
		String s = "{1, 2, , 4}";
		Parser p = new Parser(s);
		try {
			p.nextTerm(false);
			fail("");
		} catch (InvalidTermException expected) {}
	}
	
	@Test
	void testDCGActionCommaAsAnotherSymbol() {
		String s = "{1 @ 2 @ 4}";
		Parser p = new Parser(s);
		try {
			p.nextTerm(false);
			fail("");
		} catch (InvalidTermException expected) {}
	}
	
	@Test
	void testUncompleteDCGAction() {
		String s = "{1, 2,}";
		Parser p = new Parser(s);
		try {
			p.nextTerm(false);
			fail("");
		} catch (InvalidTermException expected) {}
		
		s = "{1, 2";
		p = new Parser(s);
		try {
			p.nextTerm(false);
			fail("");
		} catch (InvalidTermException expected) {}
	}

	@Test
	void testMultilineComments() throws InvalidTermException {
		String theory = String.join("\n", "t1.", "/*", "t2", "*/", "t3.") + '\n';
		Parser p = new Parser(theory);
		assertEquals(new Struct("t1"), p.nextTerm(true));
		assertEquals(new Struct("t3"), p.nextTerm(true));
	}
	
	@Test
	void testSingleQuotedTermWithInvalidLineBreaks() {
		String s = """
				out('can_do(X).
				can_do(Y).
				').""";
		Parser p = new Parser(s);
		try {
			p.nextTerm(true);
			fail("");
		} catch (InvalidTermException expected) {}
	}
	
	
	
	
	
	
	
	
	
	

}
