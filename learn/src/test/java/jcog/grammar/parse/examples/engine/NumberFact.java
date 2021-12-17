package jcog.grammar.parse.examples.engine;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * A NumberFact is a fact with a Number as its functor.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
public class NumberFact extends Atom implements ArithmeticTerm {
	/**
	 * Contructs a number fact from the double.
	 * 
	 * @param double a number to wrap as an atom
	 */
	public NumberFact(double d) {
		this((Number)d);
	}

	/**
	 * Contructs a number fact from the Numer.
	 * 
	 * @param Number
	 *            a number to wrap as an atom
	 */
    private NumberFact(Number functor) {
		super(functor);
	}
}