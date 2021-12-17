package jcog.grammar.parse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RuleReference extends Parser {

	private final String referencedRuleName;
	private final Grammar grammar;
	private String name;

	public RuleReference(String clauseName, Grammar containingGrammar) {
		this.referencedRuleName = clauseName;
		this.grammar = containingGrammar;
	}

	@Override
	public Assembly bestMatch(Assembly a) {
		return referencedParser().bestMatch(a);
	}

	private Parser referencedParser() {
		if (grammar.getRule(referencedRuleName) == null)
			throw new GrammarException("Reference to clause '" + referencedRuleName + "' cannot be resolved");
		return grammar.getRule(referencedRuleName);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		if (name != null) {
			result.append(name);
			result.append(": ");
		} else {
			result.append("ref to clause '").append(referencedRuleName).append('\'');
		}
		return result.toString();
	}

	public Iterable<Parser> children() {
		return List.of(referencedParser());
	}

	@Override
	public void accept(ParserVisitor pv, Set<Parser> visited) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Set<Assembly> match(Set<Assembly> in) {
		return new HashSet<>(referencedParser().matchAndAssemble(in));
	}

	@Override
	protected List<String> randomExpansion(int maxDepth, int depth) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	protected String unvisitedString(Set<Parser> visited) {
		return referencedRuleName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		RuleReference other = (RuleReference) obj;
		return other.referencedRuleName.equals(referencedRuleName) && (other.grammar == grammar);
	}

	@Override
	public Iterable<Parser> leftChildren() {
		return children();
	}

}
