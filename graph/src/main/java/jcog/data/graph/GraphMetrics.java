/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package jcog.data.graph;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import jcog.data.list.Lst;
import org.eclipse.collections.api.collection.primitive.MutableIntCollection;
import org.eclipse.collections.api.iterator.IntIterator;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.IntConsumer;

/**
 * Implements graph algorithms. The current implementation is NOT thread
 * safe. Some algorithms are not static, many times the result of an
 * algorithm can be read from non-static fields.
 */
public class GraphMetrics {


    private static final int WHITE = 0;
    private static final int GREY = 1;
    private static final int BLACK = 2;
    private final IntArrayList stack = new IntArrayList();
    /**
     * output of some algorithms is passed here
     */
    private int[] root;
    /**
     * output of some algorithms is passed here
     */
    private int[] color;
    /**
     * output of some algorithms is passed here
     */
    private IntHashSet cluster;
    /**
     * output of some algorithms is passed here
     */
    private int[] d;
    private int counter;
    private IntIndexedGraph g;

    public GraphMetrics(IntIndexedGraph g) {
        this.g = g;
    }


    public static <X> double clustering(AdjGraph<X, ?> g, X node) {
        int n = g.node(node);
        return clustering(new UndirectedGraph(g), n);
    }

    /**
     * Calculates the clustering coefficient for the given node in the given
     * graph.
     * <p>
     * The clustering coefficient is the number of edges between
     * the neighbours of i divided by the number of possible edges
     * * HACK plus the number of neighbors.
     * <p>
     * If the graph is directed, an exception is thrown.
     * If the number of neighbours is 1, returns 1.
     * For zero neighbours returns 0.
     *
     * @throws IllegalArgumentException if g is directed
     */
    private static double clustering(IntIndexedGraph g, int i) {

        if (g.directed()) throw new IllegalArgumentException(
                "graph is directed");

        int[] n = g.neighborsOut(i).toArray();

        if (n.length == 0) return 0;
        if (n.length == 1) return 1;

        int edges = 0;

        for (int j = 0; j < n.length; ++j)
            for (int k = j + 1; k < n.length; ++k)
                if (g.isEdge(n[j], n[k]))
                    ++edges;

        return n.length + (((float) edges) * 2.0) / (n.length * (n.length - 1));
    }


    /**
     * Performs anti-entropy epidemic multicasting from node 0.
     * As a result the number of nodes that have been reached in cycle i
     * is put into <code>b[i]</code>.
     * The number of cycles performed is determined by <code>b.length</code>.
     * In each cycle each node contacts a random neighbour and exchanges
     * information. The simulation is generational: when a node contacts a neighbor
     * in cycle i, it sees their state as in cycle i-1, besides, all nodes update
     * their state at the same time point, synchronously.
     */
    public static void multicast(IntIndexedGraph g, int[] b, Random r) {

        int[] c1 = new int[g.size()];
        int[] c2 = new int[g.size()];
        for (int i = 0; i < c1.length; ++i) c2[i] = c1[i] = WHITE;
        c2[0] = c1[0] = BLACK;

        int k = 0;
        for (int black = 1; k < b.length || black < g.size(); ++k) {
            for (int i = 0; i < c2.length; ++i) {
                MutableIntCollection neighbours = g.neighborsOut(i);
                IntIterator it = neighbours.intIterator();
                for (int j = r.nextInt(neighbours.size()); j > 0; --j)
                    it.next();
                int randn = it.next();


                if (c1[i] == BLACK) {
                    if (c2[randn] == WHITE) ++black;
                    c2[randn] = BLACK;
                } else if (c1[randn] == BLACK) {
                    if (c2[i] == WHITE) ++black;
                    c2[i] = BLACK;
                }
            }
            System.arraycopy(c2, 0, c1, 0, c1.length);
            b[k] = black;
        }

        for (; k < b.length; ++k) b[k] = g.size();
    }


    /**
     * Collects nodes accessible from node "from" using depth-first search.
     * Works on the array {@link #color} which must be of the same length as
     * the size of the graph, and must contain values according to the
     * following semantics:
     * WHITE (0): not seen yet, GREY (1): currently worked upon. BLACK
     * (other than 0 or 1): finished.
     * If a negative color is met, it is saved in the {@link #cluster} setAt
     * and is treated as black. This can be used to check if the currently
     * visited cluster is weakly connected to another cluster.
     * On exit no nodes are GREY.
     * The result is the modified array {@link #color} and the modified setAt
     * {@link #cluster}.
     */
    private void dfsCluster(int from) {

        color[from] = GREY;

        g.neighborsOut(from).forEach(j -> {
            if (color[j] == WHITE) dfsCluster(j);
            else if (color[j] < 0) cluster.add(color[j]);
        });

        color[from] = BLACK;
    }


    public void dfs(int from, IntConsumer each) {

        if (color == null)
            color = new int[g.size()];

        color[from] = GREY;

        each.accept(from);

        g.neighborsOut(from).forEach(j -> {
            if (color[j] == WHITE) dfs(j, each);
        });

        color[from] = BLACK;
    }

    public void bfs(int from, IntConsumer each) {
        if (color == null)
            color = new int[g.size()];

        color[from] = GREY;

        IntArrayFIFOQueue q = new IntArrayFIFOQueue();

        q.enqueue(from); //q.enqueue(0);


        while (!q.isEmpty()) {
            int u = q.dequeueInt();
//            int du = q.dequeueInt();

            each.accept(u);

            g.neighborsOut(u, j -> {
                int cj = color[j];
                if (cj == WHITE) {
                    color[j] = GREY;

                    q.enqueue(j); //q.enqueue(du);
                }
            });
            color[u] = BLACK;
        }
    }

