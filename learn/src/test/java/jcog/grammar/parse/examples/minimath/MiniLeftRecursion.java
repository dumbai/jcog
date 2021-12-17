package jcog.grammar.parse.examples.minimath;

import jcog.grammar.parse.Alternation;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.tokens.Num;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.TokenAssembly;

/**
 * This class uses a problematic grammar for Minimath. For
 * a better grammar, see class <code>MinimathCompute</code>. 
 * Here, the grammar is:
 * 
 * <blockquote><pre>	
 *     e = Num | e '-' Num;
 * </pre></blockquote>
 *
 * Writing a parser directly from this grammar shows
 * that left recusion will hang a parser.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class MiniLeftRecursion {
	/**
	 * Demonstrates an infinite loop.
	 */
	public static void main(String[] args) {
		Alternation e = new Alternation();
		Num n = new Num();

		Seq s = new Seq();
		s.get(e);
		s.get(new Symbol('-').ok());
		s.get(n);

		e.get(n);
		e.get(s);

		
		e.completeMatch(new TokenAssembly("25 - 16 - 9"));
	}
}