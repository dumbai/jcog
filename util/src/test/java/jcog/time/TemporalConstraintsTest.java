/**
 * Copyright 2010 Jörn Franke Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http:
 */
package jcog.time;


import jcog.time.TemporalConstraints.AllenNode;
import jcog.time.TemporalConstraints.Constraint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jörn Franke <jornfranke@gmail.com>
 *
 */
class TemporalConstraintsTest {




    /*
     * Test Example of Consistent network
     * A STARTS B
     * A CONTAINS C
     *
     */

    @Test
    void testPathConsistency1() {
        TemporalConstraints<String> n = new TemporalConstraints<>();
        AllenNode<String> nodeA = new AllenNode<>("A");
        n.add(nodeA);
        AllenNode<String> nodeB = new AllenNode<>("B");
        n.add(nodeB);
        AllenNode<String> nodeC = new AllenNode<>("C");
        n.add(nodeC);
        n.add(new Constraint<>(nodeA, nodeB, TemporalConstraints.bin_starts));
        n.add(new Constraint<>(nodeA, nodeC, TemporalConstraints.bin_contains));
        assertTrue(n.pathConsistency());
    }

    /*
     * Test Example inconsistent network
     * A EQUALS B
     * B EQUALS C
     * C EQUALS D
     * A OVERLAPS D
     *
     */

    @Test
    void testPathConsistency2() {
        TemporalConstraints<String> n = new TemporalConstraints<>();
        AllenNode<String> a = new AllenNode<>("A");
        n.add(a);
        AllenNode<String> b = new AllenNode<>("B");
        n.add(b);
        AllenNode<String> c = new AllenNode<>("C");
        n.add(c);
        AllenNode<String> d = new AllenNode<>("D");
        n.add(d);
        n.add(new Constraint<>(a, b, TemporalConstraints.bin_equals));
        n.add(new Constraint<>(b, c, TemporalConstraints.bin_equals));
        n.add(new Constraint<>(c, d, TemporalConstraints.bin_equals));
        assertTrue(n.pathConsistency());
        n.add(new Constraint<>(a, d, TemporalConstraints.bin_overlaps));
        assertFalse(n.pathConsistency());
    }

    /*
     * Github Issue #1 _ 1
     * FIRST CASE
     * 2 BEFORE 0
     * 3 BEFORE 4
     * 4 BEFORE 1
     * 0 EQUALS 3
     * 4 MEETS 2
     * 5 FINISHES 1
     * SECOND CASE
     * REMOVE 4 BEFORE 1
     *
     */

    @Test
    void testPathConsistency_GithubIssue1_1() {
        TemporalConstraints<String> n = new TemporalConstraints<>();
        AllenNode<String> node0 = new AllenNode<>("0");
        n.add(node0);
        AllenNode<String> node1 = new AllenNode<>("1");
        n.add(node1);
        AllenNode<String> node2 = new AllenNode<>("2");
        n.add(node2);
        AllenNode<String> node3 = new AllenNode<>("3");
        n.add(node3);
        AllenNode<String> node4 = new AllenNode<>("4");
        n.add(node4);
        AllenNode<String> node5 = new AllenNode<>("5");
        n.add(node5);
        Constraint<String> constraint20 = new Constraint<>(node2, node0, TemporalConstraints.bin_before);
        n.add(constraint20);
        Constraint<String> constraint34 = new Constraint<>(node3, node4, TemporalConstraints.bin_before);
        n.add(constraint34);
        Constraint<String> constraint41 = new Constraint<>(node4, node1, TemporalConstraints.bin_before);
        n.add(constraint41);
        Constraint<String> constraint03 = new Constraint<>(node0, node3, TemporalConstraints.bin_equals);
        n.add(constraint03);
        Constraint<String> constraint42 = new Constraint<>(node4, node2, TemporalConstraints.bin_meets);
        n.add(constraint42);
        Constraint<String> constraint51 = new Constraint<>(node5, node1, TemporalConstraints.bin_finishes);
        n.add(constraint51);
        assertEquals(6, n.getModeledNodes().size());
        assertEquals(6, n.getModeledConstraints().size());
        assertFalse(n.pathConsistency());
    }


    /*
     * Github Issue #1 _ 2
     * 0 equals 3
     * 5 finishes 1
     * 4 meets 2
     * 2 before 0
     * 3 before 4
     * 4 before 1
     *
     */

    @Test
    void testPathConsistency_GithubIssue1_2() {
        TemporalConstraints<String> n = new TemporalConstraints<>();
        AllenNode<String> node0 = new AllenNode<>("0");
        n.add(node0);
        AllenNode<String> node1 = new AllenNode<>("1");
        n.add(node1);
        AllenNode<String> node2 = new AllenNode<>("2");
        n.add(node2);
        AllenNode<String> node3 = new AllenNode<>("3");
        n.add(node3);
        AllenNode<String> node4 = new AllenNode<>("4");
        n.add(node4);
        AllenNode<String> node5 = new AllenNode<>("5");
        n.add(node5);
        Constraint<String> constraint03 = new Constraint<>(node0, node3, TemporalConstraints.bin_equals);
        n.add(constraint03);
        Constraint<String> constraint51 = new Constraint<>(node5, node1, TemporalConstraints.bin_finishes);
        n.add(constraint51);
        Constraint<String> constraint42 = new Constraint<>(node4, node2, TemporalConstraints.bin_meets);
        n.add(constraint42);
        Constraint<String> constraint20 = new Constraint<>(node2, node0, TemporalConstraints.bin_before);
        n.add(constraint20);
        Constraint<String> constraint34 = new Constraint<>(node3, node4, TemporalConstraints.bin_before);
        n.add(constraint34);
        Constraint<String> constraint41 = new Constraint<>(node4, node1, TemporalConstraints.bin_before);
        n.add(constraint41);
        assertEquals(6, n.getModeledNodes().size());
        assertEquals(6, n.getModeledConstraints().size());
        assertFalse(n.pathConsistency());

    }


}
