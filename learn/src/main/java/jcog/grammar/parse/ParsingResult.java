package jcog.grammar.parse;

import java.util.Stack;

public class ParsingResult implements IParsingResult {

	private final Assembly result;

	public ParsingResult(Assembly result) {
		this.result = result;
	}

	public boolean isCompleteMatch() {
		if (result == null)
			return false;
		return result.elementsRemaining() == 0;
	}

	public Stack getStack() {
		if (result == null)
			return new Stack();

		return result.getStack();
	}

	public PubliclyCloneable<?> getTarget() {
		if (result == null)
			return null;
		return result.getTarget();
	}

	@Override
	public String toString() {
		if (result == null)
			return "null parsing result";
		return result.toString();
	}

}
