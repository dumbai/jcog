package jcog.data.graph;

import com.google.common.collect.Iterables;
import jcog.data.graph.path.FromTo;
import jcog.data.set.ArrayHashSet;
import jcog.data.set.ArrayUnenforcedSortedSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import static jcog.Util.emptyIterable;
import static jcog.Util.emptyIterator;
import static jcog.data.iterator.Concaterator.concat;

public class MutableNode<N, E> extends NodeGraph.AbstractNode<N, E> {


    private Collection<FromTo<Node<N, E>, E>> in;
    private Collection<FromTo<Node<N, E>, E>> out;

    public MutableNode(N id) {
        this(id, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    public MutableNode(N id, Collection<FromTo<Node<N, E>, E>> in, Collection<FromTo<Node<N, E>, E>> out) {
        super(id);
        this.in = in;
        this.out = out;
    }

    @Override
    public int edgeCount(boolean in, boolean out) {
        return (in ? ins() : 0) + (out ? outs() : 0);
    }

    @Override
    public Iterable<FromTo<Node<N, E>, E>> edges(boolean IN, boolean OUT) {
        Collection<FromTo<Node<N, E>, E>> in = this.in;
        boolean ie = !IN || in.isEmpty();
        Collection<FromTo<Node<N, E>, E>> out = this.out;
        boolean oe = !OUT || out.isEmpty();
        if (ie && oe) return emptyIterable;
        else if (ie) return out;
        else if (oe) return in;
        else return Iterables.concat(out, in);
    }

    @Override
    public Iterator<FromTo<Node<N, E>, E>> edgeIterator(boolean IN, boolean OUT) {
        Collection<FromTo<Node<N, E>, E>> in = this.in;
        boolean ie = !IN || in.isEmpty();
        Collection<FromTo<Node<N, E>, E>> out = this.out;
        boolean oe = !OUT || out.isEmpty();
        if (ie && oe) return emptyIterator;
        else if (ie) return out.iterator();
        else if (oe) return in.iterator();
        else return concat(out, in);
    }

    final int ins() {
        return ins(true);
    }

    int ins(boolean countSelfLoops) {
        return countSelfLoops ? in.size() : (int) streamIn().filter(e -> e.from() != this).count();
    }

    int outs() {
        return out.size();
    }

    Collection<FromTo<Node<N, E>, E>> newEdgeCollection(int cap) {
        return new ArrayHashSet<>(cap);
    }

    @SafeVarargs
    final Collection<FromTo<Node<N, E>, E>> newEdgeCollection(FromTo<Node<N, E>, E>... ff) {
        Collection<FromTo<Node<N, E>, E>> c = newEdgeCollection(ff.length);
        Collections.addAll(c, ff);
        return c;
    }

    public boolean addIn(FromTo<Node<N, E>, E> e) {
        return addSet(e, true);

    }

    public boolean addOut(FromTo<Node<N, E>, E> e) {
        return addSet(e, false);
    }

    private boolean addSet(FromTo<Node<N, E>, E> e, boolean inOrOut) {
        boolean result;
        Collection<FromTo<Node<N, E>, E>> s = inOrOut ? in : out;
        if (s == Collections.EMPTY_LIST) {
            //out = newEdgeCollection();
            s = ArrayUnenforcedSortedSet.the(e);
            result = true;
        } else {
            if (s instanceof ArrayUnenforcedSortedSet) {
                FromTo<Node<N, E>, E> x = ((ArrayUnenforcedSortedSet<FromTo<Node<N, E>, E>>) s).first();
                if (result = !x.equals(e))
                    s = newEdgeCollection(x, e);
            } else {
                result = s.add(e);
            }
        }
        if (result) {
            if (inOrOut) in = s;
            else out = s;
        }
        return result;
    }

    public boolean removeIn(FromTo<Node<N, E>, E> e) {
        return removeSet(e, true);
    }

    public boolean removeOut(FromTo<Node<N, E>, E> e) {
        return removeSet(e, false);
    }

    public void removeIn(Node<N, E> src) {
        edges(true, false, e -> e.to() == src, null).forEach(e -> removeSet(e, true));
    }

    public void removeOut(Node<N, E> target) {
        edges(false, true, e -> e.to() == target, null).forEach(e -> removeSet(e, false));
    }

    private boolean removeSet(FromTo<Node<N, E>, E> e, boolean inOrOut) {
        Collection<FromTo<Node<N, E>, E>> s = inOrOut ? in : out;
        if (s == Collections.EMPTY_LIST)
            return false;

        boolean changed;
        if (s instanceof ArrayUnenforcedSortedSet) {
            if (changed = (((ArrayUnenforcedSortedSet) s).first().equals(e)))
                s = Collections.EMPTY_LIST;
        } else {
            changed = s.remove(e);
            if (changed) {
                s = switch (s.size()) {
                    case 0 -> throw new UnsupportedOperationException();
                    case 1 -> ArrayUnenforcedSortedSet.the(((ArrayHashSet<FromTo<Node<N, E>, E>>) s).first());
                    default -> s;
                };
            }
            //TODO downgrade
        }

        if (changed) {
            if (inOrOut) in = s;
            else out = s;
        }

        return changed;
    }

    @Override
    public Stream<FromTo<Node<N, E>, E>> streamIn() {
        return in.stream();
    }

    @Override
    public Stream<FromTo<Node<N, E>, E>> streamOut() {
        return out.stream();
    }

}
