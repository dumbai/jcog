package jcog.data.set;

import jcog.TODO;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.stream.Stream;

/** use with caution */
public abstract class ArrayUnenforcedSortedSet<X> extends ArrayUnenforcedSet<X> implements SortedSet<X> {

    public static final SortedSet empty = new ArrayUnenforcedSortedSet<>() {

        @Override
        public Stream<Object> stream() {
            return Stream.empty();
        }

//        @Override
//        public Object first() {
//            throw new UnsupportedOperationException();
//        }
//
//        @Override
//        public Object last() {
//            throw new UnsupportedOperationException();
//        }
    };

    @SafeVarargs
    private ArrayUnenforcedSortedSet(X... xx) {
        super(xx);
    }

    /** assumes u is already sorted and deduplicated */
    public static <X> SortedSet<X> the(X[] u) {
        return switch (u.length) {
            case 0 -> empty;
            case 1 -> the(u[0]);
            case 2 -> new Two(u[0], u[1]);
            default -> new ArrayArrayUnenforcedSortedSet<>(u);
        };
    }

    @Override
    public boolean add(X x) {
        throw new TODO();
    }


    public static <X> SortedSet<X> the(X x) {
        return new One(x);
    }

    public static <X extends Comparable> SortedSet<X> the(X x, X y) {
        int c = x.compareTo(y);
        return switch (c) {
            case 0 -> new One(x);
            case 1 -> new Two(y, x);
            default -> new Two(x, y);
        };
    }

    @Override
    public @Nullable Comparator<? super X> comparator() {
        return null;
    }

    @Override
    public SortedSet<X> subSet(X x, X e1) {
        throw new TODO();
    }

    @Override
    public SortedSet<X> headSet(X x) {
        throw new TODO();
    }

    @Override
    public SortedSet<X> tailSet(X x) {
        throw new TODO();
    }

    private static class One<X> extends ArrayUnenforcedSortedSet<X> {

        private One(X x) {
            super(x);
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Stream<X> stream() {
            return Stream.of(this.first());
        }

    }

    private static class Two<X> extends ArrayUnenforcedSortedSet<X> {

        private Two(X x, X y) {
            super(x, y);
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public Stream<X> stream() {
            return Stream.of(first(), last());
        }

    }


    @Deprecated private static final class ArrayArrayUnenforcedSortedSet<X> extends ArrayUnenforcedSortedSet<X> {
//        private final X[] u;

        ArrayArrayUnenforcedSortedSet(X[] u) {
            super(u);
//            this.u = u;
        }

//        @Override
//        public X first() {
//            return u[0];
//        }
//
//        @Override
//        public X last() {
//            return u[u.length-1];
//        }
    }
}