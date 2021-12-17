package jcog.grammar.parse.examples.sling;





import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.examples.imperative.Command;
import jcog.grammar.parse.examples.imperative.CommandSequence;
import jcog.grammar.parse.examples.track.TrackException;
import jcog.grammar.parse.tokens.TokenAssembly;
import jcog.grammar.parse.tokens.Tokenizer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/*
 * Copyright (c) 2000 Steven J. Metsker. All Rights Reserved.
 */

/**
 * This class supports a SlingIde object, handling the interaction of the IDE's
 * components.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0
 */

public class SlingMediator implements ActionListener, ChangeListener, Runnable {

	private JButton goSource;
	private JButton haltSource;
	private JButton clearSource;
	private JSlider s1;
	private JSlider s2;
	private JTextArea programArea;
	private JTextArea messageArea;
	private SlingPanel plotPanel;

	private Command command;
	private Thread computeThread;

	private String lastProgram;

	private SlingTarget target;

	private SlingParser parser = new SlingParser();

	/**
	 * This method reacts, when the user presses one of the IDE's buttons.
	 * 
	 * @param ActionEvent
	 *            the event
	 */
	public void actionPerformed(ActionEvent e) {
		Object object = e.getSource();
		if (object.equals(goSource)) {
			go();
		}
		if (object.equals(clearSource)) {
			messageArea.setText("");
			lastProgram = null;
		}
		if (object == haltSource) {
			if (computeThread != null) {
				computeThread.stop();
			}
			computeThread = null;
			setComputing(false);
		}
	}

	/*
	 * Throws a runtime exception if the input stream cannot be completely
	 * recognized, and if the next element is a reserved word.
	 */
    private void checkReserved(Assembly in, Assembly out) {
		Assembly which = out == null ? in : out;
		if (which.hasNext()) {
			String s = which.peek().toString();
			if (parser.wors.getReservedWords().contains(s)) {
				throw new RuntimeException("> " + s + " is a reserved word");
			}
		}
	}

	/*
	 * Throws a runtime exception if the input stream cannot be completely
	 * recognized.
	 */
    private void checkResult(String program, Assembly in, Assembly out) {

		if (out == null || out.hasNext()) {
			checkReserved(in, out);
			if (out == null) {
				throw new RuntimeException("> Cannot parse " + program);
			}
			if (out.hasNext()) {
				throw new RuntimeException("> Input appears complete after : \n> " + out.consumed(" "));
			}
		}
	}

	/*
	 * This method returns a composite command that it composes by popping
	 * individual commands from an assembly.
	 */
    private static CommandSequence command(Assembly out) {
		Vector statements = new Vector();
		while (!out.getStack().isEmpty()) {
			statements.addElement(out.pop());
		}
		CommandSequence cs = new CommandSequence();
		int n = statements.size();
		for (int i = n - 1; i >= 0; i--) {
			Object o = statements.elementAt(i);
			try {
				cs.addCommand((Command) o);
			} catch (ClassCastException e) {
				throw new RuntimeException("Expected Command and found " + o.getClass());
			}
		}
		return cs;
	}

	/*
	 * Parse the user's program, if it has changed, into a command. Execute the
	 * command in a separate thread to allow halting. Plot whatever functions
	 * the program specifies.
	 */
    private void go() {
		try {
			String thisProgram = programArea.getText();
			if (!thisProgram.equals(lastProgram)) {
				lastProgram = thisProgram;
				command = parse(thisProgram);
				setComputing(true);
				computeThread = new Thread(this);
				computeThread.start();
			} else {
				plotPanel.setPlot(target.getRenderables());
			}
		} catch (Exception e) {
			String text = e.toString();
			if (e.getMessage() != null) {
				text = e.getMessage();
			}
			messageArea.append(text + '\n');
		}
	}

	/**
	 * Initialize this mediator with components from the a Sling IDE.
	 */
	public void initialize(JButton goSource, JButton haltSource, JButton clearSource, JSlider s1, JSlider s2, JTextArea programArea, JTextArea messageArea, SlingPanel plotPanel) {

		this.goSource = goSource;
		this.haltSource = haltSource;
		this.clearSource = clearSource;
		this.s1 = s1;
		this.s2 = s2;
		this.programArea = programArea;
		this.messageArea = messageArea;
		this.plotPanel = plotPanel;
	}

	/*
	 * Parse a program and return a command.
	 */
    private Command parse(String program) {
		Tokenizer t = parser.tokenizer();
		t.setString(program);
		TokenAssembly ta = new TokenAssembly(t);
		target = new SlingTarget(s1, s2);
		ta.setTarget(target);
		Assembly out = tryMatch(ta);
		checkResult(program, ta, out);
		return command(out);
	}

	/*
	 * Execcute the command in a separate thread. This allows, in particular,
	 * halting a large "for" loop.
	 */
	public void run() {
		try {
			command.execute();
			setComputing(false);
			plotPanel.setPlot(target.getRenderables());
		} catch (Exception e) {
			String text = e.toString();
			if (e.getMessage() != null) {
				text = e.getMessage();
			}
			messageArea.append(text + '\n');
			setComputing(false);
		}
	}

	/*
	 * Sets the state of the IDE to computing or not. Most of the IDE's controls
	 * are grayed out during computation of a program.
	 */
    private void setComputing(boolean computing) {

		

		goSource.setEnabled(!computing);
		clearSource.setEnabled(!computing);
		programArea.setEnabled(!computing);
		messageArea.setEnabled(!computing);

		haltSource.setEnabled(computing);
	}

	/*
	 * Any time a slider changes we re-plot.
	 */
	public void stateChanged(ChangeEvent e) {
		go();
	}

	/*
	 * Try to recognize a user's program. Handle some track exceptions but pass
	 * along any other exceptions.
	 */
    private Assembly tryMatch(TokenAssembly ta) {
		try {
			return parser.start().bestMatch(ta);
		} catch (TrackException e) {
			Vector rez = parser.wors.getReservedWords();
			if (rez.contains(e.getFound())) {
				throw new RuntimeException(e.getMessage() + "\n> " + e.getFound() + " is a reserved word");
			} else {
				throw e; 
			}
		}
	}
}
