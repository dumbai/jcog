package jcog.grammar.parse.examples.pretty;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;

/**
 * Replace a given number of nodes on the stack with a new 
 * composite that holds the popped nodes as its children.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
class PrettySequenceAssembler implements IAssembler {
	private String name;
	private int numberNodes;

	/**
	 * Construct an assembler that will replace a given number of 
	 * nodes on the stack with a new composite that holds the 
	 * popped nodes as its children.
	 */
    PrettySequenceAssembler(String name, int numberNodes) {
		this.name = name;
		this.numberNodes = numberNodes;
	}

	/**
	 * Replace a given number of nodes on the stack with a new 
	 * composite that holds the popped nodes as its children.
	 *
	 * @param   Assembly   the assembly to work on
	 */
	public void accept(Assembly a) {
		CompositeNode newNode = new CompositeNode(name);
		for (int i = 0; i < numberNodes; i++) {
			ComponentNode node = (ComponentNode) a.pop();
			newNode.insert(node);
		}
		a.push(newNode);
	}
}