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

import alice.util.Tools;
import jcog.Util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Predicate;

import static alice.tuprolog.PrologPrim.PREDICATE;

/**
 * This class defines the Theory Manager who manages the clauses/theory often referred to as the Prolog database.
 * The theory (as a set of clauses) are stored in the ClauseDatabase which in essence is a HashMap grouped by functor/arity.
 * <p/>
 * The TheoryManager functions logically, as prescribed by ISO Standard 7.5.4
 * section. The effects of assertions and retractions shall not be undone if the
 * program subsequently backtracks over the assert or retract call, as prescribed
 * by ISO Standard 7.7.9 section.
 * <p/>
 * To use the TheoryManager one should primarily use the methods assertA, assertZ, consult, retract, abolish and find.
 * <p/>
 * <p>
 * rewritten by:
 *
 * @author ivar.orstavik@hist.no
 * @see Theory
 */
public class Theories {

    public static final Struct TRUE = new Struct("true");
    private static final Deque EMPTY_ARRAYDEQUE = new ArrayDeque(0) {
        @Override
        public void addFirst(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addLast(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator iterator() {
            return Util.emptyIterator;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public final int size() {
            return 0;
        }
    };
    private final MutableClauseIndex dynamicDBase;
    private final ClauseIndex staticDBase;
    private final Prolog engine;
    private final PrologPrimitives prims;
    private final Deque<Term> startGoalStack;

    Theories(Prolog vm, ClauseIndex statics, MutableClauseIndex dynamics) {
        engine = vm;
        dynamicDBase = dynamics;
        staticDBase = statics;

        prims = engine.prims;
        startGoalStack = new ArrayDeque<>(8);
    }

    /**
     * inserting of a clause at the head of the dbase
     */
    public /*synchronized*/ void assertA(Struct clause, String libName) {
        ClauseInfo d = new ClauseInfo(toClause(clause), libName);
        String key = d.head.key();
//        if (dyn) {
        dynamicDBase.add(key, d, true);
        if (staticDBase.containsKey(key)) {
            Prolog.warn("A static predicate with signature " + key + " has been overriden.");
        }
//        } else
//            staticDBase.add(key, d, true);
        if (engine.isSpy())
            engine.spy("INSERTA: " + d.clause + '\n');
    }

    /**
     * inserting of a clause at the end of the dbase
     */
    public /*synchronized*/ void assertZ(Struct clause, boolean dyn, String libName, boolean backtrackable) {
        ClauseInfo d = new ClauseInfo(toClause(clause), libName);
        String key = d.head.key();
        if (dyn) {
            dynamicDBase.add(key, d, false);
            if (engine.isSpy() && staticDBase.containsKey(key)) {
                Prolog.warn("A static predicate with signature " + key + " has been overriden.");
            }
        } else
            ((MutableClauseIndex) staticDBase).add(key, d, false); //HACK
        if (engine.isSpy())
            engine.spy("INSERTZ: " + d.clause + '\n');
    }

    /**
     * removing from dbase the first clause with head unifying with clause
     */
    public int retract(Struct cl, Predicate<ClauseInfo> each) {
        Struct clause = toClause(cl);
        Struct struct = ((Struct) clause.sub(0));
        Deque<ClauseInfo> family = dynamicDBase.clauses(struct.key());


        /*creo un nuovo clause database x memorizzare la teoria all'atto della retract
         * questo lo faccio solo al primo giro della stessa retract
         * (e riconosco questo in base all'id del contesto)
         * sara' la retract da questo db a restituire il risultato
         */


        if (family == null)
            return 0;

        int[] removals = {0};
        family.removeIf(ci -> {
            if (clause.unifiable(ci.clause)) {
                if (each.test(ci)) {
                    removals[0]++;
                    return true;
                }
            }
            return false;
        });


        return removals[0];
    }

    /**
     * removing from dbase all the clauses corresponding to the
     * predicate indicator passed as a parameter
     */
    /*synchronized*/ boolean abolish(Struct pi) {
        if (pi == null || !pi.isGround() || pi.subs() != 2)
            throw new IllegalArgumentException(pi + " is not a valid Struct");
        if (!"/".equals(pi.name()))
            throw new IllegalArgumentException(pi + " has not the valid predicate name. Espected '/' but was " + pi.name());

        String arg0 = Tools.removeApostrophes(pi.sub(0).toString());
        String arg1 = Tools.removeApostrophes(pi.sub(1).toString());
        String key = arg0 + '/' + arg1;
        Deque<ClauseInfo> abolished = dynamicDBase.remove(key); /* Reviewed by Paolo Contessi: LinkedList -> List */
        if (abolished != null)
            if (engine.isSpy())
                engine.spy("ABOLISHED: " + key + " number of clauses=" + abolished.size() + '\n');
        return true;
    }

    /**
     * Returns a family of clauses with functor and arity equals
     * to the functor and arity of the term passed as a parameter
     * <p>
     * Reviewed by Paolo Contessi: modified according to new ClauseDatabase
     * implementation
     */
    public /*synchronized*/ Deque<ClauseInfo> find(Term headt) {

        if (headt instanceof Struct s) {
            Deque<ClauseInfo> list = dynamicDBase.predicates(s);
            if (list == null) {
                list = staticDBase.predicates(s);
                if (list != null)
                    return list;
            } else {
                return list;
            }
        }

//        if (headt instanceof Var)
//            throw new RuntimeException();

        return EMPTY_ARRAYDEQUE;
    }

    /**
     * Consults a theory.
     *
     * @param theory        theory to addAt
     * @param dynamicTheory if it is true, then the clauses are marked as dynamic
     * @param libName       if it not null, then the clauses are marked to belong to the specified library
     */
    public /*synchronized*/ void consult(Theory theory, boolean dynamicTheory, String libName) throws InvalidTheoryException {
        startGoalStack.clear();

        /*Castagna 06/2011*/
        int clause = 0;
        /**/

        try {

            Iterator<Term> t = theory.iterator(engine.ops);

            while (t.hasNext()) {
                /*Castagna 06/2011*/
                clause++;
                /**/
                Struct d = (Struct) ((Iterator<? extends Term>) t).next();
                if (!runDirective(d))
                    assertZ(d, dynamicTheory, libName, true);

            }

        } catch (InvalidTermException e) {
            /*Castagna 06/2011*/


            throw new InvalidTheoryException(e.getMessage(), clause, e.line, e.pos);
            /**/
        }


    }

    /**
     * Binds clauses in the database with the corresponding
     * primitive predicate, if any
     */
    void rebindPrimitives() {
        for (ClauseInfo d : dynamicDBase)
            for (SubTree sge : d.body)
                prims.identify((Term) sge, PREDICATE);
    }

    /**
     * Clears the clause dbase.
     */
    public /*synchronized*/ void clear() {
        dynamicDBase.clear();
    }

    /**
     * remove all the clauses of lib theory
     */
    void removeLibraryTheory(String libName) {
        for (Iterator<ClauseInfo> allClauses = staticDBase.iterator(); allClauses.hasNext(); ) {
            ClauseInfo d = allClauses.next();
            if (libName.equals(d.libName)) {
                try {
                    allClauses.remove();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private boolean runDirective(Struct c) {
        String cn = c.name();
        if ("':-'".equals(cn) ||
                ((c.subs() == 1) && ":-".equals(cn) && (c.subResolve(0) instanceof Struct))) {
            Struct dir = (Struct) c.subResolve(0);
            try {
                if (!prims.evalAsDirective(dir))
                    Prolog.warn("Directive " + dir.key() + " unknown");
            } catch (Throwable t) {
                Prolog.warn("An exception has been thrown during the execution of directive " +
                        dir.key() + "\n" + t.getMessage());
            }
            return true;
        }
        return false;
    }

    /**
     * Gets a clause from a generic Term
     */
    private Struct toClause(Struct t) {

        t = (Struct) Term.term(t.toString(), this.engine.ops);
        if (!t.isClause())
            t = new Struct(":-", t, TRUE);
        prims.identify(t, PREDICATE);
        return t;
    }

    /*synchronized*/ void solveTheoryGoal() {
        Struct s = null;
        Deque<Term> goals = this.startGoalStack;

        while (!goals.isEmpty()) {
            Term g = goals.pop();
            s = (s == null) ?
                (Struct) g :
                new Struct(",", g, s);
        }
        if (s != null) {
//            try {
            engine.solve(s);
//            } catch (Exception ex) {
//                Prolog.logger.error("solveTheoryGoal {}", ex);
//            }
        }
    }

    /**
     * add a goal eventually defined by last parsed theory.
     */
    /*synchronized*/ void addStartGoal(Struct g) {
        startGoalStack.push(g);
    }

    /**
     * saves the dbase on a output stream.
     */
    synchronized boolean save(OutputStream os, boolean onlyDynamic) {
        try {
            new DataOutputStream(os).writeBytes(getTheory(onlyDynamic));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Gets current theory
     *
     * @param onlyDynamic if true, fetches only dynamic clauses
     */
    @Deprecated
    public /*synchronized*/ String getTheory(boolean onlyDynamic) {
        StringBuilder buffer = new StringBuilder();
        for (ClauseInfo d : dynamicDBase) {
            buffer.append(d.toString(engine.ops)).append('\n');
        }
        if (!onlyDynamic)
            for (ClauseInfo d : staticDBase)
                buffer.append(d.toString(engine.ops)).append('\n');
        return buffer.toString();
    }


}