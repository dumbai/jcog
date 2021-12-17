package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Alternation;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.tokens.Literal;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * Show that a parser that contains a cycle prints 
 * itself without looping.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowCycle {
	/**
	 * Show that a parser that contains a cycle prints 
	 * itself without looping.
	 */
	public static void main(String[] args) {

		

		Alternation ticks = new Alternation();
		Literal tick = new Literal("tick");

		ticks.get(tick).get(new Seq().get(tick).get(ticks));

		System.out.println(ticks.bestMatch(new TokenAssembly("tick tick tick tick")));

		System.out.println(ticks);

	}
}