package jcog.grammar.parse.examples.sling;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;

/**
 * Pop the assembly, and push a new function that multiplies
 * this function by -1.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
class NegativeAssembler implements IAssembler {
	/**
	 * Push the point (-1, -1), and ask an <code>Arithmetic</code> 
	 * "times" object to work on the assembly. The arithmetic 
	 * function will pop the point and will pop whatever function 
	 * was on top of the stack previously. The arithmetic function 
	 * will then form a multiplication function from these 
	 * elements and push this new function. 
	 *
	 * @param  Assembly  the assembly to work on
	 */
	public void accept(Assembly a) {
		
		a.push(new Point(-1, -1));
		new FunctionAssembler(new Arithmetic('*')).accept(a);
	}
}
