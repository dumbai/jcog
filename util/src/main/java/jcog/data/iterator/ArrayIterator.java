package jcog.data.iterator;

import com.google.common.collect.Iterators;
import jcog.Util;
import jcog.util.SingletonIterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** TODO optionally skip nulls */
public class ArrayIterator<X> implements Iterator<X>, Iterable<X> {

    private final X[] array;
    int index;

    public ArrayIterator(X[] array) {
        this.array = array;
    }

    @Override
    public boolean hasNext() {
        return index < array.length;
    }

    @Override
    public X next() {
        //if (index < array.length)
            return array[index++];
        //throw new NoSuchElementException();
    }

    @Override
    public void forEachRemaining(Consumer<? super X> action) {
        X[] a = this.array;
        for (int i = index; i < a.length; i++)
            action.accept(a[i]);
    }
    
    @Override
    public void forEach(Consumer<? super X> action) {
        for (X x : array)
            action.accept(x);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<X> iterator() {
        if (index != 0)
            return clone(); //already started, so return a fresh copy
        return this;
    }

    public ArrayIterator<X> clone() {
        return new ArrayIterator(array);
    }


    public static <E> Iterator<E> iterate(E[] e) {
		return /*e == null ? Util.emptyIterator : */ArrayIterator.iterateN(e, e.length);
    }

    public static <E> Iterator<E> iterate(E x, E y) {
        return Iterators.forArray(x, y);
        //return /*e == null ? Util.emptyIterator : */ArrayIterator.iterateN(e, e.length);
    }

    public static <E> Iterable<E> iterable(E[] e) {
//        if (e == null)
//            return Util.emptyIterable;
//        else {
            return switch (e.length) {
//                case 1 -> List.of(e);
                default -> (Iterable) ArrayIterator.iterate(e);
            };
//        }
    }

    public static <E> Iterator<E> iterateN(E[] e, int from, int to) {
        return new PartialArrayIterator<>(e, from, to);
    }

    public static <E> Iterator<E> iterateN(E[] e, int size) {
        return switch (size) {
            case 0 -> Util.emptyIterator;
//            case 1 -> new SingletonIterator(e[0]);
            default -> size == e.length ?
                    new ArrayIterator<>(e) :
                    new PartialArrayIterator<>(e, 0, size);
        };
    }

    public static <E> Iterator<E> iterateNonNull(E[] e) {
        return iterateNonNullN(e, e.length);
    }

    public static <E> Iterator<E> iterateNonNullN(E[] e, int size) {
        switch (size) {
            case 0:
                return Util.emptyIterator;
            case 1:
                E ee = e[0];
                return ee!=null ? new SingletonIterator<>(ee) : Util.emptyIterator;
            default:
                return new ArrayIteratorNonNull<>(e, size);
        }
    }

    public static <X> Stream<X> stream(X[] list) {
        return stream(list, list.length);
    }


    public static <X> Stream<X> stream(X[] list, int size) {
        return switch (size) {
            case 0 -> Stream.empty();
//            case 1 -> Stream.of(list[0]);
//            case 2 -> Stream.of(list[0], list[1]);
            default -> arrayStream(list, size);
        };
    }

    private static <X> Stream<X> arrayStream(X[] list, int size) {
        return list.length > size ? Arrays.stream(list, 0, size) : Arrays.stream(list);
    }

    public static <X> Stream<X> streamNonNull(X[] list, int size) {
        return switch (size) {
            case 0 -> Stream.empty();
            //            case 1: { X x0 = list[0]; return x0==null ? Stream.empty() : Stream.of(x0); }
            //            case 2: { X x0 = list[0], x1 = list[1]; if (x0!=null && x1!=null) return Stream.of(x0, x1); else if (x0 == null && x1 == null) return Stream.empty(); else if (x1 == null) return Stream.of(x0); else return Stream.of(x1); }
            default -> arrayStream(list, size).filter(Objects::nonNull);
        };
    }

    public static class PartialArrayIterator<E> extends ArrayIterator<E> {

        private final int end;

        PartialArrayIterator(E[] array, int start, int end) {
            super(array);
            this.index = start;
            this.end = end;
        }

        @Override
        public boolean hasNext() {
            return index < end;
        }

//        public ArrayIterator<E> clone() {
//            return new PartialArrayIterator<>(array, end);
//        }
    }

//    public static class AtomicArrayIterator<X> extends PartialArrayIterator<X> {
//
//        public AtomicArrayIterator(X[] array, int size) {
//            super(array, size);
//        }
//
//        @Override
//        public X next() {
//            return (X)ITEM.getOpaque(array, index++);
//        }
//    }
}