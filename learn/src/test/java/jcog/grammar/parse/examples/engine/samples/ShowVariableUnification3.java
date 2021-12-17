package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.Structure;
import jcog.grammar.parse.examples.engine.Variable;

/**
 * Show a variable unifying with a structure and then another 
 * variable.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowVariableUnification3 {
	/**
	 * Show a variable unifying with a structure and then another 
	 * variable.
	 */
	public static void main(String[] args) {

		

		Structure denver = new Structure("denver");
		Structure altitude = new Structure(5280);
		Structure city = new Structure("city", new Structure[] { denver, altitude });

		Variable a = new Variable("A");
		Variable b = new Variable("B");

		a.unify(b); 
		
		a.unify(city); 
		System.out.println("a = " + a);
		System.out.println("b = " + b);

		Variable p = new Variable("P");
		p.unify(city);
		Variable q = new Variable("Q");
		q.unify(p); 
		

		System.out.println("p = " + p);
		System.out.println("q = " + q);

		

		Variable x = new Variable("X");
		Variable y = new Variable("Y");
		x.unify(denver);
		y.unify(denver);

		System.out.println("Result of x.unify(y): " + x.unify(y));
	}
}