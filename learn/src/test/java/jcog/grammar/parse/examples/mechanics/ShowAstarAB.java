package jcog.grammar.parse.examples.mechanics;

import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Repetition;
import jcog.grammar.parse.Seq;
import jcog.grammar.parse.chars.CharacterAssembly;
import jcog.grammar.parse.chars.SpecificChar;

import java.util.HashSet;
import java.util.Set;

/**
 * This class shows that the "right" answer for a repetition 
 * object is not always to match all that it can.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class ShowAstarAB {
	/** 
	 * This class shows that the "right" answer for a repetition 
	 * object is not always to match all that it can.
	 */
	public static void main(String[] args) {

		Parser aStar = new Repetition(new SpecificChar('a'));

		Parser ab = new Seq().get(new SpecificChar('a')).get(new SpecificChar('b')); 

		Parser aStarAB = new Seq().get(aStar).get(ab); 

		Set<Assembly> v = new HashSet<>();
		v.add(new CharacterAssembly("aaaab"));

		System.out.println(aStar.match(v));
		System.out.println(ab.match(aStar.match(v)));
		System.out.println(aStarAB.match(v));
	}
}
