package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.Structure;
import jcog.grammar.parse.examples.engine.Term;
import jcog.grammar.parse.examples.engine.Variable;

/**
 * Show two structures unifying.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowStructureUnification {
	/**
	 * Show two structures unifying.
	 */
	public static void main(String[] args) {

		
		Structure denver = new Structure("denver");
		Structure altitude = new Structure(5280);
		Structure city = new Structure("city", new Structure[] { denver, altitude });

		
		Variable name = new Variable("Name");
		Variable alt = new Variable("Altitude");
		Structure vCity = new Structure("city", new Term[] { name, alt });

		
		System.out.println(city);
		System.out.println(vCity);

		
		vCity.unify(city);

		System.out.println("\n    After unifying: \n");

		System.out.println("Name = " + name);
		System.out.println("Alt  = " + alt);

	}
}