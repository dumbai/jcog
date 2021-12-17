package jcog.grammar.parse.examples.query;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.tokens.Token;

/**
 * Pops a class name, and informs a QueryBuilder that this
 * is a class to select from.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
class ClassNameAssembler implements IAssembler {

	/**
	 * Pop a class name, and inform a QueryBuilder that this
	 * is a class to select from.
	 */
	public void accept(Assembly a) {
		QueryBuilder b = (QueryBuilder) a.getTarget();
		Token t = (Token) a.pop();
		b.addClassName(t.sval());
	}
}
