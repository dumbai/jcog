package jcog.grammar.parse;

import java.util.Stack;

public interface IParsingResult {

	boolean isCompleteMatch();

	Stack<Object> getStack();

	PubliclyCloneable<?> getTarget();

}
