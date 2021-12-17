package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.Atom;
import jcog.grammar.parse.examples.engine.Comparison;
import jcog.grammar.parse.examples.engine.Query;

/**
 * Show a couple of comparisons.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowComparison {
	/**
	 * Show a couple of comparisons.
	 */
	public static void main(String[] args) {
		Atom alt1 = new Atom(5280);
		Atom alt2 = new Atom(19);

		Query q1 = new Query(null, 
				new Comparison(">", alt1, alt2));

		System.out.println(q1 + " : " + q1.canFindNextProof());

		Query q2 = new Query(null, new Comparison(">", new Atom("denver"), new Atom("richmond")));

		System.out.println(q2 + " : " + q2.canFindNextProof());
	}
}