/*
 * Copyright 2016, Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcog.constraint.discrete.search;

import jcog.constraint.discrete.propagation.PropagationQueue;
import jcog.constraint.discrete.trail.Trail;
import jcog.data.list.Lst;
import org.eclipse.collections.api.block.function.primitive.BooleanFunction;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class DFSearch {

    private final PropagationQueue pQueue;
    private final Trail trail;

    private final Lst<BooleanSupplier> decisions = new Lst<>();
    private final Lst<Runnable> solutionActions = new Lst<>();

    private Objective objective;

    public DFSearch(PropagationQueue pQueue, Trail trail) {
        this.pQueue = pQueue;
        this.trail = trail;
    }

    public void addSolutionAction(Runnable action) {
        solutionActions.add(action);
    }

    public void foundSolution(SearchStats stats) {
        stats.nSolutions++;
        solutionActions.forEach(Runnable::run);
        if (objective != null) {
            objective.tighten();
        }
    }

    public void setObjective(Objective obj) {
        this.objective = obj;
    }

    private boolean propagate() {
        
        boolean feasible = objective == null || objective.propagate();
        
        return feasible && pQueue.propagate();
    }

    /**
     * Starts the search
     *
     * @param heuristic     the search heursitic used to build the search tree.
     * @param stopCondition a predicate to stop the search.
     * @return A {@code SearchStats} object that contains some metrics related
     * to this tree search.
     */
    public SearchStats search(BooleanFunction<List<BooleanSupplier>> heuristic, Predicate<SearchStats> stopCondition) {
        SearchStats stats = new SearchStats();

        stats.startTime = System.currentTimeMillis();

        
        if (!propagate()) {
            stats.completed = true;
            return stats;
        }

        
        if (heuristic.booleanValueOf(decisions)) {
            foundSolution(stats);
            stats.completed = true;
            return stats;
        }

        
        trail.newLevel();

        
        
        
        while (!decisions.isEmpty() && !stopCondition.test(stats)) {
            stats.nNodes++;

            
            
            if (!decisions.removeLast().getAsBoolean() || !propagate()) {
                stats.nFails++;
                trail.undoLevel();
                continue;
            }

            
            
            if (heuristic.booleanValueOf(decisions)) {
                foundSolution(stats);
                trail.undoLevel();
                continue;
            }

            
            
            trail.newLevel();
        }

        
        stats.completed = decisions.isEmpty();

        
        
        trail.undoAll();
        decisions.clear();

        return stats;
    }
}
