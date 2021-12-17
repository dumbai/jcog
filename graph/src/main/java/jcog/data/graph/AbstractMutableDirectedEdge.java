package jcog.data.graph;

import jcog.data.graph.path.FromTo;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AbstractMutableDirectedEdge<N, E> implements FromTo<Node<N, E>, E> {
    protected Node<N, E> from;
    protected Node<N, E> to;
    protected E id;

    protected AbstractMutableDirectedEdge() {

    }

    protected AbstractMutableDirectedEdge(Node<N, E> from, Node<N, E> to, @Nullable E id) {
        set(from, to, id);
    }

    public void from(Node<N, E> from) {
        if (!this.from.equals(from)) {
            this.from = from;
            rehash();
        }
    }

    public void to(Node<N, E> to) {
        if (!this.to.equals(to)) {
            this.to = to;
            rehash();
        }
    }

    public void id(E id) {
        if (!Objects.equals(this.id, id)) {
            this.id = id;
            rehash();
        }
    }

    protected abstract void rehash();

    @Override
    public final Node<N, E> from() {
        return from;
    }

    @Override
    public final E id() {
        return id;
    }

    @Override
    public final Node<N, E> to() {
        return to;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FromTo ee) || (!hashDynamic() && (hashCode() != obj.hashCode())))
            return false;
        return from.equals(ee.from()) && to.equals(ee.to()) && Objects.equals(id, ee.id());
    }

    /** whether hash is computed dynamically (true) or is cached (false) */
    protected abstract boolean hashDynamic();

    @Override
    public abstract int hashCode();

    @Override
    public String toString() {
        return from + " => " + id + " => " + to;
    }

    public void set(Node<N, E> from, Node<N, E> to, E id) {
        this.from = from;
        this.to = to;
        this.id = id;
        rehash();
    }
}