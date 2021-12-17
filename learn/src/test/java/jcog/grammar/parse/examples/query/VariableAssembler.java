package jcog.grammar.parse.examples.query;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;
import jcog.grammar.parse.examples.engine.Variable;
import jcog.grammar.parse.tokens.Token;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * This assembler pops a token from the stack, extracts
 * its string, and pushes a <code>Variable</code> of that
 * name. This assembler also looks up the name in a
 * <code>ChipSpeller</code>, and throws a runtime
 * exception if this variable name is unknown.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
class VariableAssembler implements IAssembler {
	private Speller speller;

	/**
	 * Construct a VariableAssembler that will consult the
	 * given speller for the proper spelling of variable names.
	 */
    VariableAssembler(Speller speller) {
		this.speller = speller;
	}

	/**
	 * Pop a token from the stack, extract its string, and push
	 * a <code>Variable</code> of that name. Check the spelling
	 * of the name with the speller provided in the constructor.
	 */
	public void accept(Assembly a) {
		Token t = (Token) a.pop();
		String properName = speller.getVariableName(t.sval());
		if (properName == null) {
			throw new UnrecognizedVariableException("No variable named " + t.sval() + " in object model");
		}
		a.push(new Variable(properName));
	}
}