/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
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


import jcog.data.list.Lst;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Collections.EMPTY_LIST;

/**
 * @author Alex Benini
 */
public class Solve implements Supplier<StateEnd> {

    final Term query;
	Collection<Var> goalVars;

    final PrologRun run;

    State next;

    Struct startGoal;


    int step;
	private boolean stop;

    /** current context */
    PrologContext context;

    /** current alternative */
    ChoicePointContext alt;

    /** choice-point selector */
    ChoicePointContext choice;



    public Solve(PrologRun run, Term query) {
        this.run = run;
        this.next = PrologRun.INIT;
        this.query = query;
//		this.mustStop = false;
    }


    public String toString() {
        try {
            return
                    "ExecutionStack: \n" + context + '\n' +
                            "ChoicePointStore: \n" + choice + "\n\n";
        } catch (Exception ex) {
            return "";
        }
    }

    void mustStop() {
        stop = true;
    }

    /**
     * Core of engine. Finite State Machine
     */
    @Override public StateEnd get() {

        State next;
        do {

            State prev = this.next;

            run.prolog.spy(prev, this);

            this.next = next = prev.run(this);

            if (stop) next = PrologRun.END_FALSE;

        } while (!(next instanceof StateEnd));

        next.run(this);

        return (StateEnd) next;
    }


    /*
     * Methods for spyListeners
     */


    public List<PrologContext> getExecutionStack() {
        PrologContext t = context;
        if (t == null) return EMPTY_LIST;

        List<PrologContext> l = new Lst<>(1);
        do {
            l.add(t);
            t = t.parent;
        } while (t != null);
        return l;
    }


    void prepareGoal() {
        LinkedHashMap<Var, Var> goalVars = new LinkedHashMap<>();
        startGoal = (Struct) (query).copyGoal(goalVars, 0);
        this.goalVars = goalVars.values();
    }


    void initialize(PrologContext eCtx) {
        context = eCtx;
        choice = null;
        step = 1;
        alt = null;
    }

    public String getNextStateName() {
        return next.stateName;
    }


    public void add(ChoicePointContext cpc) {
        if (choice != null) {
            cpc.prevChoicePointContext = choice;
        }
        choice = cpc;
    }

    public void cut(ChoicePointContext pointerAfterCut) {
        choice = pointerAfterCut;
    }

    /**
     * Return the correct choice-point
     */
    ChoicePointContext fetch() {
        return (existChoicePoint()) ? choice : null;
    }


    /**
     * Check if a choice point exists in the store.
     * As a side effect, removes choice points which have been already used and are now empty.
     *
     * @return
     */
    boolean existChoicePoint() {
        ChoicePointContext p = this.choice;
        while (p != null) {
            if (p.compatibleGoals.fetchNext(false, true) != null)
                return true;
            this.choice = p = p.prevChoicePointContext;
        }
        return false;
    }


    /**
     * Removes choice points which have been already used and are now empty.
     */
    void removeUnusedChoicePoints() {

        existChoicePoint();
    }


    /*
     * Methods for spyListeners
     */

    //    public List<ChoicePointContext> getChoicePoints() {
    //        ArrayList<ChoicePointContext> l = new ArrayList<>();
    //        ChoicePointContext t = pointer;
    //        while (t != null) {
    //            l.addAt(t);
    //            t = t.prevChoicePointContext;
    //        }
    //        return l;
    //    }

}