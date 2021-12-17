package jcog.cluster.impl;

import org.eclipse.collections.api.block.predicate.primitive.IntPredicate;
import org.eclipse.collections.api.block.procedure.primitive.ShortIntProcedure;
import org.eclipse.collections.api.block.procedure.primitive.ShortProcedure;

/**
 * Created by me on 5/25/16.
 */
public class SemiDenseShortUndirectedGraph implements ShortUndirectedGraph {


    protected final int V; 
    protected final MyShortIntHashMap[] adj;  


    
    public SemiDenseShortUndirectedGraph(short V) {
        this.V = V;
        
        adj = new MyShortIntHashMap[V];

        for (int i = 0; i < V; i++) {
            adj[i] = new MyShortIntHashMap(0);
        }

    }

    @Override
    public void compact() {
        for (MyShortIntHashMap a : adj) {
            if (a.capacity() > 8 && a.density() <= 0.5f)
                a.compact();
        }
    }

    @Override
    public void clear() {
        for (MyShortIntHashMap a : adj)
            a.clear();
    }

    
    public int V() {
        return V;
    }






    
    @Override
    public void setEdge(short first, short second, int value) {
        MyShortIntHashMap[] e = this.adj;
        e[first].put(second, value);
        e[second].put(first, value);
    }

    public int getEdge(short first, short second) {
        return adj[first].get(second);
    }

    @Override
    public void addToEdges(short i, int d) {
        adj[i].addToValues(d); 
    }

    public void addToEdge(short first, short second, int deltaValue) {
        MyShortIntHashMap[] e = this.adj;
        e[first].addToValue(second, deltaValue);
        e[second].addToValue(first, deltaValue);
    }

    @Override
    public void removeVertex(short v) {
        MyShortIntHashMap[] e = this.adj;
        for (int i = 0, eLength = e.length; i < eLength; i++) {
            MyShortIntHashMap ii = e[i];
            if (i == v) ii.clear();
            else ii.remove(v);
        }
    }

    @Override
    public void removeEdge(short first, short second) {
        MyShortIntHashMap[] e = this.adj;
        e[first].remove(second);
        e[second].remove(first);
    }


    @Override
    public void removeEdgeIf(IntPredicate filter) {
        MyShortIntHashMap[] e = this.adj;
        for (MyShortIntHashMap h : e) {
            h.filter(filter);
        }
    }

    @Override
    public void edgesOf(short vertex, ShortIntProcedure eachKeyValue) {
        adj[vertex].forEachKeyValue(eachKeyValue);
    }
    @Override
    public void edgesOf(short vertex, ShortProcedure eachKey) {
        adj[vertex].forEachKey(eachKey);
    }















    
    public int degree(int v) {
        return adj[v].size();
    }



















}
