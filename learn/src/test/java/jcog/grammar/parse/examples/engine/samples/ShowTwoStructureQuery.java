package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.*;

/**
 * Show a two-structure query.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowTwoStructureQuery {
	/**
	 * Show a two-structure query.
	 */
	public static void main(String[] args) {
		Program p = ShowProgram.altitudes(); 

		Variable name = new Variable("Name");
		Variable alt = new Variable("Alt");
		Atom fiveThou = new Atom(5000);

		Query q = new Query(p, new Structure[] { new Structure("city", new Term[] { name, alt }), new Comparison(">", alt, fiveThou) });

		while (q.canFindNextProof()) {
			System.out.println(name + " " + alt);
		}
	}
}