package jcog.grammar.parse.examples.introduction;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.tokens.TokenAssembly;
import jcog.grammar.parse.tokens.Word;

/**
 * Show how to recognize terminals in a string.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowTerminal {
	/**
	 * Just a little demo.
	 */
	public static void main(String[] args) {
		String s = "steaming hot coffee";
		Assembly a = new TokenAssembly(s);
		Parser p = new Word();

		while (true) {
			a = p.bestMatch(a);
			if (a == null) {
				break;
			}
			System.out.println(a);
		}

	}
}
