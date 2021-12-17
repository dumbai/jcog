package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.tokens.Literal;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;

import java.util.HashSet;
import java.util.Set;

/**
 * This class shows that a <code>Sequence</code> match is equivalent to a series
 * of <code>match()</code> calls.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
public class ShowSequenceManual {
	/**
	 * Show that a <code>Sequence</code> match is equivalent to a series of
	 * <code>match()</code> calls.
	 */
	public static void main(String[] args) {

		Parser hello = new Literal("Hello");
		Parser world = new Literal("world");
		Parser bang = new Symbol('!');

		Parser s = new Seq().get(hello).get(world).get(bang);

		Set<Assembly> v = new HashSet<>();
		v.add(new TokenAssembly("Hello world!"));

		System.out.println(bang.match(world.match(hello.match(v))));

		System.out.println(s.match(v));
	}
}
