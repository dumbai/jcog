package jcog.grammar.parse.examples.regular;

import jcog.grammar.parse.Alternation;
import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.Parser;

/**
 * Pop two parsers from the stack and push a new <code>
 * Alternation</code> of them.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */

class OrAssembler implements IAssembler {

	/**
	 * Pop two parsers from the stack and push a new
	 * <code>Alternation</code> of them.
	 * 
	 * @param   Assembly   the assembly whose stack to use
	 */
	public void accept(Assembly a) {
		Object top = a.pop();
		Alternation alt = new Alternation();
		alt.get((Parser) a.pop());
		alt.get((Parser) top);
		a.push(alt);
	}
}
