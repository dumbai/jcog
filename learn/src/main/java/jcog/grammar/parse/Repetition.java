package jcog.grammar.parse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * A <code>Repetition</code> matches its underlying parser repeatedly against a
 * assembly.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */

public class Repetition extends Parser {
	/*
	 * the parser this parser is a repetition of
	 */
	private final Parser subparser;

	/*
	 * the width of a random expansion
	 */
	private static final int EXPWIDTH = 4;

	/*
	 * an assembler to apply at the beginning of a match
	 */
	private Consumer<Assembly> preAssembler;

	private int numberOfRequiredMatches = 0;

	/**
	 * Constructs a repetition of the given parser.
	 * 
	 * @param parser
	 *            the parser to repeat
	 * 
	 * @return a repetiton that will match the given parser repeatedly in
	 *         successive matches
	 */
	public Repetition(Parser p) {
		this(p, null);
	}

	/**
	 * Constructs a repetition of the given parser with the given name.
	 * 
	 * @param Parser
	 *            the parser to repeat
	 * 
	 * @param String
	 *            a name to be known by
	 * 
	 * @return a repetiton that will match the given parser repeatedly in
	 *         successive matches
	 */
	public Repetition(Parser subparser, String name) {
		super(name);
		this.subparser = subparser;
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
		pv.visitRepetition(this, visited);
	}

	/**
	 * Return this parser's subparser.
	 * 
	 * @return Parser this parser's subparser
	 */
	public Parser getSubparser() {
		return subparser;
	}

	/**
	 * Given a set of assemblies, this method applies a preassembler to all of
	 * them, matches its subparser repeatedly against each of them, applies its
	 * post-assembler against each, and returns a new set of the assemblies that
	 * result from the matches.
	 * <p>
	 * For example, matching the regular expression <code>a*
	* </code> against
	 * <code>{^aaab}</code> results in <code>
	* {^aaab, a^aab, aa^ab, aaa^b}</code>.
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
		if (preAssembler != null) {
			for (Assembly assembly : in) {
				preAssembler.accept(assembly);
			}
		}
		Set<Assembly> out = numberOfRequiredMatches == 0 ? elementClone(in) : new HashSet<>();
		Set<Assembly> s = in; 
		int countNumberOfMatches = 0;
		while (!s.isEmpty()) {
			s = subparser.matchAndAssemble(s);
			countNumberOfMatches++;
			if (countNumberOfMatches >= numberOfRequiredMatches)
				out.addAll(elementClone(s));
		}
		return out;
	}

	/**
	 * Create a collection of random elements that correspond to this
	 * repetition.
	 */
	@Override
	protected List<String> randomExpansion(int maxDepth, int depth) {
		List<String> v = new ArrayList<>();
		if (depth >= maxDepth) {
			return v;
		}

		int n = (int) (EXPWIDTH * Math.random());
		for (int j = 0; j < n; j++) {
			List<String> w = subparser.randomExpansion(maxDepth, depth++);
			v.addAll(w);
		}
		return v;
	}

	/**
	 * Sets the object that will work on every assembly before matching against
	 * it.
	 * 
	 * @param Assembler the assembler to apply
	 * 
	 * @return Parser this
	 */
	public Parser setPreAssembler(Consumer<Assembly> preAssembler) {
		this.preAssembler = preAssembler;
		return this;
	}

	/*
	 * Returns a textual description of this parser.
	 */
	@Override
	protected String unvisitedString(Set<Parser> visited) {
		if (numberOfRequiredMatches == 0)
			return subparser.toString(visited) + '*';
		return subparser.toString(visited) + '+';
	}

	public Iterable<Parser> children() {
		return List.of(subparser);
	}

	public Repetition requireMatches(int numberOfRequiredMatches) {
		this.numberOfRequiredMatches = numberOfRequiredMatches;
		return this;
	}

	public int requiredMatches() {
		return numberOfRequiredMatches;
	}

	@Override
	public Iterable<Parser> leftChildren() {
		return children();
	}

}
