package jcog.grammar.parse.examples.regular;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;

/**
 * Pop a parser from the stack and push a new <code>
 * Repetition</code> of it.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */

class StarAssembler implements IAssembler {

	/**
	 * Pop a parser from the stack and push a new <code>
	 * Repetition</code> of it.
	 * 
	 * @param   Assembly   the assembly whose stack to use
	 */
	public void accept(Assembly a) {
		a.push(new Repetition((Parser) a.pop()));
	}
}
