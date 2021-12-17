package jcog.grammar.parse.examples.reserved;

import jcog.grammar.parse.*;
import jcog.grammar.parse.tokens.Symbol;
import jcog.grammar.parse.tokens.Tokenizer;
import jcog.grammar.parse.tokens.Word;

/**
 * This class shows the use of a customized tokenizer, and
 * the use of a terminal that looks for the new token type.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
class VolumeQuery2 {
	/**
	 * Return a parser that recognizes the grammar:
	 * 
	 *    query = (Word | volume)* '?';
	 *
	 * @return   a parser that recognizes queries containing
	 *           volumes and random words.
	 */
	public static Parser query() {

		Parser a = new Alternation().get(new Word()).get(volume());

		Parser s = new Seq().get(new Repetition(a)).get(new Symbol('?'));

		return s;
	}

	/**
	 * Return a customized tokenizer that uses WordOrReservedState
	 * in place of WordState.
	 *
	 * @return   a custom tokenizer that uses WordOrReservedState
	 *           in place of WordState
	 */
	public static Tokenizer tokenizer() {

		Tokenizer t = new Tokenizer();

		WordOrReservedState wors = new WordOrReservedState();
		wors.addReservedWord("cups");
		wors.addReservedWord("gallon");
		wors.addReservedWord("liter");

		t.setCharacterState('a', 'z', wors);
		t.setCharacterState('A', 'Z', wors);
		t.setCharacterState(0xc0, 0xff, wors);

		return t;
	}

	/*
	 * Return a parser that recognizes the grammar:
	 * 
	 *    volume = "cups" | "gallon" | "liter";
	 *
	 * This parser stacks the recognized word as an
	 * argument to "VOL()".
	 */
	private static Parser volume() {

		Parser p = new ReservedWord();

		

		p.put((IAssembler) a -> {
			Object o = a.pop();
			a.push("VOL(" + o + ')');
		});

		return p;
	}
}
