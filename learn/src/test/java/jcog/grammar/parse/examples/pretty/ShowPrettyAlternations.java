package jcog.grammar.parse.examples.pretty;

import jcog.grammar.parse.Alternation;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.tokens.Literal;
import jcog.grammar.parse.tokens.TokenAssembly;

import java.util.Iterator;

/**
 * Show how the pretty printer displays a deep match of
 * alternations. The grammar this class shows is:
 *
 * <blockquote><pre> 
 *     reptile     = crocodilian | squamata;
 *     crocodilian = crocodile | alligator;
 *     squamata    = snake | lizard;
 *     crocodile   = "nileCroc" | "cubanCroc";
 *     alligator   = "chineseGator" | "americanGator";
 *     snake       = "cobra" | "python";
 *     lizard      = "gecko" | "iguana";
 * </pre></blockquote>
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0 
 */
public class ShowPrettyAlternations {
	/**
	 * Returns a parser that recognizes some alligators. 
	 */
	private static Parser alligator() {
		Alternation a = new Alternation("<alligator>");
		a.get(new Literal("chineseGator"));
		a.get(new Literal("americanGator"));
		return a;
	}

	/**
	 * Returns a parser that recognizes some crocs. 
	 */
	private static Parser crocodile() {
		Alternation a = new Alternation("<crocodile>");
		a.get(new Literal("nileCroc"));
		a.get(new Literal("cubanCroc"));
		return a;
	}

	/**
	 * Returns a parser that recognizes members of the crocodilian
	 * order. 
	 */
	private static Parser crocodilian() {
		Alternation a = new Alternation("<crocodilian>");
		a.get(crocodile());
		a.get(alligator());
		return a;
	}

	/**
	 * Returns a parser that recognizes some lizards. 
	 */
	private static Parser lizard() {
		Alternation a = new Alternation("<lizard>");
		a.get(new Literal("gecko"));
		a.get(new Literal("iguana"));
		return a;
	}

	/**
	 * Show how a series of alternations appear when pretty-
	 * printed.
	 */
	public static void main(String[] args) {
		PrettyParser p = new PrettyParser(reptile());
		p.setShowLabels(true);
		TokenAssembly ta = new TokenAssembly("gecko");
        Iterator iterator = p.parseTrees(ta).iterator();
		while (iterator.hasNext()) {
			System.out.println("The input parses as:");
			System.out.println("---------------------------");
			System.out.println(iterator.next());
		}
	}

	/**
	 * Returns a parser that recognizes some reptiles.
	 */
	private static Parser reptile() {
		Alternation a = new Alternation("<reptile>");
		a.get(crocodilian());
		a.get(squamata());
		return a;
	}

	/**
	 * Returns a parser that recognizes some snakes. 
	 */
	private static Parser snake() {
		Alternation a = new Alternation("<snake>");
		a.get(new Literal("cobra"));
		a.get(new Literal("python"));
		return a;
	}

	/**
	 * Returns a parser that recognizes some members of the 
	 * squamata order. 
	 */
	private static Parser squamata() {
		Alternation a = new Alternation("<squamata>");
		a.get(snake());
		a.get(lizard());
		return a;
	}
}