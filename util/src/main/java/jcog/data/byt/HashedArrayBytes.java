package jcog.data.byt;

/**
 * extends ArrayByteSeq with a cached hashCode
 */
public class HashedArrayBytes extends ArrayBytes {

    private final int hash;

    public HashedArrayBytes(byte[] bytes) {
        super(bytes);
        this.hash = super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (hash!=obj.hashCode()) return false;
        return super.equals(obj);
    }

    @Override
    public final int hashCode() {
        return hash;
    }
}
