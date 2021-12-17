package jcog.grammar.parse;

import jcog.grammar.parse.tokens.CaselessLiteral;
import jcog.grammar.parse.tokens.Num;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static jcog.grammar.parse.RepetitionTest.contains;
import static jcog.grammar.parse.RepetitionTest.size;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SequenceTest extends AbstractParsingTest {

	private Seq sequence;

	@BeforeEach
    void init() {
		sequence = new Seq();
	}

	@Test
    void noMatch() {
		sequence.get(new CaselessLiteral("abc"));
		assertNoMatch("def");
	}

	@Test
    void fullMatch() {
		sequence.get(new CaselessLiteral("abc"));
		assertCompleteMatch("abc");

		sequence.get(new Num());
		Assembly result = completeMatch("abc 1.0");
		assertEquals(new BigDecimal("1.0"), popValueFromAssembly(result));
	}

	@Test
    void partialMatch() {
		sequence.get(new CaselessLiteral("abc"));
		Assembly result = bestMatch("abc def");
		assertEquals(1, result.elementsRemaining());
		assertEquals(1, result.elementsConsumed());
		assertEquals("abc", popValueFromAssembly(result));
	}

	@Test
    void children() {
		sequence.get(new CaselessLiteral("abc"));
		sequence.get(new Num());
		assertEquals(2, size(getParser().children()));
	}

	@Test
    void leftChildren() {
		sequence.get(new CaselessLiteral("abc"));
		sequence.get(new Num());
		assertEquals(1, size(getParser().leftChildren()));
		assertTrue(contains(getParser().leftChildren(), new CaselessLiteral("abc")), ()-> String.valueOf(getParser().leftChildren()));
	}

	@Override
	protected Parser getParser() {
		return sequence;
	}
}
