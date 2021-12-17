package jcog.grammar.evolve.tree;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by me on 11/29/15.
 */
public abstract class ParentNode extends AbstractNode {

    protected final List<Node> children;
    private ParentNode parent;

    public abstract int getMinChildrenCount();
    public abstract int getMaxChildrenCount();

    public ParentNode(List<Node> ch) {
        children = ch;
    }

    public final void forEach(Consumer<Node> eachChild) {
        for (Node child : children) {
            eachChild.accept(child);
        }
    }

    public final void add(Node... n) {
        if (n.length > 0) {
            Collections.addAll(children, n);
            unhash();
        }
    }

    public final int size() { return children.size(); }

    public final boolean isEmpty() {
        return children.isEmpty();
    }



    public final Node get(int i) {
        return children.get(i);
    }


    public final void add(Node n) {
        children.add(n);
        unhash();
    }

    public void set(int i, Node v) {
        children.set(i, v);
        unhash();
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash == 0)
            return this.hash = rehash();
        return hash;
    }

    public void unhash() {
        hash = 0;
    }

    private int rehash() {
        int h = children.hashCode();
        h = 31 * h + getClass().hashCode();
        if (h == 0) h = 1;
        return h;
    }

    public void addAll(Collection<Node> i) {
        children.addAll(i);
        unhash();
    }

    @Override
    public final List<Node> children() {
        
        return /*Collections.unmodifiableList*/(children);
    }

    @Override
    public final ParentNode getParent() {
        return parent;
    }

    @Override
    public final void setParent(ParentNode parent) {
        this.parent = parent;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;
        ParentNode an = (ParentNode) o;
        if (hashCode() != an.hashCode()) return false;
        return children.equals(an.children);
    }

    protected static void cloneChild(Node child, ParentNode parent) {
        Node newChild = child.cloneTree();
        newChild.setParent(parent);
        parent.add(newChild);
    }


}