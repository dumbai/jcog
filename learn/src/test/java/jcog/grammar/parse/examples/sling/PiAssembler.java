package jcog.grammar.parse.examples.sling;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;

/** 
 * Pushes the function (t, pi).
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
class PiAssembler implements IAssembler {
	/**
	 * Push the function (t, pi).
	 *
	 * @param  Assembly  the assembly to work on
	 */
	public void accept(Assembly a) {
		a.push(new Cartesian(new T(), new Point(0, Math.PI)));
	}
}
