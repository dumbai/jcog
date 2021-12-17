package jcog.grammar.parse.examples.introduction;

import jcog.grammar.parse.Parser;
import jcog.grammar.parse.tokens.QuotedString;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * Show how to recognize a quoted string.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowQuotedString {
	/**
	 *  Show how to recognize a quoted string.
	 */
	public static void main(String[] args) {
		Parser p = new QuotedString();
		String id = "\"Clark Kent\"";
		System.out.println(p.bestMatch(new TokenAssembly(id)));
	}
}
