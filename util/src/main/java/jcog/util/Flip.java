package jcog.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/** atomic switching double buffer */
public class Flip<X> extends AtomicInteger {

    private volatile X a;
    private volatile X b;

    public Flip(Supplier<X> builder) {
        super(0);
        this.a = builder.get();
        this.b = builder.get();
    }

    public X write() { return (get() & 1) == 0 ? a : b;   }

    /** use with caution, may only be valid with single producer */
    public X write(X value) {
        update(value, 0);
        return value;
    }
    /** use with caution, may only be valid with single producer */
    public X read(X value) {
        update(value, 1);
        return value;
    }

    private void update(X value, int i) {
        if ((get() & 1) == i)
            a = value;
        else
            b = value;
    }

    public X read() {
        return read(get());
    }
    private X read(int v) {
        return (v & 1) == 0 ? b : a;
    }
    private X write(int v) {
        return (v & 1) == 0 ? a : b;
    }

    public X commitRead() {
        return read(commit());
    }
    public X commitWrite() {
        return write(commit());
    }

    public int commit() {
        return incrementAndGet();
    }

}
