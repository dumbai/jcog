package jcog.data.graph.path;

import jcog.Util;
import jcog.data.graph.Node;
import org.jetbrains.annotations.Nullable;

/**
 * F represents the node type
 * X represents the edge type
 */
public interface FromTo<N, E> /* extends Triple<F,X,F> */ {

    //FromTo[] EmptyFromToArray = new FromTo[0];

    static <N, E> int hash(Node<N, E> from, E id, Node<N, E> to) {
        return Util.hashCombine((id!=null ? id.hashCode() : 0), from.hashCode(), to.hashCode());
    }

    N from();
    E id();
    N to();

    default N to(boolean outOrIn) {
        return outOrIn ? to() : from();
    }

    default N from(boolean outOrIn) {
        return outOrIn ? from() : to();
    }


    /** other by equality */
    default @Nullable N otherEquality(N x) {
        N f = from(), t = to();
        //TODO maybe this needs to be .equals()
        if (f.equals(x)) return t;
        else if (t.equals(x)) return f;
        else
            return null;
    }

    /** other by identity */
    default @Nullable N other(N x) {
        N f = from(), t = to();
        //TODO maybe this needs to be .equals()
        if (f == x) return t;
        else if (t == x) return f;
        else
            throw new NullPointerException();
    }

    default boolean loop() {
        return from().equals(to());
    }

    default FromTo<N,E> reverseIf(boolean b) {
        return b ? reverse() : this;
    }
    default FromTo<N,E> reverse() {
        throw new UnsupportedOperationException();
    }

}
