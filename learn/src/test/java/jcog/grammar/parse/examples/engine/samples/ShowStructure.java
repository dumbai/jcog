package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.Structure;

/**
 * This class shows a simple structure.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowStructure {
	/**
	 * Show a simple structure.
	 */
	public static void main(String[] args) {
		Structure denver = new Structure("denver");
		Structure altitude = new Structure(5280);
		Structure city = new Structure("city", new Structure[] { denver, altitude });
		System.out.println(city);
	}
}