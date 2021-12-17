package jcog.grammar.parse.examples.query;

import jcog.grammar.parse.PubliclyCloneable;
import jcog.grammar.parse.examples.engine.*;

import java.util.Iterator;
import java.util.Vector;

/**
 * This class accepts terms, class names and comparisons,
 * and then builds a query from them.
 *
 * The query this builder creates will have the form:
 *
 * <blockquote><pre>	
 *     q(term0, term1, ...), 
 *     className0, className1, ...,
 *     comparison0, comparison1, ...
 * </pre></blockquote>
 *
 * The first structure forms a "projection", which is
 * set of terms that should all be valid after the
 * remaining structures prove themselves. To use the query
 * after building it, prove its tail to establish values
 * for its head.
 *
 * For example, consider the select statement:
 *
 * <blockquote><pre>
 *    select PricePerBag * 0.9 
 *        from chip 
 *        where PricePerBag > 10
 * </pre></blockquote>
 *
 * a parser for this statement can pass a QueryBuilder
 * one term, one class name and one comparison; the builder
 * will take these and build the query:
 *
 * <blockquote><pre>
 *     q(*(PricePerBag, 0.9)), 
 *     chip(ChipID, ChipName, PricePerBag, Ounces, Oil), 
 *     >(PricePerBag, 10.0)
 * </pre></blockquote>
 *
 * A program can prove the tail of this query (that is, all
 * the structures after the first). Each proof of the tail
 * will establish a value for the PricePerBag variable.
 * After each proof, the term in the head structure will
 * have a value of .9 times the PricePerBag.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class QueryBuilder implements PubliclyCloneable<QueryBuilder> {
	private Speller speller;
	private Vector terms = new Vector();
	private Vector classNames = new Vector();
	private Vector comparisons = new Vector();

	/**
	 * Construct a query builder that will use the given speller.
	 */
	public QueryBuilder(Speller speller) {
		this.speller = speller;
	}

	/**
	 * Add the given class name to the query. This method
	 * checks that the class name when properly spelled matches
	 * a known class name.
	 */
	public void addClassName(String s) {
		String properName = speller.getClassName(s);
		if (properName == null) {
			throw new UnrecognizedClassException("No class named " + s + " in object model");
		}
		classNames.addElement(properName);
	}

	/**
	 * Add a comparison to the query.
	 */
	public void addComparison(Comparison c) {
		comparisons.addElement(c);
	}

	/**
	 * Add a term that will appear in the head structure of
	 * the query.
	 */
	public void addTerm(Term t) {
		terms.addElement(t);
	}

	/**
	 * Create a query from the terms, class names and variables
	 * this object has received so far.
	 */
	public Query build(AxiomSource as) {


        Term[] termArray = new Term[terms.size()];
		terms.copyInto(termArray);
		Structure s = new Structure("q", termArray);
        Vector structures = new Vector();
        structures.addElement(s);


        Iterator iterator1 = classNames.iterator();
		while (iterator1.hasNext()) {
			String name = (String) iterator1.next();
			structures.addElement(ChipSource.queryStructure(name));
		}


        Iterator iterator = comparisons.iterator();
		while (iterator.hasNext()) {
			Comparison c = (Comparison) iterator.next();
			structures.addElement(c);
		}

		
		Structure[] sarray = new Structure[structures.size()];
		structures.copyInto(sarray);
		return new Query(as, sarray);
	}

	/**
	 * Return a copy of this object.
	 *
	 * @return a copy of this object
	 */
	public QueryBuilder clone() {
		try {
			QueryBuilder c = (QueryBuilder) super.clone();
			c.terms = (Vector) terms.clone();
			c.classNames = (Vector) classNames.clone();
			c.comparisons = (Vector) comparisons.clone();
			return c;
		} catch (CloneNotSupportedException e) {
			
			throw new InternalError();
		}
	}
}