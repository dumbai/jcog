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
 * @author Alex Benini
 *
 */
enum StateBacktrack  { ;

    public static final State the = new State("Back") {

        @Override
        State run(Solve s) {
            ChoicePointContext curChoice = s.fetch();

            if (curChoice == null)
                return PrologRun.END_FALSE;

            s.alt = curChoice;


            s.context = curChoice.executionContext;
            Term curGoal = s.context.goalsToEval.backTo(curChoice.indexSubGoal).term();
            if (!(curGoal instanceof Struct))
                return PrologRun.END_FALSE;

            s.context.currentGoal = (Struct) curGoal;



            PrologContext curCtx = s.context;
            OneWayList<Collection<Var>> pointer = curCtx.trailingVars;
            OneWayList<Collection<Var>> stopDeunify = curChoice.varsToDeunify;
            Collection<Var> varsToDeunify = stopDeunify.head();
            Var.free(varsToDeunify);

            do {

                while (pointer != stopDeunify) {
                    Var.free(pointer.head());
                    pointer = pointer.tail();
                }
                curCtx.trailingVars = pointer;
                if (curCtx.parent == null)
                    break;
                stopDeunify = curCtx.parentVars;
                SubGoal fatherIndex = curCtx.parentGoalID;

                Term prevGoal = curGoal;
                curCtx = curCtx.parent;
                curGoal = curCtx.goalsToEval.backTo(fatherIndex).term();
                if (!(curGoal instanceof Struct) || prevGoal == curGoal)
                    return PrologRun.END_FALSE;

                curCtx.currentGoal = (Struct)curGoal;
                pointer = curCtx.trailingVars;
            } while (true);


            return StateGoalEvaluation.the.run(s);
        }

    };
    
    


}