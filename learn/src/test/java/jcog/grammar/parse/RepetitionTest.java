package jcog.grammar.parse;

import jcog.grammar.parse.tokens.Literal;
import jcog.grammar.parse.tokens.Num;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepetitionTest extends AbstractParsingTest {

	private Repetition repetition;

	@BeforeEach
    void init() {
		repetition = new Repetition(new Literal("abc"));
	}

	@Test
    void noMatch() {
		assertNoCompleteMatch("def");
	}

	@Test
    void fullMatch() {
		
		assertCompleteMatch("");
		
	}

	@Test
    void numberOfRequiredMatches() {
		repetition.requireMatches(2);
		assertEquals(2, repetition.requiredMatches());
		assertNoCompleteMatch("");
		assertNoCompleteMatch("abc");
		assertCompleteMatch("abc abc");
		assertCompleteMatch("abc abc abc abc");
	}

	@Test
    void children() {
		repetition = new Repetition(new Num());
		assertEquals(1, size(getParser().children()));
	}

	public static long size(Iterable i) {
		return StreamSupport.stream(i.spliterator(), false).count();
	}

	@Test
    void leftChildren() {
		Num numChild = new Num();
		repetition = new Repetition(numChild);
		assertEquals(1, size(getParser().leftChildren()));
		assertTrue(contains(getParser().leftChildren(), numChild));
	}

	public static <X> boolean contains(Iterable<X> yy, X x) {
		for (X y : yy)
			if (y.equals(x))
				return true;
		return false;
	}

	@Override
	protected Parser getParser() {
		return repetition;
	}
}
