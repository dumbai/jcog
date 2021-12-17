package jcog.grammar.parse.examples.query;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.examples.engine.Term;

/**
 * This assembler pops a term and passes it to a query
 * builder.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
class SelectTermAssembler implements IAssembler {
	/**
	 * Pop a term and pass it to a query builder.
	 */
	public void accept(Assembly a) {
		QueryBuilder b = (QueryBuilder) a.getTarget();
		Term t = (Term) a.pop();
		b.addTerm(t);
	}
}
