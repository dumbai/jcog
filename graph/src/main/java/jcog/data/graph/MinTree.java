package jcog.data.graph;

import jcog.WTF;
import jcog.data.bit.MetalBitSet;
import jcog.data.list.Lst;
import org.jetbrains.annotations.Nullable;
import org.roaringbitmap.IntIterator;
import org.roaringbitmap.RoaringBitmap;

import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import static java.util.Comparator.comparingInt;

/**
 * Kruskal Minimum Spanning Tree
 * TODO impl other MST algorithms with common interface
 * from https://algorithms.tutorialhorizon.com/kruskals-algorithm-minimum-spanning-tree-mst-complete-java-implementation
 *
 */
public class MinTree {
    public static class IntEdge {
        /**
         * from -> to
         */
        int f, t;

        /**
         * weight
         */
        int w;

        IntEdge(int f, int t, int w) {
            this.f = f;
            this.t = t;
            this.w = w;
        }

        @Override
        public String toString() {
            return "{" + f + "->" + t + "}x" + w;
        }

    }

    public static class Graph {
        int vertices;
        final List<IntEdge> edges = new Lst<>(0);
        Lst<IntEdge> mst;
        transient MetalBitSet parenting;

        public Graph(int vertices) {
            this.vertices = vertices;
        }

        public void edge(int source, int destination, int weight) {
            if (source!=destination) {
                edges.add(new IntEdge(source, destination, weight));
            } //else: point
        }

        public List<IntTree> apply() {
            PriorityQueue<IntEdge> pq = new PriorityQueue<>(edges.size(), comparingInt(o -> o.w));
            pq.addAll(edges);

            //Make set- creating a new element with a parent pointer to itself.
            int v = this.vertices;
            int[] parent = new int[v];
            for (int i = 0; i < v; i++) parent[i] = i;

            int te = Math.min(edges.size(), v - 1);
            mst = new Lst<>(te);

            MetalBitSet roots = MetalBitSet.bits(v);
            roots.setRange(true, 0, v);

            //process vertices - 1 edges
            int index = 0;
            while (!pq.isEmpty() && index < te) {
                IntEdge edge = pq.remove();

                //adding this edge creates a cycle ?
                int F = find(parent, edge.f), T = find(parent, edge.t);

                //add it to our final result?
                if (F != T) {
                    mst.add(edge);
                    union(F, T, parent);
                    roots.clear(T);
                    index++;
                } //else: ignore, will create cycle
            }


            parenting = MetalBitSet.bits(v);
            List<IntTree> forest = new Lst<>(roots.cardinality());
            org.eclipse.collections.api.iterator.IntIterator i = roots.iterator(0, v);
            while (i.hasNext()) {
                int ii = i.next();
                forest.add(new IntTree(ii, this));
            }
            return forest;
        }

        RoaringBitmap childrenBitmap(int root) {
            return childrenBitmap(root, mst);
        }

        RoaringBitmap childrenBitmap(int root, List<IntEdge> mst) {
            RoaringBitmap c = new RoaringBitmap();
            for (IntEdge i : mst) {
                int F = i.f, T = i.t;
                if (F == root && !parenting.test(T)) c.add(T);
                if (T == root && !parenting.test(F)) c.add(F);
            }
            return c;
        }

        public static final class IntTree {
            public final int id;
            @Nullable
            public final IntTree[] children;

            private IntTree(int id, Graph g) {
                if (g.parenting.getAndSet(id, true))
                    throw new WTF();

                this.id = id;

                RoaringBitmap b = g.childrenBitmap(id);
                IntTree[] l;
                int size = b.getCardinality();
                if (size > 0) {

                    l = new IntTree[size];
                    int j = 0;
                    //System.out.println(b);
                    IntIterator ii = b.getIntIterator();
                    try {
                        while (ii.hasNext()) {
                            l[j++] = new IntTree(ii.next(), g);
                        }
                    } catch (StackOverflowError e) {
                        System.exit(1);
                    }
                } else
                    l = null;
                this.children = l;
            }

            @Override
            public String toString() {
                return id + (children!=null ? Arrays.toString(children) : "");
            }

            public int size() {
                return children!=null ? children.length : 0;
            }


        }

        /**
         * chain of parent pointers from x upwards through the tree
         * until an element is reached whose parent is itself
         */
        private int find(int[] parent, int vertex) {
            while (true) {
                int p = parent[vertex];
                if (p == vertex) {

                    return vertex;
                }

                vertex = p;
            }
        }

        /** set parent(x,y) */
        private void union(int x, int y, int[] parent) {
            parent[find(parent, y)] = find(parent, x);
        }

    }


}