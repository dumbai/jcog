package jcog.grammar.parse.examples.minimath;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.examples.arithmetic.MinusAssembler;
import jcog.grammar.parse.examples.arithmetic.NumAssembler;
import jcog.grammar.parse.tokens.Num;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * Show a properly working utility class that provides an 
 * parser for "Minimath", using the grammar:
 * 
 * <blockquote><pre>	
 *     e = Num m*;
 *     m = '-' Num;
 * </pre></blockquote>
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class MinimathParser {
	private static Seq e;

	/**
	 * Return a parser that will recognize a Minimath
	 * expression.
	 *
	 * @return   a parser that will recognize a Minimath 
	 *           expression
	 */
	private static Parser e() {

		

		if (e == null) {
			e = new Seq();
			e.get(n());
			e.get(new Repetition(m()));
		}
		return e;
	}

	/*
	 * a parser for the rule: m = '-' Num;
	 */
	private static Parser m() {
		Seq s = new Seq();
		s.get(new Symbol('-').ok());
		s.get(n());
		s.put(new MinusAssembler());
		return s;
	}

	/**
	 * Just a little demo.
	 */
	public static void main(String[] args) {
		Assembly a = start().completeMatch(new TokenAssembly("25 - 16 - 9"));
		System.out.println(a.pop());
	}

	/*
	 * a parser to recognize a number. By default, Num
	 * stacks a token. Here we use NumAssembler to replace
	 * the token with its double value.
	 */
	private static Parser n() {
		return new Num().put(new NumAssembler());
	}

	/**
	 * Returns a parser that will recognize a Minimath
	 * expression.
	 * 
	 * @return   a parser that will recognize a Minimath
	 *           expression
	 */
	private static Parser start() {
		return e();
	}
}
