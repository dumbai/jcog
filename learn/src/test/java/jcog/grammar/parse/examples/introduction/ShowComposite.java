package jcog.grammar.parse.examples.introduction;

import jcog.grammar.parse.Alternation;
import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.tokens.Literal;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * Show how to create a composite parser.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowComposite {
	/**
	 * Just a little demo.
	 */
	public static void main(String[] args) {

		Alternation adjective = new Alternation();
		adjective.get(new Literal("steaming"));
		adjective.get(new Literal("hot"));

		Seq good = new Seq();
		good.get(new Repetition(adjective));
		good.get(new Literal("coffee"));

		String s = "hot hot steaming hot coffee";
		Assembly a = new TokenAssembly(s);
		System.out.println(good.bestMatch(a));

	}
}
