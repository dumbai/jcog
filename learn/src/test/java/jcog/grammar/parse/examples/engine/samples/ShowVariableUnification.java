package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.Structure;
import jcog.grammar.parse.examples.engine.Variable;

/**
 * Show a variable unifying.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowVariableUnification {
	/**
	 * Show a variable unifying.
	 */
	public static void main(String[] args) {

		Variable x = new Variable("X");
		Structure denver = new Structure("denver");
		x.unify(denver);
		System.out.println(x);
	}
}