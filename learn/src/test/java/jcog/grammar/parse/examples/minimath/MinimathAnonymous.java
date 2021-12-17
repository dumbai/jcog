package jcog.grammar.parse.examples.minimath;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.tokens.Num;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.Token;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 *  
 * This class just gives a little demo of how to create
 * anonymous assemblers.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class MinimathAnonymous {
	/**
	 * Just a little demo.
	 */
	public static void main(String[] args) {
		Seq e = new Seq();

		Num n = new Num();
		n.put((IAssembler) a -> {
			Token t = (Token) a.pop();
			a.push(t.nval());
		});

		e.get(n);

		Seq m = new Seq();
		m.get(new Symbol('-').ok());
		m.get(n);
		m.put((IAssembler) a -> {
			Double d1 = (Double) a.pop();
			Double d2 = (Double) a.pop();
			Double d3 = d2 - d1;
			a.push(d3);
		});

		e.get(new Repetition(m));

		TokenAssembly t = new TokenAssembly("25 - 16 - 9");
		Assembly out = e.completeMatch(t);
		System.out.println(out.pop());

	}
}