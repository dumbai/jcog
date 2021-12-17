package jcog.data.graph;

import jcog.data.graph.path.FromTo;
import org.eclipse.collections.api.tuple.primitive.BooleanObjectPair;

import java.util.List;
import java.util.function.Consumer;

public abstract class PathVisitor<N,E> implements Consumer<BooleanObjectPair<FromTo<Node<N, E>, E>>> {

    protected PathVisitor(List<BooleanObjectPair<FromTo<Node<N, E>, E>>> path) {
        path.forEach(this);
    }

    @Override
    public void accept(BooleanObjectPair<FromTo<Node<N, E>, E>> span) {
        boolean fwd = span.getOne();
        FromTo<Node<N, E>, E> e = span.getTwo();
        E eid = e.id();
        Node<N, E> f = e.from(), t = e.to();
        if (fwd) acceptEdge(eid, f, t);
        else     acceptEdge(eid, t, f);
    }

    protected abstract void acceptEdge(E id, Node<N, E> from, Node<N, E> to);

}
