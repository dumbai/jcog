package jcog.grammar.parse.examples.design;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.tokens.TokenAssembly;
import jcog.grammar.parse.tokens.Word;

/**
 * Show how to use an assembler. The example shows how to
 * calculate the average length of words in a string.
 *
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowAssembler {
	/**
	 * Show how to use an assembler to calculate the average 
	 * length of words in a string.
	 */
	public static void main(String[] args) {

		
		String quote = "Brevity is the soul of wit";

		Assembly in = new TokenAssembly(quote);
		in.setTarget(new RunningAverage());

		Word w = new Word();
		w.put(new AverageAssembler());
		Parser p = new Repetition(w);

		Assembly out = p.completeMatch(in);

		RunningAverage avg = (RunningAverage) out.getTarget();
		System.out.println("Average word length: " + avg.average());
	}
}