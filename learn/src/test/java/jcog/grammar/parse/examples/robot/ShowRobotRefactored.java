package jcog.grammar.parse.examples.robot;

import jcog.grammar.parse.Parser;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * Show how to use a parser class that arranges its 
 * subparsers as methods.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowRobotRefactored {
	/** 
	 * Show how to use a parser class that arranges its subparsers 
	 * as methods.
	 */
	public static void main(String[] args) {
		Parser p = RobotRefactored.command();
		String s = "place carrier at WB500_IN";
		System.out.println(p.bestMatch(new TokenAssembly(s)));
	}
}
