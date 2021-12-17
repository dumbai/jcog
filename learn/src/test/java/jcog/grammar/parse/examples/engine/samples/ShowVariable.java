package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.Structure;
import jcog.grammar.parse.examples.engine.Term;
import jcog.grammar.parse.examples.engine.Variable;

/**
 * This class shows some variables.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowVariable {
	/**
	 * Show some variables.
	 */
	public static void main(String[] args) {

		Variable name = new Variable("Name");
		Variable alt = new Variable("Altitude");
		Structure vCity = new Structure("city", new Term[] { name, alt });
		System.out.println(vCity);
	}
}