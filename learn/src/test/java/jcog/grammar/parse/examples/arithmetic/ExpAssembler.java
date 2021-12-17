package jcog.grammar.parse.examples.arithmetic;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;

/**
 * Pop two numbers from the stack and push the result of
 * exponentiating the lower number to the upper one.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
class ExpAssembler implements IAssembler {
	/**
	 * Pop two numbers from the stack and push the result of
	 * exponentiation the lower number to the upper one.
	 * 
	 * @param   Assembly   the assembly whose stack to use
	 */
	public void accept(Assembly a) {
		Double d1 = (Double) a.pop();
		Double d2 = (Double) a.pop();
		Double d3 = Math.pow(d2, d1);
		a.push(d3);
	}
}