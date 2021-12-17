package jcog.grammar.parse.examples.regular;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.chars.CharacterAssembly;
import jcog.grammar.parse.chars.Letter;

/**
 * Show how to use the <code>RegularParser</code> class.
 *  
 * @author Steven J. Metsker
 *
 * @version 1.0 								       
 */

public class ShowRegularParser {
	/**
	 * Show some examples of matching regular expressions.
	 */
	public static void main(String[] args) throws RegularExpressionException {

		
		Parser aStar = RegularParser.value("a*");
		showMatch(aStar, "");
		showMatch(aStar, "a");
		showMatch(aStar, "aa");
		showMatch(aStar, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

		
		Parser abStar = RegularParser.value("(a|b)*");
		showMatch(abStar, "aabbaabaabba");
		showMatch(abStar, "aabbaabaabbaZ");

		
		showMatch(RegularParser.value("a*a*"), "aaaa");
		showMatch(RegularParser.value("a|bc"), "bc");
		showMatch(RegularParser.value("a|bc|d"), "bc");

		
		Parser L = new Letter();
		Parser L4 = new Seq("LLLL").get(L).get(L).get(L).get(L);
		showMatch(L4, "java");
		showMatch(L4, "joe");
		showMatch(new Repetition(L), "coffee");
	}

	/*
	 * Just a little help for main().
	 */
	private static void showMatch(Parser p, String s) {
		System.out.print(p);
		Assembly a = p.completeMatch(new CharacterAssembly(s));
		if (a != null) {
			System.out.print(" matches ");
		} else {
			System.out.print(" does not match ");
		}
		System.out.println(s);
	}
}