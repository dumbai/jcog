package jcog.search;

import jcog.search.impl.ClosedSet;
import jcog.search.impl.IClosedSet;
import jcog.search.impl.IOpenSet;
import jcog.search.impl.OpenSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Uses the A* Algorithm to find the shortest path from
 * an initial to a goal node.
 */
public class AStarGoalFind<F extends Solution> {



    

    public Solution bestNodeAfterSearch;
    
    
    private int maxSteps = -1;
    
    private int iteration;

    /**
     * the shortest Path from a start node to an end node according to
     * the A* heuristics (h must not overestimate). initialNode and last found node included.
     */
    public final List<Solution> plan;

    public AStarGoalFind(Problem<F> problem, F initialNode, F goalNode) {

        F endNode = this.search(problem, initialNode, goalNode);

        this.plan = endNode!=null ? AStarGoalFind.path(endNode) : null;

    }

    /**
     * returns path from the earliest ancestor to the node in the argument
     * if the parents are set via AStar search, it will return the path found.
     * This is the shortest shortest path, if the heuristic h does not overestimate
     * the true remaining costs
     *
     * @param node node from which the parents are to be found. Parents of the node should
     *             have been properly set in preprocessing (f.e. AStar.search)
     * @return path to the node in the argument
     */
    public static <F extends Solution> List<Solution> path(F node) {
        List<Solution> path = new ArrayList<>();
        path.add(node);
        Solution currentNode = node;
        while (currentNode.parent() != null) {
            Solution parent = currentNode.parent();
            path.add(0, parent);
            currentNode = parent;
        }
        return path;
    }



    /**
     *
     * @param problem
     * @param initialNode start of the search
     * @param goalNode    end of the search
     * @return goal node from which you can reconstruct the path
     */
    F search(Problem<F> problem,  F initialNode, F goalNode) {

        Comparator<F> SEARCH_COMPARATOR = Comparator.comparingDouble((x)->
                x.g() + problem.cost(x, goalNode));

        IOpenSet<F> openSet = new OpenSet(SEARCH_COMPARATOR);
        openSet.add(initialNode);

        IClosedSet<F> closedSet = new ClosedSet(SEARCH_COMPARATOR);

        this.iteration = 0;

        while (openSet.size() > 0 && (maxSteps < 0 || this.iteration < maxSteps)) {
            
            
            F currentNode = openSet.poll();






            if (goalNode.goalOf(currentNode)) {
                
                this.bestNodeAfterSearch = currentNode;
                return currentNode;
            }
            
            Iterable<F> successorNodes = problem.next(currentNode);
            for (F successorNode : successorNodes) {
                if (closedSet.contains(successorNode))
                    continue;
                /* Special rule for nodes that are generated within other nodes:
                 * We need to ensure that we use the node and
                 * its g value from the openSet if its already discovered
                 */
                F discSuccessorNode = openSet.getNode(successorNode);
                boolean inOpenSet;
                if (discSuccessorNode != null) {
                    successorNode = discSuccessorNode;
                    inOpenSet = true;
                } else {
                    inOpenSet = false;
                }
                
                double tentativeG = currentNode.g() + problem.cost(currentNode,successorNode);
                
                if (inOpenSet && tentativeG >= successorNode.g())
                    continue;
                successorNode.setParent(currentNode);
                if (inOpenSet) {
                    
                    
                    openSet.remove(successorNode);
				}
				successorNode.setG(tentativeG);
				openSet.add(successorNode);
			}
            closedSet.add(currentNode);
            this.iteration += 1;
        }

        this.bestNodeAfterSearch = closedSet.min();
        return null;
    }

    public int numSearchSteps() {
        return this.iteration;
    }

    public Solution bestNodeAfterSearch() {
        return this.bestNodeAfterSearch;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }


}
