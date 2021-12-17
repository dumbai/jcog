package jcog.exe;

import com.google.common.util.concurrent.RateLimiter;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Realtime rate controlled iterator wrapper
 */
public class RateIterator<X> implements Iterator<X> {
    final RateLimiter rate;
    final Iterator<X> iter;

    public RateIterator(Stream<X> i, double rate) {
        this(i.iterator(), rate);
    }

    /**
     *
     * @param i
     * @param rate cost per real-time second
     */
    public RateIterator(Iterator<X> i, double rate) {
        this(i, RateLimiter.create(rate));
    }

    public RateIterator(Iterator<X> i, RateLimiter rate) {
        this.rate = rate;
        this.iter = i;
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public X next() {
        X x = iter.next();
        rate.acquire(cost(x));
        return x;
    }

    /** can be used to modulate the cost of each item */
    protected int cost(X x) {
        return 1;
    }

    public Thread threadEachRemaining(Consumer<X> each) {
        return new Thread(()-> forEachRemaining(each));
    }

}
