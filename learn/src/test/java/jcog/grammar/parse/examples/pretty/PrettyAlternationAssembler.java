package jcog.grammar.parse.examples.pretty;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;

/**
 * Replace a <code>ComponentNode</code> object on the stack
 * with a new composite that holds the popped node as its
 * only child.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
class PrettyAlternationAssembler implements IAssembler {
	private String name;

	/**
	 * Create an assembler that will replace a <code>ComponentNode
	 * </code> object on the stack with a new composite that holds 
	 * the popped node as its only child and whose name is as
	 * supplied here.
	 */
    PrettyAlternationAssembler(String name) {
		this.name = name;
	}

	/**
	 * Replace a <code>ComponentNode</code> object on the stack
	 * with a new composite that holds the popped node as its
	 * only child.
	 *
	 * @param   Assembly   the assembly to work on
	 */
	public void accept(Assembly a) {
		CompositeNode newNode = new CompositeNode(name);
		ComponentNode node = (ComponentNode) a.pop();
		newNode.insert(node);
		a.push(newNode);
	}
}