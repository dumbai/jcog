package jcog.grammar.parse.examples.imperative;

import jcog.grammar.parse.examples.engine.Term;

import java.io.PrintWriter;

/*
 * Copyright (c) 2000 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * This command, when executed, prints out the value of a term provided in the
 * constructor.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */
public class PrintlnCommand extends Command {
	private Term term;
	private PrintWriter out;

	/**
	 * Construct a "print" command to print the supplied term.
	 * 
	 * @param Term
	 *            the term to print
	 */
	public PrintlnCommand(Term term) {
		this(term, new PrintWriter(System.out));
	}

	/**
	 * Construct a "print" command to print the supplied term, printing to the
	 * supplied <code>PrintWriter</code> object.
	 * 
	 * @param Term
	 *            the term to print
	 * 
	 * @param PrintWriter
	 *            where to print
	 */
    private PrintlnCommand(Term term, PrintWriter out) {
		this.term = term;
		this.out = out;
	}

	/**
	 * Print the value of this object's term onto the output writer.
	 */
	public void execute() {
		out.print(term.eval() + "\n");
		out.flush();
	}

	/**
	 * Returns a string description of this print command.
	 * 
	 * @return a string description of this print command
	 */
	public String toString() {
		return "println(" + term + ')';
	}
}
