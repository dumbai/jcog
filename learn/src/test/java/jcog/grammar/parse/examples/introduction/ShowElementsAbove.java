package jcog.grammar.parse.examples.introduction;

import jcog.grammar.parse.*;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.Token;
import jcog.grammar.parse.tokens.TokenAssembly;
import jcog.grammar.parse.tokens.Word;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * Show how to use <code>Assembler.elementsAbove()</code>.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
public class ShowElementsAbove {
	/**
	 * Show how to use <code>Assembler.elementsAbove()</code>.
	 */
	public static void main(String[] args) {

		Parser list = new Seq().get(new Symbol('{')).get(new Repetition(new Word())).get(new Symbol('}').ok());

		list.put((IAssembler) a -> {
			Token fence = new Token('{');
			System.out.println(AssemblerHelper.elementsAbove(a, fence));
		});

		list.bestMatch(new TokenAssembly("{ Washington Adams Jefferson }"));
	}
}