package jcog.data.list;

import jcog.Util;
import jcog.random.RandomBits;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/* High-performance Circular (Ring) Buffer. Not thread safe, and sacrifices safety for speed in other ways. */
public class CircularArrayList<X> extends AbstractList<X> implements RandomAccess, Deque<X>, Serializable {

    private int n = -1; 
    public X[] array;
    private int head;
    private int tail;
    private int size;

    public CircularArrayList(Collection<X> c) {
        this(c.size());
        addAll(c);
    }

    public CircularArrayList() {
        this(1);
    }

    public CircularArrayList(int capacity) {
        clear(capacity);
    }
    public CircularArrayList(X[] array) {
        this.array = array;
        this.n = array.length;
    }
    public void clear(int resize) {
        resize = Math.max(1, resize);
        if (n!=resize) {
            n = resize;
            array = (X[]) new Object[resize];
        }
        clear();
    }

    @Override
    public void clear() {
        head = tail = size = 0;
    }

    @Override
    public Iterator<X> iterator() {
        int max = size;
        if (max == 0)
            return Util.emptyIterator;

        return new Iterator<>() {

            int pos;

            @Override
            public boolean hasNext() {
                return pos < max;
            }

            @Override
            public X next() {
                int p = pos;
                X e = get(p);
                pos++;
                return e;
            }
        };
    }

    @Override
    public Iterator<X> descendingIterator() {
        return new Iterator<>() {
            int pos = size - 1;

            @Override
            public boolean hasNext() {
                return pos >= 0;
            }

            @Override
            public X next() {
                return get(pos--);
            }
        };
    }

    @Override
    public void forEach(Consumer<? super X> action) {
        /** NOTE: uses the descending iterator's semantics */
        for (int i = size - 1; i >= 0; i--) {
            action.accept(get(i--));
        }
    }

    public int capacity() {
        return n - 1;
    }

    /*
     private int wrapIndex(final int i) {
     int m = i % n;
     if (m < 0) 
     m += n;       
     return m;
     }
     */
    
    
    
    private void shiftBlock(int startIndex, int endIndex) {
        
        for (int i = endIndex - 1; i >= startIndex; i--) {
            setFast(i + 1, get(i));
        }
    }

    @Override
    public int size() {
        return size;
        
    }

    @Override
    public X get(int i) {
        
        
        
        return array[(head + i) % n];

        
        
    }

    public final X getAndSet(int i, X newValue) {
        int ii = (head + i) % n;
        X[] a = this.array;
        X e = a[ii];
        a[ii] = newValue;
        return e;
    }

    public void setFast(int i, X e) {
        array[(head + i) % n] = e;
    }

    @Override
    public X set(int i, X e) {
        /*if (i < 0 || i >= size()) {
         throw new IndexOutOfBoundsException();
         }*/
        
        int m = (head + i) % n;
        

        X existing = array[m];
        array[m] = e;
        return existing;
    }

    @Override
    public void add(int i, X e) {
        int s = size;
        /*
         if (s == n - 1) {
         throw new IllegalStateException("Cannot add element."
         + " CircularArrayList is filled to capacity.");
         }
         if (i < 0 || i > s) {
         throw new IndexOutOfBoundsException();
         }
         */
        if (++tail == n)
            tail = 0;

        size++;

        if (i < s)
            shiftBlock(i, s);

        if (e != null)
            setFast(i, e);
    }


    public void removeFast(int i) {
        if (i > 0)
            shiftBlock(0, i);


        if (++head == n)
            head = 0;
        size--;
    }

    public void removeFirst(int n) {
        n = Math.min(size(), n);
        for (int i = 0; i < n; i++)
            removeFast(0);
    }

    @Override
    public X remove(int i) {
        int s = size;
        if (i < 0 || i >= s)
            throw new IndexOutOfBoundsException();

        X e = get(i);
        removeFast(i);
        return e;
    }

    @Override
    public boolean remove(Object o) {
        return remove(indexOf(o)) != null;
    }

    public boolean removeIdentity(Object o) {
        int s = size();
        for (int i = 0; i < s; i++) {
            if (get(i) == o) {
                removeFast(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public void addFirst(X e) {
        add(0, e);
    }

    @Override
    public X getLast() {
        return get(size - 1);
    }

    public void swapWithLast(int i) {
        swap(i, size - 1);
    }

    public void swap(int a, int b) {
        X ap = get(a);
        X bp = get(b);
        if ((ap == null) || (bp == null))
            throw new RuntimeException("illegal swap");

        setFast(a, bp);
        setFast(b, ap);

    }

    @Override
    public void addLast(X e) {
        add(size, e);
    }


    @Override
    public X getFirst() {
        return get(0);
    }


    @Override
    public X removeFirst() {
        return remove(0);
    }




    @Override
    public X removeLast() {
//        int s = size();
//        return s == 0 ? null : remove(size - 1);
        return remove(size - 1);
    }

    public void removeFirstFast() {
        removeFast(0);
    }

    public void removeLastFast() {
        removeFast(size - 1);
    }


    @Override
    public final boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean offerFirst(X e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean offerLast(X e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    @Nullable public X pollFirst() {
        if (size==0) return null;
        return remove(0);
    }

    @Override
    public X pollLast() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public X peekFirst() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public X peekLast() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean offer(X e) {
        addFirst(e);
        return true;
    }

    @Override
    public X remove() {
        return removeLast();
    }

    @Override
    public X poll() {
        return removeLast();
    }

    @Override
    public X element() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public X peek() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void push(X e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public X pop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public X getModulo(int i) {
        return get(i % size());
    }

    public boolean isFull() {
        return size() == capacity();
    }

    @Nullable
    public X pollRandom(RandomBits rng) {
        int s = size();
        if (s == 0) return null;
        else if (s == 1) return removeFirst();
        else {
            return remove(rng.nextInt(s));
        }
    }
}