package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.Fact;

/**
 * This class shows the construction of a couple of facts.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowFacts {
	/**
	 * Shows the construction of a couple of facts.
	 */
	public static void main(String[] args) {
		Fact d = new Fact("city", new Fact[] { new Fact("denver"), new Fact(5280) });

		Fact j = new Fact("city", "jacksonville", 8);

		System.out.println(d + "\n" + j);
	}
}