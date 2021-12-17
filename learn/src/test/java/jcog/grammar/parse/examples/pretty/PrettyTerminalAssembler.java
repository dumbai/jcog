package jcog.grammar.parse.examples.pretty;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.tokens.Token;

/**
 * Replace a <code>Token</code> object on the stack with a 
 * <code>TerminalNode</code> that holds the token's value.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
class PrettyTerminalAssembler implements IAssembler {
	/**
	 * Replace a <code>Token</code> object on the stack with a 
	 * <code>TerminalNode</code> that holds the token's value.
	 *
	 * @param   Assembly   the assembly to work on
	 */
	public void accept(Assembly a) {
		Token t = (Token) a.pop();
		a.push(new TerminalNode(t.value()));
	}
}
