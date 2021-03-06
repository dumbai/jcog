package jcog.grammar.parse.examples.tokens;

import jcog.grammar.parse.tokens.Token;
import jcog.grammar.parse.tokens.Tokenizer;

import java.io.IOException;

/**
 * This class shows how to <i>not</i> ignore Java-style 
 * comments.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowNoComment {
	/**
	 * Demonstrate how to not ignore comments
	 */
	public static void main(String[] args) throws IOException {
		Tokenizer t = new Tokenizer("Show /* all */ // this");

		t.setCharacterState('/', '/', t.symbolState());

		while (true) {
			Token tok = t.nextToken();
			if (tok.equals(Token.EOF)) {
				break;
			}
			System.out.println(tok);
		}
	}
}