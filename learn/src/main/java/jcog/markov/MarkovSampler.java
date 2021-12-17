package jcog.markov;

import jcog.data.list.Lst;
import jcog.random.XoRoShiRo128PlusRandom;

import java.util.List;
import java.util.Random;

public class MarkovSampler<T> {

	public final MarkovChain<T> model;

	/**
	 * Nodes use this to find the next node
	 */
	private final Random rng;
	/**
	 * Pointer to the current node. Methods next() uses this
	 */
	private MarkovChain.Chain<T> current;

	/**
	 * Index for which data element is next in our tuple
	 */
	private int tupleIndex;


	public MarkovSampler(MarkovChain<T> model) {
		this(model, new XoRoShiRo128PlusRandom());
	}

	public MarkovSampler(MarkovChain<T> model, Random rng) {
		this.model = model;
		this.rng = rng;
		reset();
	}

	public List<T> generate() {
		return generate(-1);
	}

	/**
	 * Use our graph to randomly generate a possibly valid phrase
	 * from our data structure.
	 *
	 * @param max sequence length, or -1 for unlimited
	 * @return generated phrase
	 */
	public List<T> generate(int maxLen) {

		MarkovChain.Chain<T> X = MarkovChain.START.next(rng);

		Lst<T> phrase = new Lst<>(Math.max(maxLen, 0));
		int s = 0;
		while (X != null && X != MarkovChain.END) {
			List<T> x = X.data;

			if (maxLen != -1 && (s + x.size() >= maxLen)) {
				int subPhraseN = maxLen - s;
				for (int i = 0; i < subPhraseN; i++)
					phrase.add(x.get(i));
				break;
			} else {
				phrase.addAll(x);
				s += x.size();
			}

			if (maxLen != -1 && s == maxLen)
				break;

			X = X.next(rng);
		}


		return phrase;
	}

	/**
	 * Re-initialize the chain pointer  and
	 * tuple index to start from the top.
	 */
	public void reset() {
		current = MarkovChain.START;
		tupleIndex = 0;
	}

	/**
	 * Returns the next element in our gradual chain.
	 * Ignores maximum length.
	 *
	 * @return next data element
	 */
	public T next() {
		return next(false);
	}

	/**
	 * Returns the next element and loops to the front of chain
	 * on termination.
	 *
	 * @return next element
	 */
	public T nextLoop() {
		return next(true);
	}

	/**
	 * Get next element pointed by our single-element.
	 * This will also update the data structure to get ready
	 * to serve the next data element.
	 *
	 * @param loop if you would like to loop
	 * @return data element at the current node tuple index
	 */
	public T next(boolean loop) {

		if (model.nodes.isEmpty())
			return null;

		if (current == MarkovChain.START)
			current = MarkovChain.START.next(rng);

		if (current == MarkovChain.END) {
			if (!loop)
				return null;
			current = MarkovChain.START.next(rng);
			tupleIndex = 0;
		}

		T y = current.get(tupleIndex++);

		if (tupleIndex >= current.length()) {
			current = next(current);
			tupleIndex = 0;
		}

		return y;
	}

	/** next node selector */
	protected MarkovChain.Chain<T> next(MarkovChain.Chain<T> current) {
		return current.next(rng);
	}

}
