package jcog.grammar.parse.examples.preface;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.Terminal;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * This is a "Hello world" program. Once you get this 
 * working on your computer, you can get any example in this 
 * book to work.
 */
public class ShowHello {
	/**
	 * Create a little parser and use it to recognize 
	 * "Hello world!".
	 */
	public static void main(String[] args) {
		Terminal t = new Terminal();
		Repetition r = new Repetition(t);

		Assembly in = new TokenAssembly("Hello world!");
		Assembly out = r.completeMatch(in);

		System.out.println(out.getStack());
	}
}
