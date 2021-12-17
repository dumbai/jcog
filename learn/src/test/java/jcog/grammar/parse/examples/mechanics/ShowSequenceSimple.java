package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.tokens.Literal;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;

import java.util.HashSet;
import java.util.Set;

/**
 * This class uses a <code>VerboseSequence</code> to show the progress a
 * sequence makes during matching.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
public class ShowSequenceSimple {
	/**
	 * Using a <code>VerboseSequence</code>, show the progress a sequence makes
	 * during matching.
	 */
	public static void main(String[] args) {

		Parser hello = new Literal("Hello");
		Parser world = new Literal("world");
		Parser bang = new Symbol('!');

		Parser s = new VerboseSequence().get(hello).get(world).get(bang);

		Set<Assembly> v = new HashSet<>();
		v.add(new TokenAssembly("Hello world!"));
		s.match(v);
	}
}
