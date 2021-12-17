package jcog.grammar.parse;

import jcog.Util;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * A <code>Terminal</code> is a parser that is not a composition of other
 * parsers. Terminals are "terminal" because they do not pass matching work on
 * to other parsers. The criterion that terminals use to check a match is
 * something other than another parser. Terminals are also the only parsers that
 * advance an assembly.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */

public class Terminal extends Parser {
	/*
	 * whether or not this terminal should push itself upon an assembly's stack
	 * after a successful match
	 */
	private boolean discard = false;

	/**
	 * Constructs an unnamed terminal.
	 */
	public Terminal() {
	}

	/**
	 * Constructs a terminal with the given name.
	 * 
	 * @param String
	 *            A name to be known by.
	 */
	public Terminal(String name) {
		super(name);
	}

	/**
	 * Accept a "visitor" and a collection of previously visited parsers.
	 * 
	 * @param ParserVisitor
	 *            the visitor to accept
	 * 
	 * @param Vector
	 *            a collection of previously visited parsers
	 */
	@Override
	public void accept(ParserVisitor pv, Set<Parser> visited) {
		pv.visitTerminal(this, visited);
	}

	/**
	 * By default, terminals push themselves upon a assembly's stack, after a
	 * successful match. This routine will turn off that behavior.
	 * 
	 * @return boolean true, if this terminal should push itself on a assembly's
	 *         stack
	 */
	public Terminal ok() {
		this.discard = true;
		return this;
	}

	/**
	 * Given a collection of assemblies, this method matches this terminal
	 * against all of them, and returns a new collection of the assemblies that
	 * result from the matches.
	 * 
	 * @return a Vector of assemblies that result from matching against a
	 *         beginning set of assemblies
	 * 
	 * @param Vector
	 *            a vector of assemblies to match against
	 * 
	 */
	@Override
	public Set<Assembly> match(Set<Assembly> in) {
		Set<Assembly> out = in.stream().map(this::matchOneAssembly).filter(Objects::nonNull).collect(Collectors.toSet());
		return out;
	}

	/**
	 * Returns an assembly equivalent to the supplied assembly, except that this
	 * terminal will have been removed from the front of the assembly. As with
	 * any parser, if the match succeeds, this terminal's assembler will work on
	 * the assembly. If the match fails, this method returns null.
	 * 
	 * @param Assembly
	 *            the assembly to match against
	 * 
	 * @return a copy of the incoming assembly, advanced by this terminal
	 */
	private Assembly matchOneAssembly(Assembly in) {
		if (in.hasNext() && qualifies(in.peek())) {
			Assembly out = in.clone();
			Object o = out.next();
			if (!discard) {
				out.push(elementToPushOnStack(o));
			}
			return out;
		}
		return null;
	}

	/**
	 * Default behaviour is to take original assembly element, e.g. the token,
	 * and return it
	 * 
	 * @param assembyObject
	 * @return
	 */
	protected Object elementToPushOnStack(Object assembyObject) {
		return assembyObject;
	}

	/**
	 * The mechanics of matching are the same for many terminals, except for the
	 * check that the next element on the assembly qualifies as the type of
	 * terminal this terminal looks for. This method performs that check.
	 * 
	 * @param Object
	 *            an element from a assembly
	 * 
	 * @return true, if the object is the kind of terminal this parser seeks
	 */
	protected boolean qualifies(Object o) {
		return true;
	}

	/*
	 * By default, create a collection with this terminal's string
	 * representation of itself. (Most subclasses override this.)
	 */
	@Override
	public List<String> randomExpansion(int maxDepth, int depth) {



		return List.of(this.toString());
	}

	public boolean isDiscarded() {
		return discard;
	}

	/*
	 * Returns a textual description of this parser.
	 */
	@Override
	protected String unvisitedString(Set<Parser> visited) {
		return "any";
	}

	public Iterable<Parser> children() {
		return Util.emptyIterable;
	}

	@Override
	public boolean isTerminal() {
		return true;
	}

}
