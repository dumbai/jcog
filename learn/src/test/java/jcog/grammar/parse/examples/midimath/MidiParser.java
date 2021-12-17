package jcog.grammar.parse.examples.midimath;

import jcog.grammar.parse.*;
import jcog.grammar.parse.examples.arithmetic.MinusAssembler;
import jcog.grammar.parse.examples.arithmetic.NumAssembler;
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
 * and avoids looping while building the subparsers.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class MidiParser {
	private static Seq expression;

	/**
	 * Returns a parser that will recognize a Midimath
	 * expression.
	 * 
	 * @return   a parser that will recognize a Midimath 
	 *           expression
	 */
    private Parser expression() {
		if (expression == null) {
			expression = new Seq();
			expression.get(term());
			expression.get(new Repetition(minusTerm()));
		}
		return expression;
	}

	/**
	 * Demonstrate that this utility class does not loop.
	 */
	public static void main(String[] args) {
		Parser e = new MidiParser().expression();
		Assembly out = e.bestMatch(new TokenAssembly("111 - (11 - 1)"));
		System.out.println(out.pop());
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
	 *    term = '(' expression ')' | Num;
	 *
	 * This parser adds an assembler to Num, that will 
	 * replace the top token in the stack with the token's
	 * Double value.
	 */
    private Parser term() {

		Seq s = new Seq();
		s.get(new Symbol('(').ok());
		s.get(expression());
		s.get(new Symbol(')').ok());

		Alternation a = new Alternation();
		a.get(s);
		a.get(new Num().put(new NumAssembler()));
		return a;
	}
}