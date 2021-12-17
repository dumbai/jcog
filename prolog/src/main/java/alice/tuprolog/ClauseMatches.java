package alice.tuprolog;

import jcog.data.list.Lst;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * A list of clauses belonging to the same family as a goal. A family is
 * composed by clauses with the same functor and arity.
 */
public final class ClauseMatches {


    private final Term goal;
    private final List<Var> vars;

    /** pending unifiable clauses */
    private transient Deque<ClauseInfo> clauses;


    private ClauseMatches(Term goal, List<Var> vars, Deque<ClauseInfo> clauses) {
        this.goal = goal;
        this.vars = vars;
        this.clauses = clauses;
    }

    public static @Nullable ClauseMatches match(Term goal, Deque<ClauseInfo> clauses, @Nullable List<Var> vars) {

        if (!clauses.isEmpty()) {
            Deque<ClauseInfo> c = first(goal, clauses);
            if (c != null)
                return new ClauseMatches(goal, vars, c);
        }

        return null;
    }


    /**
     * Restituisce la clausola da caricare
     */
    ClauseInfo fetchNext(boolean pop, boolean save) {
        Deque<ClauseInfo> clauses = this.clauses;
        if (clauses == null)
            return null;

        int v = vars.size();
        if (save && v == 0) save = false;

        ClauseInfo clause = null;
        //while (!clauses.isEmpty()) {

            if (pop) {
                clause = clauses.removeFirst();
                if (clauses.isEmpty())
                    this.clauses = null;
            } else
                clause = clauses.peekFirst();

            List<Term> saveUnifications = deunify(vars, save ? new Lst<>(v) : null);

//            boolean u = goal.unifiable(clause.head);

            if (saveUnifications != null)
                reunify(vars, (Lst)saveUnifications, v);

//            if (!u) {
//                clause = null;
//                if (!pop) {
//                    clauses.removeFirst(); //remove the un-unifiable entry since it wasnt popped already
//                    if (clauses.isEmpty()) {
//                        this.clauses = null;
//                        break;
//                    }
//                }
//            } else {
        //        break; //got it
//            }

        //}

        return clause;
    }


    boolean haveAlternatives() {
        return clauses != null && !clauses.isEmpty();
    }


    /**
     * Salva le unificazioni delle variabili da deunificare
     *
     * @param varsToDeunify
     * @return unificazioni delle variabili
     */
    private static List<Term> deunify(Iterable<Var> varsToDeunify, @Nullable List<Term> save) {
        for (Var v : varsToDeunify) {
            if (save != null)
                save.add(v.link);
            v.link = null;
        }
        return save;
    }


    /**
     * Restore previous unifications into variables.
     *
     * @param varsToReunify
     * @param save
     */
    private static void reunify(List<Var> varsToReunify, Lst<Term> save, int size) {

//        if (varsToReunify instanceof Lst && saveUnifications instanceof Lst) {
            for (int i = size - 1, j = i; i >= 0; )
                varsToReunify.get(i--).setLink(save.get(j--));

//        } else {
//
//            ListIterator<Var> it1 = varsToReunify.listIterator(size);
//            ListIterator<Term> it2 = saveUnifications.listIterator(size);
//            while (it1.hasPrevious()) {
//                it1.previous().setLink(it2.previous());
//            }
//        }
    }


//    /**
//     * Verify if a clause exists that is compatible with goal.
//     * As a side effect, clauses that are not compatible get
//     * discarded from the currently examined family.
//     *
//     * @param goal
//     */
//    @Deprecated private boolean unifiable(Term goal) {
//        if (!this.goal.equals(goal))
//            throw new WTF();
//
//        Deque<ClauseInfo> clauses = this.clauses;
//        if (clauses == null)
//            return false;
//        for (ClauseInfo clause : clauses) {
//            if (goal.unifiable(clause.head)) return true;
//        }
//        return false;
//    }

    @Nullable
    private static Deque<ClauseInfo> first(Term u, Iterable<ClauseInfo> matching) {
        Deque<ClauseInfo> clauses = null;
        for (ClauseInfo ci : matching) {
            if (u.unifiable(ci.head)) {
                if (clauses == null) clauses = new ArrayDeque<>(/* other.size() - 1 - i */);
                clauses.add(ci); //queue for future test
            }
        }
        return clauses;
    }

    ClauseInfo fetchFirst() {
        return this.clauses.removeFirst();
    }

    public String toString() {
        return "clauses: " + clauses + '\n' +
                "goal: " + goal + '\n' +
                "vars: " + vars + '\n';
    }
}