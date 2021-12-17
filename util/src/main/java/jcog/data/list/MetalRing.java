package jcog.data.list;

import jcog.Is;
import jcog.TODO;
import jcog.Util;
import jcog.WTF;

import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Is("Disruptor_(software)") public abstract class MetalRing<X> {

    private static final VarHandle
            NEXT_HEAD = Util.VAR(MetalRing.class, "nextHead", int.class),
            NEXT_TAIL = Util.VAR(MetalRing.class, "nextTail", int.class);

    @SuppressWarnings("unused") private volatile int head, nextHead, tail, nextTail;

    public static int i(int x, int cap) {
        return Math.max(x, x + Integer.MIN_VALUE) % cap;
    }
    private static int size(int tail, int head) {
        return Math.max(tail - head, 0);
    }

    private static float availablePct(int size, int targetCapacity) {
        return /*Util.unitize*/1 - (float) size / targetCapacity;
    }

    public abstract int length();

    public abstract X get(int i);

    protected abstract void set(int i, X x);

    protected abstract X getAndSet(int i, X x);

    public void forEach(Consumer<? super X> each) {

        int h = head();
        int s = tail() - h;
        if (s <= 0)
            return;

        int c = length();
        h = i(h, c);
        while (true) {
            X next = get(h);
            if (next != null)
                each.accept(next);
            /*else
                break; //assume we reached the end? */

            if (--s <= 0) break;

            if (++h == c) h = 0; //wrap
        }
    }

    public final boolean push(X x) {
        return push(x, 0);
    }

    public final boolean push(X x, int retries) {
        return push(x, Thread::onSpinWait, retries);
    }

    public boolean push(X x, Runnable wait, int retries) {
        boolean pushed;
        while (!(pushed = offer(x)) && retries-- > 0) {
            wait.run();
        }
        return pushed;
    }

    public boolean offer(X x) {
        int spin = 0;

        int cap = length();
        int tail;
        // exceeded capacity
        while (cap > (tail = tail()) - head()) {
            // never offer onto the slot that is currently being polled off
            // will this sequence exceed the capacity
            // does the sequence still have the expected
            // value

            if (canTake(tail)) {

                // tailSeq is valid and got access without contention
                set(i(tail, cap), x);

                take(tail + 1);

                return true;
            }

            // else - sequence misfire, somebody got our spot, try again

            Util.pauseSpin(spin++);
        }

        return false;
    }

    public int i(int x) {
        return i(x, length());
    }

    public final X poll() {
        return poll(0);
    }

    public X poll(int retries) {
        int spin = 0;
        int cap = length();
        int head;
        while ((head = head()) - tail() <= 0) {

            // check if we can update the sequence
            if (canPut(head))
                return putAnother(head, i(head, cap));


            // else somebody else is reading this spot already: retry
            // this is the spin waiting for access

            if (--retries <= 0) break; //timeout?

            Util.pauseSpin(spin++);
        }

        return null;
    }

    private X putAnother(int head, int ih) {
        var pollObj = getAndSet(ih, null);

        put(head + 1);

        return pollObj;
    }

    public final X peek() {
        return getHead(0);
    }

    public final X getHead(int delta) {
        return get(head(), delta);
    }

    public final X get(int head, int delta) {
        return get(i(head + delta));
    }

    /**
     * if capacity is known it can be provided here to elide method call
     */
    public final X get(int head, int delta, int cap) {
        return get(i(head + delta, cap));
    }

    /** assumes that head+delta >= 0 */
    public final X getUnsafe(int head, int delta, int cap) {
        if (head+delta < 0)
            throw new WTF();
        return get((head + delta) % cap);
    }

    /**
     * oldest element
     */
    public final X first() {
        return peek();
    }

    /**
     * newest element
     */
    public final X last() {
        return last(0);
    }

    public final X last(int beforeEnd) {
        int tail = tail(), head = head();
        int i = size(tail, head) - beforeEnd - 1;
        return i >= 0 ? get(head, i) : null;
    }

    public int remove(X[] x) {
        return remove(x, x.length);
    }

    public int remove(Lst<X> x, int maxElements, int retries) {
        if (maxElements == 1) {
            X xx = poll(retries);
            if (xx == null)
                return 0;
            else {
                x.add(xx);
                return 1;
            }
        } else {
            int initialSize = x.size();
            int[] i = {initialSize};
            X[] a = x.array();
            int tryToGet = Math.min(a.length - initialSize, maxElements);
            int drained = clear(y -> a[i[0]++] = y, tryToGet, retries);
            x.setSize(drained);
            return drained;
        }
    }

    /**
     * This employs a "batch" mechanism to load all objects from the ring
     * in a single update.    This could have significant cost savings in comparison
     * with poll
     */
    public int remove(X[] x, int maxElements) {

        throw new TODO("impl with clear(..)");

//        maxElements = Math.min(x.length, maxElements);
//        assert(maxElements > 0);
//
//        int spin = 0;
//
//        for (; ; ) {
//            final int head = head(); // prepare to qualify?
//            // is there data for us to poll
//            // note we must take a difference in values here to guard against
//            // integer overflow
//            int tail = tail();
//            if (tail == head) return 0; //empty
//
//            final int r = Math.min((tail - head), maxElements);
//            if (r > 0) {
//                // if we still control the sequence, update and return
//                if(canPut(head, r)) {
//
//                    int cap = capacity();
//                    int n = i(head, cap);
//                    for (int i = 0; i < r; i++) {
//                        x[i] = getAndSet(n, null);
//                        if (++n == cap) n = 0;
//                    }
//
//                    put(head, r);
//                    return r;
//                } else {
//                    spin = progressiveYield(spin); // wait for access
//                }
//
//
//            } else {
//                // nothing to read now
//                return 0;
//            }
//
//        }
    }

    private boolean canTake(int tail) {
        //return this.nextTail.compareAndSet(tail, tail + 1);
        //return NEXT_TAIL.compareAndSet(this, tail, tail + 1);
        return NEXT_TAIL.weakCompareAndSetAcquire(this, tail, tail + 1);
    }

    private boolean canPut(int head) {
        //return this.nextHead.compareAndSet(head, head + 1);
        //return NEXT_HEAD.compareAndSet(this, head, head + 1);
        return NEXT_HEAD.weakCompareAndSetAcquire(this, head, head + 1);
    }

    private void take(int tail) {
        //this.tail.setOpaque(tail);
        this.tail = tail;
    }

    private void put(int head) {
        //TODO attempt to set head=tail=0 if size is now zero. this returns the queue to a canonical starting position and might somehow improve cpu caching
        //this.head.setOpaque(head);
        this.head = head;
    }

    public final int clear(Consumer<X> each) {
        return clear(each, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * TODO make a custom clear impl, avoiding lambda
     */
    public <Y> int clear(BiConsumer<X, Y> each, Y param) {
        return clear(x -> each.accept(x, param));
    }

    /**
     * TODO clearWith(IntObjectConsumer...
     */
    public int clear(Consumer<X> each, int limit, int retries) {
        assert limit > 0;

        int cap = length();

        int spin = 0;
        int k = 0;
        main:
        while (true) {
            int head = head(); // prepare to qualify?
            // is there data for us to poll
            // we must take a difference in values here to guard against integer overflow
            int tail = tail();
            int s = tail - head;
            if (s <= 0)
                return k; //empty

            int ih = i(head, cap);

            while (head < tail) {

                if (canPut(head)) {
                    //get, nullify, and advance
                    X next = getAndSet(ih, null);
                    put(++head);

                    //callback
                    if (next != null) {
                        each.accept(next);

                        //done?
                        if (++k >= limit)
                            return k;
                    }

                    if (++ih == cap) ih = 0;
                    spin = 0; //reset spin now that we are reading again

                } else {

                    if (--retries > 0) {
                        Util.pauseSpin(spin++); // wait for access

                        continue main; //restart
                    } else {
                        return k;
                    }
                }
            }


        }
    }

    /**
     * This implemention is known to be broken if preemption were to occur after
     * reading the tail pointer.
     * <p>
     * Code should not depend on size for a correct result.
     *
     * @return int - possibly the size, or possibly any value less than capacity()
     */
    public final int size() {
        // size of the ring
        // note these values can roll from positive to
        // negative, this is properly handled since
        // it is a difference
        return size(tail(), head());
    }

    public final boolean isEmpty() {
        return size() == 0;
    }

    private int tail() {
        //return this.tail.getOpaque();
        return tail;
    }

    public final int head() {
        //return this.head.getOpaque();
        return head;
    }

    public final void clear() {
        clear(x -> {
        });

        //throw new TODO("review");
//        int spin = 0;
//        int cap = capacity();
//        for (; ; ) {
//            final int head = this.head.getAcquire();
//            if (headCursor.weakCompareAndSetAcquire(head, head + 1)) {
//            //if (headCursor.weakCompareAndSetAcquire(head, head + 1)) {
//                for (; ; ) {
//                    final int tail = this.tail.getAcquire();
//                    if (tailCursor.weakCompareAndSetAcquire(tail, tail + 1)) {
//                    //if (tailCursor.weakCompareAndSetVolatile(tail, tail + 1)) {
//
//                        // we just blocked all changes to the queue
//
//                        // remove leaked refs
//                        for (int i = 0; i < cap; i++)
//                            set(i, null);
//
//                        // advance head to same location as current end
//                        this.tail.setRelease(tail+1);
//                        this.head.addAndGet(tail - head + 1);
//                        headCursor.setRelease(tail + 1);
//
//                        return;
//                    }
//                    spin = progressiveYield(spin);
//                }
//            }
//            spin = progressiveYield(spin);
//        }
    }

    public final boolean contains(Object o) {
        int s = size();
        if (s > 0) {
            //TODO use fast iteration method
            int h = head();
            for (int i = 0; i < s; i++) {
                X b = get(h, i);
                if (b != null && b.equals(o)) return true;
            }
        }
        return false;
    }

    public int available() {
        return Math.max(0, length() - size());
    }

    public float availablePct() {
        return availablePct(length());
    }

    public float availablePct(int targetCapacity) {
        return availablePct(size(), targetCapacity);
    }

    public Stream<X> stream() {
        return IntStream.range(0, size()).mapToObj(this::getHead).filter(Objects::nonNull);
    }

    public boolean isFull() {
        return isFull(0);
    }

    public boolean isFull(int afterAdding) {
        return size() + afterAdding >= length();
    }

    public void add(X x) {
        add(x, xx -> {
            throw new RuntimeException(this + " overflow on add: " + xx);
        });
    }

    public void add(X x, Consumer<X> ifBlocked) {
        if (!offer(x)) {
            ifBlocked.accept(x);
        }
    }

    public void add(X x, Function<X, Predicate<X>> continueWaiting, TimeUnit waitUnit, int timePeriods) {
        throw new TODO();
    }

    public boolean remove(X x) {
        throw new TODO();
    }
}