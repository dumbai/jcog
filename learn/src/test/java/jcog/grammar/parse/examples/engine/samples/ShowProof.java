package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.*;

/**
 * Show a simple query proving itself.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowProof {
	/**
	 * Return a small database of shipping charges.
	 */
	private static Program charges() {

		Fact[] facts = { new Fact("charge", "athens", 23), new Fact("charge", "sparta", 13), new Fact("charge", "milos", 17) };

		return new Program(facts);
	}

	/**
	 * Return a small database of customers.
	 */
	private static Program customers() {

		Fact[] facts = { new Fact("customer", "Marathon Marble", "sparta"), new Fact("customer", "Acropolis Construction", "athens"), new Fact("customer", "Agora Imports", "sparta") };

		return new Program(facts);
	}

	/**
	 * Show a simple query proving itself.
	 */

	public static void main(String[] args) {
		Program p = new Program();
		p.append(charges());
		p.append(customers());

		System.out.println("Program:");
		System.out.println(p);

		Variable city = new Variable("City");
		Variable fee = new Variable("Fee");
		Variable name = new Variable("Name");

		Structure s1 = new Structure("charge", new Term[] { city, fee });

		Structure s2 = new Structure("customer", new Term[] { name, city });

		
		Query q = new Query(p, new Structure[] { s1, s2 });

		System.out.println("\nQuery:");
		System.out.println(q);

		System.out.println("\nProofs:");
		while (q.canFindNextProof()) {
			System.out.println("City: " + city);
			System.out.println("Fee:  " + fee);
			System.out.println("Name: " + name);
			System.out.println();
		}
	}
}