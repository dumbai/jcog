package jcog.grammar.parse.examples.tokens;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.tokens.*;

/*
 * Copyright (c) 2000 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * This class shows a collaboration of objects from classes
 * <code>Tokenizer</code>, <code>TokenStringSource</code>, 
 * <code>TokenString</code>, <code>TokenAssembly</code>.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */

public class ShowTokenString {
	/**
	 * Show a collaboration of token-related objects.
	 */
	public static void main(String[] args) {

		

		Parser w = new Word().ok();
		w.put((IAssembler) a -> {
			if (a.getStack().isEmpty()) {
				a.push(1);
			} else {
				Integer i = (Integer) a.pop();
				a.push(i + 1);
			}
		});

		

		Parser p = new Repetition(w);

		

		String s = "I came; I saw; I left in peace;";
		ITokenizer t = new Tokenizer(s);
		TokenStringSource tss = new TokenStringSource(t, ";");

		

		while (tss.hasMoreTokenStrings()) {
			TokenString ts = tss.nextTokenString();
			TokenAssembly ta = new TokenAssembly(ts);
			Assembly a = p.completeMatch(ta);
			System.out.println(ts + " (" + a.pop() + " words)");
		}
	}
}