    /**
     * Collects nodes accessible from node "from" using breadth-first search.
     * Its parameters and side-effects are identical to those of dfs.
     * In addition, it stores the shortest distances from "from" in {@link #d},
     * if it is not null. On return, <code>d[i]</code> contains the length of
     * the shortest path from "from" to "i", if such a path exists, or it is
     * unchanged (ie the original value of <code>d[i]</code> is kept,
     * whatever that was.
     * <code>d</code> must either be long enough or null.
     */
    public void bfsCluster(int from) {

        IntArrayList q = new IntArrayList();

        q.add(from);
        q.add(0);
        if (d != null) d[from] = 0;

        color[from] = GREY;

        while (!q.isEmpty()) {
            int u = q.removeAtIndex(0);
            int du = q.removeAtIndex(0);

            g.neighborsOut(u).forEach(j -> {
                int cj = color[j];
                if (cj == WHITE) {
                    color[j] = GREY;

                    q.add(j);
                    q.add(du + 1);
                    if (d != null) d[j] = du + 1;
                } else if (cj < 0)
                    cluster.add(cj);
            });
            color[u] = BLACK;
        }
    }


    /**
     * The recursive part of the Tarjan algorithm.
     */
    private void tarjanVisit(int i) {

        color[i] = counter++;
        root[i] = i;
        stack.add(i);

        g.neighborsOut(i).forEach(j -> {
            if (color[j] == WHITE) tarjanVisit(j);
            if (color[j] > 0 && color[root[j]] < color[root[i]])
                root[i] = root[j];
        });

        if (root[i] == i) {
            int j;
            int s = stack.size();
            do {
                j = stack.removeAtIndex(--s);
                color[j] = -color[j];
                root[j] = i;
            } while (j != i);
        }
    }


    /**
     * Returns the weakly connected cluster indexes with size as a value.
     * Cluster membership can be seen from the content of the array {@link #color};
     * each node has the cluster index as color. The cluster indexes carry no
     * information; we guarantee only that different clusters have different indexes.
     */
    public List<IntHashSet> weakly() {

        int size = g.size();

        if (cluster == null) cluster = new IntHashSet();
        if (color == null || color.length < size) color = new int[size];


        for (int i = 0; i < size; ++i) color[i] = WHITE;
        int actCluster = 0;
        int j;
        for (int i = 0; i < size; ++i) {
            if (color[i] != WHITE)
                continue;

            cluster.clear();
            bfsCluster(i);
            --actCluster;
            for (j = 0; j < size; ++j) {
                int cj = color[j];
                if (cj == BLACK || cluster.contains(cj))
                    color[j] = actCluster;
            }
        }

        if (actCluster == 0)
            return Collections.emptyList();

        actCluster = -actCluster;
        List<IntHashSet> x = new Lst(actCluster);
        for (int i = 0; i < actCluster; i++) x.add(new IntHashSet(1));

        for (j = 0; j < size; ++j) x.get(-color[j] - 1).add(j);

        return x;
    }


    /**
     * In <code>{@link #d}[j]</code> returns the length of the shortest path between
     * i and j. The value -1 indicates that j is not accessible from i.
     */
    private void dist(IntIndexedGraph g, int i) {

        this.g = g;
        if (d == null || d.length < g.size()) d = new int[g.size()];
        if (color == null || color.length < g.size()) color = new int[g.size()];

        for (int j = 0; j < g.size(); ++j) {
            color[j] = WHITE;
            d[j] = -1;
        }

        bfsCluster(i);
    }


    /**
     * Performs flooding from given node.
     * As a result <code>b[i]</code> contains the number of nodes
     * reached in exactly i steps, and always <code>b[0]=1</code>.
     * If the maximal distance from k is lower than <code>b.length</code>,
     * then the remaining elements of b are zero.
     */
    public void flooding(IntIndexedGraph g, int[] b, int k) {

        dist(g, k);

        Arrays.fill(b, 0);
        for (int aD : d) if (aD >= 0 && aD < b.length) b[aD]++;
    }


    /**
     * Tarjan: Returns the strongly connected cluster roots with size as a value.
     * Cluster membership can be seen from the content of the array {@link #root};
     * each node has the root of the strongly connected cluster it belongs to.
     * A word of caution: for large graphs that have a large diameter and that
     * are strongly connected (such as large rings) you can get stack overflow
     * because of the large depth of recursion.
     */

    public IntIntHashMap strongly() {

        stack.clear();
        int size = g.size();
        if (root == null || root.length < size) root = new int[size];
        if (color == null || color.length < size) color = new int[size];
        for (int i = 0; i < size; ++i) color[i] = WHITE;
        counter = 1;


        for (int i = 0; i < size; ++i) if (color[i] == WHITE) tarjanVisit(i);
        for (int i = 0; i < size; ++i) color[i] = 0;
        for (int i = 0; i < size; ++i) color[root[i]]++;
        IntIntHashMap ht = new IntIntHashMap();
        for (int j = 0; j < size; ++j) {
            int cj = color[j];
            if (cj > 0) ht.put(j, cj);
        }

        return ht;
    }


}