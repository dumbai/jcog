package jcog.grammar.parse.examples.tests;

import jcog.grammar.parse.Parser;
import jcog.grammar.parse.tokens.TokenTester;

/**
 * Test the statement parser from class <code>Dangle</code>.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0 
 */
public class ShowDangleTest {
	/**
	 * Test the statement parser from class <code>Dangle</code>.
	 */
	public static void main(String[] args) {
		Parser p = new Dangle().statement();
		TokenTester tt = new TokenTester(p);
		tt.setLogTestStrings(false);
		tt.test();
	}
}
