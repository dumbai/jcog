package jcog.grammar.parse;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

class ParserCollector {

	private final Function<Parser, Iterable<Parser>> collectable;

	private ParserCollector(Function<Parser, Iterable<Parser>> collectable) {
		this.collectable = collectable;
	}

	public static Set<Parser> collectAllReferencedParsers(Parser parentParser) {
		return new ParserCollector(Parser::children).collect(parentParser);
	}

	private Set<Parser> collect(Parser root) {
		Set<Parser> collector = new LinkedHashSet<>();
		collect(root, collector);
		return collector;
	}

	private void collect(Parser parent, Set<Parser> collector) {
		Iterable<Parser> newEntries = collectable.apply(parent);
		for (Parser each : newEntries) {
			if (collector.add(each))
				collect(each, collector);
		}
	}

	public static Set<Parser> collectLeftChildren(Parser candidate) {
		return new ParserCollector(Parser::leftChildren).collect(candidate);
	}

}
