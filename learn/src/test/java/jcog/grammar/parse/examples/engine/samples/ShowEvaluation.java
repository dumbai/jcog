package jcog.grammar.parse.examples.engine.samples;

import jcog.grammar.parse.examples.engine.ArithmeticOperator;
import jcog.grammar.parse.examples.engine.Evaluation;
import jcog.grammar.parse.examples.engine.NumberFact;
import jcog.grammar.parse.examples.engine.Variable;

/**
 * Show an evaluation.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowEvaluation {
	/**
	 * Show an evaluation.
	 */
	public static void main(String[] args) {
		Variable you = new Variable("You");
		Variable youAndBaby = new Variable("YouAndBaby");
		Variable baby = new Variable("Baby");

        ArithmeticOperator diff = new ArithmeticOperator('-', youAndBaby, you);

		Evaluation e = new Evaluation(baby, diff);
		System.out.println("Before weighing:");
		System.out.println(e);

		you.unify(new NumberFact(185));
		youAndBaby.unify(new NumberFact(199));

		System.out.println("\nAfter weighing:");
		System.out.println(e);

		e.canFindNextProof();

		System.out.println("\nThat baby weighs about " + baby + " pounds.");
	}
}
