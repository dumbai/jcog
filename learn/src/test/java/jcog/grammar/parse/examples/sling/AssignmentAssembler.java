package jcog.grammar.parse.examples.sling;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;

/**
 * Pops a function and a variable, constructs an <code>
 * AssignFunctionCommand</code> from these terms, and
 * pushes the command. For example, after parsing:
 *
 * <blockquote><pre>
 *     y = sin(x);
 * </pre></blockquote>
 *
 * this assembler expects the variable <code>y</code> and
 * the function <code>sin(x)</code> to be on the stack, and
 * constructs a command from these objects.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
class AssignmentAssembler implements IAssembler {
	/**
	 * Pops a function and a variable, constructs an <code>
	 * AssignFunctionCommand</code> from these terms, and
	 * pushes the command.
	 *
	 * @param  Assembly  the assembly to work on
	 */
	public void accept(Assembly a) {
		SlingFunction f = (SlingFunction) a.pop();
		Variable v = (Variable) a.pop();
		a.push(new AssignFunctionCommand(v, f));
	}
}
