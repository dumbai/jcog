package jcog.grammar.parse.examples.robot;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * Show how to use the <code>RobotParser</code> class.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowRobotParser {
	/**
	 * Show how to use the <code>RobotParser</code> class.
	 */
	public static void main(String[] args) {
		Parser p = RobotParser.start();

		String[] tests = { "pick carrier from LINE_IN", "place carrier at DB101_IN", "pick carrier from DB101_OUT", "place carrier at WB500_IN", "pick carrier from WB500_OUT", "place carrier at LINE_OUT", "scan DB101_OUT" };

		for (int i = 0; i < tests.length; i++) {
			TokenAssembly ta = new TokenAssembly(tests[i]);
			Assembly out = p.bestMatch(ta);
			System.out.println(out.getTarget());
		}
	}
}
