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

import java.lang.reflect.InvocationTargetException;

import static alice.tuprolog.PrologRun.*;

/**
 * @author Alex Benini
 */
class StateGoalEvaluation extends State {

    public static final State the = new StateGoalEvaluation();

    private StateGoalEvaluation() {
        super("Eval");
    }

    @Override
    State run(Solve s) {
        Struct x = s.context.currentGoal;
        PrologPrim p = x.getPrimitive();
        if (p==null)
            return StateRuleSelection.the.run(s);

        s.step++;

        try {
            return p.evalAsPredicate(x) ?
                    GOAL_SELECTION :
                    BACKTRACK;
        } catch (HaltException he) {
            return END_HALT;
        } catch (Throwable t) {
            return throwable(s, t);
        }

    }

    private static State throwable(Solve s, Throwable t) {
        State nextState;
        if (t instanceof InvocationTargetException) {
            t = t.getCause();
        }

        if (t instanceof PrologError error) {

            s.context.currentGoal = new Struct("throw", error.getError());
            s.run.prolog.exception(error);

        } else if (t instanceof JavaException exception) {

            s.context.currentGoal = new Struct("java_throw", exception.getException());
            s.run.prolog.exception(exception); //exception.getException());

            //System.err.println(((JavaException) t).getException());
        }

        nextState = EXCEPTION;
        return nextState;
    }

}