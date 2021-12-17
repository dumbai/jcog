package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Alternation;
import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;
import jcog.grammar.parse.tokens.Word;

import java.util.HashSet;

/**
 * This class shows that an alternation can, by itself, 
 * create a collection of possible matches.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowAlternationSet {
	/**
	 * Show that an alternation can, by itself, create a 
	 * collection of possible matches.
	 */
	public static void main(String[] args) {

		

		Alternation assignment = new Alternation();

		assignment.get(new Seq().get(new Word()).get(new Symbol('=').ok()).get(assignment));
		assignment.get(new Word());

		String s = "i = j = k = l = m";

		HashSet<Assembly> v = new HashSet<>();
		v.add(new TokenAssembly(s));

		System.out.println(assignment.match(v));
	}
}