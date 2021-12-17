package alice.tuprolog;


public class SubGoalStore {

    private SubGoalTree goals;
    private SubGoalTree commaStruct;
    private int index;
    private SubGoal curSGId;

    SubGoalStore(SubGoalTree subTrees) {
        index = 0;
        curSGId = null;
        commaStruct = subTrees;
        goals= commaStruct;
    }

    /**
     * Ripristina ClauseStore allo stato i-esimo
     */
    Term backTo(SubGoal identifier) {
        popSubGoal(identifier);
        index--;
        return fetch();
    }

    void pushSubGoal(SubGoalTree subGoals) {
        curSGId = new SubGoal(curSGId, commaStruct, index);
        commaStruct = subGoals;
        goals = commaStruct;
        index = 0;
    }

    private void popSubGoal(SubGoal id) {
        goals = commaStruct;
        commaStruct = id.root();
        index = id.index();
        curSGId = id.parent();
    }

    /**
     * Restituisce la clausola da caricare
     */
    Term fetch() {
        while (true) {
//            fetched = true;
            if (index >= commaStruct.size()) {
                if (curSGId != null) {
                    popSubGoal(curSGId);
                } else {
                    return null;
                }
            } else {

                SubTree s = commaStruct.get(index++);
                if (s instanceof SubGoalTree) {
                    pushSubGoal((SubGoalTree) s);
                } else {
                    return (Term) s;
                }

            }
        }
    }

    /**
     * Indice del correntemente in esecuzione
     */
    public SubGoal current() {
        return new SubGoal(curSGId, commaStruct, index);
    }

    boolean haveSubGoals() {
        return index < goals.size();
    }

    public String toString() {
        return "goals: " + goals + ' '
                + "index: " + index;
    }

    /*
     * Methods for spyListeners
     */
    public SubGoalTree getSubGoals() {
        return goals;
    }

    boolean freeForTailCall() {
        return curSGId == null && !haveSubGoals();
    }
}