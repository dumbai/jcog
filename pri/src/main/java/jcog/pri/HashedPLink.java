package jcog.pri;

/** caches hashcode in field */
public class HashedPLink<X> extends PLink<X>{

    public final int hash;

    public HashedPLink(X x, int hash, float p) {
        super(x, p);
        this.hash = hash;
    }

    public HashedPLink(X x, float p) {
        this(x, x.hashCode(), p);
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object that) {
        return hash == that.hashCode() && super.equals(that);
    }

}
