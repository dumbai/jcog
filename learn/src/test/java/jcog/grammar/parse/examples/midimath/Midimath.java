package jcog.grammar.parse.examples.midimath;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.examples.arithmetic.NumAssembler;
import jcog.grammar.parse.examples.arithmetic.PlusAssembler;
import jcog.grammar.parse.examples.arithmetic.TimesAssembler;
import jcog.grammar.parse.tokens.Num;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * This class creates and uses a parser that recognizes
 * arithmetic expressions that use addition and 
 * multiplication. The rules of the Midimath language are:
 * 
 * <blockquote><pre>	
 *     expression = term ('+' term)*;
 *     term       = Num ('*' Num)*;
 * </pre></blockquote>
 *
 * This class exists to show operator precedence.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class Midimath {
	/**
	 * Returns a parser that will recognize a Midimath
	 * expression.
	 * 
	 * @return   a parser that will recognize a Midimath 
	 *           expression
	 */
    private static Parser expression() {
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
	 * Demonstrate a parser for Midimath.
	 */
	public static void main(String[] args) {
		Assembly out = Midimath.expression().bestMatch(new TokenAssembly("2 + 3 * 7 + 19"));
		System.out.println(out.pop());
	}

	/**
	 * Returns a parser that will recognize arithmetic
	 * expressions containing just multiplication.
	 * 
	 * @return   a parser that will recognize arithmetic
	 *           expressions containing just multiplication
	 */
    private static Parser term() {
		Seq term = new Seq();

		Num n = new Num();
		n.put(new NumAssembler());

		Seq timesNum = new Seq();
		timesNum.get(new Symbol('*').ok());
		timesNum.get(n);
		timesNum.put(new TimesAssembler());

		term.get(n);
		term.get(new Repetition(timesNum));
		return term;
	}
}