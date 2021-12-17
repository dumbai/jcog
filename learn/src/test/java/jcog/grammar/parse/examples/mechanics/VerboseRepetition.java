package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;

import java.util.Set;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * The <code>match()</code> method of this class prints the collection of
 * assemblies it receives, and the new collection it creates.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
class VerboseRepetition extends Repetition {
	/**
	 * Constructs a VerboseRepetition of the given parser.
	 * 
	 * @param parser
	 *            the parser to repeat
	 * 
	 * @return a VerboseRepetiton that will match the given parser repeatedly in
	 *         successive matches
	 */
    VerboseRepetition(Parser p) {
		super(p);
	}

	/**
	 * Constructs a VerboseRepetition of the given parser with the given name.
	 * 
	 * @param parser
	 *            the parser to repeat
	 * 
	 * @param name
	 *            a name to be known by
	 * 
	 * @return a VerboseRepetiton that will match the given parser repeatedly in
	 *         successive matches
	 */
    VerboseRepetition(Parser p, String name) {
		super(p, name);
	}

	/**
	 * Just a verbose version of <code>Repetition.match()
	* </code>.
	 */
	public Set<Assembly> match(Set<Assembly> in) {
		System.out.println(" in: " + in);
		Set<Assembly> out = super.match(in);
		System.out.println("out: " + out);
		return out;
	}
}