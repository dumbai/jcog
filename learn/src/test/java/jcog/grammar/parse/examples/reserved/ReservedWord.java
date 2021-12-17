package jcog.grammar.parse.examples.reserved;

import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Terminal;
import jcog.grammar.parse.tokens.Token;

import java.util.Vector;

/**
 * A ReservedWord matches a word whose token type is 
 * <code>WordOrReservedState.TT_RESERVED</code>.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0 
 */
class ReservedWord extends Terminal {

	/**
	 * Returns true if an assembly's next element is a reserved 
	 * word.
	 *
	 * @param   object   an element from an assembly
	 *
	 * @return   true, if an assembly's next element is a 
	 *           reserved word
	 */
	protected boolean qualifies(Object o) {
		Token t = (Token) o;
		return t.ttype() == WordOrReservedState.TT_RESERVED;
	}

	/**
	 * Returns a textual description of this parser.
	 *
	 * @param   vector   a list of parsers already printed in this
	 *                   description
	 * 
	 * @return   string   a textual description of this parser
	 *
	 * @see Parser#toString()
	 */
	public static String unvisitedString(Vector visited) {
		return "ReservedWord";
	}
}
