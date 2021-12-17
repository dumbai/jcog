package jcog.grammar.parse.chars;

import jcog.grammar.parse.Parser;
import jcog.grammar.parse.Terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * A Letter matches any letter from a character assembly.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
public class Letter extends Terminal {

	/**
	 * Returns true if an assembly's next element is a letter.
	 * 
	 * @param object
	 *            an element from an assembly
	 * 
	 * @return true, if an assembly's next element is a letter
	 */
	@Override
	public boolean qualifies(Object o) {
		return Character.isLetter((Character) o);
	}

	/**
	 * Create a set with one random letter.
	 */
	@Override
	public List<String> randomExpansion(int maxDepth, int depth) {
		char c = (char) (ThreadLocalRandom.current().nextInt(26) + 'a');
		List<String> v = new ArrayList<>(1);
		v.add(String.valueOf(c));
		return v;
	}

	/**
	 * Returns a textual description of this parser.
	 * 
	 * @param vector
	 *            a list of parsers already printed in this description
	 * 
	 * @return string a textual description of this parser
	 * 
	 * @see Parser#toString()
	 */
	@Override
	public String unvisitedString(Set<Parser> visited) {
		return "L";
	}
}