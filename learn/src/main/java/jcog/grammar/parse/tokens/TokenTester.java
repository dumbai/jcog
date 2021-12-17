package jcog.grammar.parse.tokens;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.ParserTester;

public class TokenTester extends ParserTester {
	/**
	* 
	*/
	public TokenTester(Parser p) {
		super(p);
	}

	/**
	 * assembly method comment.
	 */
	@Override
	protected Assembly assembly(String s) {
		return new TokenAssembly(s);
	}
}
