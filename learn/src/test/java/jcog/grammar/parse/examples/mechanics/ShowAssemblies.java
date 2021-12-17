package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.Terminal;
import jcog.grammar.parse.chars.CharacterAssembly;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * This class shows that a repetition of an object of the
 * <code>Terminal</code> base class will match an entire
 * assembly. This example brings out the fact that a
 * <code>TokenAssembly</code> returns tokens as elements,
 * and <code>CharacterAssembly</code> returns individual
 * characters as elements.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowAssemblies {
	/**
	 * This class shows that a repetition of an object of the
	 * <code>Terminal</code> base class will match an entire
	 * assembly. 
	 *
	 * @param   args   ignored
	 */
	public static void main(String[] args) {

		Parser p = new Repetition(new Terminal());

		String s = "She's a 'smart cookie'!";
		System.out.println(p.bestMatch(new TokenAssembly(s)));
		System.out.println(p.bestMatch(new CharacterAssembly(s)));
	}
}
