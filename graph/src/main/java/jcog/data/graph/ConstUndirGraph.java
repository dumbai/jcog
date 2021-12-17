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

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

/**
 * This class is an adaptor making any Graph an undirected graph
 * by making its edges bidirectional. The graph to be made undirected
 * is passed to the constructor. Only the reference is stored.
 * However, at construction time the incoming edges are stored
 * for each node, so if the graph
 * passed to the constructor changes over time then
 * methods {@link #neighborsOut(int)} and {@link #degreeOut(int)}
 * become inconsistent (but only those).
 * The upside of this inconvenience is that {@link #neighborsOut} will have
 * constant time complexity.
 *
 * @see UndirectedGraph
 */
public class ConstUndirGraph extends IntIndexedGraph {






    final IntIndexedGraph g;

    final IntArrayList[] in;




    /**
     * Initialization based on given graph. Stores the graph and if necessary
     * (if the graph is directed) searches for the incoming edges and stores
     * them too. The given graph is stored by reference (not cloned) so it should
     * not be modified while this object is in use.
     */
    ConstUndirGraph(IntIndexedGraph g) {

        this.g = g;
        in = g.directed() ? new IntArrayList[g.size()] : null;

        initGraph();
    }



    /**
     * Finds and stores incoming edges
     */
    void initGraph() {

        int max = g.size();
        for (int i = 0; i < max; ++i) in[i] = new IntArrayList();
        for (int i = 0; i < max; ++i) {
            int ii = i;
            g.neighborsOut(i).forEach(j -> {
                if (!g.isEdge(j, ii)) in[j].add(ii);
            });
        }
    }






    @Override
    public boolean isEdge(int r, int c) {

        return g.isEdge(r, c) || g.isEdge(c, r);
    }



    /**
     * Uses sets as collection so does not support multiple edges now, even if
     * the underlying directed graph does.
     */
    @Override
    public IntHashSet neighborsOut(int r) {

        IntHashSet result = new IntHashSet();
        result.addAll(g.neighborsOut(r));
        if (in != null) result.addAll(in[r]);
        return result;
    }



    /**
     * Returns the node from the underlying graph
     */
    @Override
    public Object vertex(int v) {
        return g.vertex(v);
    }



    /**
     * If there is an (i,j) edge, returns that, otherwise if there is a (j,i)
     * edge, returns that, otherwise returns null.
     */
    @Override
    public Object edge(int i, int j) {

        if (g.isEdge(i, j)) return g.edge(i, j);
        if (g.isEdge(j, i)) return g.edge(j, i);
        return null;
    }



    @Override
    public int size() {
        return g.size();
    }



    @Override
    public boolean directed() {
        return false;
    }

    /**
     * not supported
     */
    @Override
    public boolean removeEdge(int i, int j) {
        throw new UnsupportedOperationException();
    }



    @Override
    public int degreeOut(int i) {
        return g.degreeOut(i) + (in == null ? 0 : in[i].size());
    }


/*
public static void main( String[] args ) {

	Graph net = new BitMatrixGraph(20);
	GraphFactory.wireKOut(net,5,new Random());
	ConstUndirGraph ug = new ConstUndirGraph(net);
	for(int i=0; i<net.size(); ++i)
		System.err.println(
			i+" "+net.getNeighbours(i)+" "+net.degree(i));
	System.err.println("============");
	for(int i=0; i<ug.size(); ++i)
		System.err.println(i+" "+ug.getNeighbours(i)+" "+ug.degree(i));
	System.err.println("============");
	for(int i=0; i<ug.size(); ++i)
		System.err.println(i+" "+ug.in[i]);
	for(int i=0; i<ug.size(); ++i)
	{
		for(int j=0; j<ug.size(); ++j)
			System.err.print(ug.isEdge(i,j)?"W ":"+ ");
		System.err.println();
	}

	GraphIO.writeUCINET_DL(net,System.out);
	GraphIO.writeUCINET_DL(ug,System.out);
}
*/
}

