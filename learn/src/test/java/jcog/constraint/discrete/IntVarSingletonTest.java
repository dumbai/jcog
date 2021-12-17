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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntVarSingletonTest {

  
  @Test
  void test1() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = solver.intVar(5);
    assertEquals(x.size(), 1);
    assertTrue(x.isAssigned());
    assertTrue(x.contains(5));
  }

  
  @Test
  void test3() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = solver.intVar(5);
    assertFalse(x.contains(-5));
    assertFalse(x.contains(0));
    assertFalse(x.contains(4));
    assertFalse(x.contains(6));
  }

  
  
  @Test
  void test5() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = solver.intVar(5);
    assertTrue(x.updateMin(4));
    assertEquals(x.size(), 1);
    assertEquals(x.min(), 5);
    assertEquals(x.max(), 5);
    assertTrue(x.updateMin(5));
    assertEquals(x.size(), 1);
    assertEquals(x.min(), 5);
    assertEquals(x.max(), 5);
  }

  
  @Test
  void test7() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = solver.intVar(5);
    assertFalse(x.updateMin(6));
  }

  
  
  @Test
  void test9() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = solver.intVar(5);
    assertTrue(x.updateMax(6));
    assertEquals(x.size(), 1);
    assertEquals(x.min(), 5);
    assertEquals(x.max(), 5);
    assertTrue(x.updateMax(5));
    assertEquals(x.size(), 1);
    assertEquals(x.min(), 5);
    assertEquals(x.max(), 5);
  }

  
  @Test
  void test11() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = solver.intVar(5);
    assertFalse(x.updateMax(0));
  }

  
  @Test
  void test14() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = solver.intVar(5);
    assertTrue(x.assign(5));
    assertTrue(x.contains(5));
    assertTrue(x.isAssigned());
    assertEquals(x.min(), 5);
    assertEquals(x.max(), 5);
  }

  
  @Test
  void test16() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = solver.intVar(5);
    assertFalse(x.assign(20));
  }

  
  @Test
  void test19() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = solver.intVar(5);
    assertTrue(x.remove(4));
    assertTrue(x.isAssigned());
    assertTrue(x.remove(6));
    assertTrue(x.isAssigned());
  }

  
  @Test
  void test23() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = solver.intVar(5);
    assertFalse(x.remove(5));
  }

  
  @Test
  void test28() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    int[] domain = new int[1];
    IntVar x = solver.intVar(5);
    assertEquals(x.size(), x.copyDomain(domain));
    assertEquals(domain[0], 5);
  }
}
