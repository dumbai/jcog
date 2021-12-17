/*
 *
 *
 */
package alice.tuprolog;

import jcog.data.list.Lst;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;

import java.io.Serializable;
import java.util.List;

import static alice.tuprolog.PrologPrim.PREDICATE;

/**
 * @author Alex Benini
 * <p>
 * Core engine
 */
public class PrologRun implements Serializable, Runnable {

    static final int HALT = -1;
    public static final int FALSE = 0;
    public static final int TRUE = 1;
    public static final int TRUE_CP = 2;

    static final State INIT = StateInit.the;

    static final State EXCEPTION = StateException.the;

    static final State GOAL_SELECTION = StateGoalSelection.the;

    static final State BACKTRACK = StateBacktrack.the;

    static final State END_FALSE = new StateEnd(FALSE);
    static final State END_TRUE = new StateEnd(TRUE);
    static final State END_TRUE_CP = new StateEnd(TRUE_CP);
    static final State END_HALT = new StateEnd(HALT);

    private final int id;
    /* Stack environments of nidicate solving */
    private final Lst<Solve> stackEnv = new Lst<>();
    Solution sinfo;

//    private Lock lockVar;
//    private Condition cond;
//    private final Object semaphore = new Object();
    Solve solve;
    Prolog prolog;
    private boolean relinkVar;
    private List<Term> bagOFres;
    private List<String> bagOFresString;
    private Term bagOFvarSet;
    private Term bagOfgoal;
    private Term bagOfBag;
//    private boolean solving;
    private Term query;
    private BooleanArrayList next;
    private int countNext;
    /* Current environment */
    /* Last environment used */
    private Solve last_env;
    private String sinfoSetOf;

    public PrologRun(int id) {
        this.id = id;
    }

    public void initialize(Prolog vm) {
        prolog = vm;
//        solving = false;
        sinfo = null;
        next = new BooleanArrayList();
        countNext = 0;
    }


    private Solution solve() {
        try {
            query.resolveTerm();

            prolog.libs.onSolveBegin(query);
            prolog.prims.identify(query, PREDICATE);

            freeze();

            StateEnd y = (solve = new Solve(this, query)).get();

            defreeze();

            sinfo = new Solution(
                    query,
                    y.goal,
                    y.endState,
                    y.vars
            );
            if (sinfoSetOf != null)
                sinfo.setSetOfSolution(sinfoSetOf);
            if (!sinfo.hasOpenAlternatives())
                solveEnd();
            return sinfo;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Solution(query);
        }
    }


    public Solution solveNext() throws NoMoreSolutionException {
        if (!hasOpenAlternatives()) {
            throw new NoMoreSolutionException();
        }

        freeze();
        solve = last_env;
        solve.next = BACKTRACK;
        StateEnd result = solve.get();
        defreeze();
        sinfo = new Solution(
                solve.query,
                result.goal,
                result.endState,
                result.vars
        );
        if (this.sinfoSetOf != null)
            sinfo.setSetOfSolution(sinfoSetOf);

        if (!sinfo.hasOpenAlternatives())
            solveEnd();

        return sinfo;
    }


    /**
     * Halts current solve computation
     */
    void solveHalt() {
        solve.mustStop();
        prolog.libs.onSolveHalt();
    }

    /**
     * Accepts current solution
     */
    void solveEnd() {
        prolog.libs.onSolveEnd();
    }


    private void freeze() {
        if (solve == null)
            return;

        if (!stackEnv.isEmpty() && stackEnv.getLast() == solve)
            return;

        stackEnv.add(solve);
    }

    private void defreeze() {
        last_env = solve;
        Solve last = stackEnv.poll();
        if (last != null)
            solve = last;
    }


    void identify(Term t) {
        prolog.prims.identify(t, PREDICATE);
    }


    void pushSubGoal(SubGoalTree goals) {
        solve.context.goalsToEval.pushSubGoal(goals);
    }


    void cut() {
        solve.cut(solve.context.choicePointAfterCut);
    }


    /**
     * Asks for the presence of open alternatives to be explored
     * in current demostration process.
     *
     * @return true if open alternatives are present
     */
    public boolean hasOpenAlternatives() {
        Solution i = this.sinfo;
        return i != null && i.hasOpenAlternatives();
    }


    /**
     * Checks if the demonstration process was stopped by an halt command.
     *
     * @return true if the demonstration was stopped
     */
    boolean isHalted() {
        Solution i = this.sinfo;
        return i != null && i.isHalted();
    }


    @Override
    public void run() {
//        solving = true;

        if (sinfo == null)
            solve();

        try {
            while (hasOpenAlternatives())
                if (next.get(countNext))
                    solveNext();
        } catch (NoMoreSolutionException e) {
            e.printStackTrace();
        }
    }

    //    public boolean nextSolution() {
//        solving = true;
//        next.add(true);
////
////        synchronized (semaphore) {
////            semaphore.notify();
////        }
//        return true;
//    }

//    public Solution read() {
//        lockVar.lock();
//        try {
//            while (solving || sinfo == null)
//                try {
//                    cond.await();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//        } finally {
//            lockVar.unlock();
//        }
//
//        return sinfo;
//    }

//    public void setSolving(boolean solved) {
//        solving = solved;
//    }


    boolean getRelinkVar() {
        return this.relinkVar;
    }

    void setRelinkVar(boolean b) {
        this.relinkVar = b;
    }

    List<Term> getBagOFres() {
        return this.bagOFres;
    }

    void setBagOFres(List<Term> l) {
        this.bagOFres = l;
    }

    List<String> getBagOFresString() {
        return this.bagOFresString;
    }

    void setBagOFresString(List<String> l) {
        this.bagOFresString = l;
    }

    Term getBagOFvarSet() {
        return this.bagOFvarSet;
    }

    void setBagOFvarSet(Term l) {
        this.bagOFvarSet = l;
    }

    Term getBagOFgoal() {
        return this.bagOfgoal;
    }

    void setBagOFgoal(Term l) {
        this.bagOfgoal = l;
    }

    Term getBagOFBag() {
        return this.bagOfBag;
    }

    void setBagOFBag(Term l) {
        this.bagOfBag = l;
    }


    void setSetOfSolution(String s) {
        if (sinfo != null)
            sinfo.setSetOfSolution(s);
        this.sinfoSetOf = s;
    }

    void clearSinfoSetOf() {
        this.sinfoSetOf = null;
    }

    public final Solution solve(Term query) {
        this.query = query;
        return solve();
    }


}