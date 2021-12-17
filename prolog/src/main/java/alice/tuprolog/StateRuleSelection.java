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
import jcog.data.list.Lst;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * @author Alex Benini
 */
public class StateRuleSelection extends State {


    public static final StateRuleSelection the = new StateRuleSelection();


    private StateRuleSelection() {
        super("Init");
    }

    /* (non-Javadoc)
     * @see alice.tuprolog.AbstractRunState#doJob()
     */
    @Override
    State run(Solve s) {
        PrologContext ctx = s.context;

        /*----------------------------------------------------
         * Individuo compatibleGoals e
         * stabilisco se derivo da Backtracking.
         */
        ChoicePointContext alt = s.alt;
        s.alt = null;
        ClauseMatches matches;
        boolean fromBT;
        ClauseInfo clause;
        if (alt == null) {
            /* from normal evaluation */

            List<Var> varsList = new Lst<>();
            ctx.trailingVars = new OneWayList<>(varsList, ctx.trailingVars);

            Struct currentGoal = ctx.currentGoal;
            matches = ClauseMatches.match(
                    currentGoal,
                    s.run.prolog.theories.find(currentGoal), varsList);

            if (matches == null) //g.isEmpty() || (clauseStore = ClauseStore.build(goal, g, varsList))==null) {
                return PrologRun.BACKTRACK;

            fromBT = false;
            clause = matches.fetchFirst();
        } else {
            matches = alt.compatibleGoals;
            clause = matches.fetchNext(true, false);  assert(clause!=null);
            fromBT = true;
        }

        return goalSelect(s, ctx, matches, fromBT, new PrologContext(s, alt, clause));
    }

    @Nullable
    private static State goalSelect(Solve s, PrologContext context, ClauseMatches matches, boolean fromBacktracking, PrologContext ec) {
        Collection<Var> unifiedVars = context.trailingVars.head();
        context.currentGoal.unify(ec.headClause, unifiedVars, unifiedVars);


        if ((ec.haveAlternatives = matches.haveAlternatives()) && !fromBacktracking) {
            ChoicePointContext cpc = new ChoicePointContext();
            cpc.compatibleGoals = matches;
            cpc.executionContext = context;
            cpc.indexSubGoal = context.goalsToEval.current();
            cpc.varsToDeunify = context.trailingVars;
            s.add(cpc);
        }

        if (fromBacktracking && !ec.haveAlternatives) {
            s.removeUnusedChoicePoints();
        }

        ec.tailCallOptimize(context);
        ec.saveParentState();
        s.context = ec;

        return PrologRun.GOAL_SELECTION.run(s);
    }

}