package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.*;

/**
 * Show a relational join in a coffee database.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowJoin {
	/**
	 * Return a small database of coffee types.
	 *
	 * @return a small database of coffee types.
	 */
	private static Program coffee() {
		Fact[] facts = {

		new Fact("coffee", new Object[] { "Launch Mi", "french", "kenya", 6.95}),

		new Fact("coffee", new Object[] { "Simple Best", "regular", "columbia", 5.95}),

		new Fact("coffee", new Object[] { "Revit", "italian", "guatemala", 7.95}),

		new Fact("coffee", new Object[] { "Brimful", "regular", "kenya", 6.95}),

		new Fact("coffee", new Object[] { "Smackin", "french", "columbia", 7.95}) };

		Program p = new Program();
		for (int i = 0; i < facts.length; i++) {
			p.addAxiom(facts[i]);
		}
		return p;

	}

	/**
	 * Return a small database of coffee customers.
	 *
	 * @return a small database of coffee customers.
	 */
	private static Program customer() {
		Fact[] facts = {

		new Fact("customer", new Object[] { "Jim Johnson", 2024}),

		new Fact("customer", new Object[] { "Jane Jerrod", 2077}),

		new Fact("customer", new Object[] { "Jasmine Jones", 2093}) };

		Program p = new Program();
		for (int i = 0; i < facts.length; i++) {
			p.addAxiom(facts[i]);
		}
		return p;

	}

	/**
	 * Show a relational join in a coffee database.
	 */
	public static void main(String[] args) {
		Program p = new Program();
		p.append(coffee());
		p.append(customer());
		p.append(order());

		Variable name = new Variable("Name");
		Variable custNum = new Variable("CustNum");
		Variable type = new Variable("Type");
		Variable pounds = new Variable("Pounds");

		Structure s1 = new Structure("customer", new Term[] { name, custNum });

		Structure s2 = new Structure("order", new Term[] { custNum, type, pounds });

		
		Query q = new Query(p, new Structure[] { s1, s2 });

		while (q.canFindNextProof()) {
			System.out.println("Customer:     " + name);
			System.out.println("Type:         " + type);
			System.out.println("Pounds/Month: " + pounds);
			System.out.println();
		}
	}

	/**
	 * Return a small database of coffee orders.
	 *
	 * @return a small database of coffee orders.
	 */
	private static Program order() {
		Fact[] facts = {

		new Fact("order", new Object[] {2024, "Simple Best", 1}),

		new Fact("order", new Object[] {2077, "Launch Mi", 3}),

		new Fact("order", new Object[] {2077, "Smackin", 3}),

		new Fact("order", new Object[] {2093, "Brimful", 2})

		};

		Program p = new Program();
		for (int i = 0; i < facts.length; i++) {
			p.addAxiom(facts[i]);
		}
		return p;

	}
}