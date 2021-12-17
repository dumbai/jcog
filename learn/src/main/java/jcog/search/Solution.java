package jcog.search;

/**
 * Interface of a search node.
 */
public interface Solution {

    
    double g();

    
    void setG(double g);

    
    Solution parent();

    
    void setParent(Solution parent);

    boolean equals(Object other);

    int hashCode();

    boolean goalOf(Solution other);

}

