package jcog.grammar.parse.examples.minimath;

import jcog.grammar.parse.Alternation;
import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.examples.arithmetic.MinusAssembler;
import jcog.grammar.parse.examples.arithmetic.NumAssembler;
import jcog.grammar.parse.tokens.Num;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * This class uses a problematic grammar for Minimath. For
 * a better grammar, see class <code>MinimathCompute</code>. 
 * Here, the grammar is:
 * 
 * <blockquote><pre>	
 *     e = Num '-' e | Num;
 * </pre></blockquote>
 *
 * Writing a parser directly from this grammar will show
 * that the associativity is wrong. For example, this 
 * grammar will lead to a parser that calculates the value 
 * of 25 - 16 - 9 as 18.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class MiniWrongAssociativity {
	/**
	 * Demonstrates incorrect associativity.
	 */
	public static void main(String[] args) {
		Alternation e = new Alternation();
		Num n = new Num();
		n.put(new NumAssembler());

		Seq s = new Seq();
		s.get(n);
		s.get(new Symbol('-').ok());
		s.get(e);
		s.put(new MinusAssembler());

		e.get(s);
		e.get(n);

		Assembly out = e.completeMatch(new TokenAssembly("25 - 16 - 9"));

		System.out.println(out.pop() + " // arguably wrong!");
	}
}