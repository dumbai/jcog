package jcog.markov;

import jcog.data.list.Lst;
import jcog.pri.NLink;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A class for generating a Markov phrase out of some sort of
 * arbitrary comparable data.
 *
 * @param <T> the type of data you would like to generate phrases for (e.g., <code>java.lan
 * @author pkilgo
 */
public class MarkovChain<X> {

	/**
	 * HashMap to help us resolve data to the node that contains it
	 */
	public final Map<List<X>, Chain<X>> nodes;

	/**
	 * Node that marks the beginning of a phrase. All Markov phrases start here.
	 */
	public static final Chain START = new Chain(Collections.EMPTY_LIST);
	/**
	 * Node that signals the end of a phrase. This node should have no edges.
	 */
	public static final Chain END = new Chain(Collections.EMPTY_LIST);

	public MarkovChain() {
		this(new HashMap<>());
	}
	public MarkovChain(Map<List<X>, Chain<X>> nodes) {
		this.nodes = nodes;
	}

	/**
	 * Forget everything.
	 */
	public void clear() {
		nodes.clear();
		START.clear();
		END.clear();
	}

	public MarkovChain<X> learn(int arity, Iterable<? extends X> phrase) {
		return learn(phrase, arity, 1f);
	}

	/**
	 * Interpret an ArrayList of data as a possible phrase.
	 *
	 * @param phrase to learn
	 */
	public MarkovChain<X> learn(Iterable<? extends X> phrase, int arity, float strength) {
		if (arity <= 0) throw new IllegalArgumentException("Can't have MarkovChain with tuple length <= 0");

		Chain current = START;

		Lst<X> tuple = new Lst<>(arity);

		for (X x : phrase) {

			int sz = tuple.size();
			if (sz < arity) {
				tuple.addFast(x);
			} else {

				current = current.learn(getOrAdd(tuple), strength);
				(tuple = new Lst<>(arity)).add(x);
			}

		}

		if (!tuple.isEmpty())
			current = current.learn(getOrAdd(tuple), strength);

		if (current!=START)
			current.learn(END, strength);
		return this;
	}

	/**
	 * Interpret an array of data as a valid phrase.
	 *
	 * @param phrase to interpret
	 */
	public MarkovChain<X> learn(int arity, X[] phrase) {
		return learn(arity, List.of(phrase));
	}

	@SafeVarargs
	public final MarkovChain<X> learnAll(int arity, X[]... phrases) {
		for (X[] p : phrases)
			learn(arity, p);
		return this;
	}

	/**
	 * This method is an alias to find a node if it
	 * exists or create it if it doesn't.
	 *
	 * @param x to find a node for
	 * @return the newly created node, or resolved node
	 */
	private Chain<X> getOrAdd(List<X> x) {
		return nodes.computeIfAbsent(x, Chain::new);
	}


	/**
	 * This is our Markov phrase node. It contains the data
	 * that this node represents as well as a list of edges to
	 * possible nodes elsewhere in the graph.
	 *
	 * @author pkilgo
	 */
	public static class Chain<X> {

		/**
		 * The data this node represents
		 */
		public final List<X> data;

		/**
		 * A list of edges to other nodes
		 */
		protected final Map<Chain<X>, NLink<Chain<X>>> edges = new LinkedHashMap<>();
		private final int hash;

		/**
		 * Blank constructor for data-less nodes (the header or trailer)
		 */
		public Chain() {
			this(Collections.EMPTY_LIST);
		}

		/**
		 * Constructor for node which will contain data.
		 *
		 * @param d the data this node should represent
		 */
		public Chain(List<X> d) {
		    if (d instanceof Lst)
		    	((Lst)d).trimToSize();
			this.data = d;
			this.hash = data.hashCode();
		}

		static <T> NLink<T> selectRoulette(Random RNG, Collection<? extends NLink<T>> edges) {
			int s = edges.size();
			if (s == 0)
				return null;
			if (s == 1)
				return edges.iterator().next();


			float totalScore = 0;
			for (NLink e : edges)
				totalScore += e.priElseZero();

			float r = RNG.nextFloat() * totalScore;

			int current = 0;

			for (NLink<T> e : edges) {
				float dw = e.pri();

				if (r >= current && r < current + dw)
					return e;

				current += dw;
			}

			return edges.iterator().next();
		}

		@Override
		public final int hashCode() {
			return hash;
		}


		/**
		 * Get the data from the tuple at given position
		 *
		 * @param i the index of the data
		 * @return data at index
		 */
		public X get(int i) {
			return data.get(i);
		}

		public boolean isTerminal() {
			return data.isEmpty();
		}

		public void clear() {
			edges.clear();
		}

		/**
		 * Returns this node's tuple's size.
		 *
		 * @return size of tuple represented by this node
		 */
		public int length() {
			return data.size();
		}

		/**
		 * Add more weight to the given node
		 * or create an edge to that node if we didn't
		 * already have one.
		 *
		 * @param n node to add more weight to
		 * @return the node that was learned
		 */
		public Chain<X> learn(Chain<X> n, float strength) {
			NLink<Chain<X>> e = edges.computeIfAbsent(n, nn -> new NLink<>(nn, 0));
			e.priAdd(strength);
            return e.id;
        }

		/**
		 * Randomly choose which is the next node to go to, or
		 * return null if there are no edges.
		 *
		 * @return next node, or null if we could not choose a next node
		 */
		@Nullable
		protected Chain<X> next(Random rng) {
            return switch (edges.size()) {
                case 0 -> null;
                case 1 -> edges.values().iterator().next().id;
                default -> selectRoulette(rng, edges.values()).id;
            };
		}

	}

}
