package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Seq;

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
 * assemblies it receives, and each new collection it creates.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
class VerboseSequence extends Seq {
	/**
	 * Constructs a nameless VerboseSequence.
	 */
    VerboseSequence() {
		super();
	}

	/**
	 * Constructs a Sequence with the given name.
	 * 
	 * @param name
	 *            a name to be known by
	 */
    VerboseSequence(String name) {
		super(name);
	}

	/**
	 * Just a verbose version of <code>Sequence.match()</code>.
	 */
	public Set<Assembly> match(Set<Assembly> inputState) {
		Set<Assembly> finalState = inputState;
		System.out.println(finalState); 
		for (Parser p : subparsers) {
			finalState = p.matchAndAssemble(finalState);
			if (finalState.isEmpty()) {
				return finalState;
			}
			System.out.println(finalState); 
		}
		return finalState;
	}
}