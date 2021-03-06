package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.ArithmeticOperator;
import jcog.grammar.parse.examples.engine.NumberFact;

/**
 * Show the evaluation of an arithmetic operator.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowArithmeticEvaluation {
	/**
	 * Show the evaluation of an arithmetic operator.
	 */
	public static void main(String[] args) {

		NumberFact a = new NumberFact(231);
		NumberFact b = new NumberFact(3367);

		ArithmeticOperator op = new ArithmeticOperator('*', a, b);

		System.out.println(op);
		System.out.println(op.eval());
	}
}