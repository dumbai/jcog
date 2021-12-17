package jcog.grammar.parse.examples.coffee;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;

/**
 * This assembler sets a target coffee object boolean that
 * indicates the type of coffee also comes in a french
 * roast.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
class AlsoFrenchAssembler implements IAssembler {
	/** 
	 * Set a target coffee object's boolean to indicate that this 
	 * type of coffee also comes in a french roast.
	 *
	 * @param  Assembly  the assembly to work on
	 */
	public void accept(Assembly a) {
		Coffee c = (Coffee) a.getTarget();
		c.setAlsoOfferFrench(true);
	}
}
