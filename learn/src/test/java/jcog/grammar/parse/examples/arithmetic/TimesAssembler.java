package jcog.grammar.parse.examples.arithmetic;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;

/**
 * Pop two numbers from the stack and push the result of
 * multiplying the top number by the one below it.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
public class TimesAssembler implements IAssembler {
	/**
	 * Pop two numbers from the stack and push the result of
	 * multiplying the top number by the one below it.
	 * 
	 * @param   Assembly   the assembly whose stack to use
	 */
	public void accept(Assembly a) {
		Double d1 = (Double) a.pop();
		Double d2 = (Double) a.pop();
		Double d3 = d2 * d1;
		a.push(d3);
	}
}