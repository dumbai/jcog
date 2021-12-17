package jcog.grammar.parse.examples.minimath;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.examples.arithmetic.MinusAssembler;
import jcog.grammar.parse.examples.arithmetic.NumAssembler;
import jcog.grammar.parse.tokens.Num;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * This class provides a parser that recognizes minimal 
 * arithmetic expressions, specifically allowing only the
 * '-' operator. The rules of the Minimath language are:
 * 
 * <blockquote><pre>	
 *     e = Num m*;
 *     m = '-' Num;
 * </pre></blockquote>
 *
 * This class shows, in a minimal example, where assemblers 
 * plug into a parser composite.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class MinimathCompute {
	/**
	 * Just a little demo.
	 */
	public static void main(String[] args) {
		Seq e = new Seq();

		Num n = new Num();
		n.put(new NumAssembler());

		e.get(n);

		Seq m = new Seq();
		m.get(new Symbol('-').ok());
		m.get(n);
		m.put(new MinusAssembler());

		e.get(new Repetition(m));

		TokenAssembly t = new TokenAssembly("25 - 16 - 9");
		Assembly out = e.completeMatch(t);
		System.out.println(out.pop());
	}
}