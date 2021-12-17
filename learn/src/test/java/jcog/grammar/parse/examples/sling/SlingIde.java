package jcog.grammar.parse.examples.sling;






import jcog.grammar.parse.examples.utensil.SwingUtensil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * This class provides an interactive development environment
 * for the Sling programming language.
 * <p>
 * This class contains just the Swing components, and
 * delegates responsibility for the interaction of these
 * components to a SlingMediator object. 
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class SlingIde {
	private SlingMediator mediator;
	private JButton clearButton;
	private JButton goButton;
	private JButton haltButton;
	private JTextArea messageArea;
	private JTextArea programArea;
	private JSlider s1;
	private JSlider s2;
	private SlingPanel plotPanel;
	private int preferredWidth = 500;
	private Dimension min = new Dimension(preferredWidth, 30);

	/*
	 * Creates and returns the box that contains the IDE's
	 * buttons.
	 */
	protected Box buttonBox() {
		Box b = Box.createHorizontalBox();
		b.add(goButton());
		b.add(haltButton());
		b.add(Box.createHorizontalGlue());
		b.add(clearButton());
		return b;
	}

	/*
	 * Creates and returns the box that contains the IDE's
	 * buttons.
	 */
    private JPanel buttonPanel() {
		JPanel upperButtons = new JPanel();
		upperButtons.setLayout(new BorderLayout());
		upperButtons.add(goButton(), "Center");
		upperButtons.add(haltButton(), "South");

		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
		p.setLayout(new BorderLayout());
		p.add(upperButtons, "North");
		p.add(clearButton(), "South");
		return p;
	}

	/*
	 * The button that clears the message area.
	 */
    private JButton clearButton() {
		if (clearButton == null) {
			clearButton = new JButton("Clear");
			clearButton.addActionListener(mediator());
			clearButton.setFont(SwingUtensil.ideFont());
		}
		return clearButton;
	}

	/*
	 * The button that starts the proof thread.
	 */
    private JButton goButton() {
		if (goButton == null) {
			goButton = new JButton("Go!");
			goButton.addActionListener(mediator());
			goButton.setFont(SwingUtensil.ideFont());

			
			KeyStroke ctrlg = KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK);

			goButton.registerKeyboardAction(mediator(), ctrlg, JComponent.WHEN_IN_FOCUSED_WINDOW);
		}
		return goButton;
	}

	/*
	 * The button that halts the proof thread.
	 */
    private JButton haltButton() {
		if (haltButton == null) {
			haltButton = new JButton("Halt");
			haltButton.setEnabled(false);
			haltButton.addActionListener(mediator());
			haltButton.setFont(SwingUtensil.ideFont());
		}
		return haltButton;
	}

	/*
	 * The panel with the program/message split pane and the
	 * button panel.
	 */
    private JPanel lowerPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(programMessagePane(), "Center");
		p.add(buttonPanel(), ("East"));
		return p;
	}

	/**
	 * Launch a Sling interactive development environment.
	 */
	public static void main(String[] args) {
		SwingUtensil.launch(new SlingIde().mainPane(), "Sling");
	}

	/*
	 * A split pane that contains, ultimately, all the components
	 * in the IDE.
	 */
    private JSplitPane mainPane() {

		JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, upperPanel(), lowerPanel());

		sp.setDividerSize(6);
		return sp;
	}

	/*
	 * The object that controls or "mediates" the interaction
	 * of this development environment's Swing components.
	 */
    private SlingMediator mediator() {
		if (mediator == null) {
			mediator = new SlingMediator();
			mediator.initialize(goButton(), haltButton(), clearButton(), s1(), s2(), programArea(), messageArea(), plotPanel());
		}
		return mediator;
	}

	/*
	 * The message text area.
	 */
    private JTextArea messageArea() {
		if (messageArea == null) {
			messageArea = SwingUtensil.ideTextArea();
		}
		return messageArea;
	}

	/*
	 * The <code>SlingPanel</code> where plotting occurs.
	 */
    private SlingPanel plotPanel() {
		if (plotPanel == null) {
			plotPanel = new SlingPanel();
			plotPanel.setPreferredSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		}
		return plotPanel;
	}

	/*
	 * The program text area.
	 */
    private JTextArea programArea() {
		if (programArea == null) {
			programArea = SwingUtensil.ideTextArea();
		}
		return programArea;
	}

	/*
	 * The split pane that contains the program panel and the
	 * message panel.
	 */
    private JSplitPane programMessagePane() {

		JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, titledProgramPanel(), titledMessagePanel());
		inner.setDividerSize(6);

		return inner;
	}

	/*
	 * The first slider.
	 */
    private JSlider s1() {
		if (s1 == null) {
			s1 = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 100);
			s1.addChangeListener(mediator());
		}
		return s1;
	}

	/*
	 * The second slider.
	 */
    private JSlider s2() {
		if (s2 == null) {
			s2 = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 100);
			s2.addChangeListener(mediator());
		}
		return s2;
	}

	/*
	 * Creates a slider that the mediator will listen to.
	 */
	protected JSlider slider() {
		JSlider s = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 100);
		s.addChangeListener(mediator());
		return s;
	}

	/*
	 * Creates and returns the box that contains a slider.
	 */
    private static Box sliderBox(String name, JSlider s) {
		Box b = Box.createHorizontalBox();
		JLabel label = new JLabel(name);
		label.setFont(SwingUtensil.ideFont());
		label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		label.setForeground(Color.black);
		b.add(label);
		b.add(s);
		return b;
	}

	/*
	 * The panel that contains the slider boxes.
	 */
    private JPanel sliderPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		Box b = Box.createVerticalBox();
		b.add(sliderBox("s1", s1()));
		b.add(Box.createVerticalGlue());
		b.add(sliderBox("s2", s2()));

		p.setBorder(BorderFactory.createCompoundBorder(SwingUtensil.ideTitledBorder("Sliding Variables"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		p.add(b, "Center");

		return p;
	}

	/*
	 * Forms and returns a standard text panel, which is
	 * a scroll pane around a text area, with a title border.
	 */
	private static JPanel textPanel(String title, JTextArea ta, Dimension pref, Dimension min) {

		
		JScrollPane s1 = new JScrollPane(ta);
		s1.setPreferredSize(pref);
		s1.setMinimumSize(min);

		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBorder(SwingUtensil.ideTitledBorder(title));
		p.add(s1, "Center");
		return p;
	}

	/*
	 * The panel that wraps a title around the message area.
	 */
    private JPanel titledMessagePanel() {

		return textPanel("Messages", messageArea(), new Dimension(preferredWidth, 60), min);
	}

	/*
	 * The panel that wraps a title around the plot panel.
	 */
    private JPanel titledPlotPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBorder(BorderFactory.createCompoundBorder(SwingUtensil.ideTitledBorder("Plot"), BorderFactory.createEmptyBorder(0, 5, 10, 5)));
		p.add(plotPanel(), "Center");
		p.setPreferredSize(new Dimension(preferredWidth, preferredWidth));
		p.setMinimumSize(new Dimension(50, 50));
		return p;
	}

	/*
	 * The panel that wraps a title around the program area.
	 */
    private JPanel titledProgramPanel() {

		return textPanel("Program", programArea(), new Dimension(preferredWidth, 120), min);
	}

	/*
	 * The panel that contains the (titled) plot panel and the
	 * slider panel.
	 */
    private JPanel upperPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(titledPlotPanel(), "Center");
		p.add(sliderPanel(), "South");
		return p;
	}
}