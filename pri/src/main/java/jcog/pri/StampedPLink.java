package jcog.pri;

public class StampedPLink<X> extends HashedPLink<X> {

    private volatile long y = 0;

    public StampedPLink(X x, float p) {
        this(x, x.hashCode(), p);
    }

    public StampedPLink(X x, int hash, float p) {
        super(x, hash, p);
    }

    public final StampedPLink<X> stamp(long yy) {
        this.y = yy;
        return this;
    }
    public final long stamp() {
        return y;
    }
}
