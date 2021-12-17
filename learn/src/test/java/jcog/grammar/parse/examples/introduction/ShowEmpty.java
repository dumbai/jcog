package jcog.grammar.parse.examples.introduction;

import jcog.grammar.parse.*;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;
import jcog.grammar.parse.tokens.Word;

/**
 * Show how to put the <code>Empty</code> class to good use.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowEmpty {
	/**
	 * Show the value of the <code>Empty</code> parser, using a 
	 * list parser.
	 * 
	 * A list, in this example, is a pair of brackets around some
	 * contents. The contents may be empty, or may be an actual
	 * list. An actual list is one or more words, separated by
	 * commas. That is, an actual list is a word followed by
	 * zero or more sequences of (comma, word).
	 */
	public static void main(String[] args) {

        Parser empty = new Empty();

        Parser commaTerm = new Seq().get(new Symbol(',').ok()).get(new Word());

        Parser actualList = new Seq().get(new Word()).get(new Repetition(commaTerm));

        Parser contents = new Alternation().get(empty).get(actualList);

        Parser list = new Seq().get(new Symbol('[').ok()).get(contents).get(new Symbol(']').ok());

		String[] test = { "[die_bonder_2, oven_7, wire_bonder_3, mold_1]", "[]", "[mold_1]" };
		for (int i = 0; i < test.length; i++) {
			TokenAssembly a = new TokenAssembly(test[i]);
			System.out.println(list.completeMatch(a).getStack());
		}
	}
}