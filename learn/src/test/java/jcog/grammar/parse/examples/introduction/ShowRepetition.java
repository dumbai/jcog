package jcog.grammar.parse.examples.introduction;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.tokens.TokenAssembly;
import jcog.grammar.parse.tokens.Word;

import java.util.HashSet;
import java.util.Set;

/**
 * Show that a <code>Repetition</code> object creates 
 * multiple interpretations.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowRepetition {
	/**
	 * Just a little demo.
	 */
	public static void main(String[] args) {
		String s = "steaming hot coffee";
		Assembly a = new TokenAssembly(s);
		Parser p = new Repetition(new Word());

		Set<Assembly> v = new HashSet<>();
		v.add(a);

		System.out.println(p.match(v));
	}
}
