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

import jcog.constraint.discrete.modeling.Constraints;
import jcog.constraint.discrete.modeling.Views;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NQueensTest {
  
  @Test
  void test3Queens() {
    assertEquals(0, solveNQueens(3));
  }
  
  @Test
  void test4Queens() {
    assertEquals(2, solveNQueens(4) );
  }
  
  @Test
  void test8Queens() {
    assertEquals(92, solveNQueens(8));
  }
  
  @Test
  void test10Queens() {
    assertEquals(724, solveNQueens(10));
  }

  private static int solveNQueens(int n) {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar[] queens = new IntVar[n];
    IntVar[] queensUp = new IntVar[n];
    IntVar[] queensDown = new IntVar[n];
    for (int i = 0; i < n; i++) {
      queens[i] = solver.intVar(0, n - 1);
      queensUp[i] = Views.offset(queens[i], i);
      queensDown[i] = Views.offset(queens[i], -i);
    }
    solver.add(Constraints.allDifferent(queens));
    solver.add(Constraints.allDifferent(queensUp));
    solver.add(Constraints.allDifferent(queensDown));
    return solver.solve(DiscreteConstraintSolver.binaryFirstFail(queens)).nSolutions;
  }
}
