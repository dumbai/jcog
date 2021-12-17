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

import alice.util.OneWayList;

import java.util.Collection;


/**
 * Execution Context / Frame
 *
 * @author Alex Benini
 */
public class PrologContext {

    private final int id;
    int depth;
    Struct currentGoal;
    PrologContext parent;
    SubGoal parentGoalID;
    Struct clause;
    Struct headClause;
    SubGoalStore goalsToEval;
    OneWayList<Collection<Var>> trailingVars;
    OneWayList<Collection<Var>> parentVars;
    ChoicePointContext choicePointAfterCut;
    boolean haveAlternatives;

    PrologContext(int id) {
        this.id = id;
    }

    public PrologContext(Solve s, ChoicePointContext alternative, ClauseInfo clause) {
        this(s.step++);
        this.clause = clause.clause;

        clause.copyTo(id, this);

        if (alternative != null) {
            this.choicePointAfterCut = alternative.prevChoicePointContext;
//            Struct currentGoal = choicePoint.executionContext.currentGoal;
//            while (currentGoal.subs() == 2 && ";".equals(currentGoal.name())) {
//                if (choicePoint.prevChoicePointContext != null) {
//                    int distance;
//                    while ((distance = alternative.executionContext.depth - choicePoint.prevChoicePointContext.executionContext.depth) == 0 && choicePoint.prevChoicePointContext != null) {
//                        ec.choicePointAfterCut = choicePoint.prevChoicePointContext.prevChoicePointContext;
//                        choicePoint = choicePoint.prevChoicePointContext;
//                    }
//                    if (distance == 1) {
//                        ChoicePointContext cppp = choicePoint.prevChoicePointContext;
//                        ec.choicePointAfterCut = cppp.prevChoicePointContext;
//                        currentGoal = cppp.executionContext.currentGoal;
//                        choicePoint = cppp;
//                    } else
//                        break;
//                } else
//                    break;
//            }
        } else {
            this.choicePointAfterCut = s.choice;
        }

    }

    public int getId() {
        return id;
    }


    public String toString() {
        return "         id: " + id + '\n' + "     currentGoal:  " + currentGoal + '\n' + "     clause:       " + clause + '\n' + "     subGoalStore: " + goalsToEval + '\n' + "     trailingVars: " + trailingVars + '\n';
    }


    /*
     * Methods for spyListeners
     */

    public int getDepth() {
        return depth;
    }

    public Struct getCurrentGoal() {
        return currentGoal;
    }


    public Struct getClause() {
        return clause;
    }


    public SubGoalStore getSubGoalStore() {
        return goalsToEval;
    }


    /**
     * Save the state of the parent context to later bring the ExectutionContext
     * objects tree in a consistent state after a backtracking step.
     */
    void saveParentState() {
        if (parent != null) {
            parentGoalID = parent.goalsToEval.current();
            parentVars = parent.trailingVars;
        }
    }


    /**
     * If no open alternatives, no other term to execute and
     * current context doesn't contain as current goal a catch or java_catch predicate ->
     * current context no more needed ->
     * reused to execute g subgoal =>
     * got TAIL RECURSION OPTIMIZATION!
     */
    void tailCallOptimize(PrologContext ctx) {
        if (!haveAlternatives) {
            SubGoalStore ge = ctx.goalsToEval;
            if (ge.freeForTailCall()) {
                switch (ctx.currentGoal.name()) {
                    case "catch":
                    case "java_catch":
                        break;
                    default: {
                        parent = ctx.parent;
                        depth = ctx.depth;
                        return;
                    }
                }
            }
        }

        parent = ctx;
        depth = ctx.depth + 1;
    }
}