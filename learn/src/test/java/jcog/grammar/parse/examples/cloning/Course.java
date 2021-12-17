package jcog.grammar.parse.examples.cloning;

import jcog.grammar.parse.PubliclyCloneable;

/**  
 * This class shows a typical clone() method.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class Course implements PubliclyCloneable<Course> {
	private Professor professor;
	private Textbook textbook;

	/**
	 * Return a copy of this object.
	 *
	 * @return a copy of this object
	 */
	public Course clone() {
		try {
			Course copy = (Course) super.clone();
			copy.setProfessor(professor.clone());
			copy.setTextbook(textbook.clone());
			return copy;
		} catch (CloneNotSupportedException e) {
			
			throw new InternalError();
		}
	}

	/**
	 * Get the professor.
	 *
	 * @return the professor
	 */
	public Professor getProfessor() {
		return professor;
	}

	/**
	 * Get the textbook.
	 *
	 * @return the textbook
	 */
	public Textbook getTextbook() {
		return textbook;
	}

	/**
	 * Set the professor.
	 *
	 * @param   Professor   professor
	 */
    private void setProfessor(Professor professor) {
		this.professor = professor;
	}

	/**
	 * Set the textbook.
	 *
	 * @param   Textbook   textbook
	 */
    private void setTextbook(Textbook textbook) {
		this.textbook = textbook;
	}
}
