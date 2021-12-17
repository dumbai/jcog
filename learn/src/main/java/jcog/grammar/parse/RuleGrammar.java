package jcog.grammar.parse;

import jcog.grammar.parse.tokens.*;

import java.util.Set;
import java.util.regex.Pattern;

public class RuleGrammar extends Grammar {

	private final Grammar targetGrammar;

	public RuleGrammar(Grammar targetGrammar) {
		super("rule grammar");
		this.targetGrammar = targetGrammar;
		intTerminalTypes();
		initRules();
	}

	private void intTerminalTypes() {
		registerTerminal(SymbolDef.class);
		registerTerminal(CaselessLiteralDef.class);
	}

	private void initRules() {

		addRule("rule", seq(new LowerCaseWord(), sym("=").ok(), ref("expression"), alt(sym(";").ok(), new Empty())));
		addAssembler("rule", new RuleAssembler());

		addRule("expression", seq(ref("sequence"), rep(ref("alternationSequence"))));

		addRule("sequence", alt(ref("factor"), ref("sequenceFactor")));

		addRule("sequenceFactor", seq(ref("factor"), ref("sequence")));
		addAssembler("sequenceFactor", new SequenceAssembler());

		addRule("alternationSequence", seq(sym("|").ok(), (ref("sequence"))));
		addAssembler("alternationSequence", new AlternationAssembler());

		addRule("factor", alt(ref("phrase"), ref("repetition"), ref("atLeastOne")));

		addRule("repetition", seq(ref("phrase"), sym("*").ok()));
		addAssembler("repetition", new RepetitionAssembler(0));

		addRule("atLeastOne", seq(ref("phrase"), sym("+").ok()));
		addAssembler("atLeastOne", new RepetitionAssembler(1));

		addRule("phrase", alt(seq(sym("(").ok(), ref("expression"), sym(")").ok()), ref("atom")));

		addRule("atom", alt(ref("caseless"), ref("symbol"), ref("discard"), ref("reference"), ref("terminal")));

		addRule("discard", seq(sym("#").ok(), alt(ref("symbol"), ref("caseless"))));
		addAssembler("discard", new DiscardAssembler());

		addRule("caseless", terminal("CaselessLiteralDef"));
		addAssembler("caseless", new CaselessAssembler());

		addRule("symbol", terminal("SymbolDef"));
		addAssembler("symbol", new SymbolAssembler());

		addRule("reference", terminal("LowerCaseWord"));
		addAssembler("reference", new ReferenceAssembler());

		addRule("terminal", terminal("UpperCaseWord"));
		addAssembler("terminal", new TerminalAssembler());
	}

	@Override
	public String defineRule(String ruleText) {
		throw new GrammarException("addTextualRule() does not work in RuleGrammar to prevent infinite recursion.");
	}

	private static Repetition rep(Parser parser) {
		return new Repetition(parser);
	}

	private RuleReference ref(String ruleName) {
		return new RuleReference(ruleName, this);
	}

	private static Seq seq(Parser... parsers) {
		Seq seq = new Seq();
		for (Parser each : parsers) {
			seq.get(each);
		}
		return seq;
	}

	private static Alternation alt(Parser... parsers) {
		Alternation alt = new Alternation();
		for (Parser each : parsers) {
			alt.get(each);
		}
		return alt;
	}

	private static Symbol sym(String symbol) {
		return new Symbol(symbol);
	}

	static class SequenceAssembler implements IAssembler {
		public void accept(Assembly a) {
			Parser last = (Parser) a.pop();
			Parser butlast = (Parser) a.pop();
			Seq seq;
			seq = last instanceof Seq ? (Seq) last : seq(last);
			seq.addTop(butlast);
			a.push(seq);
		}
	}

	static class AlternationAssembler implements IAssembler {
		public void accept(Assembly a) {
			Parser last = (Parser) a.pop();
			Parser butlast = (Parser) a.pop();
			Alternation alt;
			alt = butlast instanceof Alternation ? (Alternation) butlast : alt(butlast);
			alt.get(last);
			a.push(alt);
		}
	}

	class RuleAssembler implements IAssembler {
		public void accept(Assembly a) {
			if (a.elementsRemaining() > 0)
				return;
			Parser parser = (Parser) a.pop();
			String ruleName = ((Token) a.pop()).sval();
			targetGrammar.addRule(ruleName, parser);
			a.push(ruleName);
		}
	}

	class CaselessAssembler implements IAssembler {
		public void accept(Assembly a) {
			String quoted = ((Token) a.pop()).sval();
			String caseless = quoted.substring(1, quoted.length() - 1);
			CaselessLiteral constant = new CaselessLiteral(caseless);
			if (targetGrammar.areAllConstantsDiscarded())
				constant.ok();
			a.push(constant);
		}
	}

	static class DiscardAssembler implements IAssembler {
		public void accept(Assembly a) {
			Terminal t = (Terminal) a.getStack().peek();
			t.ok();
		}
	}

	class SymbolAssembler implements IAssembler {
		public void accept(Assembly a) {
			String quoted = ((Token) a.pop()).sval();
			String symbol = quoted.substring(1, quoted.length() - 1);
			Symbol constant = new Symbol(symbol);
			if (targetGrammar.areAllConstantsDiscarded())
				constant.ok();
			a.push(constant);
		}
	}

	class TerminalAssembler implements IAssembler {
		public void accept(Assembly a) {
			String terminalType = ((Token) a.pop()).sval();
			a.push(targetGrammar.terminal(terminalType));
		}
	}

	class ReferenceAssembler implements IAssembler {
		public void accept(Assembly a) {
			String ruleName = ((Token) a.pop()).sval();
			a.push(new RuleReference(ruleName, targetGrammar));
		}
	}

	static class RepetitionAssembler implements IAssembler {
		private final int requiredMatches;

		RepetitionAssembler(int requiredMatches) {
			this.requiredMatches = requiredMatches;
		}

		public void accept(Assembly a) {
			Parser atomicStep = (Parser) a.pop();
			a.push(new Repetition(atomicStep).requireMatches(requiredMatches));
		}
	}
}

class CaselessLiteralDef extends QuotedString {
	private static final Pattern CASELESS = Pattern.compile("\"\\w+\"");

	@SuppressWarnings("PublicConstructorInNonPublicClass")
	public CaselessLiteralDef() {

	}

	@Override
	protected boolean qualifies(Object o) {
		if (!super.qualifies(o))
			return false;
		Token t = (Token) o;
		String quotedString = t.sval();
        return CASELESS.matcher(quotedString).matches();
    }

	@Override
	public String unvisitedString(Set<Parser> visited) {
		return "CaselessLiteralDef";
	}

	@Override
	public boolean isConstant() {
		return true;
	}

}

class SymbolDef extends QuotedString {
	private static final Pattern SYMBOL = Pattern.compile("'.'");

	@SuppressWarnings("PublicConstructorInNonPublicClass")
	public SymbolDef() { }

	@Override
	protected boolean qualifies(Object o) {
		if (!super.qualifies(o))
			return false;
		Token t = (Token) o;
		String quotedString = t.sval();
        return SYMBOL.matcher(quotedString).matches();
    }

	@Override
	public String unvisitedString(Set<Parser> visited) {
		return "SymbolDef";
	}

	@Override
	public boolean isConstant() {
		return true;
	}

}