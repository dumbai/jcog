package jcog.grammar.parse.examples.tests;

import jcog.grammar.parse.examples.coffee.CoffeeParser;
import jcog.grammar.parse.tokens.TokenTester;

/**
 * Test the <code>CoffeeParser</code> class.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0 
 */
public class ShowCoffeeTest {
	/**
	 * Test the <code>CoffeeParser</code> class.
	 */
	public static void main(String[] args) {
		new TokenTester(CoffeeParser.start()).test();
	}
}
