package jcog.data.iterator;

import org.eclipse.collections.api.iterator.IntIterator;

abstract public class AbstractIntIterator implements IntIterator {

    private final int to;
    int next; //HACK
    boolean ready = true;//start < to;

    protected AbstractIntIterator(int start, int to) {
        this.to = to;
        next = start;
    }

    @Override
    public boolean hasNext() {
        int next = this.next, to = this.to;
        if (next >= to) return false;
        if (!ready) {
            int n = next(next);
            if (n < 0) {
                this.next = to; //done
                return false;
            }

            this.next = n;
            ready = true;
        }
        return true;
    }


    /**
     * return -1 to end iteration
     */
    abstract protected int next(int next);

    @Override
    public int next() {
        //assert(ready);
        ready = false;
        return next;
    }

}