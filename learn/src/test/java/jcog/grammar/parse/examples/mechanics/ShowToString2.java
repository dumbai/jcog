package jcog.grammar.parse.examples.mechanics;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

import jcog.grammar.parse.examples.robot.RobotParser;

/**
 * Show how a moderately complex composite parser prints
 * itself.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowToString2 {
	/**
	 * Show how a moderately complex composite parser prints
	 * itself.
	 */
	public static void main(String[] args) {
		System.out.println(RobotParser.start());
	}
}
