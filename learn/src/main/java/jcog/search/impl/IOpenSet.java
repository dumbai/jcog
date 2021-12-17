package jcog.search.impl;

public interface IOpenSet<F> {

    void add(F node);

    void remove(F node);

    F poll();

    
    F getNode(F node);

    int size();

}
