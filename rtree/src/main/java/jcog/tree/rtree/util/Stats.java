package jcog.tree.rtree.util;

/*
 * #%L
 * Conversant RTree
 * ~~
 * Conversantmedia.com © 2016, Conversant, Inc. Conversant® is a trademark of Conversant, Inc.
 * ~~
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import jcog.tree.rtree.Spatialization;

import java.io.PrintStream;
import java.util.Arrays;

/**
 * Created by jcovert on 5/20/15.
 */
public class Stats {

    private static final int MAX_DEPTH = 96;
    private final int[] entriesAtDepth = new int[MAX_DEPTH];
    private final int[] branchesAtDepth = new int[MAX_DEPTH];
    private final int[] leavesAtDepth = new int[MAX_DEPTH];
    private Spatialization type;
    private int maxFill;
    private int minFill;
    private int maxDepth;
    private int branchCount;
    private int leafCount;
    private int entryCount;

    public Stats print(PrintStream out) {
        out.println("[" + type + "] m=" + minFill + " M=" + maxFill);
        out.println("   Branches (" + branchCount + " total)");
        out.print("      ");
        for (int i = 0; i <= maxDepth; i++) {
            out.print(i + ": " + branchesAtDepth[i] + "  ");
        }
        out.print("\n\tLeaves (" + leafCount + "): ");
        for (int i = 0; i <= maxDepth; i++) {
            out.print(i + ": " + leavesAtDepth[i] + "  ");
        }
        out.print("\n\tEntries (" + entryCount + "): ");
        for (int i = 0; i <= maxDepth; i++) {
            out.print(i + ": " + entriesAtDepth[i] + "  ");
        }
        out.printf("\n\tLeaf Fill Percentage: %.2f%%", getLeafFillPercentage());
        out.printf("\tEntries per Leaf: %.2f", getEntriesPerLeaf());
        out.println("\tMax Depth: " + maxDepth);
        out.println();

        return this;
    }

    public float getEntriesPerLeaf() {
        return ((entryCount * 1.0f) / leafCount);
    }

    private float getLeafFillPercentage() {
        return (getEntriesPerLeaf() * 100) / maxFill;
    }

    public Spatialization getType() {
        return type;
    }

    public void setType(Spatialization type) {
        this.type = type;
    }

    public void setMaxFill(int maxFill) {
        this.maxFill = maxFill;
    }

    public void setMinFill(int minFill) {
        this.minFill = minFill;
    }

    public int getBranchCount() {
        return branchCount;
    }

    public int getLeafCount() {
        return leafCount;
    }

    public int size() {
        return entryCount;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void countEntriesAtDepth(int entries, int depth) {
        entryCount += entries;
        entriesAtDepth[depth] += entries;
    }

    public void countLeafAtDepth(int depth) {
        leafCount++;
        leavesAtDepth[depth]++;
    }

    public void countBranchAtDepth(int depth) {
        branchCount++;
        branchesAtDepth[depth]++;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "type=" + type +
                ", maxFill=" + maxFill +
                ", minFill=" + minFill +
                ", maxDepth=" + maxDepth +
                ", branchCount=" + branchCount +
                ", leafCount=" + leafCount +
                ", entryCount=" + entryCount +
                ", entriesAtDepth=" + Arrays.toString(entriesAtDepth) +
                ", branchesAtDepth=" + Arrays.toString(branchesAtDepth) +
                ", leavesAtDepth=" + Arrays.toString(leavesAtDepth) +
                '}';
    }

    public final void print() {
        print(System.out);
    }
}