package jcog.search;


/**
 * Implements trivial functions for a search node.
 */
public abstract class Find<X> implements Solution {

    private double g = 0.0;
    public final X id;
    private Solution parent;

    public Find(X id) {
        this.id = id;
    }

    
    public Solution parent() {
        return this.parent;
    }

    
    public void setParent(Solution parent) {
        this.parent = parent;
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof Find otherNode) {
            return (this.id.equals(otherNode.id));
        }
        return false;
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public String toString() {
        return this.id.toString();
    }

    @Override
    public boolean goalOf(Solution other) {
        return equals(other);
    }

    
    @Override
    public double g() {
        return this.g;
    }

    
    @Override
    public void setG(double g) {
        this.g = g;
    }

}