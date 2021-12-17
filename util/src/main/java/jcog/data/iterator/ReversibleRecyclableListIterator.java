package jcog.data.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** recyclable reversable array iterator
 *
 * modified from: http:
 * */
public class ReversibleRecyclableListIterator<T> implements Iterator<T> {
    private int count;    
    private int current;  
    private List<T> a;
    private boolean reverse;

    public ReversibleRecyclableListIterator(List<T> array, boolean reverse) {
        init(array, array.size(), reverse);
    }

    public ReversibleRecyclableListIterator(List<T> array, int size, boolean reverse) {
        init(array, size, reverse);
    }

    public void init(List<T> array, int size, boolean reverse) {
        a = array;
        count = size;
        current = 0;
        this.reverse = reverse;
    }

    @Override
    public boolean hasNext() {
        return (current < count);
    }

    @Override
    public T next() {
        if (!hasNext()) throw new NoSuchElementException();

        int i = (reverse) ? ((count-1) - current) : current;
        current++;

        return a.get(i);
    }

    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
