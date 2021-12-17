package jcog.grammar.parse;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class LeftRecursionCheckerTest {

	private Grammar grammar;

	@BeforeEach
    void init() {
		grammar = new Grammar("test");
	}

	@Test
    void noRecursion() {
		grammar.defineRule("s = '<' '+'");
		assertNoLeftRecursionException();
	}

	@Test
    void rightRecursionIsFine() {
		grammar.defineRule("s = '<' s");
		assertNoLeftRecursionException();
	}

	@Test
    void ruleReferencesItself() {
		grammar.defineRule("s = s");
		assertLeftRecursionException();
	}

	@Test
    void immediateLeftRecursionInSequence() {
		grammar.defineRule("s = s '>'");
		assertLeftRecursionException();
	}

	@Test
    void immediateLeftRecursionInRepetition() {
		grammar.defineRule("s = (s '>')*");
		assertLeftRecursionException();

		grammar.defineRule("s = (s '>')+");
		assertLeftRecursionException();
	}

	@Test
    void immediateLeftRecursionInAlternation() {
		grammar.defineRule("s = ('>' s) | s");
		assertLeftRecursionException();

		grammar.defineRule("s = (s '>') | '<'");
		assertLeftRecursionException();
	}

	@Test
    void indirectLeftRecursion() {
		grammar.defineRule("s = a '<'");
		grammar.defineRule("a = Num | s '+'");
		assertLeftRecursionException();
	}

	@Test
    void complexExample() {
		grammar.defineRule("s = a '<'");
		grammar.defineRule("a = Num | b '+'");
		grammar.defineRule("b = Num | (c '+') | ('=' d)");
		grammar.defineRule("c = Int | ('+' c) | d");
		grammar.defineRule("d = s+");
		assertLeftRecursionException();

		grammar.defineRule("d = Word");
		assertNoLeftRecursionException();
	}

	private void assertNoLeftRecursionException() {
		new LeftRecursionChecker(grammar).check();
	}

	private void assertLeftRecursionException() {
		try {
			new LeftRecursionChecker(grammar).check();
			fail("Left Recursion expected");
		} catch (GrammarException expected) {
		}
	}

}
