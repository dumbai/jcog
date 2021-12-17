package jcog.grammar.parse.examples.query;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.examples.engine.Comparison;
import jcog.grammar.parse.examples.engine.ComparisonTerm;
import jcog.grammar.parse.tokens.Token;

/**
 * This assembler pops a comparison term, an operator, and
 * another comparison term. It builds the comparison and
 * passes the comparison to the query builder that this
 * assembler expects to find in the assembly's target.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
class ComparisonAssembler implements IAssembler {
	/**
	 * Pops a comparison term, an operator, and another 
	 * comparison term. Builds the comparison and passes the 
	 * comparison to the query builder that this assembler 
	 * expects to find in the assembly's target.
	 */
	public void accept(Assembly a) {
		ComparisonTerm second = (ComparisonTerm) a.pop();
		Token t = (Token) a.pop();
		ComparisonTerm first = (ComparisonTerm) a.pop();
		QueryBuilder b = (QueryBuilder) a.getTarget();
		b.addComparison(new Comparison(t.sval(), first, second));
	}
}
