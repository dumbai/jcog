package jcog.grammar.parse.examples.tests;

import jcog.grammar.parse.chars.CharacterTester;
import jcog.grammar.parse.examples.regular.RegularParser;

/**
 * Test the <code>RegularParser</code> class.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0 
 */
public class ShowRegularTester {
	/**
	 * Test the <code>RegularParser</code> class.
	 */
	public static void main(String[] args) {
		new CharacterTester(RegularParser.start()).test();
	}
}
