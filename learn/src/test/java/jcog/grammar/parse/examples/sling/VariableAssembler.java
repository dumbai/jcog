package jcog.grammar.parse.examples.sling;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.tokens.Token;

/**
 * This class looks up a variable by name, using the word on 
 * an assembly's stack, and pushes a <code>Variable</code> of 
 * that name.
 * <p>
 * This class expects an assembly's target to be a <code>
 * SlingPlot</code> object. The target has a "scope", which is 
 * a collection of variables organized by name. When this 
 * assembler works on an assembly, it pops a name from the 
 * stack, looks up a variable in the scope using the name, and 
 * pushes the variable onto the stack. This lookup creates the 
 * variable in the scope if the scope does not already contain 
 * a variable of that name.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
class VariableAssembler implements IAssembler {
	/**
	 * Pop the name of a variable, lookup the variable in the
	 * target's scope, and push the variable.
	 */
	public void accept(Assembly a) {
		SlingTarget target = (SlingTarget) a.getTarget();
		Token t = (Token) a.pop();
		a.push(target.lookup(t.sval()));
	}
}
