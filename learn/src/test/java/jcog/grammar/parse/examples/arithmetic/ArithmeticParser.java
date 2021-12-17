package jcog.grammar.parse.examples.arithmetic;

import jcog.grammar.parse.*;
import jcog.grammar.parse.tokens.Num;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * This class provides a parser that recognizes 
 * arithmetic expressions. This class includes the method 
 * <code>value</code>, which is a "facade" that provides an
 * example and makes the parser easy to use. For example,
 * 
 * <blockquote><pre>
 * 
 *     System.out.println(
 *         ArithmeticParser.value("(5 + 4) * 3 ^ 2 - 81"));
 * </pre></blockquote>
 *
 * This prints out <code>0.0</code>.
 * 
 * <p>
 * This class exists to show how a simple arithmetic 
 * parser works. It recognizes expressions according to 
 * the following rules:
 * 
 * <blockquote><pre>	
 *     expression    = term (plusTerm | minusTerm)*;
 *     term          = factor (timesFactor | divideFactor)*;
 *     plusTerm      = '+' term;
 *     minusTerm     = '-' term;
 *     factor        = phrase expFactor | phrase;
 *     timesFactor   = '*' factor;
 *     divideFactor  = '/' factor;
 *     expFactor     = '^' factor;
 *     phrase        = '(' expression ')' | Num;
 * </pre></blockquote>
 * 
 * These rules recognize conventional operator precedence and 
 * associativity. They also avoid the problem of left 
 * recursion, and their implementation avoids problems with 
 * the infinite loop inherent in the cyclic dependencies of 
 * the rules. In other words, the rules may look simple, but
 * their structure is subtle. 
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0 
 * 
 */

public class ArithmeticParser {
	private Seq expression;
	private Alternation factor;

	/*
	 * Returns a parser that for the grammar rule:
	 *    
	 *     divideFactor = '/' factor;
	 *
	 * This parser has an assembler that will pop two 
	 * numbers from the stack and push their quotient.
	 */
    private Parser divideFactor() {
		Seq s = new Seq();
		s.get(new Symbol('/').ok());
		s.get(factor());
		s.put(new DivideAssembler());
		return s;
	}

	/*
	 * Returns a parser that for the grammar rule:
	 *    
	 *     expFactor = '^' factor;
	 *
	 * This parser has an assembler that will pop two 
	 * numbers from the stack and push the result of
	 * exponentiating the lower number to the upper one.
	 */
    private Parser expFactor() {
		Seq s = new Seq();
		s.get(new Symbol('^').ok());
		s.get(factor());
		s.put(new ExpAssembler());
		return s;
	}

	/**
	 * Returns a parser that will recognize an arithmetic
	 * expression. (Identical to <code>start()</code>).
	 * 
	 * @return a parser that will recognize an arithmetic
	 *         expression
	 */
    private Parser expression() {
		/*
		 * This use of a static variable avoids the infinite 
		 * recursion inherent in the grammar.
		 */
		if (expression == null) {

			
			expression = new Seq("expression");
			expression.get(term());

			Alternation a = new Alternation();
			a.get(plusTerm());
			a.get(minusTerm());

			expression.get(new Repetition(a));
		}
		return expression;
	}

	/*
	 * Returns a parser that for the grammar rule:
	 *
	 *     factor = phrase expFactor | phrase;
	 */
    private Parser factor() {
		/*
		 * This use of a static variable avoids the infinite
		 * recursion inherent in the grammar; factor depends
		 * on expFactor, and expFactor depends on factor.
		 */
		if (factor == null) {
			factor = new Alternation("factor");

			Seq s = new Seq();
			s.get(phrase());
			s.get(expFactor());

			factor.get(s);
			factor.get(phrase());
		}
		return factor;
	}

	/*
	 * Returns a parser that for the grammar rule:
	 *    
	 *     minusTerm = '-' term;
	 *
	 * This parser has an assembler that will pop two 
	 * numbers from the stack and push their difference.
	 */
    private Parser minusTerm() {
		Seq s = new Seq();
		s.get(new Symbol('-').ok());
		s.get(term());
		s.put(new MinusAssembler());
		return s;
	}

	/*
	 * Returns a parser that for the grammar rule:
	 *
	 *    phrase = '(' expression ')' | Num;
	 *
	 * This parser adds an assembler to Num, that will 
	 * replace the top token in the stack with the token's
	 * Double value.
	 */
    private Parser phrase() {
		Alternation phrase = new Alternation("phrase");

		Seq s = new Seq();
		s.get(new Symbol('(').ok());
		s.get(expression());
		s.get(new Symbol(')').ok());
		phrase.get(s);

		phrase.get(new Num().put(new NumAssembler()));
		return phrase;
	}

	/*
	 * Returns a parser that for the grammar rule:
	 *    
	 *     plusTerm = '+' term;
	 *
	 * This parser has an assembler that will pop two 
	 * numbers from the stack and push their sum.
	 */
    private Parser plusTerm() {
		Seq s = new Seq();
		s.get(new Symbol('+').ok());
		s.get(term());
		s.put(new PlusAssembler());
		return s;
	}

	/**
	 * Returns a parser that will recognize an arithmetic
	 * expression.
	 * 
	 * @return   a parser that will recognize an 
	 *           arithmetic expression
	 */
	public static Parser start() {
		return new ArithmeticParser().expression();
	}

	/*
	 * Returns a parser that for the grammar rule:
	 *
	 *    term = factor (timesFactor | divideFactor)*;
	 */
    private Parser term() {
		Seq s = new Seq("term");
		s.get(factor());

		Alternation a = new Alternation();
		a.get(timesFactor());
		a.get(divideFactor());

		s.get(new Repetition(a));
		return s;
	}

	/*
	 * Returns a parser that for the grammar rule:
	 *    
	 *     timesFactor = '*' factor;
	 *
	 * This parser has an assembler that will pop two 
	 * numbers from the stack and push their product.
	 */
    private Parser timesFactor() {
		Seq s = new Seq();
		s.get(new Symbol('*').ok());
		s.get(factor());
		s.put(new TimesAssembler());
		return s;
	}

	/**
	 * Return the value of an arithmetic expression given in a
	 * string. This method is a facade, which provides an
	 * example of how to use the parser.
	 *
	 * @return the value of an arithmetic expression given in a
	 *         string
	 *
	 * @param String the string to evaluate.
	 *
	 * @exception ArithmeticExpressionException if this 
	 *            parser does not recognize the given string
	 *            as a valid expression
	 */
	public static double value(String s) throws ArithmeticExpressionException {

		TokenAssembly ta = new TokenAssembly(s);
		Assembly a = start().completeMatch(ta);
		if (a == null) {
			throw new ArithmeticExpressionException("Improperly formed arithmetic expression");
		}
		Double d;
		try {
			d = (Double) a.pop();
		} catch (Exception e) {
			throw new ArithmeticExpressionException("Internal error in ArithmeticParser");
		}
		return d;
	}

	@Test
    void examples() throws ArithmeticExpressionException {
		assertEquals(0.0, ArithmeticParser.value("(5 + 4) * 3 ^ 2 - 81"), 0.01);
		assertEquals(289.0, ArithmeticParser.value("(5 + 4 * 3) ^ 2"), 0.01);
		assertEquals(1.9, ArithmeticParser.value("(5.1 + 4.4) * 2 / 10"), 0.01);
	}
}
