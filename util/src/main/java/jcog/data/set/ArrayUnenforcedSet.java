package jcog.data.set;

import com.google.common.collect.Iterables;
import jcog.TODO;
import jcog.data.iterator.ArrayIterator;
import jcog.util.ArrayUtil;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Helper for efficiently representing small sets whose elements are known to be unique by
 * construction, implying we don't need to enforce the uniqueness property in the data structure
 * itself. Use with caution.
 *
 * <p>
 * Note that for equals/hashCode, the class implements the Set behavior (unordered), not the list
 * behavior (ordered); the fact that it subclasses ArrayList should be considered an implementation
 * detail.
 *
 * @param <X> the element type
 * @author John V. Sichi
 */
public class ArrayUnenforcedSet<X> extends AbstractSet<X> {

    public X[] items;

    /**
     * Constructs a new empty setAt
     */
    public ArrayUnenforcedSet() {
        super();
        items = (X[]) ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    public ArrayUnenforcedSet(X[] x) {
        this.items = x;
    }

    public static <X> X[] toArrayShared(Set<X> mm) {
        if (mm instanceof ArrayUnenforcedSet)
            return ((ArrayUnenforcedSet<X>)mm).items;
        else {
            throw new TODO();
            //return mm.toArray(null); //HACK TODO
        }
    }

    @Override
    public Stream<X> stream() {
        return ArrayIterator.stream(items);
    }

    @Override
    public Iterator<X> iterator() {
        return ArrayIterator.iterate(items);
    }


    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length >= items.length) {
            System.arraycopy(items, 0, a, 0, items.length);
            return a;
        } else
            return (T[]) items.clone();
    }

    @Override
    public int size() {
        return items.length;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) !=-1;
    }

    @Override
    public boolean add(X x) {
        if (!contains(x)) {
            items = ArrayUtil.add(items, x);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        if (index == -1) return false;
        items = ArrayUtil.remove(items, index);
        return true;
    }

    private int indexOf(Object o) {
        return ArrayUtil.indexOf(items, o);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof ArrayUnenforcedSet) {
            return Arrays.equals(items, ((ArrayUnenforcedSet)o).items);
        }
        if (o instanceof Iterable) {
            return Iterables.elementsEqual(this, (Iterable) o);
        }
        return false;// new SetForEquality().equals(o);
    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(items);

        //throw new TODO("which is right?");

//        int h = 0;
//        Iterator<X> i = this.iterator();
//
//        while(i.hasNext()) {
//            X obj = i.next();
//            if (obj != null) {
//                h += obj.hashCode();
//            }
//        }
//
//        return h;
//
//        //Obeying (Abstract)Set<> semantics:
//        int s = ;
//        X[] ii = this.items;
//
//        int sum = 0;
//        for for (int i = 0; i < s; i++)
//            sum += ii[i].hashCode();
//        return sum;

    }

    public final X first() {
        return items[0];
    }
    public final X last() {
        return items[items.length-1];
    }

//    @Override
//    public boolean addAll(Collection<? extends X> source) {
//        boolean acc = false;
//        for (X x : source) {
//            boolean add = add(x);
//            acc = acc || add;
//        }
//        return acc;
//    }

//    /**
//     * Multiple inheritance helper.
//     */
//    private class SetForEquality extends AbstractSet<X> {
//        @Override
//        public Iterator<X> iterator() {
//            return ArrayUnenforcedSet.this.iterator();
//        }
//
//        @Override
//        public int size() {
//            return ArrayUnenforcedSet.this.size();
//        }
//    }

}