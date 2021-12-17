package jcog.grammar.parse.examples.pretty;

import jcog.grammar.parse.AssemblerHelper;
import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;

import java.util.List;

/**
 * Replace the nodes above a given "fence" object with a new composite that
 * holds the popped nodes as its children.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
class PrettyRepetitionAssembler implements IAssembler {
	private String name;
	private Object fence;

	/**
	 * Construct an assembler that will replace the nodes above the supplied
	 * "fence" object with a new composite that will hold the popped nodes as
	 * its children.
	 */
    PrettyRepetitionAssembler(String name, Object fence) {
		this.name = name;
		this.fence = fence;
	}

	/**
	 * Replace the nodes above a given "fence" object with a new composite that
	 * holds the popped nodes as its children.
	 * 
	 * @param Assembly
	 *            the assembly to work on
	 */
	public void accept(Assembly a) {
		CompositeNode newNode = new CompositeNode(name);
		List<Object> v = AssemblerHelper.elementsAbove(a, fence);
		for (Object each : v) {
			newNode.add((ComponentNode) each);

		}
		a.push(newNode);
	}
}