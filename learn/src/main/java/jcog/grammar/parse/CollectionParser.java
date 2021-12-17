package jcog.grammar.parse;

import jcog.data.list.Lst;
import jcog.grammar.parse.tokens.Symbol;

import java.util.List;
import java.util.Set;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * This class abstracts the behavior common to parsers that consist of a series
 * of other parsers.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
public abstract class CollectionParser extends Parser {
	/**
	 * the parsers this parser is a collection of
	 */
	public final List<Parser> subparsers = new Lst<>(1);

	/**
	 * Supports subclass constructors with no arguments.
	 */
	CollectionParser() {
	}

	/**
	 * Supports subclass constructors with a name argument
	 * 
	 * @param string
	 *            the name of this parser
	 */
	CollectionParser(String name) {
		super(name);
	}

	/**
	 * Adds a parser to the collection.
	 * 
	 * @param Parser
	 *            the parser to addAt
	 * 
	 * @return this
	 */
	public CollectionParser get(Parser e) {
		subparsers.add(e);
		return this;
	}

	/** match a character (but dont push it) */
	public CollectionParser see(char c) {
		return get(new Symbol(c).ok());
	}

	public CollectionParser addTop(Parser e) {
		subparsers.add(0, e);
		return this;
	}

	public Parser getChild(int index) {
		return subparsers.get(index);
	}

	/**
	 * Helps to textually describe this CollectionParser.
	 * 
	 * @returns the string to place between parsers in the collection
	 */
	protected abstract String toStringSeparator();

	/**
	 * Returns a textual description of this parser.
	 */
	@Override
	protected String unvisitedString(Set<Parser> visited) {
		StringBuilder buf = new StringBuilder("(");
		boolean needSeparator = false;
		for (Parser subparser : subparsers) {
			if (needSeparator) {
				buf.append(toStringSeparator());
			}
			buf.append(subparser.toString(visited));
			needSeparator = true;
		}
		buf.append(')');
		return buf.toString();
	}

	public Iterable<Parser> children() {
		return subparsers;
	}

	public CollectionParser or(Parser... p) {
		assert(p.length > 1);
		return get(new Alternation(p));
	}
}
