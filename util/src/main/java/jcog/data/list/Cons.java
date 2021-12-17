package jcog.data.list;

import jcog.TODO;
import jcog.data.iterator.ArrayIterator;
import jcog.data.iterator.Concaterator;

import java.util.*;
import java.util.function.Consumer;

/** immutable cons list useful for growing paths */
public class Cons<T> extends AbstractList<T> {

    private final List<T> head;
    public final T tail;

    public static <T> List<T> the(List<T> f, T r) {
		//new FasterList(1).with(r);
		return f.isEmpty() ? List.of(r) : new Cons(f, r);
    }

    private Cons(List<T> f, T r) {
        head = f;
        tail = r;
    }

    @Override
    public int hashCode() {
        throw new TODO();
    }

    @Override
    public boolean equals(Object o) {
        throw new TODO();
    }

    @Override
    public boolean add(T t) {
        throw new TODO();
    }

    @Override
    public boolean remove(Object o) {
        throw new TODO();
    }

    @Override
    public T get(int index) {
		return index < head.size() ? head.get(index) : tail;
//        else if (index == head.size())
//            return tail;
//        else
//            throw new TODO();
    }

    public boolean isEmpty() { return false; }

    @Override
    public int size() {
        return head.size()+1;
    }

    @Override
    public Iterator<T> iterator() {
        if (head instanceof RandomAccess && head.size()==1)
            return ArrayIterator.iterate(head.get(0), tail); //tail-call-ish optimization
        else
            return Concaterator.concat(head, tail); //recursive cons
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new TODO();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        head.forEach(action);
        action.accept(tail);
    }


}



