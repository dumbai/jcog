package jcog.grammar.parse;

import jcog.data.set.ArrayUnenforcedSet;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * An <code>Alternation</code> object is a collection of parsers, any one of
 * which can successfully match against an assembly.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */

public class Alternation extends CollectionParser {

	/**
	 * Constructs a nameless alternation.
	 */
	public Alternation() {
	}
	public Alternation(Parser... p) {
		for (Parser pp : p)
			get(pp);
	}

	/**
	 * Constructs an alternation with the given name.
	 * 
	 * @param name
	 *            a name to be known by
	 */
	public Alternation(String name) {
		super(name);
	}
	public Alternation(String name, Parser... alts) {
		this(name);
		for (Parser a : alts)
			get(a);
	}

	/**
	 * Accept a "visitor" and a collection of previously visited parsers.
	 * 
	 * @param ParserVisitor
	 *            the visitor to accept
	 * 
	 * @param Vector
	 *            a collection of previously visited parsers
	 */
	@Override
	public void accept(ParserVisitor pv, Set<Parser> visited) {
		pv.visitAlternation(this, visited);
	}

	/**
	 * Given a set of assemblies, this method matches this alternation against
	 * all of them, and returns a new set of the assemblies that result from the
	 * matches.
	 * 
	 * @return a Vector of assemblies that result from matching against a
	 *         beginning set of assemblies
	 * 
	 * @param Vector
	 *            a vector of assemblies to match against
	 * 
	 */
	@Override
	public Set<Assembly> match(Set<Assembly> in) {
		Set<Assembly> out = new ArrayUnenforcedSet<>();
		for (int i = 0, subparsersSize = subparsers.size(); i < subparsersSize; i++)
			out.addAll(subparsers.get(i).matchAndAssemble(in));
		return out;
	}

	/*
	 * Create a random collection of elements that correspond to this
	 * alternation.
	 */
	@Override
	public List<String> randomExpansion(int maxDepth, int depth) {
		return depth >= maxDepth ?
			randomSettle(maxDepth, depth)
				:
			subparsers.get(ThreadLocalRandom.current()
				.nextInt(subparsers.size()))
				.randomExpansion(maxDepth, depth++);
	}

	/*
	 * This method is similar to randomExpansion, but it will pick a terminal if
	 * one is available.
	 */
	private List<String> randomSettle(int maxDepth, int depth) {


        List<Terminal> terms = subparsers.stream().filter(j -> j instanceof Terminal).map(j -> (Terminal) j).collect(Collectors.toList());


		List<? extends Parser> which = terms;
		if (terms.isEmpty()) {
			which = subparsers;
		}

		double n = which.size();
		int i = (int) (n * Math.random());
		Parser p = which.get(i);
		return p.randomExpansion(maxDepth, depth++);
	}

	/*
	 * Returns the string to show between the parsers this parser is an
	 * alternation of.
	 */
	@Override
	protected String toStringSeparator() {
		return " | ";
	}

	@Override
	public Iterable<Parser> leftChildren() {



		return subparsers;
	}

}
