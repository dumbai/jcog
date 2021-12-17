package jcog.grammar.parse.examples.robot;

import jcog.grammar.parse.Alternation;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.tokens.CaselessLiteral;
import jcog.grammar.parse.tokens.TokenAssembly;
import jcog.grammar.parse.tokens.Word;

/**
 * Show how to create a parser and use it in a single 
 * method.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class RobotMonolithic {
	/**
	 * Show how to create a parser and use it in a single 
	 * method.
	 */
	public static void main(String[] args) {
		Alternation command = new Alternation();
		Seq pickCommand = new Seq();
		Seq placeCommand = new Seq();
		Seq scanCommand = new Seq();
		Word location = new Word();

		command.get(pickCommand);
		command.get(placeCommand);
		command.get(scanCommand);

		pickCommand.get(new CaselessLiteral("pick"));
		pickCommand.get(new CaselessLiteral("carrier"));
		pickCommand.get(new CaselessLiteral("from"));
		pickCommand.get(location);

		placeCommand.get(new CaselessLiteral("place"));
		placeCommand.get(new CaselessLiteral("carrier"));
		placeCommand.get(new CaselessLiteral("at"));
		placeCommand.get(location);

		scanCommand.get(new CaselessLiteral("scan"));
		scanCommand.get(location);

		String s = "pick carrier from DB101_IN";

		System.out.println(command.bestMatch(new TokenAssembly(s)));
	}
}
