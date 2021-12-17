package jcog.markov;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import jcog.io.Twokenize;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import static java.util.stream.Collectors.toList;

/**
 * Adds functionality to the MarkovChain class which
 * tokenizes a String argument for use in the Markov graph.
 *
 * @author OEP
 */
public class MarkovSentence extends MarkovChain<String> {


	/**
	 * Stream-safe method to parse an InputStream.
	 *
	 * @param is InputStream to parse
	 */
	public void learnTwokenize(int arity, String sentence) {
		List<Twokenize.Span> phrase = Twokenize.twokenize(sentence);
		learn(arity, Iterables.transform(phrase, Twokenize.Span::toString));
	}

	public void learnTokenize(int maxArity, String sentence) {
		StringTokenizer s = new StringTokenizer(sentence, ",\"';:<>()-_=+/*~#%&^$@ .!?\t\n\r\f", true);
		Iterator<?> ii = s.asIterator();
		while (ii.hasNext()) {

			List<String> xx = Streams.stream(()->ii)
				.map(x -> x.toString().trim())
				.filter(x -> !x.isEmpty())
				.takeWhile(x -> !punct(x))
				.map(String::toLowerCase)
				.collect(toList());

			for (int arity = 1; arity < maxArity; arity++) {
				learn(xx, arity, arity /* more weight given to sequences */);
			}
		}
	}

	private static boolean punct(String x) {
        return switch (x) {
            case ".", "!", "?" -> true;
            default -> false;
        };
    }


	public String generateSentence() {
		return generateSentence(-1);
	}

	/**
	 * Make our generated Markov phrase into a String
	 * object that is more versatile.
	 *
	 * @return String of our Markov phrase
	 */
	public String generateSentence(int len) {

		List<String> phrase = ((MarkovSampler<String>) new MarkovSampler(this)).generate(len);

		StringBuilder sb = new StringBuilder();
		int sz = phrase.size();

		for (String word : phrase) {

			//			boolean space = false;
//			switch (word) {
//				case ",":
//				case "-":
//				case ":":
//					space = true;
//					break;
//			}
			/*if (i != sz - 1)*/
			sb.append(' ');

			sb.append(word);
		}

		return sb.toString();
	}


}