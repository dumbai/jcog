package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.*;

/**
 * Show the construction and use of a simple program.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowProgram {
	/**
	 * Return a small database of cities and their altitudes.
	 */
	public static Program altitudes() {
		Fact[] facts = { new Fact("city", "abilene", 1718), new Fact("city", "addis ababa", 8000), new Fact("city", "denver", 5280), new Fact("city", "flagstaff", 6970),
				new Fact("city", "jacksonville", 8), new Fact("city", "leadville", 10200), new Fact("city", "madrid", 1305), new Fact("city", "richmond", 19),
				new Fact("city", "spokane", 1909), new Fact("city", "wichita", 1305) };

		Program p = new Program();
		for (int i = 0; i < facts.length; i++) {
			p.addAxiom(facts[i]);
		}
		return p;

	}

	/**
	 * Show the construction and use of a simple program.
	 */
	public static void main(String[] args) {
		Program p = altitudes();

		Variable name = new Variable("Name");
		Variable height = new Variable("Height");
		Structure s = new Structure("city", new Term[] { name, height });
		Query q = new Query(p, s);

		while (q.canFindNextProof()) {
			System.out.println(name + " is about " + height + " feet above sea level.");
		}

	}
}