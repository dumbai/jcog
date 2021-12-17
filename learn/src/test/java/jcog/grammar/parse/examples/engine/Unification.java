package jcog.grammar.parse.examples.engine;

import jcog.data.set.ArrayHashSet;

import java.util.Iterator;
import java.util.StringJoiner;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * A Unification is a collection of variables.
 * 
 * Structures and variables use unifications to keep track of the variable
 * assignments that make a proof work. The unification class itself provides
 * behavior for adding and accessing variables.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */

public class Unification {
	public static final Unification empty = new Unification();
	private final ArrayHashSet<Variable> variables = new ArrayHashSet();

	/**
	 * Creates an empty unification.
	 */
	public Unification() {
	}

	/**
	 * Creates a unification that starts off including a single variable.
	 * 
	 * @param Variable
	 *            the variable with which the unification begins
	 */
	public Unification(Variable v) {
		addVariable(v);
	}

	/**
	 * Adds a variable to this unification.
	 * 
	 * @param Variable
	 *            the variable to add to this unification
	 * 
	 * @return this unification
	 */
    private Unification addVariable(Variable v) {
		variables.add(v);
		return this;
	}

	/**
	 * Adds all the variables of another unification to this one.
	 * 
	 * @param Unification
	 *            the unification to append
	 * 
	 * @return this unification
	 */
	public Unification append(Unification u) {
		int s = u.size();
		for (int i = 0; i < s; i++)
			addVariable(u.variableAt(i));
		return this;
	}

	/**
	 * Return the variables in this unification.
	 * 
	 * @return the variables in this unification.
	 */
	public Iterator<Variable> iterator() {
		return variables().iterator();
	}

	/**
	 * Returns the number of variables in this unification.
	 * 
	 * @return int the number of variables in this unification
	 */
	public int size() {
		if (variables == null) {
			return 0;
		}
		return variables.size();
	}

	/**
	 * Returns a string representation of this unification.
	 * 
	 * @return a string representation of this unification
	 */
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ");
		int bound = size();
		for (int i = 0; i < bound; i++) {
			String s = variableAt(i).definitionString();
			joiner.add(s);
		}
		String buf = joiner.toString();
        return buf;
	}

	/**
	 * Returns a string representation of this unification, without printing
	 * variable names.
	 * 
	 * @return a string representation of this unification, without printing
	 *         variable names
	 */
	public String toStringQuiet() {
		StringJoiner joiner = new StringJoiner(", ");
		int bound = size();
		for (int i = 0; i < bound; i++) {
			String s = String.valueOf(variableAt(i));
			joiner.add(s);
		}
		String buf = joiner.toString();
        return buf;
	}

	/**
	 * Asks all the contained variables to unbind.
	 */
	public void unbind() {
//		for (int i = 0; i < size(); i++) {
//			variableAt(i).unbind();
//		}
		for (Variable variable : variables) {
			variable.unbind();
		}
	}

	/*
	 * Returns the variable at the indicated index.
	 * 
	 * @param int the index of the variable to return
	 * 
	 * @return variable the variable at the indicated index
	 */
    private Variable variableAt(int i) {
		return variables.get(i);
	}

	/*
	 * lazy-initialize this unification's vector
	 */
    private ArrayHashSet<Variable> variables() {
//		if (variables == null) {
//			variables = new ArrayList<Variable>();
//		}
		return variables;
	}
}
