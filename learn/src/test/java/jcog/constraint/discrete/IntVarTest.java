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

import jcog.constraint.discrete.trail.Trail;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

abstract class IntVarTest {

  protected abstract IntVar intVar(DiscreteConstraintSolver solver, int min, int max);

  protected abstract IntVar intVar(DiscreteConstraintSolver solver, int[] values);

  
  private static boolean containsAll(IntVar x, int... values) {
    return IntStream.range(0, values.length).allMatch(i -> x.contains(values[i]));
  }

  
  @Test
  void test1() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 10);
    assertEquals(x.size(), 6);
    assertTrue(containsAll(x, 5, 6, 7, 8, 9, 10));
  }

  
  @Test
  void test1a() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    int[] values = {-10, 0, 10, 100};
    IntVar x = intVar(solver, values);
    assertEquals(x.size(), 4);
    assertTrue(containsAll(x, values));
  }

  
  @Test
  void test2() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 15);
    assertTrue(x.contains(5));
    assertTrue(x.contains(10));
    assertTrue(x.contains(15));
  }

  
  @Test
  void test3() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 15);
    assertFalse(x.contains(-1000));
    assertFalse(x.contains(-100));
    assertFalse(x.contains(4));
    assertFalse(x.contains(16));
    assertFalse(x.contains(100));
    assertFalse(x.contains(1000));
  }

  
  @Test
  void test4() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 15);
    assertEquals(x.size(), 11);
    assertTrue(x.updateMin(10));
    assertEquals(x.size(), 6);
    assertEquals(x.min(), 10);
    assertTrue(containsAll(x, 10, 11, 12, 13, 14, 15));
  }

  
  
  @Test
  void test5() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 15);
    assertTrue(x.updateMin(4));
    assertEquals(x.size(), 11);
    assertEquals(x.min(), 5);
    assertEquals(x.max(), 15);
    assertTrue(x.updateMin(5));
    assertEquals(x.size(), 11);
    assertEquals(x.min(), 5);
    assertEquals(x.max(), 15);
  }

  
  @Test
  void test6() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 15);
    assertTrue(x.updateMin(15));
    assertEquals(x.size(), 1);
    assertTrue(x.isAssigned());
    assertTrue(x.contains(15));
  }

  
  @Test
  void test7() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 15);
    assertFalse(x.updateMin(20));
  }

  
  @Test
  void test8() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 15);
    assertEquals(x.size(), 11);
    assertTrue(x.updateMax(10));
    assertEquals(x.size(), 6);
    assertEquals(x.max(), 10);
    assertTrue(containsAll(x, 5, 6, 7, 8, 9, 10));
  }

  
  
  @Test
  void test9() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 15);
    assertTrue(x.updateMax(20));
    assertEquals(x.size(), 11);
    assertEquals(x.min(), 5);
    assertEquals(x.max(), 15);
    assertTrue(x.updateMax(15));
    assertEquals(x.size(), 11);
    assertEquals(x.min(), 5);
    assertEquals(x.max(), 15);
  }

  
  @Test
  void test10() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 15);
    assertTrue(x.updateMax(5));
    assertEquals(x.size(), 1);
    assertTrue(x.isAssigned());
    assertTrue(x.contains(5));
  }

  
  @Test
  void test11() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 15);
    assertFalse(x.updateMax(0));
  }

  
  @Test
  void test13() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    Trail trail = solver.trail();
    IntVar x = intVar(solver, 5, 15);
    trail.newLevel();
    assertTrue(x.updateMax(10));
    assertEquals(x.min(), 5);
    assertEquals(x.max(), 10);
    assertEquals(x.size(), 6);
    assertTrue(containsAll(x, 5, 6, 7, 8, 9, 10));
    trail.newLevel();
    assertTrue(x.updateMax(9));
    assertTrue(x.updateMin(6));
    assertEquals(x.min(), 6);
    assertEquals(x.max(), 9);
    assertEquals(x.size(), 4);
    assertTrue(containsAll(x, 6, 7, 8, 9));
    trail.newLevel();
    trail.undoLevel();
    assertEquals(x.min(), 6);
    assertEquals(x.max(), 9);
    assertEquals(x.size(), 4);
    assertTrue(containsAll(x, 6, 7, 8, 9));
    trail.undoLevel();
    assertEquals(x.min(), 5);
    assertEquals(x.max(), 10);
    assertEquals(x.size(), 6);
    assertTrue(containsAll(x, 5, 6, 7, 8, 9, 10));
    trail.undoLevel();
    assertEquals(x.min(), 5);
    assertEquals(x.max(), 15);
    assertEquals(x.size(), 11);
    assertTrue(containsAll(x, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15));
  }

  
  @Test
  void test14() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 15);
    assertTrue(x.assign(10));
    assertTrue(x.contains(10));
    assertEquals(x.min(), 10);
    assertEquals(x.max(), 10);
  }

  
  @Test
  void test15() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 15);
    assertTrue(x.assign(10));
    assertEquals(x.size(), 1);
  }

  
  @Test
  void test16() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 15);
    assertFalse(x.assign(20));
  }

  
  @Test
  void test17() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 10);
    assertTrue(x.remove(5));
    assertFalse(x.contains(5));
    assertTrue(x.remove(7));
    assertFalse(x.contains(7));
    assertTrue(x.remove(8));
    assertFalse(x.contains(8));
  }

  
  @Test
  void test18() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 10);
    int size = x.size();
    assertTrue(x.remove(5));
    assertEquals(x.size(), size - 1);
    assertTrue(x.remove(5));
    assertEquals(x.size(), size - 1);
    assertTrue(x.remove(6));
    assertEquals(x.size(), size - 2);
  }

  
  @Test
  void test19() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 10);
    int size = x.size();
    assertTrue(x.remove(4));
    assertEquals(x.size(), size);
    assertTrue(x.remove(11));
    assertEquals(x.size(), size);
  }

  
  @Test
  void test20() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 10);
    assertTrue(x.remove(5));
    assertEquals(x.min(), 6);
    assertTrue(x.remove(6));
    assertTrue(x.remove(7));
    assertEquals(x.min(), 8);
    assertTrue(x.remove(10));
    assertEquals(x.min(), 8);
  }

  
  @Test
  void test21() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 5, 10);
    assertTrue(x.remove(5));
    assertTrue(x.contains(7));
    assertTrue(x.remove(6));
    assertTrue(x.contains(7));
    assertTrue(x.remove(9));
    assertTrue(x.contains(7));
    assertTrue(x.remove(10));
    assertTrue(x.contains(7));
    assertTrue(x.remove(8));
    assertTrue(x.contains(7));
    assertTrue(x.isAssigned());
  }

  
  @Test
  void test22() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    Trail trail = solver.trail();
    IntVar x = intVar(solver, 5, 10);
    trail.newLevel();
    assertTrue(x.remove(5));
    assertTrue(x.remove(6));
    trail.newLevel();
    assertTrue(x.remove(9));
    trail.newLevel();
    assertTrue(x.remove(8));
    assertFalse(x.contains(5));
    assertFalse(x.contains(6));
    assertTrue(x.contains(7));
    assertFalse(x.contains(8));
    assertFalse(x.contains(9));
    assertTrue(x.contains(10));
    trail.undoLevel();
    assertFalse(x.contains(5));
    assertFalse(x.contains(6));
    assertTrue(x.contains(7));
    assertTrue(x.contains(8));
    assertFalse(x.contains(9));
    assertTrue(x.contains(10));
    trail.undoLevel();
    assertFalse(x.contains(5));
    assertFalse(x.contains(6));
    assertTrue(x.contains(7));
    assertTrue(x.contains(8));
    assertTrue(x.contains(9));
    assertTrue(x.contains(10));
    trail.undoLevel();
    assertTrue(x.contains(5));
    assertTrue(x.contains(6));
    assertTrue(x.contains(7));
    assertTrue(x.contains(8));
    assertTrue(x.contains(9));
    assertTrue(x.contains(10));
  }

  
  @Test
  void test23() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    IntVar x = intVar(solver, 10, 10);
    assertFalse(x.remove(10));
  }

  
  @Test
  void test24() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    int[] values = {10, 11, 15, 16, 17, 20, 21, 25};
    IntVar x = intVar(solver, values);
    assertTrue(x.updateMin(12));
    assertEquals(x.size(), 6);
    assertEquals(x.min(), 15);
  }

  
  @Test
  void test25() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    int[] values = {10, 11, 15, 16, 17, 20, 21, 25};
    IntVar x = intVar(solver, values);
    assertTrue(x.updateMin(16));
    assertFalse(x.contains(10));
    assertFalse(x.contains(11));
    assertFalse(x.contains(15));
  }

  
  @Test
  void test26() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    int[] values = {10, 11, 15, 16, 17, 20, 21, 25};
    IntVar x = intVar(solver, values);
    assertTrue(x.updateMax(19));
    assertEquals(x.size(), 5);
    assertEquals(x.max(), 17);
  }

  
  @Test
  void test27() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    int[] values = {10, 11, 15, 16, 17, 20, 21, 25};
    IntVar x = intVar(solver, values);
    assertTrue(x.updateMax(17));
    assertFalse(x.contains(20));
    assertFalse(x.contains(21));
    assertFalse(x.contains(25));
  }

  
  @Test
  void test28() {
    DiscreteConstraintSolver solver = new DiscreteConstraintSolver();
    int[] values1 = {10, 11, 15, 16, 17, 20, 21, 25};
    int[] domain1 = new int[values1.length];
    IntVar x = intVar(solver, values1);
    assertTrue(containsAll(x, values1));
    assertEquals(x.size(), values1.length);
    assertEquals(x.size(), x.copyDomain(domain1));
    assertTrue(containsAll(x, domain1));
    assertTrue(x.remove(11));
    assertTrue(x.remove(17));
    assertTrue(x.remove(25));
    assertEquals(x.size(), 5);
    int[] values2 = {10, 15, 16, 20, 21};
    int[] domain2 = new int[values2.length];
    assertTrue(containsAll(x, values2));
    assertEquals(x.size(), values2.length);
    assertEquals(x.size(), x.copyDomain(domain2));
    Arrays.sort(domain2);
    for (int i = 0; i < x.size(); i++) {
      assertEquals(values2[i], domain2[i]);
    }
  }
}
