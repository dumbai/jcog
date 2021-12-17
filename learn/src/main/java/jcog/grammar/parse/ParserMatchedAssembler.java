package jcog.grammar.parse;

import java.util.List;
import java.util.Stack;
import java.util.function.BiConsumer;

public class ParserMatchedAssembler implements IAssembler {

	private final BiConsumer<List, Stack> matchedRule;

	public ParserMatchedAssembler(BiConsumer<List,Stack> matchedRule) {
		this.matchedRule = matchedRule;
	}

	public void accept(Assembly a) {
		List matches = a.popAllMatches();
		matchedRule.accept(matches, a.getStack());
	}

}
