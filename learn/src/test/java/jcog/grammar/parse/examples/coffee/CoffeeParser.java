package jcog.grammar.parse.examples.coffee;

import jcog.grammar.parse.Alternation;
import jcog.grammar.parse.Empty;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.tokens.*;

/**
 * This class provides a parser that recognizes a
 * textual description of a type of coffee, and builds a
 * corresponding coffee object.
 * <p>
 * The grammar this class supports is:
 * <blockquote><pre>
 * 
 *     coffee     = name ',' roast ',' country ',' price;
 *     name       = Word (formerName | Empty);
 *     formerName = '(' Word ')';
 *     roast      = Word (orFrench | Empty);
 *     orFrench   = '/' "french";
 *     country    = Word;
 *     price      = Num;
 * 
 * </pre></blockquote>
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */

public class CoffeeParser {
	/**
	 * Return a parser that will recognize the grammar:
	 * 
	 * <blockquote><pre>
	 * 
	 *     coffee  = name ',' roast ',' country ',' price;
	 *
	 *  </pre></blockquote>
	 *
	 * This parser creates a <code>Coffee</code> object
	 * as an assembly's target.
	 *
	 * @return a parser that will recognize and build a 
	 *         <code>Coffee</object> from a textual description.
	 */
    private static Parser coffee() {
		Symbol comma = new Symbol(',');
		comma.ok();
		Seq s = new Seq();
		s.get(name());
		s.get(comma);
		s.get(roast());
		s.get(comma);
		s.get(country());
		s.get(comma);
		s.get(price());
		return s;
	}

	/*
	 * Return a parser that will recognize the grammar:
	 * 
	 *    country = Word;
	 *
	 * Use a CountryAssembler to update the target coffee 
	 * object.
	 */
    private static Parser country() {
		return new Word().put(new CountryAssembler());
	}

	/*
	 * Return a parser that will recognize the grammar:
	 * 
	 *     formerName = '(' Word ')';
	 *
	 * Use a FormerNameAssembler to update the target coffee 
	 * object.
	 */
    private static Parser formerName() {
		Seq s = new Seq();
		s.get(new Symbol('(').ok());
		s.get(new Word().put(new FormerNameAssembler()));
		s.get(new Symbol(')').ok());
		return s;
	}

	/*
	 * Return a parser that will recognize the grammar:
	 * 
	 *     name = Word (formerName | empty);
	 *
	 * Use a NameAssembler to update the target coffee object 
	 * with the recognized Word; formerName also uses an
	 * assembler.
	 */
    private static Parser name() {
		Seq s = new Seq();
		s.get(new Word().put(new NameAssembler()));
		Alternation a = new Alternation();
		a.get(formerName());
		a.get(new Empty());
		s.get(a);
		return s;
	}

	/*
	 * Return a parser that will recognize the sequence:
	 * 
	 *    orFrench = '/' "french";
	 *
	 * Use an AlsoFrenchAssembler to update the target coffee 
	 * object.
	 */
    private static Parser orFrench() {
		Seq s = new Seq();
		s.get(new Symbol('/').ok());
		s.get(new CaselessLiteral("french").ok());
		s.put(new AlsoFrenchAssembler());
		return s;
	}

	/*
	 * Return a parser that will recognize the sequence:
	 * 
	 *    price = Num;
	 *
	 * Use a PriceAssembler to update the target coffee object.
	 */
    private static Parser price() {
		return new Num().put(new PriceAssembler());
	}

	/*
	 * Return a parser that will recognize the grammar:
	 * 
	 *     roast = Word (orFrench | Empty);
	 *
	 * Use a RoastAssembler to update the target coffee object 
	 * with the recognized Word; orFrench also uses an 
	 * assembler.
	 */
    private static Parser roast() {
		Seq s = new Seq();
		s.get(new Word().put(new RoastAssembler()));
		Alternation a = new Alternation();
		a.get(orFrench());
		a.get(new Empty());
		s.get(a);
		return s;
	}

	/**
	 * Return the primary parser for this class -- coffee().
	 *
	 * @return the primary parser for this class -- coffee()
	 */
	public static Parser start() {
		return new CoffeeParser().coffee();
	}

	/**
	 * Returns a tokenizer that allows spaces to appear inside
	 * the "words" that identify a coffee's name.
	 *
	 * @return a tokenizer that allows spaces to appear inside
	 * the "words" that identify a coffee's name.
	 */
	public static Tokenizer tokenizer() {
		Tokenizer t = new Tokenizer();
		t.wordState().setWordChars(' ', ' ', true);
		return t;
	}
}
