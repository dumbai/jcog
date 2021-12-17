package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.*;
import jcog.grammar.parse.tokens.Literal;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;
import jcog.grammar.parse.tokens.Word;

import java.util.HashSet;
import java.util.Set;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * This class shows than a parser can find more than one way to completely
 * consume an assembly.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
public class ShowAmbiguity {
	/**
	 * Show than a parser can find more than one way to completely consume an
	 * assembly.
	 */
	public static void main(String[] args) {

		

		Parser volume = new Alternation().get(new Literal("cups")).get(new Literal("gallon")).get(new Literal("liter"));

		

		volume.put((IAssembler) a -> {
			Object o = a.pop();
			a.push("VOL(" + o + ')');
		});

		

		Parser wordOrVolume = new Alternation().get(new Word()).get(volume);

		Parser query = new Seq().get(new Repetition(wordOrVolume)).get(new Symbol('?'));

		Set<Assembly> v = new HashSet<>();
		v.add(new TokenAssembly("How many cups are in a gallon?"));

		System.out.println(query.match(v));
	}
}
