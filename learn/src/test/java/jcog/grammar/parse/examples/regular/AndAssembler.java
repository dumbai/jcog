package jcog.grammar.parse.examples.regular;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Seq;

/**
 * Pop two Parsers from the stack and push a new <code>
 * Sequence</code> of them.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
class AndAssembler implements IAssembler {
	/**
	 * Pop two parsers from the stack and push a new 
	 * <code>Sequence</code> of them.
	 * 
	 * @param   Assembly   the assembly whose stack to use
	 */
	public void accept(Assembly a) {
		Object top = a.pop();
		Seq s = new Seq();
		s.get((Parser) a.pop());
		s.get((Parser) top);
		a.push(s);
	}
}
