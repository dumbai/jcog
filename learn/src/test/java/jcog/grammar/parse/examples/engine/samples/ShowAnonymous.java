package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.*;

/**
 * Show how to use an anonymous variable.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowAnonymous {
	/**
	 * Show how to use an anonymous variable.
	 */
	public static void main(String[] args) {

		
		Fact m1 = new Fact("marriage", new Object[] {1, "balthasar", "grimelda", 14560512, 14880711});

		
		Fact m257 = new Fact("marriage", new Object[] {257, "kevin", "karla", 19790623, "present" });

		Program p = new Program();
		p.addAxiom(m1);
		p.addAxiom(m257);

		

		Variable id = new Variable("Id");
		Variable hub = new Variable("Hub");
		Anonymous a = new Anonymous();

		Query q = new Query(p, new Structure("marriage", new Term[] { id, hub, a, a, a }));

		
		System.out.println("Program: \n" + p + '\n');
		System.out.println("Query:   \n" + q + '\n');
		System.out.println("Results: \n");

		while (q.canFindNextProof()) {
			System.out.println("Id: " + id + ", Husband: " + hub);
		}
	}
}