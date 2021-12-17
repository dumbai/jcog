package jcog.grammar.parse;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * Copyright (c) 2000 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * This class generates random language elements for a parser and tests that the
 * parser can accept them.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
public abstract class ParserTester {
	private final Parser p;
	private boolean logTestStrings = true;

	/**
	 * Constructs a tester for the given parser.
	 */
	protected ParserTester(Parser p) {
		this.p = p;
	}

	private static boolean test(Assembly a) {
		return !a.hasNext();
	}

	/*
	 * Subclasses must override this, to produce an assembly from the given
	 * (random) string.
	 */
	protected abstract Assembly assembly(String s);

	/*
	 * Generate a random language element, and return true if the parser cannot
	 * unambiguously parse it.
	 */
	private boolean canGenerateProblem(int depth) {
		String s = p.randomInput(depth, separator());
		logTestString(s);
		Assembly a = assembly(s);
		a.setTarget(freshTarget());
		Set<Assembly> in = new HashSet<>();
		in.add(a);
		Set<Assembly> out = completeMatches(p.match(in));
		if (out.size() != 1) {
			logProblemFound(s, out.size());
			return true;
		}
		return false;
	}

	/**
	 * Return a subset of the supplied vector of assemblies, filtering for
	 * assemblies that have been completely matched.
	 * 
	 * @param Vector
	 *            a collection of partially or completely matched assemblies
	 * 
	 * @return a collection of completely matched assemblies
	 */
	private static Set<Assembly> completeMatches(Set<Assembly> in ) {
		Set<Assembly> out =null==in? new HashSet<>() :in;
		Set<Assembly> set = out.stream().filter(ParserTester::test).collect(Collectors.toSet());
		return set;
	}

	/*
	 * Give subclasses a chance to provide fresh target at the beginning of a
	 * parse.
	 */
	protected PubliclyCloneable<?> freshTarget() {
		return null;
	}

	/*
	 * This method is broken out to allow subclasses to create less verbose
	 * tester, or to direct logging to somewhere other than System.out.
	 */
	private static void logDepthChange(int depth) {
		System.out.println("Testing depth " + depth + "...");
	}

	/*
	 * This method is broken out to allow subclasses to create less verbose
	 * tester, or to direct logging to somewhere other than System.out.
	 */
	private static void logPassed() {
		System.out.println("No problems found.");
	}

	/*
	 * This method is broken out to allow subclasses to create less verbose
	 * tester, or to direct logging to somewhere other than System.out.
	 */
	private static void logProblemFound(String s, int matchSize) {
		System.out.println("Problem found for string:");
		System.out.println(s);
		if (matchSize == 0) {
			System.out.println("Parser cannot match this apparently " + "valid string.");
		} else {
			System.out.println("The parser found " + matchSize + " ways to parse this string.");
		}
	}

	/*
	 * This method is broken out to allow subclasses to create less verbose
	 * tester, or to direct logging to somewhere other than System.out.
	 */
	private void logTestString(String s) {
		if (logTestStrings) {
			System.out.println("    Testing string " + s);
		}
	}

	/*
	 * By default, place a blank between randomly generated "words" of a
	 * language.
	 */
	protected String separator() {
		return " ";
	}

	/**
	 * Set the boolean which determines if this class displays every test
	 * string.
	 * 
	 * @param boolean true, if the user wants to see every test string
	 */
	public void setLogTestStrings(boolean logTestStrings) {
		this.logTestStrings = logTestStrings;
	}

	/**
	 * Create a series of random language elements, and test that the parser can
	 * unambiguously parse each one.
	 */
	public void test() {
		for (int depth = 2; depth < 8; depth++) {
			logDepthChange(depth);
			for (int k = 0; k < 100; k++) {
				if (canGenerateProblem(depth)) {
					return;
				}
			}
		}
		logPassed();
	}
}
