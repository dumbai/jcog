package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.tokens.TokenAssembly;
import jcog.grammar.parse.tokens.Word;

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
 * This class shows that a repetition object creates more
 * than one match from a single assembly.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowRepetitionMatch {
	/**
	 * This class shows that a repetition object creates more
	 * than one match from a single assembly.
	 */
	public static void main(String[] args) {

		Parser p = new Repetition(new Word());
		Set<Assembly> v = new HashSet<>();
		String s = "How many cups are in a gallon?";
		v.add(new TokenAssembly(s));
		System.out.println(p.match(v));
	}
}
