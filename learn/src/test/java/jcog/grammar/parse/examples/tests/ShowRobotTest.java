package jcog.grammar.parse.examples.tests;

import jcog.grammar.parse.examples.robot.RobotParser;
import jcog.grammar.parse.tokens.TokenTester;

/**
 * Test the <code>RobotParser</code> class.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0 
 */
public class ShowRobotTest {
	/**
	 * Test the <code>RobotParser</code> class.
	 */
	public static void main(String[] args) {
		new TokenTester(RobotParser.start()).test();
	}
}
