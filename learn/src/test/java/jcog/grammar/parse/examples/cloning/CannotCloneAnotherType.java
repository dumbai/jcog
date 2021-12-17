package jcog.grammar.parse.examples.cloning;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * This class will not compile; it just shows that an object
 * cannot send clone() to another type of object.
 *
 * @author Steven J. Metsker
 * @version 1.0
 */
public class CannotCloneAnotherType {
	/**
	 * Just a demo, this will not compile.
	 */
	public static void main(String[] args) {
		CannotCloneAnotherType ccat = new CannotCloneAnotherType();

		try {
			ccat.clone(); 
		} catch (CloneNotSupportedException e) {
			e.printStackTrace(); 
		}

		Integer i = 42;
		System.out.println(i);
		
	}
}