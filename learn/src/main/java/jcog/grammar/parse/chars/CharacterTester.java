package jcog.grammar.parse.chars;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.ParserTester;

public class CharacterTester extends ParserTester {
	/**
	* 
	*/
	public CharacterTester(Parser p) {
		super(p);
	}

	/**
	 * assembly method comment.
	 */
	@Override
	protected Assembly assembly(String s) {
		return new CharacterAssembly(s);
	}

	/**
	 * 
	 * @return java.lang.String
	 */
	@Override
	protected String separator() {
		return "";
	}
}
