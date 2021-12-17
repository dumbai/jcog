package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.tokens.Num;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;

import java.util.Stack;

/**
 * Show the value of not pushing the element a terminal 
 * matches.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowPush {
	/**
	 * Show the value of not pushing the element a terminal 
	 * matches.
	 */
	public static void main(String[] args) {

		Parser open = new Symbol('(').ok();
		Parser close = new Symbol(')').ok();
		Parser comma = new Symbol(',').ok();

		Num num = new Num();

		Parser coord = new Seq().get(open).get(num).get(comma).get(num).get(comma).get(num).get(close);

		Assembly out = coord.bestMatch(new TokenAssembly("(23.4, 34.5, 45.6)"));

		Stack<?> s = out.getStack();
		while (!s.empty()) {
			System.out.println(s.pop());
		}
	}
}