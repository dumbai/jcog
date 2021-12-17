package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Alternation;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.tokens.Literal;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * Show that <code>Parser.bestMatch()</code> matches a 
 * parser against an input as far as possible.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowBestMatch {
	/**
	 * Show that <code>Parser.bestMatch()</code> matches a 
	 * parser against an input as far as possible.
	 */
	public static void main(String[] args) {

		Alternation a = new Alternation();

		a.get(new Literal("steaming"));
		a.get(new Literal("hot"));

		Repetition adjectives = new Repetition(a);

		TokenAssembly ta = new TokenAssembly("hot hot steaming hot coffee");

		System.out.println(adjectives.bestMatch(ta));
	}
}
