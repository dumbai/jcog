/*
 * tuProlog - Copyright (C) 2001-2007  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package alice.tuprolog;

import com.google.common.io.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Iterator;

import static alice.tuprolog.Parser.defaultOps;

/**
 * This class represents prolog theory which can be provided
 * to a prolog engine.
 *
 * Actually theory incapsulates only textual representation
 * of prolog theories, without doing any check about validity
 *
 * @see Prolog
 *
 */
public class Theory implements Iterable<Term>, Serializable {

	public static final Theory Null = new Theory();

    private String theory;
    private Struct clauseList;


    /**
     * Creates a theory getting its source text from an input stream
     *
     * @param is the input stream acting as source
     */
    public Theory(InputStream is) throws IOException {
        theory = new String(is.readAllBytes());
        is.close();
    }


    /**
     * Creates a theory from its source text
     *
     * @param theory the source text
     * @throws s InvalidTheoryException if theory is null
     */
    public Theory(String theory) throws InvalidTheoryException {
        this.theory=theory;
        if (theory == null) {
            throw new InvalidTheoryException();
        }
    }
    
    private Theory() {
        this.theory = "";
    }

    /**
     * Creates a theory from a clause list
     *
     * @param clauseList the source text
     * @throws s InvalidTheoryException if clauseList is null or is not a prolog list
     */
    public Theory(Struct clauseList) throws InvalidTheoryException {
        if (clauseList==null || !clauseList.isList()) {
            throw new InvalidTheoryException();
        }
        this.clauseList = clauseList;
    }

    public static Theory resource(String classPath) throws IOException, InvalidTheoryException {
        return resource(Prolog.class, classPath);
    }

    public static Theory resource(Class root, String classPath) throws IOException, InvalidTheoryException {
        return new Theory(Resources.toString(Resources.getResource(root, classPath), Charset.defaultCharset()));
    }

    public Iterator<Term> iterator(PrologOperators ops) {
        return isTextual() ? new Parser(theory, ops).iterator() : clauseList.listIterator();
    }

    public Iterator<Term> iterator() {
        return iterator(defaultOps);
    }

    /**
     * Adds (appends) a theory to this.
     *
     * @param th is the theory to be appended
     * @throws s InvalidTheoryException if the theory object are not compatibles (they are
     *  compatibles when both have been built from texts or both from clause lists)
     */
    public void append(Theory th) throws InvalidTheoryException {
        if (th.isTextual() && isTextual()) {
            theory += th.theory;
        } else if (!th.isTextual() && !isTextual()) {
            Struct otherClauseList = th.clauseList;
            if (clauseList.isEmptyList())
                clauseList = otherClauseList;
            else {
                Struct p = clauseList, q;
                while (!(q = (Struct) p.sub(1)).isEmptyList())
                    p = q;
                p.setSub(1, otherClauseList);
            }
        } else if (!isTextual() && th.isTextual()) {
            theory = theory + '\n' + th;
            clauseList = null;
        } else if (isTextual() && !th.isTextual()) {
            theory += th.toString();
        }
        else {
            throw new InvalidTheoryException();
        }
    }

    /**
     * Checks if the theory has been built
     * from a text or a clause list
     *
     */
    boolean isTextual() {
        return theory != null;
    }

    public String toString() {
        return theory != null ? theory : clauseList.toString();
    }


    public static Theory string(String s) throws InvalidTheoryException {
        return new Theory(s);
    }
//    public static Theory string(String... s) throws InvalidTheoryException {
//        return new Theory(Joiner.on("\n").join(s));
//    }
}