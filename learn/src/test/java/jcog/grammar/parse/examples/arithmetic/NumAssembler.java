package jcog.grammar.parse.examples.arithmetic;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.tokens.Token;

/**
 * Replace the top token in the stack with the token's
 * Double value.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
public class NumAssembler implements IAssembler {
	/**
	 * Replace the top token in the stack with the token's
	 * Double value.
	 * 
	 * @param   Assembly   the assembly whose stack to use
	 */
	public void accept(Assembly a) {
		Token t = (Token) a.pop();
		a.push(t.nval());
	}
}
