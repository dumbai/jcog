package jcog.data.graph.search;

import jcog.data.graph.Node;
import jcog.data.graph.NodeGraph;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.roaringbitmap.RoaringBitmap;

/** search log history for detecting cycles, reachability, etc */
public interface TraveLog {
    void clear();

    /** returns false if it was already added */
    boolean visit(int n);

    void unvisit(int n);

    boolean hasVisited(int n);

    static int id(Node n) {
        return ((NodeGraph.AbstractNode)n).serial;
    }

    int size();

    final class RoaringHashTraveLog extends RoaringBitmap implements TraveLog {

        @Override
        public int size() {
            return getCardinality();
        }

        @Override
        public boolean visit(int n) {
            return checkedAdd(n);
        }

        @Override
        public void unvisit(int n) {
            remove(n);
        }

        @Override
        public boolean hasVisited(int n) {
            return contains(n);
        }
    }

    final class IntHashTraveLog extends IntHashSet implements TraveLog {

        public IntHashTraveLog(int cap) {
            super(cap);
        }

        @Override
        public boolean visit(int n) {
            return add(n);
        }

        @Override
        public void unvisit(int n) {
            remove(n);
        }

        @Override
        public boolean hasVisited(int n) {
            return contains(n);
        }

    }

    /** TODO a bitvector based travelog, with a failsafe max limit  */

}
