package jcog.grammar.parse.examples.midimath;

import jcog.grammar.parse.*;
import jcog.grammar.parse.examples.arithmetic.NumAssembler;
import jcog.grammar.parse.examples.arithmetic.PlusAssembler;
import jcog.grammar.parse.examples.arithmetic.TimesAssembler;
import jcog.grammar.parse.tokens.Num;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * This class creates and uses a parser that aims
 * to recognize simple arithmetic expressions, but gets
 * caught in a loop. Allowable expressions include the
 * use of multiplication, addition and parentheses. The
 * grammar for this language is:
 * 
 * <blockquote><pre>	
 *     expression = term ('+' term)*;
 *     term       = factor ('*' factor)*;
 *     factor     = '(' expression ')' | Num;
 * </pre></blockquote>
 *
 * This class implements this grammar as a utility class,
 * but introduces a definitional loop in so doing.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class Midiloop {
	/**
	 * Returns a parser that will recognize a Midimath
	 * expression.
	 * 
	 * @return   a parser that will recognize a Midimath 
	 *           expression
	 */
    private Parser expression() {
		Seq expression = new Seq();

		Seq plusTerm = new Seq();
		plusTerm.get(new Symbol('+').ok());
		plusTerm.get(term());
		plusTerm.put(new PlusAssembler());

		expression.get(term());
		expression.get(new Repetition(plusTerm));
		return expression;
	}

	/**
	 * Returns a parser that will recognize either
	 * numbers, or arithmetic expressions in parentheses.
	 *
	 * @return   a parser that will recognize either
	 *           numbers, or arithmetic expressions in 
	 *           parentheses
	 */
    private Parser factor() {
		Alternation factor = new Alternation();

		Seq parenExpression = new Seq();
		parenExpression.get(new Symbol('(').ok());
		parenExpression.get(expression());
		parenExpression.get(new Symbol(')').ok());

		factor.get(parenExpression);
		factor.get(new Num().put(new NumAssembler()));
		return factor;
	}

	/**
	 * Demonstrate an infinite loop!
	 */
	public static void main(String[] args) {

		Parser e = new Midiloop().expression(); 

		Assembly out = e.bestMatch(new TokenAssembly("(7 + 13) * 5"));
		System.out.println(out.pop());
	}

	/**
	 * Returns a parser that will recognize arithmetic
	 * expressions containing just multiplication.
	 * 
	 * @return   a parser that will recognize arithmetic
	 *           expressions containing just multiplication
	 */
    private Parser term() {
		Seq term = new Seq();

		Seq timesFactor = new Seq();
		timesFactor.get(new Symbol('*').ok());
		timesFactor.get(factor());
		timesFactor.put(new TimesAssembler());

		term.get(factor());
		term.get(new Repetition(timesFactor));
		return term;
	}
}