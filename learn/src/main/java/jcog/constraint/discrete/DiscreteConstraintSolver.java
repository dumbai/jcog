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
package jcog.constraint.discrete;

import jcog.constraint.discrete.propagation.PropagationQueue;
import jcog.constraint.discrete.propagation.Propagator;
import jcog.constraint.discrete.search.BinaryVarVal;
import jcog.constraint.discrete.search.DFSearch;
import jcog.constraint.discrete.search.Objective;
import jcog.constraint.discrete.search.SearchStats;
import jcog.constraint.discrete.trail.Trail;
import org.eclipse.collections.api.block.function.primitive.BooleanFunction;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

public class DiscreteConstraintSolver {

    private final Trail trail;
    private final PropagationQueue pQueue;
    private final DFSearch search;

    private boolean feasible;

    public DiscreteConstraintSolver() {
        this.trail = new Trail();
        this.pQueue = new PropagationQueue();
        this.search = new DFSearch(pQueue, trail);
        this.feasible = true;
    }

    public static BooleanFunction<List<BooleanSupplier>> binaryFirstFail(IntVar[] vars) {
        return new BinaryVarVal(vars, i -> vars[i].size(), i -> vars[i].min());
    }

    public static BooleanFunction<List<BooleanSupplier>> binary(IntVar[] vars, IntUnaryOperator varCost,
                                                                IntUnaryOperator valSelector) {
        return new BinaryVarVal(vars, varCost, valSelector);
    }

    public boolean isFeasible() {
        return feasible;
    }

    public Trail trail() {
        return trail;
    }

    public void setObjective(Objective obj) {
        this.search.setObjective(obj);
    }

    public void onSolution(Runnable action) {
        search.addSolutionAction(action);
    }

    public SearchStats solve(BooleanFunction<List<BooleanSupplier>> heuristic, Predicate<SearchStats> stopCondition) {
        return search.search(heuristic, stopCondition);
    }

    public SearchStats solve(BooleanFunction<List<BooleanSupplier>> heuristic) {
        return solve(heuristic, s -> false);
    }

    public IntVar intVar(int min, int max) {
        return new IntVarImpl(pQueue, trail, min, max);
    }

    public IntVar intVar(int value) {
        return new IntVarSingleton(pQueue, trail, value);
    }

    public IntVar intVar(int[] values) {
        return new IntVarImpl(pQueue, trail, values);
    }

    public boolean add(Propagator propagator) {
        feasible = feasible && propagator.setup() && pQueue.propagate();
        return feasible;
    }
}
