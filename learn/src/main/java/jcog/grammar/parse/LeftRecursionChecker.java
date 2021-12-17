package jcog.grammar.parse;

import java.util.Set;

public class LeftRecursionChecker {

	private final Grammar grammar;

	public LeftRecursionChecker(Grammar grammar) {
		this.grammar = grammar;
	}

	public void check() {
		Set<Parser> allParsers = ParserCollector.collectAllReferencedParsers(grammar.startParser());
		for (Parser each : allParsers) {
			checkLeftRecursionFor(each);
		}
	}

	private static void checkLeftRecursionFor(Parser candidate) {
		Set<Parser> allLeftChildren = ParserCollector.collectLeftChildren(candidate);
		if (allLeftChildren.contains(candidate))
			throw new GrammarException("Left recursion detected for: " + candidate);
	}

}
