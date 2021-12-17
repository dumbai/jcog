package jcog.tree.atomic;


import jcog.data.map.ConcurrentFastIteratingHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public abstract class AtomicTreeNode<T extends AtomicTreeNode<T>> {

    public final ConcurrentFastIteratingHashSet<AtomicTreeNode<T>> children = new ConcurrentFastIteratingHashSet<>(EmptyAtomicTreeNodeArray);

    private static final AtomicTreeNode[] EmptyAtomicTreeNodeArray = new AtomicTreeNode[0];

    /** TODO use VarHandle */
    private final AtomicReference<T> parent = new AtomicReference(null);

    private static final AtomicInteger serial = new AtomicInteger();
    final int hash = serial.getAndIncrement();;

    protected AtomicTreeNode() {

    }

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public final boolean equals(Object obj) {
        return this == obj;
    }

    public void parent(@Nullable T parent) {
        if (parent == null) {
            unparent();
            return;
        }

        assert(this!=parent);
        if (!this.parent.compareAndSet(null, parent)) {
            T curParent = parent();
            if (curParent == parent)
                return; //same parent
            
            throw new UnsupportedOperationException(this + " parent already setAt: " + this.parent.get());
        }
        parent.children.add(this);
    }

    public T reparent(T newParent) {
        assert(this!=newParent);
        T prevParent = parent.getAndSet(newParent);
        if (prevParent!=newParent) {
            if (prevParent!=null)
                prevParent.children.remove(this);
            if (newParent!=null)
                newParent.children.add(this);
        }
        return prevParent;
    }

    public void unparent() {
        T prevParent;
        if ((prevParent = this.parent.getAndSet(null))==null)
            throw new UnsupportedOperationException(this + " wasnt parented");
        prevParent.children.remove(this);
    }

    public T parent() {
        return parent.getOpaque();
    }

    public boolean contains(T child) {
        return children.contains(child);
    }

    public int size() { return children.size(); }

    public Stream<T> childrenStream() {
        return children.stream().map(x->(T)x);
    }

    public Stream<T> childrenStreamRecurse() {
        return Stream.concat(Stream.of(this).map(x->(T)x), childrenStream().flatMap(AtomicTreeNode::childrenStreamRecurse));
    }

//    public static class AtomicTreeBranch<T> extends AtomicTreeNode<T> {
//
//
//    }
}
