package jcog.grammar.parse.examples.regular;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.chars.SpecificChar;

/**
 * Pop a <code>Character</code> from the stack and push a 
 * <code>SpecificChar</code> parser in its place.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
class CharAssembler implements IAssembler {
	/**
	 * Pop a <code>Character</code> from the stack and push a 
	 * <code>SpecificChar</code> interpeter in its place.
	 * 
	 * @param   Assembly   the assembly whose stack to use
	 */
	public void accept(Assembly a) {
		a.push(new SpecificChar((Character) a.pop()));
	}
}
