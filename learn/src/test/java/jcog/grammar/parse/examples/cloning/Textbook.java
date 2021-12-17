package jcog.grammar.parse.examples.cloning;

import jcog.grammar.parse.PubliclyCloneable;

/** 
 * This class just supports the <code>ThisClass</code>
 * example of a typical clone.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class Textbook implements PubliclyCloneable<Textbook> {
	/**
	 * Return a copy of this object.
	 *
	 * @return a copy of this object
	 */
	public Textbook clone() {
		try {
			return (Textbook) super.clone();
		} catch (CloneNotSupportedException e) {
			
			throw new InternalError();
		}
	}
}
