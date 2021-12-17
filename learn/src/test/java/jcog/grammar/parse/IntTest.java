package jcog.grammar.parse;

import jcog.grammar.parse.tokens.Int;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static jcog.grammar.parse.RepetitionTest.size;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntTest extends AbstractParsingTest {

	private Int intTerminal;

	@BeforeEach
    void init() {
		intTerminal = new Int();
	}

	@Test
    void noMatch() {
		assertNoMatch("abc");
		assertNoMatch("1.23");
	}

	@Test
    void match() {
		Assembly result = completeMatch("1000");
		assertEquals(new BigInteger("1000"), popValueFromAssembly(result));
	}

	@Test
    void noChildren() {
        assertEquals(0, size(getParser().children()));
	}

	@Override
	protected Parser getParser() {
		return intTerminal;
	}
}
