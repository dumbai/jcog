package alice.tuprolog;


import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

/**
 * <code>FamilyClausesList</code> is a common <code>LinkedList</code>
 * which stores {@link ClauseInfo} objects. Internally it indexes stored data
 * in such a way that, knowing what type of clauses are required, only
 * goal compatible clauses are returned
 *
 * @author Paolo Contessi
 * @see LinkedList
 * @since 2.2
 */
public class FamilyClausesList extends ArrayDeque<ClauseInfo> {

    private final FamilyClausesIndex<NumberTerm> numCompClausesIndex;
    private final FamilyClausesIndex<String> constantCompClausesIndex;
    private final FamilyClausesIndex<String> structCompClausesIndex;
    private final Deque<ClauseInfo> listCompClausesList;

    






    public FamilyClausesList() {
        super();

        numCompClausesIndex = new FamilyClausesIndex<>();
        constantCompClausesIndex = new FamilyClausesIndex<>();
        structCompClausesIndex = new FamilyClausesIndex<>();

        listCompClausesList = new ArrayDeque<>();
    }

    /**
     * Adds the given clause as first of the family
     *
     * @param ci The clause to be added (with related informations)
     */
    
    public void addFirst(ClauseInfo ci) {
        super.addFirst( ci);

        
        register(ci, true);
    }

    /**
     * Adds the given clause as last of the family
     *
     * @param ci The clause to be added (with related informations)
     */
    
    public void addLast(ClauseInfo ci) {
        super.addLast(ci);

        
        register(ci, false);
    }

    @Override
    public boolean add(ClauseInfo o) {
        return add(o, false);
    }

    public final boolean add(ClauseInfo o, boolean first) {
        if (first)
            addFirst(o);
        else
            addLast(o);
        return true;
    }



    @Nullable
    @Override
    public ClauseInfo removeLast() {
        int s = size();
        return s == 0 ? null : super.removeLast();
    }

    @Override
    public boolean remove(Object ci) {
        if (super.remove(ci)) {
            unregister((ClauseInfo) ci);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        while (removeLast() != null) {
        }
    }

    /**
     * Retrieves a sublist of all the clauses of the same family as the goal
     * and which, in all probability, could match with the given goal
     *
     * @param goal The goal to be resolved
     * @return The list of goal-compatible predicates
     */
    public Deque<ClauseInfo> get(Term goal) {
        
        if (goal instanceof Struct) {
            Struct g = (Struct) goal.term();

            /*
             * If no arguments no optimization can be applied
             * (and probably no optimization is needed)
             */
            if (g.subs() == 0) {
                
                return /*Collections.unmodifiableList*/(this);
            }

            /* Retrieves first argument and checks type */
            Term t = g.sub(0).term();
            if (t instanceof Var) {
                /*
                 * if first argument is an unbounded variable,
                 * no reasoning is possible, all family must be returned
                 */
                
                return /*Collections.unmodifiableList*/(this);
            } else if (t.isAtom()) {
                if (t instanceof NumberTerm) {
                    /* retrieves clauses whose first argument is numeric (or Var)
                     * and same as goal's first argument, if no clauses
                     * are retrieved, all clauses with a variable
                     * as first argument
                     */
                    
                    return /*Collections.unmodifiableList*/(
                            numCompClausesIndex.get((NumberTerm) t));
                } else if (t instanceof Struct) {
                    /* retrieves clauses whose first argument is a constant (or Var)
                     * and same as goal's first argument, if no clauses
                     * are retrieved, all clauses with a variable
                     * as first argument
                     */
                    
                    return /*Collections.unmodifiableList*/(
                            constantCompClausesIndex.get(((Struct) t).name()));
                }
            } else if (t instanceof Struct) {
                return /*Collections.unmodifiableList*/(
                        isAList((Struct) t) ?
                                listCompClausesList :
                                structCompClausesIndex.get(((Struct) t).key())
                );
            }
        }

        /* Default behaviour: no optimization done */
        return /*Collections.unmodifiableList*/(this);
        
    }














    private static boolean isAList(Struct t) {
        /*
         * Checks if a Struct is also a list.
         * A list can be an empty list, or a Struct with name equals to "."
         * and arity equals to 2.
         */
        return t.isEmptyList() || (t.subs() == 2 && ".".equals(t.name()));

    }

    
    private void register(ClauseInfo ci, boolean first) {
        
        Struct g = ci.head;

        if (g.subs() == 0) {
            return;
        }

        Term t = g.sub(0).term();
        if (t instanceof Var) {
            numCompClausesIndex.insertAsShared(ci, first);
            constantCompClausesIndex.insertAsShared(ci, first);
            structCompClausesIndex.insertAsShared(ci, first);

            if (first) {
                listCompClausesList.addFirst(ci);
            } else {
                listCompClausesList.addLast(ci);
            }
        } else if (t.isAtom()) {
            if (t instanceof NumberTerm) {
                numCompClausesIndex.insert((NumberTerm) t, ci, first);
            } else if (t instanceof Struct) {
                constantCompClausesIndex.insert(((Struct) t).name(), ci, first);
            }
        } else if (t instanceof Struct) {
            if (isAList((Struct) t)) {
                if (first) {
                    listCompClausesList.addFirst(ci);
                } else {
                    listCompClausesList.addLast(ci);
                }
            } else {
                structCompClausesIndex.insert(((Struct) t).key(), ci, first);
            }
        }

    }

    
    private void unregister(ClauseInfo ci) {
        Term clause = ci.head;
        if (clause != null) {
            Struct g = (Struct) clause.term();

            if (g.subs() == 0) {
                return;
            }

            Term t = g.sub(0).term();
            if (t instanceof Var) {
                numCompClausesIndex.removeShared(ci);
                constantCompClausesIndex.removeShared(ci);
                structCompClausesIndex.removeShared(ci);

                listCompClausesList.remove(ci);
            } else if (t.isAtom()) {
                if (t instanceof NumberTerm) {
                    numCompClausesIndex.remove((NumberTerm) t, ci);
                } else if (t instanceof Struct) {
                    constantCompClausesIndex.remove(((Struct) t).name(), ci);
                }
            } else if (t instanceof Struct) {
                if (t.isList()) {
                    listCompClausesList.remove(ci);
                } else {
                    structCompClausesIndex.remove(((Struct) t).key(), ci);
                }
            }
        }
    }


















































































}