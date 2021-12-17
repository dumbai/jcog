package jcog.grammar.parse.examples.coffee;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.tokens.TokenAssembly;
import jcog.grammar.parse.tokens.Tokenizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class CoffeeParserTest {

	@Test
    void examples() {
		Tokenizer t = CoffeeParser.tokenizer();
		t.setString("Thai Bulenc (Manchester), black, Argentina, 3.0");
		Assembly result = CoffeeParser.start().bestMatch(new TokenAssembly(t));
		Coffee coffee = (Coffee) result.getTarget();
		assertEquals("Thai Bulenc", coffee.getName());
		assertEquals("Manchester", coffee.getFormerName());
		assertEquals("black", coffee.getRoast());
		assertEquals("Argentina", coffee.getCountry());
		assertEquals(3.0, coffee.getPrice(), 0.01f);
	}
}
