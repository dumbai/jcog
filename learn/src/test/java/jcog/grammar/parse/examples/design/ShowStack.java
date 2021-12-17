package jcog.grammar.parse.examples.design;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.tokens.Num;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * Show how to use an assembly's stack.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowStack {
	/**
	 * Show how to use an assembly's stack.
	 */
	public static void main(String[] args) {

		Parser p = new Repetition(new Num());
		Assembly a = p.completeMatch(new TokenAssembly("2 4 6 8"));
		System.out.println(a);
	}
}