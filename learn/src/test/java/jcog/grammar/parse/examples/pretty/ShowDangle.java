package jcog.grammar.parse.examples.pretty;

import jcog.grammar.parse.examples.tests.Dangle;
import jcog.grammar.parse.tokens.TokenAssembly;

import java.util.Iterator;
import java.util.Vector;

/**
 * Show that the <code>Dangle.statement()</code> parser
 * is ambiguous.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0 
 */
public class ShowDangle {
	/**
	 * Show that the <code>Dangle.statement()</code> parser
	 * is ambiguous.
	 */
	public static void main(String[] args) {
        String s = "if (overdueDays > 90)    \n";
        s += "    if (balance >= 1000) \n";
		s += "        callCustomer();  \n";
		s += "else sendBill();";

		TokenAssembly ta = new TokenAssembly(s);

		PrettyParser p = new PrettyParser(Dangle.statement());

		Vector out = p.parseTrees(ta);
        Iterator iterator = out.iterator();
		while (iterator.hasNext()) {
			System.out.println("The input parses as:");
			System.out.println("---------------------------");
			System.out.println(iterator.next());
		}
	}
}