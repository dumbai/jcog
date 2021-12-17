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

import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

/**
 * This class is an adaptor making any Graph an undirected graph
 * by making its edges bidirectional. The graph to be made undirected
 * is passed to the constructor. Only the reference is stored so
 * if the directed graph changes later, the undirected version will
 * follow that change. However, {@link #neighborsOut} has O(n) time complexity
 * (in other words, too slow for large graphs).
 *
 * @see ConstUndirGraph
 */
public class UndirectedGraph extends IntIndexedGraph {






    private final IntIndexedGraph g;






    public UndirectedGraph(IntIndexedGraph g) {

        this.g = g;
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

        IntHashSet result = new IntHashSet(g.neighborsOut(r));
        int max = g.size();
        for (int j = 0; j < max; ++j) {
            if (r !=j && g.isEdge(j, r))
                result.add(j);
        }

        return result;
    }



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
        return neighborsOut(i).size();
    }


/*
public static void main( String[] args ) {

	
	Graph net = null;	
	UndirectedGraph ug = new UndirectedGraph(net);
	for(int i=0; i<net.size(); ++i)
		System.err.println(i+" "+net.getNeighbours(i));
	System.err.println("============");
	for(int i=0; i<ug.size(); ++i)
		System.err.println(i+" "+ug.getNeighbours(i));
	for(int i=0; i<ug.size(); ++i)
	{
		for(int j=0; j<ug.size(); ++j)
			System.err.print(ug.isEdge(i,j)?"W ":"- ");
		System.err.println();
	}

	GraphIO.writeGML(net,System.out);
}
*/
}


