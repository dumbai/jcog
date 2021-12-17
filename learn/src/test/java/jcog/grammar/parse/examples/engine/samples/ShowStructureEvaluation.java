package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.Atom;

/**
 * Show the evaluation of a structure.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowStructureEvaluation {
	/**
	 * Show the evaluation of a structure.
	 */
	public static void main(String[] args) {
		Atom a = new Atom("maine");
		Object o = a.eval();
		System.out.println(String.valueOf(a) + o);
	}
}