package jcog.grammar.parse.examples.track;

import jcog.grammar.parse.*;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;
import jcog.grammar.parse.tokens.Word;

/**
 * Show some examples of using a <code>Track</code>.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0 
 */
public class ShowTrack {
	/*
	 * Return a parser that will recognize a list, for the 
	 * grammar:
	 *
	 *     list       = '(' contents ')';
	 *     contents   = empty | actualList;
	 *     actualList = Word (',' Word)*;
	 */
	private static Parser list() {

        Parser empty = new Empty();

        Parser commaWord = new SeqEx().get(new Symbol(',').ok()).get(new Word());

        Parser actualList = new Seq().get(new Word()).get(new Repetition(commaWord));

        Parser contents = new Alternation().get(empty).get(actualList);

        Parser list = new SeqEx().get(new Symbol('(').ok()).get(contents).get(new Symbol(')').ok());

		return list;
	}

	/**
	 * Show some examples of using a <code>Track</code>.
	 */
	public static void main(String[] args) {

		Parser list = list();

        System.out.println("Using parser: " + list);
        String[] test = {"()", "(pilfer)", "(pilfer, pinch)", "(pilfer, pinch, purloin)", "(pilfer, pinch,, purloin)", "(", "(pilfer", "(pilfer, ", "(, pinch, purloin)", "pilfer, pinch"};
        for (int i = 0; i < test.length; i++) {
			System.out.println("---\ntesting: " + test[i]);
			TokenAssembly a = new TokenAssembly(test[i]);
			try {
				Assembly out = list.completeMatch(a);
				if (out == null) {
					System.out.println("list.completeMatch() returns null");
				} else {
					Object s = list.completeMatch(a).getStack();
					System.out.println("Ok, stack is: " + s);
				}
			} catch (TrackException e) {
				System.out.println(e.getMessage());
			}
		}
	}
}