package jcog.tree.radix;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An unmodifiable iterator which computes the next element to return only when it is requested.
 * <p/>
 * This class is inspired by com.google.common.collect.AbstractIterator in Google Guava,
 * which was written by the Google Guava Authors, in particular by Kevin Bourrillion.
 *
 * @author Niall Gallagher
 */
public abstract class LazyIterator<T> implements Iterator<T> {

    T next;

    enum State { READY, NOT_READY, DONE, FAILED }

    State state = State.NOT_READY;

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Iterator.remove() is not supported");
    }

    @Override
    public final boolean hasNext() {
        if (state == State.FAILED) {
            throw new IllegalStateException("This iterator is in an inconsistent state, and can no longer be used, " +
                    "due to an exception previously thrown by the computeNext() method");
        }
        return switch (state) {
            case DONE -> false;
            case READY -> true;
            default -> tryToComputeNext();
        };
    }

    boolean tryToComputeNext() {
        state = State.FAILED; 
        next = computeNext();
        if (state != State.DONE) {
            state = State.READY;
            return true;
        }
        return false;
    }

    @Override
    public final T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        state = State.NOT_READY;
        return next;
    }

    /**
     *
     * @return a dummy value which if returned by the <code>computeNext()</code> method, signals that there are no more
     * elements to return
     */
    protected final T endOfData() {
        state = State.DONE;
        return null;
    }

    /**
     * @return The next element which the iterator should return, or the result of calling <code>endOfData()</code>
     * if there are no more elements to return
     */
    protected abstract T computeNext();
}
