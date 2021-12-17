package jcog.grammar.parse.examples.robot;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.tokens.Token;

/**
 * Sets an assembly's target to be a <code>PickCommand
 * </code> and note its location.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
class PickAssembler implements IAssembler {
	/**
	 * Sets an assembly's target to be a 
	 * <code>PickCommand</code> object and note its location.
	 *
	 * @param  Assembly  the assembly to work on
	 */
	public void accept(Assembly a) {
		PickCommand pc = new PickCommand();
		Token t = (Token) a.pop();
		pc.setLocation(t.sval());
		a.setTarget(pc);
	}
}
