package jcog.grammar.parse;

import jcog.grammar.parse.tokens.Token;
import jcog.grammar.parse.tokens.TokenAssembly;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

abstract class AbstractParsingTest {

	private TokenAssembly assembly;

	void assertCompleteMatch(String text) {
		assertNotNull(completeMatch(text));
	}

	Assembly completeMatch(String text) {
		assembly = new TokenAssembly(text);
		return getParser().completeMatch(assembly);
	}

	void assertNoCompleteMatch(String text) {
		assertNull(completeMatch(text));
	}

	Assembly bestMatch(String text) {
		assembly = new TokenAssembly(text);
		Assembly bestMatch = getParser().bestMatch(assembly);
		return bestMatch;
	}

	protected abstract Parser getParser();

	static Object popValueFromAssembly(Assembly result) {
		return ((Token) result.getStack().pop()).value();
	}

	void assertNoMatch(String text) {
		assertNull(bestMatch(text));
	}

}