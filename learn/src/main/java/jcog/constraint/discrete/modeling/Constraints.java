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
package jcog.constraint.discrete.modeling;

import jcog.constraint.discrete.DiscreteConstraintSolver;
import jcog.constraint.discrete.IntVar;
import jcog.constraint.discrete.constraint.*;
import jcog.constraint.discrete.propagation.Propagator;

public enum Constraints {
	;

	public static Propagator lowerEqual(IntVar x, IntVar y) {
        return new LowerEqualVar(x, y, false);
    }

    public static Propagator lowerEqual(IntVar x, int k) {
        return new LowerEqualVal(x, k, false);
    }

    public static Propagator lower(IntVar x, IntVar y) {
        return new LowerEqualVar(x, y, true);
    }

    public static Propagator lower(IntVar x, int k) {
        return new LowerEqualVal(x, k, true);
    }

    public static Propagator greaterEqual(IntVar x, IntVar y) {
        return new LowerEqualVar(x, y, false);
    }

    public static Propagator greaterEqual(IntVar x, int k) {
        return new LowerEqualVal(Views.opposite(x), -k, false);
    }

    public static Propagator greater(IntVar x, IntVar y) {
        return new LowerEqualVar(x, y, true);
    }

    public static Propagator greater(IntVar x, int k) {
        return new LowerEqualVal(Views.opposite(x), -k, true);
    }

    public static Propagator different(IntVar x, IntVar y) {
        return new DifferentVar(x, y);
    }

    public static Propagator different(IntVar x, int k) {
        return new DifferentVal(x, k);
    }

    public static Propagator allDifferent(IntVar[] variables) {
        return new AllDifferent(variables);
    }

    public static Propagator sum(IntVar[] variables, IntVar sum, int k) {
        return new Sum(variables, sum, k);
    }

    public static IntVar sum(DiscreteConstraintSolver solver, IntVar[] variables, int k) {
        int min = k;
        int max = k;
        for (IntVar variable : variables) {

            min += variable.min();
            max += variable.max();
        }
        IntVar result = solver.intVar(min, max);
        solver.add(sum(variables, result, k)); 
        return result;
    }
}
