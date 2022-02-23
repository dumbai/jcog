package jcog.pri.bag.tree;

import jcog.TODO;
import jcog.data.list.Lst;
import jcog.data.map.UnifriedMap;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

public class DefaultTreeBag<X, Y> extends TreeBag<X, Y> {

    private int cap = 0;
//        private int size = 0;

    static final class TBNode<X, Y> {
        private final Map<X, TBNode<X, Y>> branch = new UnifriedMap<>(0);
        private final Map<Y,Y> leaves = new UnifriedMap<>(0);

        Stream<Y> streamRecursive() {
            return Stream.concat(
                leaves.keySet().stream(),
                branch.values().stream().flatMap(TBNode::streamRecursive)
            );
        }

        public void clear() {
            branch.clear();
            leaves.clear();
        }

        public boolean remove(Y item) {
            return leaves.remove(item)!=null;
        }

        public boolean put(Y item, TreeBag<?,Y> t) {
            Y exist = leaves.putIfAbsent(item, item);
            if (exist!=null) {
                t.merge(exist, item);
            }
            return true;
        }

        @Nullable
        public TBNode<X, Y> find(X x, boolean b) {
            return !b ?
                    branch.get(x) :
                    branch.computeIfAbsent(x, (X) -> new TBNode<>());
        }

        public boolean removeBranch(X x) {
            return branch.remove(x) != null;
        }

        public boolean isEmpty() {
            return branch.isEmpty() && leaves.isEmpty();
        }

        public void forEach(Lst<X> l, BiConsumer<List<X>, Y> each) {
            leaves.keySet().forEach(y -> each.accept(l, y));

            l.ensureCapacityForAdditional(1, true);
            branch.forEach((x, b)->{
              l.addFast(x);
              b.forEach(l, each);
              l.poll();
            });
        }

        public void removeIf(@Nullable Predicate<Y> each) {
            leaves.values().removeIf(each);
            branch.values().forEach(b -> b.removeIf(each));
        }
    }

    final TBNode<X, Y> root = new TBNode<>();

    @Override public String toString() {
        StringBuilder s = new StringBuilder();
        print(s);
        return s.toString();
    }

    public void print(Appendable p) {
        forEach((x, y)->{
            try {
                p.append(x.toString());
                p.append('=');
                p.append(y.toString());
                p.append('\n');
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public boolean isEmpty() {
        return root.isEmpty();
    }

    @Override
    public int size() {
        //return size;
        return (int) stream().count();
    }

    @Override
    public void capacity(int cap) {
        this.cap = cap;
    }

    @Override
    public Iterator<Y> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<Y> stream() {
        return root.streamRecursive();
    }

    @Override
    public void sampleOrPop(RandomGenerator p, boolean sampleOrPop, BiPredicate<List<X>, Y> each) {
        throw new TODO();
    }

    @Override public void forEach(BiConsumer<List<X>, Y> each) {
        Lst<X> l = new Lst(1);
        root.forEach(l, each);
    }
    @Override
    public void removeIf(@Nullable Predicate<Y> each) {
        root.removeIf(each);
    }

    @Override
    public void clear(@Nullable Consumer<Y> each) {
        if (each == null) {
            root.clear();
            return;
        }

        List<Y> b = stream().toList(); //HACK
        root.clear();
        b.forEach(each); //HACK
//            size = 0;
    }

    @Override
    public boolean remove(List<X> pp, Y item) {
        TBNode<X, Y> t = find(pp, false);
        if (t != null && t.remove(item)) {
            TBNode<X, Y> p = t;
            int n = pp.size();
            while (n-- > 0 && p.isEmpty()) {
                (p = find(pp.subList(0, n), false)).removeBranch(pp.get(n));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean put(List<X> path, Y item) {
        return find(path, true).put(item, this);
    }

    @Nullable
    private TBNode<X, Y> find(List<X> path, boolean createIfMissing) {
        TBNode<X, Y> t = root;
        for (X x : path)
            t = t.find(x, createIfMissing);
        return t;
    }

}