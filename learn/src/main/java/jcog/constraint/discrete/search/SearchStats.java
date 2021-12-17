package jcog.constraint.discrete.search;

public class SearchStats {
    public long startTime;
    public boolean completed;
    public int nNodes;
    public int nFails;
    public int nSolutions;

    @Override
    public String toString() {
        String bf = (completed ? "Complete search\n" : "Incomplete search\n") +
                "#solutions  : " + nSolutions + '\n' +
                "#nodes      : " + nNodes + '\n' +
                "#fails      : " + nFails + '\n';
        return bf;
    }
}
