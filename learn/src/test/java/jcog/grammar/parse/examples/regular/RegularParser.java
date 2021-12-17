package jcog.grammar.parse.examples.regular;

import jcog.grammar.parse.*;
import jcog.grammar.parse.chars.CharacterAssembly;
import jcog.grammar.parse.chars.Digit;
import jcog.grammar.parse.chars.Letter;
import jcog.grammar.parse.chars.SpecificChar;

/**
 * This class provides a parser that recognizes regular 
 * expressions.
 * <p>
 * Regular expressions are a "metalanguage", which means they 
 * form a language for describing languages. For example, 
 * <code>a*</code> is a regular expression that describes a 
 * simple language whose elements are strings composed of 0 
 * or more <code>a's</code>. Thus the result of parsing 
 * <code>a*</code> is a new parser, namely a
 * parser that will match strings of <code>a's</code>.
 * <p>
 * This class exists to show how a simple regular expression 
 * parser works. It recognizes expressions according to 
 * the following rules.
 *
 * <blockquote><pre>
 *     expression    = term orTerm*;
 *     term          = factor nextFactor*;
 *     orTerm        = '|' term;
 *     factor        = phrase | phraseStar;
 *     nextFactor    = factor;
 *     phrase        = letterOrDigit | '(' expression ')';
 *     phraseStar    = phrase '*';
 *     letterOrDigit = Letter | Digit;
 * </pre></blockquote>
 *
 * These rules recognize conventional operator precedence. 
 * They also avoid the problem of left recursion, and their 
 * implementation avoids problems with the infinite loop 
 * inherent in the cyclic dependencies of the rules. 
 *  
 * @author Steven J. Metsker
 *
 * @version 1.0 								       
 */

public class RegularParser {
	private Seq expression;

	/**
	 * Returns a parser that will recognize a regular
	 * expression. (Identical to <code>start()</code>).
	 * 
	 * @return a parser that will recognize a regular
	 *         expression
	 */
    private Parser expression() {
		if (expression == null) {

			
			expression = new Seq();
			expression.get(term());
			expression.get(new Repetition(orTerm()));
		}
		return expression;
	}

	/*
	 * Returns a parser that for the grammar rule:
	 *
	 *    factor = phrase | phraseStar; 
	 */
    private Parser factor() {
		Alternation a = new Alternation();
		a.get(phrase());
		a.get(phraseStar());
		return a;
	}

	/*
	 * Returns a parser that for the grammar rule:
	 *
	 *    letterOrDigit = Letter | Digit;
	 *
	 * This parser has an assembler that will pop a 
	 * character and push a SpecificChar parser in its 
	 * place.
	 */
    private static Parser letterOrDigit() {
		Alternation a = new Alternation();
		a.get(new Letter());
		a.get(new Digit());
		a.put(new CharAssembler());
		return a;
	}

	/*
	 * Returns a parser that for the grammar rule:
	 *
	 *    nextFactor = factor;
	 *
	 * This parser has an assembler that will pop two
	 * parsers and push a Sequence of them. 
	 */
    private Parser nextFactor() {
		Parser p = factor();
		p.put(new AndAssembler());
		return p;
	}

	/*
	 * Returns a parser that for the grammar rule:
	 *
	 *    orTerm = '|' term;
	 *
	 * This parser has an assembler that will pop two
	 * parsers and push an Alternation of them. 
	 */
    private Parser orTerm() {
		Seq s = new Seq();
		s.get(new SpecificChar('|').ok());
		s.get(term());
		s.put(new OrAssembler());
		return s;
	}

	/*
	 * Returns a parser that for the grammar rule:
	 *
	 *     phrase = letterOrDigit | '(' expression ')';
	 */
    private Parser phrase() {
		Alternation a = new Alternation();
		a.get(letterOrDigit());

		Seq s = new Seq();
		s.get(new SpecificChar('(').ok());
		s.get(expression());
		s.get(new SpecificChar(')').ok());

		a.get(s);
		return a;
	}

	/*
	 * Returns a parser that for the grammar rule:
	 *
	 *    phraseStar = phrase '*'; 
	 *
	 * This parser has an assembler that will pop a
	 * parser and push a Repetition of it.
	 */
    private Parser phraseStar() {
		Seq s = new Seq();
		s.get(phrase());
		s.get(new SpecificChar('*').ok());
		s.put(new StarAssembler());
		return s;
	}

	/**
	 * Returns a parser that will recognize a regular
	 * expression. 
	 * 
	 * @return a parser that will recognize a regular
	 *         expression
	 */
	public static Parser start() {
		return new RegularParser().expression();
	}

	/*
	 * Returns a parser that for the grammar rule:
	 *
	 *    term = factor nextFactor*; 
	 */
    private Parser term() {
		Seq term = new Seq();
		term.get(factor());
		term.get(new Repetition(nextFactor()));
		return term;
	}

	/**
	 * Return a parser that will match a <code>
	 * CharacterAssembly</code>, according to the value of a 
	 * regular expression given in a string.
	 *
	 * For example, given the string <code>a*</code>, this 
	 * method will return a parser which will match any element
	 * of the set <code>{"", "a", "aa", "aaa", ...}</code>.
	 *
	 * @return a parser that will match a <code>
	 *         CharacterAssembly</code>, according to the value
	 *         of a regular expression in the given string
	 *
	 * @param   String   the string to evaluate
	 *
	 * @exception RegularExpressionException if this parser
	 *            does not recognize the given string as a 
	 *            valid expression
	 */
	public static Parser value(String s) throws RegularExpressionException {

		CharacterAssembly c = new CharacterAssembly(s);
		Assembly a = start().completeMatch(c);
		if (a == null) {
			throw new RegularExpressionException("Improperly formed regular expression");
		}
		Parser p;
		try {
			p = (Parser) a.pop();
		} catch (Exception e) {
			throw new RegularExpressionException("Internal error in RegularParser");
		}
		return p;
	}
}
