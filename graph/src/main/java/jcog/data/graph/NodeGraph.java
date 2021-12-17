package jcog.data.graph;

import com.google.common.collect.Iterables;
import jcog.data.graph.path.FromTo;
import jcog.data.graph.search.Search;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.api.tuple.primitive.BooleanObjectPair;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static jcog.data.graph.search.Search.newQueue;

public abstract class NodeGraph<N, E> /* TODO merge with guava Graph: implements ValueGraph<N,E> */ {

    public abstract Node<N, E> node(Object key);

    public abstract Iterable<? extends Node<N,E>> nodes();

    public final Iterable<N> nodeIDs() {
        return Iterables.transform(nodes(), Node::id);
    }

    abstract int nodeCount();

    public abstract void forEachNode(Consumer<Node<N, E>> n);

    protected abstract Node<N, E> newNode(N data);

    /**
     * can override in mutable subclass implementations
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * gets existing node, or creates and adds a node if missing
     * can override in mutable subclass implementations
     */
    public Node<N, E> addNode(N key) {
        throw new UnsupportedOperationException();
    }

    public final boolean dfs(Object root, Search<N, E> search) {
        return dfs(List.of(root), search);
    }


    public void rootsEach(Consumer<Node<N,E>> each) {
        nodes().forEach(n -> {
            if (n.edgeCount(true, false) == 0)
                each.accept(n);
        });

    }
    public void bfsEach(Consumer<Node<N,E>> each) {
        rootsEach(n -> bfsEach(n, each));
    }

    /** iterate all nodes, in topologically sorted order */
    public void bfsEach(Node<N,E> root, Consumer<Node<N,E>> each) {
        each.accept(root);
        bfs(root, new Search<>() {
            @Override
            protected boolean go(List<BooleanObjectPair<FromTo<Node<N, E>, E>>> path, Node<N, E> next) {
                each.accept(next);
                return true;
            }
        });
    }

    private boolean dfs(Iterable<?> roots, Search<N, E> search) {
        return search.dfs(roots, this);
    }


    public final boolean bfs(Object root, Search<N, E> search) {
        int c = nodeCount();
        return switch (c) {
            case 0, 1 -> true;
            case 2 -> dfs(root, search);
            default -> bfs(root, null, search);
        };
    }

    public boolean bfs(Iterable<?> roots, Search<N, E> search) {
        int c = nodeCount();
        return switch (c) {
            case 0, 1 -> true;
            case 2 -> dfs(roots, search);
            default -> bfs(roots, newQueue(c), search);
        };
    }

    public boolean bfs(Iterable<?> roots, Queue<Pair<List<BooleanObjectPair<FromTo<Node<N, E>, E>>>, Node<N, E>>> q, Search<N, E> search) {
        return search.bfs(roots, q, this);
    }
    public boolean bfs(Object root, Queue<Pair<List<BooleanObjectPair<FromTo<Node<N, E>, E>>>, @Nullable Node<N, E>>> q, Search<N, E> search) {
        return search.bfs(root, q, this);
    }

    public void print() {
        print(System.out);
    }

    private void print(PrintStream out) {
        forEachNode(node -> node.print(out));
    }

    public abstract static class AbstractNode<N, E> implements Node<N, E> {
        private static final AtomicInteger serials = new AtomicInteger(1);
        public final N id;
        public final int serial;
        final int hash;


        protected AbstractNode(N id) {
            this.serial = serials.getAndIncrement();
            this.id = id;
            this.hash = id.hashCode();
        }

        @Override
        public final N id() {
            return id;
        }

        //        public Stream<N> successors() {
//            return streamOut().map(e -> e.to().id);
//        }
//
//        public Stream<N> predecessors() {
//            return streamIn().map(e -> e.from().id);
//        }

        @Override
        public final boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public final int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return id.toString();
        }


    }

}