package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.examples.arithmetic.ArithmeticParser;
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
 * This class shows the complete results of matching an arithmetic expression
 * parser against an expression.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
public class ShowSequenceLeftovers {
	/**
	 * Show the complete results of matching an arithmetic expression parser
	 * against an expression.
	 */
	public static void main(String[] args) {

		Set<Assembly> v = new HashSet<>();
		v.add(new TokenAssembly("3 * 4 + 5"));

		System.out.println(ArithmeticParser.start().match(v));
	}
}