package jcog.pri.bag.tree;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

/** a node and possible root of n-ary tree of prioritized items.
 *  each item is referenced by a "path" (sequence of X)
 *  TODO
 *
 *  ex:
 *      (task)                   ->   task premise
 *      (task, beliefTerm)       ->   single premises
 *      (task, beliefTerm, rule) ->   single premise rule
 *      (task, beliefTask)       ->   double premises
 *      (task, beliefTask, rule) ->   double premise rule
 * */
public abstract class TreeBag<X,Y> {

    public abstract int size();

    /** shared between branches and items. so it can either have many branches OR many items (leaf) but not both */
    public abstract void capacity(int cap);

    /** all items */
    public abstract Iterator<Y> iterator();

//    /** resolves a sub-branch, optionally creating it if missing */
//    @Nullable abstract public TreeBag<X,Y> find(X next, boolean createIfMissing);

    /** sample an item; deciding whether it is from the local or which recursive branch. do not retain the list, as it mutates, without cloning */
    public abstract void sampleOrPop(RandomGenerator p, boolean sampleOrPop, BiPredicate<List<X>, Y> each);

    /** do not retain the list, as it mutates, without cloning */
    public abstract void forEach(BiConsumer<List<X>, Y> each);

    /** local and children; node will be empty afterward */
    public abstract void clear(@Nullable Consumer<Y> each);

    public abstract void removeIf(@Nullable Predicate<Y> each);

    public final void clear() { clear(null); }

    public abstract boolean remove(List<X> path, Y item);

    public abstract boolean put(List<X> path, Y item);

    public abstract Stream<Y> stream();

    public void merge(Y exist, Y item) {

    }

    public boolean isEmpty() {
        return size()==0;
    }

    /** path to this node
     *  (may not be necessary) */
    //abstract public List<X> path();

}