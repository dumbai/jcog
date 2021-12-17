package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Alternation;
import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.tokens.Literal;
import jcog.grammar.parse.tokens.TokenAssembly;

import java.util.HashSet;
import java.util.Set;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * This class shows the basics of using an alternation.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
public class ShowAlternationBasic {
	/**
	 * Just show the basics of alternation.
	 */
	public static void main(String[] args) {

		Alternation a = new Alternation();
		a.get(new Literal("steaming"));
		a.get(new Literal("hot"));

		Set<Assembly> v = new HashSet<>();
		v.add(new TokenAssembly("hot hot steaming hot coffee"));

		System.out.println("a match: \n" + a.match(v));

		System.out.println("a* match: \n" + new Repetition(a).match(v));
	}
}