package jcog.grammar.parse.examples.sling;



import jcog.grammar.parse.Assembly;
import jcog.grammar.parse.IAssembler;

import javax.swing.*;

/**
 * Uses one of the sliders in an assembly's target to create
 * a <code>Slider</code> function, and pushes this 
 * function.
 * <p>
 * This class expects the assembly's target to contain a
 * <code>SlingPlot</code> object which holds references to
 * two <code>JSlider</code> objects. This class's 
 * constructor determines which slider to ask for. When an 
 * object of this class works on an assembly, it gets the 
 * slider component from the target, creates a 
 * <code>Slider</code> function with it, and pushes this 
 * function.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0
 */
class SliderAssembler implements IAssembler {
	private int slider;

	/**
	 * Creates an assembler that will extract the indicated 
	 * slider from an assembly's target.
	 */
    SliderAssembler(int slider) {
		this.slider = slider;
	}

	/**
	 * Push a <code>Slider</code> object formed from a <code>
	 * JSlider</code>
	 * <p>
	 * Ask the assembly's target, which must be a <code>SlingTarget
	 * </code>, for a <code>JSlider</code> component. Which slider
	 * to ask for depends on the int this object was constructed
	 * with. Create a <code>Slider</code> around this <code>JSlider
	 * </code>, and push it.
	 * 
	 * @param  Assembly  the assembly to work on
	 * 
	 */
	public void accept(Assembly a) {
		SlingTarget target = (SlingTarget) a.getTarget();
		JSlider js = (slider == 1) ? target.s1() : target.s2();
		a.push(new Slider(js));
	}
}