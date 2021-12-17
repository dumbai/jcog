package jcog.grammar.parse;

import jcog.grammar.parse.tokens.Literal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static jcog.grammar.parse.RepetitionTest.size;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LiteralTest extends AbstractParsingTest {

	private Literal literal;

	@BeforeEach
    void init() {
		literal = new Literal("hello");
	}

	@Test
    void noMatch() {
		assertNoMatch("abc");
		assertNoMatch("HELLO");
	}

	@Test
    void match() {
		assertCompleteMatch("hello");
	}

	@Test
    void noChildren() {
        assertEquals(0, size(getParser().children()));
	}

	@Override
	protected Parser getParser() {
		return literal;
	}
}
