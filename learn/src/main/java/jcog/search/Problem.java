package jcog.search;

/** actually, instances of the type X are generally a component or step
 * of a complete solution and not an entire solution */
public interface Problem<X extends Solution> {

    
    double cost(X currentNode, X successorNode);

    Iterable<X> next(X current);

}
