package jcog.grammar.parse.examples.sling;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * Signals that a given string is not the name of a known
 * variable.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
class UnassignedVariableException extends RuntimeException {
	/**
	 * Constructs a <code>UnrecognizedVariableException</code> 
	 * with no detail message.
	 */
    UnassignedVariableException() {
		super();
	}

	/**
	 * Constructs a <code>UnrecognizedVariableException</code>
	 * with the specified detail message. 
	 *
	 * @param String the detail message.
	 */
    UnassignedVariableException(String s) {
		super(s);
	}
}