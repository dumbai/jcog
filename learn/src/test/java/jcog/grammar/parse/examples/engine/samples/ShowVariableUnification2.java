package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.Structure;
import jcog.grammar.parse.examples.engine.Variable;

/**
 * Show a variable unifying with a structure and then 
 * another variable.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowVariableUnification2 {
	/**
	 * Show a variable unifying with a structure and then another 
	 * variable.
	 */
	public static void main(String[] args) {

		Variable x = new Variable("X");
		Variable y = new Variable("Y");
		Structure denver = new Structure("denver");

		x.unify(denver);
		x.unify(y);

		System.out.println("X = " + x);
		System.out.println("Y = " + y);
	}
}