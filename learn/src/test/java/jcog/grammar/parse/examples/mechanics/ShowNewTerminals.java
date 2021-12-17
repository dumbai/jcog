package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Alternation;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * Show the use of new subclasses of <code>Terminal</code>.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowNewTerminals {
	/**
	 * Show the use of new subclasses of <code>Terminal</code>.
	 */
	public static void main(String[] args) {

		/*  term     = variable | known;
		 *  variable = UppercaseWord;
		 *  known    = LowercaseWord;
		 */

		Parser variable = new UppercaseWord();
		Parser known = new LowercaseWord();

		Parser term = new Alternation().get(variable).get(known);

		

		variable.put((IAssembler) a -> {
			Object o = a.pop();
			a.push("VAR(" + o + ')');
		});

		known.put((IAssembler) a -> {
			Object o = a.pop();
			a.push("KNOWN(" + o + ')');
		});

		

		System.out.println(new Repetition(term).bestMatch(new TokenAssembly("member X republican democrat")));
	}
}
