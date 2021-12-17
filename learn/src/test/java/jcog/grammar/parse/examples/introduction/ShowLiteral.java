package jcog.grammar.parse.examples.introduction;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.tokens.*;

/**
 * Show a parser that recognizes an int declaration.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowLiteral {
	/**
	 * Just a little demo.
	 */
	public static void main(String[] args) {
		Seq s = new Seq();
		s.get(new Literal("int"));
		s.get(new Word());
		s.get(new Symbol('='));
		s.get(new Num());
		s.get(new Symbol(';'));

		Assembly a = s.completeMatch(new TokenAssembly("int i = 3;"));

		System.out.println(a);
	}
}