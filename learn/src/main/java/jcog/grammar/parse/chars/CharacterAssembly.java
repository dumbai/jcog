package jcog.grammar.parse.chars;

import jcog.grammar.parse.Assembly;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * A CharacterAssembly is an Assembly whose elements are characters.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 * 
 * @see Assembly
 */

public class CharacterAssembly extends Assembly {
	/**
	 * the string to consume
	 */
	private final String string;

	/**
	 * Constructs a CharacterAssembly from the given String.
	 * 
	 * @param String
	 *            the String to consume
	 * 
	 * @return a CharacterAssembly that will consume the supplied String
	 */
	public CharacterAssembly(String string) {
		this.string = string;
	}

	/**
	 * Returns a textual representation of the amount of this characterAssembly
	 * that has been consumed.
	 * 
	 * @param delimiter
	 *            the mark to show between consumed elements
	 * 
	 * @return a textual description of the amount of this assembly that has
	 *         been consumed
	 */
	@Override
	public String consumed(String delimiter) {
		if (delimiter.isEmpty()) {
			return string.substring(0, elementsConsumed());
		}
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < elementsConsumed(); i++) {
			if (i > 0) {
				buf.append(delimiter);
			}
			buf.append(string.charAt(i));
		}
		return buf.toString();
	}

	/**
	 * Returns the default string to show between elements consumed or
	 * remaining.
	 * 
	 * @return the default string to show between elements consumed or remaining
	 */
	@Override
	public String defaultDelimiter() {
		return "";
	}

	/**
	 * Returns the number of elements in this assembly.
	 * 
	 * @return the number of elements in this assembly
	 */
	@Override
	protected int length() {
		return string.length();
	}

	/**
	 * Returns the next character.
	 * 
	 * @return the next character from the associated token string
	 * 
	 * @exception ArrayIndexOutOfBoundsException
	 *                if there are no more characters in this assembly's string
	 */
	public Object next() {
		return string.charAt(index++);
	}

	/**
	 * Shows the next object in the assembly, without removing it
	 * 
	 * @return the next object
	 */
	@Override
	public Object peek() {
		return index < length() ? string.charAt(index) : null;
	}

	/**
	 * Returns a textual representation of the amount of this characterAssembly
	 * that remains to be consumed.
	 * 
	 * @param delimiter
	 *            the mark to show between consumed elements
	 * 
	 * @return a textual description of the amount of this assembly that remains
	 *         to be consumed
	 */
	@Override
	public String remainder(String delimiter) {
		if (delimiter.isEmpty()) {
			return string.substring(elementsConsumed());
		}
		StringBuilder buf = new StringBuilder();
		for (int i = elementsConsumed(); i < string.length(); i++) {

			if (i > elementsConsumed()) {
				buf.append(delimiter);
			}
			buf.append(string.charAt(i));
		}
		return buf.toString();
	}
}
