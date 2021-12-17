package jcog.grammar.parse.examples.tokens;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

/**
 * Show a <code>StreamTokenizer</code> object at work.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowTokenizer2 {
	/**
	 * Show a StreamTokenizer at work.
	 */
	public static void main(String[] args) throws IOException {

		String s =

		"\"It's 123 blast-off!\", she said, // watch out!\n" + "and <= 3 'ticks' later /* wince */ , it's blast-off!";

		System.out.println(s);
		System.out.println();

		StreamTokenizer t = new StreamTokenizer(new StringReader(s));
		t.ordinaryChar('/');
		t.slashSlashComments(true);
		t.slashStarComments(true);

		boolean done = false;
		while (!done) {
			t.nextToken();
            switch (t.ttype) {
                case StreamTokenizer.TT_EOF -> done = true;
                case StreamTokenizer.TT_WORD, '\"', '\'' -> System.out.println('(' + t.sval + ')');
                case StreamTokenizer.TT_NUMBER -> System.out.println("(" + t.nval + ')');
                default -> System.out.println("(" + (char) t.ttype + ')');
            }
		}
	}
}