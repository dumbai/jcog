package jcog.grammar.parse.examples.sling;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;

/** 
 * Pops a function, and pushes an <code>AddFunctionCommand
 * </code> object. This command, when executed, will create
 * a renderable function. The renderable function includes 
 * the function that we pop now and the value of the "nLine"
 * variable.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
class PlotAssembler implements IAssembler {
	/**
	 * Pop a function, and push a command that will, at 
	 * execution time, create a renderable function. The
	 * renderable function includes the popped function and
	 * the value of the "nLine" variable.
	 *
	 * @param  Assembly  the assembly to work on
	 */
	public void accept(Assembly a) {
		SlingTarget target = (SlingTarget) a.getTarget();
		SlingFunction f = (SlingFunction) a.pop();
		a.push(new AddFunctionCommand(target.getRenderables(), f, target.nLine()));
	}
}
