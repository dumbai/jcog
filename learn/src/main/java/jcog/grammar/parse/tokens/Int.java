package jcog.grammar.parse.tokens;

import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Terminal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Int extends Terminal {

	@Override
	protected boolean qualifies(Object o) {
		Token t = (Token) o;
		if (!t.isNumber()) {
			return false;
		}
		BigDecimal number = (BigDecimal) t.value();
		return number.scale() <= 0;
	}

	@Override
	public List<String> randomExpansion(int maxDepth, int depth) {
		int d = (int) Math.floor(100 * Math.random());
		List<String> v = new ArrayList<>();
		v.add(Integer.toString(d));
		return v;
	}

	@Override
	public String unvisitedString(Set<Parser> visited) {
		return "Int";
	}

	@Override
	protected Object elementToPushOnStack(Object assemblyObject) {
		BigDecimal value = (BigDecimal) ((Token) assemblyObject).value();

		return new Token(value.toBigInteger());
	}

}
