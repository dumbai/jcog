package jcog.grammar.parse.examples.design;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.tokens.Token;

/**
 * This assembler updates a running average.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
class AverageAssembler implements IAssembler {
	/**
	 * Increases a running average, by the length of the string
	 * on the stack.
	 */
	public void accept(Assembly a) {
		Token t = (Token) a.pop();
		String s = t.sval();
		RunningAverage avg = (RunningAverage) a.getTarget();
		avg.add(s.length());
	}
}
