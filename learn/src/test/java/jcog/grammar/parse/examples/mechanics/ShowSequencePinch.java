package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;
import jcog.grammar.parse.tokens.Word;

import java.util.HashSet;
import java.util.Set;

/**
 * This class shows that a <code>Sequence</code> match may
 * widen and then narrow the state of a match.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowSequencePinch {
	/**
	 * Show that a <code>Sequence</code> match may widen and then
	 * narrow the state of a match.
	 */
	public static void main(String[] args) {

		Parser s = new VerboseSequence().get(new Repetition(new Word())).get(new Symbol('!'));

		Set<Assembly> v = new HashSet<>();
		v.add(new TokenAssembly("Hello world!"));
		s.match(v);
	}
}
