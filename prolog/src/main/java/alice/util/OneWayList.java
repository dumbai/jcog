package alice.util;

import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.Iterator;

public record OneWayList<E>(E head, OneWayList<E> tail) {

    public OneWayList(E head) {
        this(head, null);
    }

    public OneWayList(E head, @Nullable OneWayList<E> tail) {
        this.head = head;
        this.tail = tail;
    }

    public static <T> OneWayList<T> get(Deque<T> d) {
        int s = d.size();
        switch (s) {
            case 0:
                return null;
            case 1:
                return new OneWayList<>(d.getFirst());
            case 2:
                return new OneWayList<>(d.getFirst(), new OneWayList<>(d.getLast()));
            default:
                OneWayList<T> o = null;
                Iterator<T> i = d.descendingIterator();
                while (i.hasNext()) {
                    o = new OneWayList<>(i.next(), o);
                }
                return o;
        }
    }


    public String toString() {
        E head = this.head;
        String elem = (head == null) ? "null" : head.toString();
        OneWayList<E> tail = this.tail;
        return '[' + (tail == null ? elem : tail.toString(elem)) + ']';
    }

    private String toString(String elems) {
        String elem = head == null ? "null" : head.toString();
        if (tail == null) return elems + ',' + elem;
        return elems + ',' + tail.toString(elem);
    }

}