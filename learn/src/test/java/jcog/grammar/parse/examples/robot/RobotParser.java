package jcog.grammar.parse.examples.robot;

import jcog.grammar.parse.Alternation;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.tokens.CaselessLiteral;
import jcog.grammar.parse.tokens.Word;

/**
 * This class's start() method provides a parser that
 * will recognize a command for a track robot and build a
 * corresponding command object.
 * <p>
 * The grammar for the language that this class recognizes
 * is:
 * 
 * <blockquote><pre>
 *     command      = pickCommand | placeCommand | 
 *                    scanCommand;
 *     pickCommand  = "pick" "carrier" "from" location;
 *     placeCommand = "place" "carrier" "at" location;
 *     scanCommand  = "scan" location;
 *     location     = Word; 
 * </pre></blockquote>
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class RobotParser {
	/**
	 * Returns a parser that will recognize a command for a 
	 * track robot and build a corresponding command object.
	 *
	 * (This method returns the same value as 
	 * <code>start()</code>).
	 *
	 * @return   a parser that will recognize a track robot 
	 *           command
	 */

	/* The parser this method returns recognizes the grammar:
	 *
	 *     command = pickCommand | placeCommand | scanCommand;
	 */
    private static Parser command() {
		Alternation a = new Alternation();
		a.get(pickCommand());
		a.get(placeCommand());
		a.get(scanCommand());
		return a;
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 *
	 *     location = Word;
	 */
    private static Parser location() {
		return new Word();
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 *
	 *     pickCommand  = "pick" "carrier" "from" location;
	 */
    private static Parser pickCommand() {
		Seq s = new Seq();
		s.get(new CaselessLiteral("pick"));
		s.get(new CaselessLiteral("carrier"));
		s.get(new CaselessLiteral("from"));
		s.get(location());
		s.put(new PickAssembler());
		return s;
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 *
	 *     placeCommand = "place" "carrier" "at" location;
	 */
    private static Parser placeCommand() {
		Seq s = new Seq();
		s.get(new CaselessLiteral("place"));
		s.get(new CaselessLiteral("carrier"));
		s.get(new CaselessLiteral("at"));
		s.get(location());
		s.put(new PlaceAssembler());
		return s;
	}

	/*
	 * Returns a parser that will recognize the grammar:
	 *
	 *     scanCommand  = "scan" location;
	 */
    private static Parser scanCommand() {
		Seq s = new Seq();
		s.get(new CaselessLiteral("scan"));
		s.get(location());
		s.put(new ScanAssembler());
		return s;
	}

	/**
	 * Returns a parser that will recognize a command for a 
	 * track robot and build a corresponding command object.
	 *
	 * @return   a parser that will recognize a track robot 
	 *           command
	 */
	public static Parser start() {
		return RobotParser.command();
	}
}
