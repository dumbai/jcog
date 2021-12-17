package jcog.grammar.parse.examples.coffee;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.tokens.Token;

/**
 * This assembler pops a string, and sets the target coffee's
 * roast to this string.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
class RoastAssembler implements IAssembler {
	/**
	 * Pop a string, and set the target coffee's roast to this
	 * string.
	 *
	 * @param   Assembly   the assembly to work on
	 */
	public void accept(Assembly a) {
		Token t = (Token) a.pop();
		Coffee c = (Coffee) a.getTarget();
		c.setRoast(t.sval().trim());
	}
}
