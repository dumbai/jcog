package jcog.grammar.parse.examples.reserved;

import jcog.grammar.parse.tokens.Token;
import jcog.grammar.parse.tokens.TokenType;
import jcog.grammar.parse.tokens.Tokenizer;
import jcog.grammar.parse.tokens.WordState;

import java.io.IOException;
import java.io.PushbackReader;
import java.math.BigDecimal;
import java.util.Vector;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * Override WordState to return known reserved words as 
 * tokens of type TT_RESERVED.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class WordOrReservedState extends WordState {
	private Vector reserved = new Vector();

	/**
	 * A constant indicating that a token is a reserved word.
	 */
	public static final TokenType TT_RESERVED = new TokenType("reserved");

	/**
	 * Adds the specified string as a known reserved word. 
	 *
	 * @param   String   the word to addAt
	 */
	public void addReservedWord(String word) {
		reserved.addElement(word);
	}

	/**
	 * Return all the known reserved words.
	 *
	 * @return   Vector  all the known reserved words
	 */
	public Vector getReservedWords() {
		return reserved;
	}

	/**
	 * Return a reserved token or a word token from a reader.
	 *
	 * @return a reserved token or a word token from a reader
	 */
	public Token nextToken(PushbackReader r, int c, Tokenizer t) throws IOException {

		Token tok = super.nextToken(r, c, t);
		if (reserved.contains(tok.sval())) {
			return new Token(TT_RESERVED, tok.sval(), BigDecimal.ZERO);
		}
		return tok;
	}
}
