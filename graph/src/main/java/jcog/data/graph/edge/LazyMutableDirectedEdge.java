package jcog.data.graph.edge;

import jcog.data.graph.AbstractMutableDirectedEdge;
import jcog.data.graph.Node;
import jcog.data.graph.path.FromTo;
import org.jetbrains.annotations.Nullable;

public class LazyMutableDirectedEdge<N,E> extends AbstractMutableDirectedEdge<N, E> {

    public LazyMutableDirectedEdge() {
        super();
    }
    public LazyMutableDirectedEdge(Node<N, E> from, @Nullable E id, Node<N, E> to) {
        super(from, to, id);
    }

    @Override
    protected void rehash() {
    }

    @Override
    protected boolean hashDynamic() {
        return true;
    }

    @Override
    public int hashCode() {
        return FromTo.hash(from, id, to);
    }
}
